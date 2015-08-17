
package com.accela.mobile.http;

import android.content.Context;

import com.accela.mobile.AMLogger;
import com.accela.mobile.AMSetting;
import com.accela.mobile.http.mime.AccelaMultipartFormEntity;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.SyncBasicHttpContext;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
import java.util.concurrent.Future;
import java.util.zip.GZIPInputStream;

/**
 * <pre>
 * 
 *  Accela Amobile
 *  File: AsyncHttpClient.java
 * 
 *  Accela, Inc.
 *  Copyright (C): 2012
 * 
 *  Description:
 *  HTTP Client wrapper object, used to process asynchronous HTTP requests.
 * 
 *  Notes:
 * 
 * 
 *  Revision History
 *  
 * 
 * 	@since 1.0
 * 
 * </pre>
 */

public class AsyncHttpClient {	
	private static final int HTTP_MAX_CONNECTIONS = 10;
	private static final int HTTP_MAX_RETRIES = 1;
	private static final int HTTP_SOCKET_BUFFER_SIZE = 8192;
	private static final String HTTP_HEADER_ACCEPT_ENCODING = "Accept-Encoding";
	private static final String HTTP_ENCODING_GZIP = "gzip";	
	
	private final DefaultHttpClient httpClient;
	private final HttpContext httpContext;
	private Header httpResponsHeader;
	private final Map<Context, List<WeakReference<Future<?>>>> requestMap;
	private final Map<String, String> clientHeaderMap;
	
	private Boolean isCancelled = false;

	private ResourceBundle stringLoader = AMSetting.getStringResourceBundle();
	/**
	 * Constructor without parameters.
	 * 
	 * @return An initialized AsyncHttpClient instance
	 * 
	 * @since 1.0
	 */
	public AsyncHttpClient() {
		
		SSLSocketFactory sslSocketFactory = null;
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);
			sslSocketFactory = new SimpleSSLSocketFactory(trustStore);
			sslSocketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		  } catch (Exception e) {			
			 AMLogger.logError("In AsyncHttpClient.AsyncHttpClient(): Exception " + stringLoader.getString("Log_Exception_Occured"), e.getMessage());
			 if (AMSetting.DebugMode) {
				e.printStackTrace();
			 } 
          }		
		BasicHttpParams httpParams = new BasicHttpParams();
		ConnManagerParams.setTimeout(httpParams, AMSetting.HTTP_CONNECTION_TIMEOUT);
		ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(HTTP_MAX_CONNECTIONS));
		ConnManagerParams.setMaxTotalConnections(httpParams, HTTP_MAX_CONNECTIONS);
		HttpConnectionParams.setSoTimeout(httpParams, AMSetting.SOCKET_TIMEOUT);
		HttpConnectionParams.setConnectionTimeout(httpParams, AMSetting.HTTP_CONNECTION_TIMEOUT);
		HttpConnectionParams.setTcpNoDelay(httpParams, true);
		HttpConnectionParams.setSocketBufferSize(httpParams, HTTP_SOCKET_BUFFER_SIZE);
		HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setUserAgent(httpParams, AMSetting.AM_SDK_NAME);

		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		if(sslSocketFactory!=null) {
			schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));
		}
		ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(httpParams, schemeRegistry);

		httpContext = new SyncBasicHttpContext(new BasicHttpContext());
		httpClient = new DefaultHttpClient(cm, httpParams);
		httpClient.addRequestInterceptor(new HttpRequestInterceptor() {
			public void process(HttpRequest request, HttpContext context) {
				if (!request.containsHeader(HTTP_HEADER_ACCEPT_ENCODING)) {
					request.addHeader(HTTP_HEADER_ACCEPT_ENCODING, HTTP_ENCODING_GZIP);
				}
				for (String header : clientHeaderMap.keySet()) {
					request.addHeader(header, clientHeaderMap.get(header));
				}
			}
		});

		httpClient.addResponseInterceptor(new HttpResponseInterceptor() {
			public void process(HttpResponse response, HttpContext context) {
				final HttpEntity entity = response.getEntity();
				final Header encoding = entity.getContentEncoding();	
			
				
				if (encoding != null) {
					for (HeaderElement element : encoding.getElements()) {
						if (element.getName().equalsIgnoreCase(HTTP_ENCODING_GZIP)) {
							response.setEntity(new InflatingEntity(response.getEntity()));
							break;
						}
					}					
				
				}
			}			
		});		

		httpClient.setHttpRequestRetryHandler(new RetryHandler(HTTP_MAX_RETRIES));
		requestMap = new WeakHashMap<Context, List<WeakReference<Future<?>>>>();
		clientHeaderMap = new HashMap<String, String>();	
	}	

	/**
	 * Sets headers that will be added to all requests this client makes (before sending).
	 * 
	 * @param header The name of the header
	 * @param value The contents of the header	          
	 * 
	 * @return Void.
	 * 
	 * @since 1.0 
	 * 
	 */
	public void addHeader(String header, String value) {
		clientHeaderMap.put(header, value);
	}
	
	/**
	 * 
	 * Get the string line used to separate form data in multipart type request
	 * 
	 * @return The separator string line.
	 * 
	 * @since 4.0
	 */
	public String getMultipartSeparatorLine() {        
        return AccelaMultipartFormEntity.MULTIPART_SEPARATOR_LINE;
    }

	/**
	 * Cancels any pending (or potentially active) requests associated with the
	 * passed Context.
	 * <p>
	 * <b>Note:</b> This will only affect requests which were created with a
	 * non-null Android Context. This method is intended to be used in the
	 * onDestroy method of your Android activities to destroy all requests which
	 * are no longer required.
	 * 
	 * @param context The Android Context instance associated to the request.
	 * @param mayInterruptIfRunning Specifies if active requests should be cancelled along with pending requests.	     
	 *      
	 * @return Void.
	 * 
	 * @since 1.0            
	 */
	public void cancelRequests(Context context, boolean mayInterruptIfRunning) {
		List<WeakReference<Future<?>>> requestList = requestMap.get(context);
		if (requestList != null) {
			for (WeakReference<Future<?>> requestRef : requestList) {
				Future<?> request = requestRef.get();
				if (request != null) {
					request.cancel(mayInterruptIfRunning);
				}
			}
		}
		requestMap.remove(context);		
		this.isCancelled = true;
	}

	/**
	 * Perform a HTTP DELETE request.
	 * 
	 * @param context The Android Context which initiated the request.
	 * @param url The URL to send the request to.
	 * @param responseHandler The response handler instance that should handle the response.	      
	 *      
	 * @return Void.
	 * 
	 * @since 1.0            
	 */
	public void delete(Context context, String url, AsyncHttpResponseHandler responseHandler) {
		final HttpDelete delete = new HttpDelete(url);
		sendRequest(httpClient, httpContext, delete, null, responseHandler,context);
	}
	
	
	/**
	 * Perform a HTTP DELETE request.
	 * 
	 * @param context The Android Context which initiated the request.
	 * @param url The URL to send the request to.
	 * @param headers Set one-time headers for this request
	 * @param responseHandler The response handler instance that should handle the response.	      
	 *      
	 * @return Void.
	 * 
	 * @since 1.0            
	 */
	public void delete(Context context, String url, Header[] headers, AsyncHttpResponseHandler responseHandler) {
		final HttpDelete delete = new HttpDelete(url);
		if (headers != null) delete.setHeaders(headers);
		sendRequest(httpClient, httpContext, delete, null, responseHandler, context);
	}
	

	/**
	 * Perform a HTTP DELETE request.
	 * 
	 * @param url The URL to send the request to.
	 * @param responseHandler The response handler instance that should handle the response.	  
	 *          
	 * @return Void.
	 * 
	 * @since 1.0            
	 */
	public void delete(String url, AsyncHttpResponseHandler responseHandler) {
		delete(null, url, responseHandler);
	}

	/**
	 * Perform a HTTP GET request without any parameters and track the Android
	 * Context which initiated the request.
	 * 
	 * @param context The Android Context which initiated the request.
	 * @param url The URL to send the request to.
	 * @param responseHandler The response handler instance that should handle the response.	     
	 *       
	 * @return Void.
	 * 
	 * @since 1.0            
	 */
	public void get(Context context, String url, AsyncHttpResponseHandler responseHandler) {
		get(context, url, null, responseHandler);
	}

	/**
	 * Perform a HTTP GET request and track the Android Context which initiated
	 * the request with customized headers
	 * 
	 * @param context The Android Context which initiated the request.
	 * @param url The URL to send the request to.
	 * @param headers Set headers only for this request
	 * @param params Additional GET parameters to send with the request.
	 * @param responseHandler The response handler instance that should handle the response.	     
	 *      
	 * @return Void.
	 * 
	 * @since 1.0            
	 */
	public void get(Context context, String url, Header[] headers, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		HttpUriRequest request = new HttpGet(assembleUrlWithParams(url, params));
		if (headers != null) {
			request.setHeaders(headers);
		}
		sendRequest(httpClient, httpContext, request, null, responseHandler,context);
	}

	/**
	 * Perform a HTTP GET request and track the Android Context which initiated
	 * the request.
	 * 
	 * @param context The Android Context which initiated the request.
	 * @param url The URL to send the request to.
	 * @param params Additional GET parameters to send with the request.
	 * @param responseHandler The response handler instance that should handle the response.	         
	 *  
	 * @return Void.
	 * 
	 * @since 1.0            
	 */
	public void get(Context context, String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		sendRequest(httpClient, httpContext, new HttpGet(assembleUrlWithParams(url, params)), null, responseHandler, context);
	}

	/**
	 * Perform a HTTP GET request, without any parameters.
	 * 
	 * @param url The URL to send the request to.
	 * @param responseHandler The response handler instance that should handle the response.	 
	 *          
	 * @return Void.
	 * 
	 * @since 1.0            
	 */
	public void get(String url, AsyncHttpResponseHandler responseHandler) {
		get(null, url, null, responseHandler);
	}
	
	/**
	 * Perform a HTTP GET request with parameters.
	 * 
	 * @param url The URL to send the request to.
	 * @param params Additional GET parameters to send with the request.
	 * @param responseHandler The response handler instance that should handle the response.	   
	 *       
	 * @return Void.
	 * 
	 * @since 1.0            
	 */
	public void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		get(null, url, params, responseHandler);
	}

	/**
	 * Get client's header map. 	 
	 *          
	 * @return The map which contains client header data..
	 * 
	 * @since 1.0            
	 */
	public Map<String, String> getHeader() {
		return clientHeaderMap;
	}

	/**
	 * Get response's header list. 	 
	 *          
	 * @return The list which contains header of HTTP response
	 * 
	 * @since 1.0            
	 */
	public List<NameValuePair> getHttpResponseHeaders() {		
		List<NameValuePair> httpHeadersList = new ArrayList<NameValuePair>();
		if (this.httpResponsHeader != null) {
			for (HeaderElement element : this.httpResponsHeader.getElements()) {
				httpHeadersList.add(new BasicNameValuePair(element.getName(), element.getValue()));
			}
		}
		return httpHeadersList;
	}

	/**
	 * Get whether the current HTTP request has been cancelled.
	 *            
	 * @return Return true if the request has been cancelled; Otherwise, return false.
	 * 
	 * @since 1.0            
	 */
	public Boolean isCancelled() {
		return this.isCancelled;
	}

	/**
	 * Get loading status of current HTTP client.	
	 *            
	 * @return true if the http client is loading; otherwise return false.
	 * 
	 * @since 1.0            
	 */
	public Boolean isLoading() {
		return ((requestMap != null) && (requestMap.size() > 0));
	}	

	/**
	 * Perform a HTTP POST request and track the Android Context which initiated
	 * the request. Set headers only for this request
	 * 
	 * @param context The Android Context which initiated the request.
	 * @param url The URL to send the request to.
	 * @param headers Set headers only for this request
	 * @param params Additional POST parameters or files to send with the request.
	 * @param contentType The content type of the payload you are sending, 
	 * 						 for example application/json if sending a json payload.
	 * @param responseHandler The response handler instance that should handle the response.	         
	 *   
	 * @return Void.
	 * 
	 * @since 1.0            
	 */
	public void post(Context context, String url, Header[] headers, RequestParams params, String contentType, AsyncHttpResponseHandler responseHandler) {
		HttpEntityEnclosingRequestBase request = new HttpPost(url);
		if (params != null) {
			request.setEntity(paramsToEntity(params));
		}
		if (headers != null) {
			request.setHeaders(headers);
		}
		sendRequest(httpClient, httpContext, request, contentType, responseHandler, context);
	}

	/**
	 * Perform a HTTP POST request and track the Android Context which initiated
	 * the request.
	 * 
	 * @param context The Android Context which initiated the request.
	 * @param url The URL to send the request to.
	 * @param entity A raw {@link HttpEntity} to send with the request, 
	 * 						 for example, use this to send string/json/xml payloads to a server
	 *           			 by passing a {@link org.apache.http.entity.StringEntity}.
	 * @param contentType The content type of the payload you are sending, 
	 * 						 for example application/json if sending a json payload.
	 * @param responseHandler The response handler instance that should handle the response.	        
	 *    
	 * @return Void.
	 * 
	 * @since 1.0            
	 */
	public void post(Context context, String url, HttpEntity entity, String contentType, AsyncHttpResponseHandler responseHandler) {
		sendRequest(httpClient, httpContext, addEntityToRequestBase(new HttpPost(url), entity), contentType, responseHandler, context);
	}

	/**
	 * Perform a HTTP POST request and track the Android Context which initiated
	 * the request.
	 * 
	 * @param context The Android Context which initiated the request.
	 * @param url The URL to send the request to.
	 * @param params Additional POST parameters or files to send with the request.
	 * @param responseHandler The response handler instance that should handle the response.	       
	 *    
	 * @return Void.
	 * 
	 * @since 1.0            
	 */
	public void post(Context context, String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		post(context, url, paramsToEntity(params), null, responseHandler);
	}

	/**
	 * Perform a HTTP POST request, without any parameters.
	 * 
	 * @param url The URL to send the request to.
	 * @param responseHandler The response handler instance that should handle the response.	  
	 *          
	 * @return Void.
	 * 
	 * @since 1.0            
	 */
	public void post(String url, AsyncHttpResponseHandler responseHandler) {
		post(null, url, null, responseHandler);
	}
	

	/**
	 * Perform a HTTP POST request with parameters.
	 * 
	 * @param url The URL to send the request to.
	 * @param params Additional POST parameters or files to send with the request.
	 * @param responseHandler The response handler instance that should handle the response.	    
	 *        
	 * @return Void.
	 * 
	 * @since 1.0            
	 */
	public void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		post(null, url, params, responseHandler);
	}

	/**
	 * Perform a HTTP PUT request and track the Android Context which initiated
	 * the request. And set one-time headers for the request
	 * 
	 * @param context The Android Context which initiated the request.
	 * @param url The URL to send the request to.
	 * @param headers Set one-time headers for this request
	 * @param entity A raw {@link HttpEntity} to send with the request, 
	 * 						 for example, use this to send string/json/xml payloads to a server
	 *            			 by passing a {@link org.apache.http.entity.StringEntity}.
	 * @param contentType The content type of the payload you are sending, 
	 * 								  for example application/json if sending a json payload.
	 * @param responseHandler The response handler instance that should handle the response.	  
	 *         
	 * @return Void.
	 * 
	 * @since 1.0            
	 */
	public void put(Context context, String url, Header[] headers, HttpEntity entity, String contentType, AsyncHttpResponseHandler responseHandler) {
		HttpEntityEnclosingRequestBase request = addEntityToRequestBase( new HttpPut(url), entity);
		if (headers != null) {
			request.setHeaders(headers);
		}
		sendRequest(httpClient, httpContext, request, contentType, responseHandler, context);
	}

	/**
	 * Perform a HTTP PUT request and track the Android Context which initiated
	 * the request. And set one-time headers for the request
	 * 
	 * @param context The Android Context which initiated the request.
	 * @param url The URL to send the request to.
	 * @param entity A raw {@link HttpEntity} to send with the request, 
	 * 						 for example, use this to send string/json/xml payloads to a server
	 *            			 by passing a {@link org.apache.http.entity.StringEntity}.
	 * @param contentType The content type of the payload you are sending,
	 * 								   for example application/json if sending a json payload.
	 * @param responseHandler The response handler instance that should handle the response.
	 * 	          
	 * @return Void.
	 * 
	 * @since 1.0            
	 */
	public void put(Context context, String url, HttpEntity entity, String contentType, AsyncHttpResponseHandler responseHandler) {
		sendRequest(httpClient, httpContext, addEntityToRequestBase(new HttpPut(url), entity), contentType, responseHandler, context);
	}

	/**
	 * Perform a HTTP PUT request and track the Android Context which initiated
	 * the request.
	 * 
	 * @param context The Android Context which initiated the request.
	 * @param url The URL to send the request to.
	 * @param params Additional PUT parameters or files to send with the request.
	 * @param responseHandler The response handler instance that should handle the response.	  
	 *          
	 * @return Void.
	 * 
	 * @since 1.0            
	 */
	public void put(Context context, String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		put(context, url, paramsToEntity(params), null, responseHandler);
	}		
	
	/**
	 * Perform a HTTP PUT request, without any parameters.
	 * 
	 * @param url The URL to send the request to.
	 * @param responseHandler The response handler instance that should handle the response.	      
	 *      
	 * @return Void.
	 * 
	 * @since 1.0            
	 */
	public void put(String url, AsyncHttpResponseHandler responseHandler) {
		put(null, url, null, responseHandler);
	}
	
	/**
	 * Perform a HTTP PUT request with parameters.
	 * 
	 * @param url The URL to send the request to.
	 * @param params Additional PUT parameters or files to send with the request.
	 * @param responseHandler The response handler instance that should handle the response.	    
	 *       
	 * @return Void.
	 * 
	 * @since 1.0            
	 */
	public void put(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		put(null, url, params, responseHandler);
	}	
		
	
	/**
	 * Sets an optional CookieStore to use when making requests
	 * 
	 * @param cookieStore The CookieStore implementation to use, usually an instance of {@link PersistentCookieStore}     
	 *     
	 * @return Void.
	 * 
	 * @since 1.0     
	 * 	 
	 */
	
	public void setCookieStore(CookieStore cookieStore) {
		httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
	}
	
	/**
	 * Sets the SSLSocketFactory to user when making requests. 
	 * By default, a new, default SSLSocketFactory is used.
	 * 
	 * @param sslSocketFactory The socket factory to use for https requests.	      
	 *     
	 * @return Void.
	 * 
	 * @since 1.0      
	 * 
	 */
	public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
		this.httpClient.getConnectionManager().getSchemeRegistry()
				.register(new Scheme("https", sslSocketFactory, 443));
	}

	
	/**
	 * Sets the User-Agent header to be sent with each request.
	 *	  
	 * @param userAgent The string to use in the User-Agent header.  
	 *         
	 * @return Void.
	 * 
	 * @since 1.0  
	 *        
	 */
	public void setUserAgent(String userAgent) {
		HttpProtocolParams.setUserAgent(this.httpClient.getParams(), userAgent);
	}	
	
	/**
	 * Private inner class, used to wrap http entity.
	 */	
	private static class InflatingEntity extends HttpEntityWrapper {
		public InflatingEntity(HttpEntity wrapped) {
			super(wrapped);
		}
		@Override
		public InputStream getContent() throws IOException {
			return new GZIPInputStream(wrappedEntity.getContent());
		}
		@Override
		public long getContentLength() {
			return -1;
		}
	}	
	
	/**
	 * Private method, used to add entity to request base.
	 */	
	private HttpEntityEnclosingRequestBase addEntityToRequestBase(HttpEntityEnclosingRequestBase requestBase, HttpEntity entity) {		
		if (entity != null) {
			requestBase.setEntity(entity);
		}
		return requestBase;
	}
	
	/**
	 * Private method, used to convert request parameters to http entity.
	 */	
	private HttpEntity paramsToEntity(RequestParams params) {
		HttpEntity entity = null;
		if (params != null) {
			entity = params.getEntity();
		}

		return entity;
	}
	
	/**
	 * Private method, used to process the request.
	 */	
	protected void sendRequest(DefaultHttpClient client, HttpContext httpContext, HttpUriRequest uriRequest, String contentType, AsyncHttpResponseHandler responseHandler, Context context) {
		if (contentType != null) {
			 uriRequest.addHeader("Content-Type", contentType);
		} 
		if (AMSetting.DebugMode) {
			AMLogger.logInfo("In AsyncHttpClient.sendRequest(): uriRequest = %s",uriRequest.getURI());
		}
		new Thread(new AsyncHttpRequest(client, httpContext, uriRequest, responseHandler)).start();
	}	
	
	/**
	 * Private method, used to assemble URL with query string.
	 */

	private String assembleUrlWithParams(String url, RequestParams urlParams) {
		boolean urlContainsLanguage = (url != null) && (url.contains("lang=")); 
		boolean paramsContainsLanguage = (urlParams != null) && (urlParams.hasKey("lang"));
		if ((!urlContainsLanguage) && (!paramsContainsLanguage)) {	
			String languageCode = String.format("%s_%s", Locale.getDefault().getLanguage(),Locale.getDefault().getCountry());
			if (urlParams == null) {
				urlParams = new RequestParams("lang", languageCode);
			} else { 			
				urlParams.put("lang", languageCode);
			}
		}
		if (urlParams != null) {
	    	String paramString = urlParams.getParamString();
	    	if (!url.contains("?")) {			
	    		url += "?" + paramString;
	    	} else {
	    		url += "&" + paramString;
	    	}	
		}
		return url;
	}
	
	
}

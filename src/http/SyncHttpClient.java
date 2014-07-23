package com.accela.mobile.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.SyncBasicHttpContext;
import org.apache.http.util.EntityUtils;


import org.json.JSONException;
import org.json.JSONObject;

import android.os.StrictMode;

import com.accela.mobile.AMLogger;
import com.accela.mobile.AMRequest.HTTPMethod;
import com.accela.mobile.AMSetting;
import com.accela.mobile.http.mime.AccelaMultipartFormEntity;



/**
 * <pre>
 * 
 *  Accela Amobile
 *  File: SyncHttpClient.java
 * 
 *  Accela, Inc.
 *  Copyright (C): 2012
 * 
 *  Description:
 *  HTTP Client wrapper, used to process synchronous HTTP requests.
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

public class SyncHttpClient {	
	private static final int HTTP_MAX_CONNECTIONS = 10;	
	private static final int HTTP_MAX_RETRIES = 5;
	private static final int HTTP_SOCKET_BUFFER_SIZE = 8192;
	private static final String HTTP_HEADER_ACCEPT_ENCODING = "Accept-Encoding";
	private static final String HTTP_ENCODING_GZIP = "gzip";	
	private HTTPMethod httpMethod;
	private String serviceURL;
	private final DefaultHttpClient httpClient;	
	private final HttpContext httpContext;
	private Header httpResponseHeader;
	private HttpResponse httpResponse;
	private final Map<String, String> clientHeaderMap;	
	private Boolean isLoading = false;
	private Boolean isTimeout = false;
	
	private ResourceBundle stringLoader = AMSetting.getStringResourceBundle();

	/**
	 * Constructor without parameters.
	 * 
	 * @return An initialized SyncHttpClient instance
	 * 
	 * @since 1.0
	 */
	public SyncHttpClient() {
		SSLSocketFactory sslSocketFactory = null;
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);
			sslSocketFactory = new SimpleSSLSocketFactory(trustStore);
			sslSocketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		  } catch (Exception e) {
			AMLogger.logError("In SyncHttpClient.SyncHttpClient(): Exception " + stringLoader.getString("Log_Exception_Occured"), e.getMessage());
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
		schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));
		ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(httpParams, schemeRegistry);

		httpContext = new SyncBasicHttpContext(new BasicHttpContext());
		httpClient = new DefaultHttpClient(cm, httpParams);

		httpClient.setHttpRequestRetryHandler(new RetryHandler(HTTP_MAX_RETRIES));
		clientHeaderMap = new HashMap<String, String>();
		// To avoid android.os.NetworkOnMainThreadException occurring
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
	}

	
	/**
	 * Sets an optional CookieStore to use when making requests
	 * 
	 * @param cookieStore The CookieStore implementation to use, usually an instance of {@link PersistentCookieStore}
	 *          
	 * @return Void.
	 * 
	 * @since 1.0            
	 */
	public void setCookieStore(CookieStore cookieStore) {
		httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
	}


	/**
	 * Sets the User-Agent header to be sent with the current request.
	 *	  
	 * @param userAgent The string to use in the User-Agent header.         
	 *  
	 * @return Void.
	 * 
	 * @since 1.0           
	 */
	public void setUserAgent(String userAgent) {
		HttpProtocolParams.setUserAgent(this.httpClient.getParams(), userAgent);
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
	 */
	public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
		this.httpClient.getConnectionManager().getSchemeRegistry()
				.register(new Scheme("https", sslSocketFactory, 443));
	}

	/**
	 * Sets headers that will be added to the current request.
	 * 
	 * @param header The name of the header.
	 * @param value The contents of the header.	 
	 *          
	 * @return Void.
	 * 
	 * @since 1.0            
	 */
	public void addHeader(String header, String value) {
		clientHeaderMap.put(header, value);
	}

	/**
	 * Perform a HTTP GET request, without any parameters.
	 * 
	 * @param url The URL to send the request to.         
	 * 
	 * @return A Json object which contains the response returned from server.
	 * 
	 * @since 1.0            
	 */
	public JSONObject get(String url) {
		this.httpMethod = HTTPMethod.GET;
		this.serviceURL = url;
		return sendRequest(httpClient, new HttpGet(url), null);
		
	}

	/**
	 * Perform a HTTP POST request, without the given post parameters.
	 * 
	 * @param url The URL to send the request to.
	 * @param params Additional POST parameters to send with the request.
	 * 	           
	 * @return Void.
	 * 
	 * @since 1.0            
	 */
	public JSONObject post(String url, RequestParams params) {		
		return post(url, paramsToEntity(params), null);
	}

	/**
	 * Perform a HTTP POST request with the given http entity.
	 * 
	 * @param url The URL to send the request to.
	 * @param entity A raw {@link HttpEntity} to send with the request,
	 * for example, use this to send string/json/xml payloads to a server
	 *            by passing a {@link org.apache.http.entity.StringEntity}.
	 * @param contentType The content type of the payload you are sending,
	 *            for example, application/json if sending a json payload.
	 * 
	 * @return Void.
	 * 
	 * @since 1.0
	 */
	public JSONObject post(String url, HttpEntity entity, String contentType) {
		this.httpMethod = HTTPMethod.POST;
		this.serviceURL = url;
		return sendRequest(httpClient,addEntityToRequestBase(new HttpPost(url), entity), contentType);
	}

	/**
	 * Perform a HTTP PUT request with the given put parameters.
	 * 
	 * @param url The URL to send the request to.
	 * @param params Additional PUT parameters or files to send with the request. 
	 * 
	 * @return Void.
	 * 
	 * @since 1.0            
	 */
	public JSONObject put(String url, RequestParams params) {
		return put(url, params);
	}

	/**
	 * Perform a HTTP PUT request with the given http entity.
	 * 
	 * @param url The URL to send the request to.
	 * @param entity A raw {@link HttpEntity} to send with the request, 
	 * 						 for example, use this to send string/json/xml payloads to a server
	 *           			 by passing a {@link org.apache.http.entity.StringEntity}.
	 * @param contentType The content type of the payload you are sending.
	 * 
	 * @return Void.
	 * 
	 * @since 1.0            
	 */
	public JSONObject put(String url, HttpEntity entity,String contentType) {
		this.httpMethod = HTTPMethod.PUT;
		this.serviceURL = url;
		return sendRequest(httpClient, addEntityToRequestBase(new HttpPut(url), entity), contentType);
	}

	/**
	 * Perform a HTTP DELETE request.
	 * 
	 * @param url The URL to send the request to.   
	 *    
	 * @return Void.
	 * 
	 * @since 1.0            
	 */
	public JSONObject delete(String url) {
		this.httpMethod = HTTPMethod.DELETE;
		this.serviceURL = url;
		final HttpDelete delete = new HttpDelete(url);
		return sendRequest(httpClient, delete, null);
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
	 * @return The array which contains header of HTTP response
	 * 
	 * @since 1.0            
	 */
	public Header[] getHttpResponseHeader() {
		Header[] headers = new Header[]{};
		if (this.httpResponseHeader != null) {
			int index = 0;
			for (HeaderElement element : this.httpResponseHeader.getElements()) {
				BasicHeader header = new BasicHeader(element.getName(), element.getValue());
				headers[index] = header;
				index++;			}
		}
		return headers;
	}	
	
	/**
	 * Clear header list. 	 
	 *          
	 * @return Void
	 * 
	 * @since 2.1            
	 */
	public void clearHttpResponseHeader() {
		this.httpResponseHeader = null;		
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
	 * Get response's data string. 	 
	 *          
	 * @return The the original string of HTTP response
	 * 
	 * @since 1.0            
	 */
	public String getHttpResponse() {
		String httpResponseString = null;
		try {
			httpResponseString = EntityUtils.toString(httpResponse.getEntity());
		} catch (ParseException e) {
			AMLogger.logError("In SyncHttpClient.getHttpResponse(): ParseException " + stringLoader.getString("Log_Exception_Occured"), e.getMessage());
			if (AMSetting.DebugMode) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			AMLogger.logError("In SyncHttpClient.getHttpResponse(): IOException " + stringLoader.getString("Log_Exception_Occured"), e.getMessage());
			if (AMSetting.DebugMode) {
				e.printStackTrace();
			}
		}		
		return httpResponseString;		
	}	
	
	/**
	 * Clear reponse data. 	 
	 *          
	 * @return Void
	 * 
	 * @since 2.1            
	 */
	public void clearHttpResponseBody() {
		this.httpResponse = null;		
	}	

	/**
	 * Get the loading status of current request 	 
	 *          
	 * @return true or false
	 * 
	 * @since 1.0            
	 */
	public Boolean isLoading() {
		return this.isLoading;
	}
	
	/**
	 * Private method, used to process the request.
	 */	
	private JSONObject sendRequest(DefaultHttpClient client, HttpUriRequest uriRequest, String contentType) {		
		
		if (!clientHeaderMap.isEmpty()) {
			for (String key : clientHeaderMap.keySet()) {
				uriRequest.addHeader(key, clientHeaderMap.get(key));
			}
		}
		
		if (!uriRequest.containsHeader(HTTP_HEADER_ACCEPT_ENCODING)) {
			uriRequest.addHeader(HTTP_HEADER_ACCEPT_ENCODING, HTTP_ENCODING_GZIP);
		}				
	
		this.isLoading = true;
		
		// Print log information before starting the request
		AMLogger.logInfo(stringLoader.getString("Log_AMRequestDelegate_amRequestStarted_URL"), "synchronous", httpMethod,serviceURL);
		AMLogger.logInfo(stringLoader.getString("Log_AMRequestDelegate_amRequestStarted_Header"), "synchronous",  httpMethod, this.getHeader().toString());
				
		JSONObject returnedJsonObject = null;		
		if (contentType != null) {
			uriRequest.addHeader("Content-Type", contentType);
		}
		
		try {	
			httpResponse = httpClient.execute(uriRequest);		
		} catch (ConnectTimeoutException e) {
			isTimeout = true;
			client.getConnectionManager().shutdown();			
			AMLogger.logError("In SyncHttpClient.sendRequest(): ConnectTimeoutException " + stringLoader.getString("Log_Exception_Occured"), e.getMessage());
			if (AMSetting.DebugMode) {
				e.printStackTrace();
			}
		} catch (SocketTimeoutException e) {
			isTimeout = true;
			client.getConnectionManager().shutdown();
			AMLogger.logError("In SyncHttpClient.sendRequest(): SocketTimeoutException " + stringLoader.getString("Log_Exception_Occured"), e.getMessage());
			if (AMSetting.DebugMode) {
				e.printStackTrace();
			}
		} catch (ClientProtocolException e) {
			AMLogger.logError("In SyncHttpClient.sendRequest(): ClientProtocolException " + stringLoader.getString("Log_Exception_Occured"), e.getMessage());
			if (AMSetting.DebugMode) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			AMLogger.logError("In SyncHttpClient.sendRequest(): IOException " + stringLoader.getString("Log_Exception_Occured"), e.getMessage());
			if (AMSetting.DebugMode) {
				e.printStackTrace();
			}
		}	
		
		// Return directly if exception occurs in above process
		if (httpResponse == null) {
			return null;
		}		
	
//		int responseCode = httpResponse.getStatusLine().getStatusCode();
//		String responseMessage = httpResponse.getStatusLine().getReasonPhrase();	
		
		HttpEntity entity = httpResponse.getEntity();
		String responseContent = null;
		if (entity != null)
		{			
			InputStream instream = null;
			try {
				instream = entity.getContent();
				responseContent = convertStreamToString(instream);						
				instream.close();
			} catch (IllegalStateException e) {
				AMLogger.logError("In SyncHttpClient.sendRequest(): IllegalStateException " + stringLoader.getString("Log_Exception_Occured"), e.getMessage());
				if (AMSetting.DebugMode) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				AMLogger.logError("In SyncHttpClient.sendRequest(): IOException " + stringLoader.getString("Log_Exception_Occured"), e.getMessage());
				if (AMSetting.DebugMode) {
					e.printStackTrace();
				}
			}
		}
		
		if ((responseContent != null) && (isValidJsonString(responseContent))) {			
			try {
				returnedJsonObject = new JSONObject(responseContent);
			} catch (JSONException e) {
				AMLogger.logError("In SyncHttpClient.sendRequest(): JSONException " + stringLoader.getString("Log_Exception_Occured"), e.getMessage());
				if (AMSetting.DebugMode) {
					e.printStackTrace();
				}
			}			
		}		
		this.isLoading = false;		
		// Print log information after finishing the request
		Header[] responseHeaders = httpResponse.getAllHeaders();
		if (AMSetting.DebugMode) {
			AMLogger.logInfo(
					stringLoader
							.getString("Log_AMRequestDelegate_amRequestDidReceiveResponse_Header"),
					"synchronous", httpMethod,
					getHeadersString(responseHeaders));
			AMLogger.logInfo(
					stringLoader
							.getString("Log_AMRequestDelegate_amRequestDidReceiveResponse_Body"),
					"synchronous", httpMethod, responseContent);
		}
		// Add timeout flag into the returned Json.
		try {
			returnedJsonObject.put("isTimeout", isTimeout);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnedJsonObject;
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
	 * Private method, used to add entity to request base.
	 */	
	private HttpEntityEnclosingRequestBase addEntityToRequestBase(HttpEntityEnclosingRequestBase requestBase, HttpEntity entity) {		
		if (entity != null) {
			requestBase.setEntity(entity);
		}
		return requestBase;
	}		
	
	/**
	 * Private method, used to convert an input stream to a string.
	 */
	private String convertStreamToString(InputStream is) {		
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			AMLogger.logError(
					"In SyncHttpClient.convertStreamToString(): IOException "
							+ stringLoader.getString("Log_Exception_Occured"),
					e.getMessage());
			if (AMSetting.DebugMode) {
				e.printStackTrace();
			}
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				AMLogger.logError(
						"In SyncHttpClient.convertStreamToString(): IOException "
								+ stringLoader
										.getString("Log_Exception_Occured"),
						e.getMessage());
				if (AMSetting.DebugMode) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}
	
	/**
	 * Private method, used to check whether a string is in Json format or not.
	 */
	private Boolean isValidJsonString(String response) {
		
		Boolean isValidJson = false;		
	
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(response);
		} catch (JSONException e) {
			AMLogger.logError("In SyncHttpClient.isValidJsonString(): JSONException " + stringLoader.getString("Log_Exception_Occured"), e.getMessage());
			if (AMSetting.DebugMode) {
				e.printStackTrace();
			}
		}
		
		if (jsonObject == null) {
			AMLogger.logError("In SyncHttpClient.isValidJsonString(): The response is not a valid Json string: \n %s", response);	
		} else {			
			isValidJson = true;
		}
		
		return isValidJson;
	}
	
	 /**
	 * private method, used to populate response's headers into a string.
	*/
    private String getHeadersString(Header[] headers) {    	
    	if ((headers == null) || (headers.length == 0)) {
    		return null;
    	}
    	
    	String headersString = "";
    	for (Header header : headers)
    	{
    		headersString += header.getName() + "=" + header.getValue() + ";";
    	}
    	// Remove the last char
    	headersString = headersString.substring(0, headersString.length()-2);
    	// Return the string with JSON format.
    	return "{" + headersString + "}";
    }
	
}

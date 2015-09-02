/**
  * Copyright 2015 Accela, Inc.
  *
  * You are hereby granted a non-exclusive, worldwide, royalty-free license to
  * use, copy, modify, and distribute this software in source code or binary
  * form for use in connection with the web services and APIs provided by
  * Accela.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
  * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
  * DEALINGS IN THE SOFTWARE.
  *
  */
package com.accela.mobile;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.accela.mobile.http.AMDocDownloadRequest;
import com.accela.mobile.http.AMHttpRequest;
import com.accela.mobile.http.AMImageLoader;
import com.accela.mobile.http.AMRequestFactory;
import com.accela.mobile.http.AMRequestQueue;
import com.accela.mobile.http.RequestParams;
import com.accela.mobile.http.volley.Request;


/**
 *  AccelaMobile Request object, used to process HTTP/HTTPS requests.
 *
 * 	@since 1.0
 */

public class AMRequest {

	/**
	 * HTTP method enumerations.
	 *
	 * @since 1.0
	 */
	public enum HTTPMethod
	{
		GET, POST, PUT, DELETE
	}

	/**POST
	 * Request type enumerations.
	 *
	 * @since 1.0
	 */
	public enum RequestType
	{
		AUTHENTICATION, MULTIPART, IMAGE, DEFAULT
	}

	/**
	 * Trace ID header name in HTTP/HTTPS response headers.
	 *
	 * @since 3.0
	 */
	protected static final String HEADER_X_ACCELA_TRACEID = "x-accela-traceid";

	/**
	 * Response Message header name in HTTP/HTTPS response headers.
	 *
	 * @since 3.0
	 */
	protected static final String HEADER_X_ACCELA_RESP_MESSAGE = "x-accela-resp-message";

	/**
	 * Environment header name in HTTP/HTTPS request headers.
	 *
	 * @since 3.0
	 */
	public static final String HEADER_X_ACCELA_ENVIRONMENT = "x-accela-environment";

	/**
	 * App ID header name in HTTP/HTTPS request headers.
	 *
	 * @since 3.0
	 */
	private static final String HEADER_X_ACCELA_APPID = "x-accela-appid";

	/**
	 * App Secret header name in HTTP/HTTPS request headers.
	 *
	 * @since 3.0
	 */
	private static final String HEADER_X_ACCELA_APPSECRET = "x-accela-appsecret";

	/**
	 * App Version header name in HTTP/HTTPS request headers.
	 *
	 * @since 3.0
	 */
	private static final String HEADER_X_ACCELA_APPVERSION = "x-accela-appversion";

	/**
	 * App Platform header name in HTTP/HTTPS request headers.
	 *
	 * @since 3.0
	 */
	private static final String HEADER_X_ACCELA_APPPLATFORM = "x-accela-appplatform";


	/**
	 * Agency header name in HTTP/HTTPS request headers.
	 *
	 * @since 3.0
	 */
	public static final String HEADER_X_ACCELA_AGENCY = "x-accela-agency";

	/**
	 * Agencies header name in HTTP/HTTPS request headers.
	 *
	 * @since 3.0
	 */
	public static final String HEADER_X_ACCELA_AGENCIES = "x-accela-agencies";

	/**
	 * The context which processes the request.
	 *
	 * @since 1.0
	 */
	private Context ownerContext;

	/**
	 * The tag of the request.
	 *
	 * @since 1.0
	 */
	private String tag;

	/**
	 * The service's full URL, which consists of both cloud host and service URI.
	 *
	 * @since 1.0
	 */
	private String serviceURL;

	/**
	 * HTTP method of the request.
	 *
	 * @since 1.0
	 */
	private HTTPMethod httpMethod;

	/**
	 * The request type, used to indicates the type of the current request.
	 * For example, the value AUTHENTICATION means it is used for user authenticating,
	 * the values UPLOAD and DOWNLOAD means they are used for document uploading and downloading respectively.
	 *
	 * @since 1.0
	 */
	private RequestType requestType;

	/**
	 * The delegate that will be called during the request life cycle.
	 *
	 * @since 1.0
	 */
	private AMRequestDelegate requestDelegate;


	/**
	 * The local path of the document which is uploaded(used in document uploading request only).
	 *
	 * @since 1.0
	 */
	private String amUploadDestinationPath;

	/**
	 * The view which presents the waiting indicator while request is being processed.
	 *
	 * @since 1.0
	 */
	private ViewGroup ownerView;

	/**
	 * The waiting indicator which is shown while a request is being processed.
	 *
	 * @since 1.0
	 */
	private ProgressDialog requestWaitingView;

	/**
	 * The AccelaMobile instance which creates the request.
	 *
	 * @since 3.0
	 */
	private AccelaMobile accelaMobile;

	/**
	 * The flag which indicates whether the request is cancelled (for AsyncHttpClient only).
	 *
	 * @since 3.0
	 */
	private Boolean isCancelled = false;

    /**
     * The collection of request parameters which will be appended to service URL with & symbol.
     *
     * @since 1.0
     */
    private RequestParams urlParams;

    private RequestParams postParams;

	/**
	 * The string loader which loads localized text.
	 *
	 * @since 3.0
	 */
	private ResourceBundle stringLoader = AMSetting.getStringResourceBundle();

    private AMRequestQueue requestQueue;

    private Map<String, String> requestHttpHeader = new HashMap<String, String>();

    protected AMHttpRequest mRequest;
	/**
	 * Constructor with the given parameters.
	 *
	 * @param serviceURL The service's full path(including server host).
	 * @param httpMethod One of HTTP method such as GET, POST, PUT or DELETE.
	 *
	 * @return An initialized AMRequest instance.
	 *
	 * @since 1.0
	 */
	public AMRequest(String serviceURL, RequestParams urlParams, RequestParams postParams, HTTPMethod httpMethod) {
		this.accelaMobile = AccelaMobile.getInstance();
		this.ownerContext = accelaMobile.ownerContext;
		this.serviceURL = serviceURL;
		this.urlParams = urlParams;
        this.postParams = postParams;
		this.httpMethod = httpMethod;
		this.tag = String.valueOf(new Random().nextInt(100));
        requestQueue = AMRequestQueue.getAMRequestQueue(this.ownerContext);
	}

	/**
	 * Constructor with the given parameters and delegate.
	 *
	 * @param serviceURL The service's full path(including server host).
	 * @param httpMethod One of HTTP method such as GET, POST, PUT or DELETE.
	 * @param requestDelegate The delegate which manages the request's lifecycle.
	 *
	 * @return An initialized AMRequest instance.
	 *
	 * @since 4.0
	 */
	public AMRequest(String serviceURL, RequestParams urlParams, RequestParams postParams, HTTPMethod httpMethod, AMRequestDelegate requestDelegate) {
		this(serviceURL, urlParams, postParams, httpMethod);
		this.requestDelegate = requestDelegate;
	}


	/**
	 * Cancel the current request.
	 *
	 * @return Void.
	 *
	 * @since 1.0
	 */
	public void cancelRequest() {
		if(mRequest !=null)
            mRequest.cancel();
	}

	/**
	 * Cancel the current request.
	 *
	 * @return Void.
	 *
	 * @since 3.0
	 */
	public Boolean isCancelled() {
		return this.isCancelled;
	}


    private HashMap<String, String> generateHttpHeader(){
        HashMap<String, String> httpHeader = new HashMap<String, String>();
        // Add app version and app id to HTTP header
        httpHeader.put(HEADER_X_ACCELA_APPVERSION, accelaMobile.getAppVersion());
        httpHeader.put(HEADER_X_ACCELA_ENVIRONMENT, accelaMobile.getEnvironment().name());
        httpHeader.put(HEADER_X_ACCELA_APPSECRET, accelaMobile.appSecret);
        httpHeader.put(HEADER_X_ACCELA_APPID, accelaMobile.appId);

        httpHeader.put(HEADER_X_ACCELA_AGENCY, accelaMobile.getAgency());
        httpHeader.put("Accept", "*/*");

//        if(requestHttpHeader!=null && requestHttpHeader.get(AccelaMobile.IS_ALL_AGENCIES)!=null){
//            httpHeader.put(HEADER_X_ACCELA_AGENCIES, requestHttpHeader.get(AccelaMobile.IS_ALL_AGENCIES));
//        }else if(requestHttpHeader!=null && requestHttpHeader.get(AccelaMobile.AGENCY_NAME)!=null){
//            httpHeader.put(HEADER_X_ACCELA_AGENCY, requestHttpHeader.get(AccelaMobile.AGENCY_NAME).toUpperCase());
//        }
//
//        if(requestHttpHeader!=null && requestHttpHeader.get(AccelaMobile.ENVIRONMENT_NAME)!=null){
//            httpHeader.put(HEADER_X_ACCELA_ENVIRONMENT, requestHttpHeader.get(AccelaMobile.ENVIRONMENT_NAME));
//        }else{
        httpHeader.put(HEADER_X_ACCELA_ENVIRONMENT, accelaMobile.getEnvironment().name());


        httpHeader.put(HEADER_X_ACCELA_APPPLATFORM, accelaMobile.getAppPlatform());
        // Add access token or app secret to HTTP header
        AuthorizationManager authorizationManager = accelaMobile.authorizationManager;
        if ((authorizationManager != null) && (authorizationManager.getAccessToken() != null)) {
            httpHeader.put("Authorization", accelaMobile.authorizationManager.getAccessToken());
        }
        else if ((accelaMobile != null) && (accelaMobile.appSecret != null)) {
            httpHeader.put(HEADER_X_ACCELA_APPSECRET, accelaMobile.appSecret);
        }

        if(requestHttpHeader!=null){   //copy the requestHttpHeader
            for (Map.Entry<String, String> entry : requestHttpHeader.entrySet()) {
                httpHeader.put(entry.getKey(), entry.getValue());
            }
        }
        return httpHeader;
    }

    public AMRequest loadImage(AMRequestDelegate requestDelegate, int maxWidth, int maxHeight, ImageView.ScaleType scaleType){
        // Initialize request delegate
        if (requestDelegate != null) {
            this.requestDelegate = requestDelegate;
        } else {
            this.requestDelegate = defaultRequestDelegate;
        }
        HashMap<String, String> httpHeader = generateHttpHeader();
        String serializeURL = assembleUrlWithParams(this.serviceURL, this.urlParams);
        AMImageLoader.getAMImageLoader(ownerContext).loadImage(serializeURL, httpHeader, this.requestDelegate, maxWidth, maxHeight, scaleType);
        return this;
    }

    public AMRequest loadImage(AMRequestDelegate requestDelegate, int maxWidth, int maxHeight){
        // Initialize request delegate
        if (requestDelegate != null) {
            this.requestDelegate = requestDelegate;
        } else {
            this.requestDelegate = defaultRequestDelegate;
        }
        HashMap<String, String> httpHeader = generateHttpHeader();
        String serializeURL = assembleUrlWithParams(this.serviceURL, this.urlParams);
        AMImageLoader.getAMImageLoader(ownerContext).loadImage(serializeURL, httpHeader, this.requestDelegate, maxWidth, maxHeight);
        return this;
    }

    public AMRequest loadImage(AMRequestDelegate requestDelegate){
        // Initialize request delegate
        if (requestDelegate != null) {
            this.requestDelegate = requestDelegate;
        } else {
            this.requestDelegate = defaultRequestDelegate;
        }
        HashMap<String, String> httpHeader = generateHttpHeader();
        String serializeURL = assembleUrlWithParams(this.serviceURL, this.urlParams);
        AMImageLoader.getAMImageLoader(ownerContext).loadImage(serializeURL, httpHeader, this.requestDelegate);
        return this;
    }

    public AMRequest downloadDocument(RequestParams paramData, String localFilePath, AMDocDownloadRequest.AMDownloadDelegate downloadRequest){
        HashMap<String, String> httpHeader = generateHttpHeader();
        String serializeURL = assembleUrlWithParams(this.serviceURL, this.urlParams);
        AMDocumentManager documentManager = AMDocumentManager.getAMDocumentManager(this.ownerContext);
        documentManager.addRequest(AMRequestFactory.createAMDocDownloadRequest(serializeURL, httpHeader, paramData, localFilePath, downloadRequest));
        documentManager.startRequest();
        return this;
    }

	/**
	 * Makes a request to the Accela Construct API endpoint with the given data using an HTTP POST method as an asynchronous operation.
	 *
	 * @param requestDelegate The request's delegate.
	 *
	 * @return The AMRequest object corresponding to this API call.
	 *
	 * @since 1.0
	 */
	public AMRequest sendRequest(AMRequestDelegate requestDelegate) throws JSONException {
		// Initialize request delegate
		if (requestDelegate != null) {
			this.requestDelegate = requestDelegate;
		} else {
			this.requestDelegate = defaultRequestDelegate;
		}

        HashMap<String, String> httpHeader = generateHttpHeader();

		String serializeURL = assembleUrlWithParams(this.serviceURL, this.urlParams);
		switch (this.httpMethod) {
			case GET:
				if (RequestType.AUTHENTICATION.equals(this.requestType))
				{
                    mRequest = AMRequestFactory.createLoginRequest(serializeURL, Request.Method.GET, httpHeader, null, false, this.requestDelegate);

                }else{
                    mRequest = AMRequestFactory.createJsonRequest(serializeURL, Request.Method.GET, httpHeader, null, false, this.requestDelegate);
				}
                if (mRequest!=null)
                    requestQueue.addToRequestQueue(mRequest);
				break;
			case POST:
				if (postParams == null) {
                    mRequest = AMRequestFactory.createJsonRequest(serializeURL, Request.Method.POST, httpHeader, null, false, this.requestDelegate);
                    requestQueue.addToRequestQueue(mRequest);
				} else {
					String contentType = null;
					if (RequestType.AUTHENTICATION.equals(this.requestType))
					{
                        mRequest = AMRequestFactory.createLoginRequest(serializeURL, Request.Method.POST, httpHeader, postParams.getAuthBody(), false, this.requestDelegate);
                        requestQueue.addToRequestQueue(mRequest);
                    } else if (RequestType.MULTIPART.equals(this.requestType)){
                        AMDocumentManager documentManager = AMDocumentManager.getAMDocumentManager(this.ownerContext);
                        documentManager.addRequest(AMRequestFactory.createAMMultiPartRequests(serializeURL, httpHeader, postParams, this.requestDelegate));
                        documentManager.startRequest();
					} else {
                        mRequest = AMRequestFactory.createJsonRequest(serializeURL, Request.Method.POST, httpHeader, postParams.getStringBody(), false, this.requestDelegate);
                        requestQueue.addToRequestQueue(mRequest);
					}
				}
				break;
			case PUT:
				if (postParams == null) {
                    mRequest = AMRequestFactory.createJsonRequest(serializeURL, Request.Method.PUT, httpHeader, null, false, this.requestDelegate);
                    requestQueue.addToRequestQueue(mRequest);
				} else {
					String contentType = null;
					if (RequestType.AUTHENTICATION.equals(this.requestType))
					{
						contentType = "application/x-www-form-urlencoded";
                        httpHeader.put("Content-Type", contentType);
                        mRequest = AMRequestFactory.createJsonRequest(serializeURL, Request.Method.PUT, httpHeader, postParams.getAuthBody(), false, this.requestDelegate);
                        requestQueue.addToRequestQueue(mRequest);
					}
					else if (RequestType.MULTIPART.equals(this.requestType))
					{
						contentType = "multipart/form-data";
                        httpHeader.put("Content-Type", contentType);
                        throw new RuntimeException("Not supported yet!");
					}
					else
					{
						contentType = "application/json";
                        httpHeader.put("Content-Type", contentType);
                        mRequest = AMRequestFactory.createJsonRequest(serializeURL, Request.Method.PUT, httpHeader, postParams.getStringBody(), false, this.requestDelegate);
                        requestQueue.addToRequestQueue(mRequest);
					}
				}
				break;
			case DELETE:
                mRequest = AMRequestFactory.createJsonRequest(serializeURL, Request.Method.DELETE, httpHeader, null, false, this.requestDelegate);
                requestQueue.addToRequestQueue(mRequest);
				break;
			default:
			}
		return this;
	}


	/**
	 * Uploads multiple binary files together with JSON data represented as an asynchronous operation or synchronous operation.
	 * That depends on the value of isSynchronous boolean variable.	 *
	 *
	 * @param attachmentInfo The file collection of key-value pairs.
	 * 									 Note the key name is "fileName", and the value is file's full path.
	 * @param requestDelegate The delegate for asynchronous request, or null for synchronous request.
	 * 									  Note this parameter is used only for asynchronous request (this.isSynchronous = false).
	 *
	 * @return The AMRequest object corresponding to this API call.
	 *
	 * @since 4.0
	 */
	public AMRequest uploadAttachments(Map<String, String> attachmentInfo, AMRequestDelegate requestDelegate) {
        RequestParams postData = new RequestParams();
		for (Map.Entry<String, String> entry : attachmentInfo.entrySet()) {
			String fileName = entry.getKey();
			String filePath = entry.getValue();
			File file = new File(filePath);
			if (file.exists()) {
				try {
					postData.put(fileName, file);
				} catch (FileNotFoundException e) {
					AMLogger.logError("In AMRequest.sendRequest(): FileNotFoundException " + stringLoader.getString("Log_Exception_Occured"), e.getMessage());
				}
			} else {
				AMLogger.logError("AMRequest.uploadAttachment(): " +  stringLoader.getString("Log_FILE_NOT_FOUND"), filePath);
			}
		}

		// Send request.
        // Send request.
        this.requestType = RequestType.MULTIPART;
        this.postParams = postData;
        try {
            this.sendRequest(requestDelegate);
        } catch (JSONException e) {
            AMLogger.logWarn(e.toString());
        }
        return this;
	}

	/**
	 * Get the value of property accelaMobile.
	 *
     * @return The value of property accelaMobile.
	 *
	 * @since 3.0
	 */
	public AccelaMobile getAccelaMobile() {
		return this.accelaMobile;
	}


	/**
	 * Get the value of property amUploadDestinationPath.
	 *
	 * @return The value of property amUploadDestinationPath.
	 *
	 * @since 1.0
	 */
	public String getAmUploadDestinationPath() {
		return this.amUploadDestinationPath;
	}


	/**
	 * Get the value of property httpMethod.
	 *
     * @return The value of property httpMethod.
	 *
	 * @since 3.0
	 */
	public HTTPMethod  getHttpMethod() {
		return this.httpMethod;
	}

	/**
	 * Get the value of property requestDelegate.
	 *
     * @return The value of property requestDelegate.
	 *
	 * @since 4.0
	 */
	public AMRequestDelegate getRequestDelegate(){
		if(null == this.requestDelegate){
			return this.defaultRequestDelegate;
		}
		else{
			return this.requestDelegate;
		}
	}

	/**
	 * Get the value of property postParams.
	 *
	 * @return The value of property postParams.
	 *
	 * @since 3.0
	 */
	public RequestParams getPostParams() {
		return this.postParams;
	}

	/**
	 * Get the value of property requestType.
	 *
     * @return The value of property requestType.
	 *
	 * @since 3.0
	 */
	public RequestType  getRequestType() {
		return this.requestType;
	}

	/**
	 * Get the value of property requestWaitingView.
	 *
	 * @return The value of property requestWaitingView.
	 *
	 * @since 1.0
	 */
	public ProgressDialog getRequestWaitingView() {
		return this.requestWaitingView;
	}

	/**
	 * Get the value of property serviceURL.
	 *
     * @return The value of property serviceURL.
	 *
	 * @since 3.0
	 */
	public String  getServiceURL() {
		return this.serviceURL;
	}

	/**
	 * Get the value of property tag.
	 *
     * @return The value of property tag.
	 *
	 * @since 3.0
	 */
	public String  getTag() {
		return this.tag;
	}

	/**
	 * Get the value of property urlParams.
	 *
	 * @return The value of property urlParams.
	 *
	 * @since 3.0
	 */
	public RequestParams getUrlParams() {
		return this.urlParams;
	}

	/**
	 *
	 * Set HTTP / HTTPS headers for the current request.
	 *
	 * @param httpHeaders Pairs of header key and value.
	 *
	 * @return Void.
	 *
	 * @since 3.0
     */
	public void setHttpHeader(Map<String, String> httpHeaders) {
		if ( httpHeaders == null || httpHeaders.size() == 0 )
			return;

        requestHttpHeader = httpHeaders;
	}


	/**
	 * Show a waiting indicator in the specified view.

	 * Note: This method sets holder view for the current request and then show a progress dialog in it.
	 * 			1.In asynchronous request, please call this method in the request delegate's onStart() method,
	 * 			   then dismiss the progress dialog in the request delegate's onSuccess() method or onFailure() method.
	 *			2.In synchronous request, please call this method just before the code line which sends out the request,
	 * 			   then dismiss the progress dialog after that code line.
	 *
	 * @param ownerView The view which presents the waiting view.
	 * @param labelText The message which is displayed in the waiting view.
	 *
	 * @return Void.
	 *
	 * @since 1.0
	 */
	public void setOwnerView(ViewGroup ownerView, String labelText) {
		this.ownerView = ownerView;
		if (this.ownerView != null) {
			this.requestWaitingView = ProgressDialog.show(this.ownerView.getContext(), null, labelText, false, false);
		}
	}

	/**
	 * Set message content in the waiting indicator view.
	 *
	 * @param labelText The message text to be assigned.
	 *
	 * @return Void.
	 *
	 * @since 3.0
	 */
	public void setProgressLabelText(String labelText) {
		this.requestWaitingView.setMessage(labelText);
	}

	/**
	 * Set the value of property requestType.
	 *
	 * @param requestType The new value to be assigned.
	 *
     * @return Void.
	 *
	 * @since 3.0
	 */
	public void  setRequestType(RequestType requestType) {
		this.requestType = requestType;
	}

	/**
	 * Set the value of property tag.
	 *
	 * @param tag The new tag to be assigned.
	 *
     * @return The value of property tag.
	 *
	 * @since 4.0
	 */
	public void setTag(String tag) {
		this.tag = tag;
	}






	/**
	 * Private method, used to assemble URL with query string..
	 */
	String assembleUrlWithParams(String url, RequestParams requestParams) {
		//AMLogger.logError("In AMRequest.assembleUrlWithParams()");
        if(requestParams==null)
            return url;
		boolean urlContainsLanguage = (url != null) && (url.contains("lang="));
        Map<String, String> urlParams = requestParams.getUrlParams();
		boolean paramsContainsLanguage = (urlParams != null) && (urlParams.containsKey("lang"));
		if ((!urlContainsLanguage) && (!paramsContainsLanguage)) {
			String languageCode = String.format("%s_%s", Locale.getDefault().getLanguage(),Locale.getDefault().getCountry());
			if (this.urlParams == null) {
                Map<String, String> params = new HashMap<String, String>();
                params.put("lang", languageCode);
				this.urlParams = new RequestParams();
                this.urlParams.setUrlParams(params);
			} else {
                Map<String, String> params = requestParams.getUrlParams();
                params.put("lang", languageCode);
			}
		}
		//Log.d("DEBUG", "************* In AMRequest.assembleUrlWithParams(): url = " + url);
		if (requestParams != null) {
	    	this.urlParams = requestParams;
	    	String paramString = requestParams.getParamString();
	    	//Log.d("DEBUG", "************* In AMRequest.assembleUrlWithParams(): paramString = " + paramString);
	    	if (!url.contains("?")) {
	    		//Log.d("DEBUG", "		==> Add: url.contains(\"?\") == false ");
	    		url += "?" + paramString;
	    	} else {
	    		//Log.d("DEBUG", "		==> Skip: url.contains(\"?\") == true ");
	    		url += "&" + paramString;
	    	}
		}
		return url;
	}


	/**
	 * Private variable, used as the default request delegate if it is not specified.
	 */
	private AMRequestDelegate defaultRequestDelegate= new AMRequestDelegate() {
		@Override
		public void onStart() {
			amRequestStarted(AMRequest.this);
		}
		@Override
		public void onSuccess(JSONObject responseJson) {
			amRequestDidReceiveResponse(AMRequest.this);
			this.amRequestDidLoad(AMRequest.this, responseJson);
		}
		@Override
		public void onFailure(AMError error) {
			amRequestDidReceiveResponse(AMRequest.this);
			this.amRequestDidFailWithError(AMRequest.this, error);
		}
	};
}

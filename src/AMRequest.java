package com.accela.mobile;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.view.ViewGroup;

import com.accela.mobile.http.AsyncHttpClient;
import com.accela.mobile.http.RequestParams;
import com.accela.mobile.http.SyncHttpClient;


/**
 * <pre>
 * 
 *  Accela Amobile
 *  File: AMRequest.java
 * 
 *  Accela, Inc.
 *  Copyright (C): 2013
 * 
 *  Description:
 *  AccelaMobile Request object, used to process HTTP/HTTPS requests.
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
	
	/**
	 * Request type enumerations.
	 * 
	 * @since 1.0
	 */
	protected enum RequestType
	{
		NORMAL, AUTHENTICATION, DOWNLOAD, MULTIPART, POST_JSON
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
	private static final String HEADER_X_ACCELA_ENVIRONMENT = "x-accela-environment";
	
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
	private static final String HEADER_X_ACCELA_AGENCY = "x-accela-agency";
	
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
	 * The AsyncHttpClient instance which processes asynchronous request.
	 * 
	 * @since 1.0
	 */
	private AsyncHttpClient  asyncHttpClient;
	
	/**
	 * The SyncHttpClient instance which processes synchronous request.
	 * 
	 * @since 1.0
	 */	
	private SyncHttpClient  syncHttpClient;
	
	/**
	 * The collection of request parameters which will be appended to service URL with & symbol.
	 * 
	 * @since 1.0
	 */
	private RequestParams urlParams;	
	
	/**
	 * The post data (only used for http method POST or PUT).
	 * 
	 * @since 1.0
	 */
	private RequestParams postParams;
	
	/**
	 * The local path of the document which is downloaded(used in document downloading request only).
	 * 
	 * @since 1.0
	 */
	private String amDownloadDestinationPath;	
	
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
	 * The flag which indicates whether the request is synchronous or not.
	 * 
	 * @since 3.0
	 */
	private Boolean isSynchronous = false;	
	
	/**
	 * The flag which indicates whether the request is cancelled (for AsyncHttpClient only).
	 * 
	 * @since 3.0
	 */
	private Boolean isCancelled = false;	
	
	/**
	 * The string loader which loads localized text.
	 * 
	 * @since 3.0
	 */
	private ResourceBundle stringLoader = AMSetting.getStringResourceBundle();
	
	
	/**
	 * Constructor with the given parameters.
	 * 
	 * @param accelaMobile The AccelaMobile instance which create this request.	
	 * @param serviceURL The service's full path(including server host).
	 * @param params The collection of URL parameters which will be added to URL with & symbol.
	 * @param httpMethod One of HTTP method such as GET, POST, PUT or DELETE.
	 * 
	 * @return An initialized AMRequest instance.
	 * 
	 * @since 1.0
	 */	
	public AMRequest(AccelaMobile accelaMobile, String serviceURL, RequestParams params, HTTPMethod httpMethod) {				
		this.accelaMobile = accelaMobile;
		this.ownerContext = accelaMobile.ownerContext;
		this.serviceURL = serviceURL;	
		this.urlParams = params;	
		this.httpMethod = httpMethod;	
		this.tag = String.valueOf(new Random().nextInt(100));			
	}	

	/**
	 * Constructor with the given parameters and delegate.
	 * 
	 * @param accelaMobile The AccelaMobile instance which create this request.	
	 * @param serviceURL The service's full path(including server host).
	 * @param params The collection of URL parameters which will be added to URL with & symbol.
	 * @param httpMethod One of HTTP method such as GET, POST, PUT or DELETE.
	 * @param requestDelegate The delegate which manages the request's lifecycle.
	 * 
	 * @return An initialized AMRequest instance.
	 * 
	 * @since 4.0
	 */	
	public AMRequest(AccelaMobile accelaMobile, String serviceURL, RequestParams params, HTTPMethod httpMethod, AMRequestDelegate requestDelegate) {				
		this(accelaMobile, serviceURL, params, httpMethod);
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
		if (!isSynchronous && (asyncHttpClient != null)) {  // is asynchronous
			asyncHttpClient.cancelRequests(this.ownerContext, true);
			isCancelled = true;
			if ((this.requestWaitingView != null) && (this.requestWaitingView.isShowing())) {
				this.requestWaitingView.dismiss();
			}
		}
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

	/**
	 * Synchronously send a request identified by the Accela Mobile Cloud API endpoint with the given data using an HTTP POST method.
	 *
	 * @param paramData The content sent with the corresponding request.
	 *
	 * @return The resulting object is a JSON object if succeed; otherwise, null.
	 *
	 * @since 1.0
	 */
	
	public JSONObject fetch(RequestParams paramData) {						
		// Initialize HTTP client.
		asyncHttpClient = null;
		if (syncHttpClient == null) {
			syncHttpClient = createSyncHttpClient();
		}
		
		// Add app version and app id to HTTP header
		syncHttpClient.addHeader(HEADER_X_ACCELA_APPVERSION, getAppVersion());
		syncHttpClient.addHeader(HEADER_X_ACCELA_APPID, accelaMobile.getAppId());	
		syncHttpClient.addHeader(HEADER_X_ACCELA_APPSECRET, accelaMobile.getAppSecret());
		syncHttpClient.addHeader(HEADER_X_ACCELA_AGENCY, accelaMobile.getAgency());
		syncHttpClient.addHeader(HEADER_X_ACCELA_ENVIRONMENT, accelaMobile.getEnvironment().name());	
		syncHttpClient.addHeader(HEADER_X_ACCELA_APPPLATFORM, getAppPlatform());
		// Add access token or app secret to HTTP header
		AuthorizationManager authorizationManager = accelaMobile.authorizationManager;		
		if ((authorizationManager != null) && (authorizationManager.getAccessToken() != null)) {
			syncHttpClient.addHeader("Authorization", accelaMobile.authorizationManager.getAccessToken());
		} 		
		// Process HTTP/HTTPS request
		JSONObject returnedJsonObject = null;		
		String serializeURL = assembleUrlWithParams(this.serviceURL, this.urlParams);				
		switch (this.httpMethod) {
			case GET:		
				returnedJsonObject = syncHttpClient.get(serializeURL);				
				break;				
			case POST:
				this.postParams = paramData;
				if (paramData == null) {	
					returnedJsonObject = syncHttpClient.post(serializeURL, null);					
				} else {
					String contentType = null;
					if (RequestType.AUTHENTICATION.equals(this.requestType))
					{
						contentType = "application/x-www-form-urlencoded";
						returnedJsonObject = syncHttpClient.post(serializeURL, paramData.getStringEntity(true), contentType);	
					}
					else if (RequestType.MULTIPART.equals(this.requestType))
					{
						contentType = "multipart/form-data;boundary=" + syncHttpClient.getMultipartSeparatorLine();
						syncHttpClient.addHeader("Content-Type", contentType);
						syncHttpClient.post(serializeURL, paramData.getEntity(), contentType);
					}
					else 
					{	
						contentType = "application/json";
						returnedJsonObject = syncHttpClient.post(serializeURL, paramData.getStringEntity(true), contentType);
					}
				}
				break;				
			case PUT:
				this.postParams = paramData;
				if (paramData == null) {					
					returnedJsonObject = syncHttpClient.put(serializeURL, null);
				} else {
					String contentType = null;
					if (RequestType.AUTHENTICATION.equals(this.requestType)) 
					{
						contentType = "application/x-www-form-urlencoded";
						returnedJsonObject = syncHttpClient.put(serializeURL, paramData.getStringEntity(true), contentType);					
					} else {		
						contentType = "application/json";
						returnedJsonObject = syncHttpClient.put(serializeURL, paramData.getStringEntity(true), contentType);
					}					
				}			
				break;						
			case DELETE:
				returnedJsonObject = syncHttpClient.delete(serializeURL);
				break;					
			default:				
			}			
			return returnedJsonObject;	
	}

	/**
	 * Makes a request to the Accela Mobile Cloud API endpoint with the given data using an HTTP POST method as an asynchronous operation.
	 *
	 * @param paramData The content sent with the corresponding request.
	 * @param requestDelegate The request's delegate.
	 *
	 * @return The AMRequest object corresponding to this API call.
	 *
	 * @since 1.0
	 */
	public AMRequest sendRequest(RequestParams paramData, AMRequestDelegate requestDelegate) {		
		// Initialize request delegate
		if (requestDelegate != null) {
			this.requestDelegate = requestDelegate;
		} else {
			this.requestDelegate = defaultRequestDelegate;
		}		
		// Initialize HTTP client.
		syncHttpClient = null;
		if (asyncHttpClient == null) {
			asyncHttpClient = createAsyncHttpClient();
		}
		// Add app version and app id to HTTP header
		asyncHttpClient.addHeader(HEADER_X_ACCELA_APPVERSION, getAppVersion());
		asyncHttpClient.addHeader(HEADER_X_ACCELA_ENVIRONMENT, accelaMobile.getEnvironment().name());	
		asyncHttpClient.addHeader(HEADER_X_ACCELA_APPSECRET, accelaMobile.getAppSecret());
		asyncHttpClient.addHeader(HEADER_X_ACCELA_APPID, accelaMobile.getAppId());
		asyncHttpClient.addHeader(HEADER_X_ACCELA_AGENCY, accelaMobile.getAgency());
		asyncHttpClient.addHeader(HEADER_X_ACCELA_APPPLATFORM, getAppPlatform());
		// Add access token or app secret to HTTP header
		AuthorizationManager authorizationManager = accelaMobile.authorizationManager;		
		if ((authorizationManager != null) && (authorizationManager.getAccessToken() != null)) {
			asyncHttpClient.addHeader("Authorization", accelaMobile.authorizationManager.getAccessToken());
		} 
		else if ((accelaMobile != null) && (accelaMobile.getAppSecret() != null)) {
			asyncHttpClient.addHeader(HEADER_X_ACCELA_APPSECRET, accelaMobile.getAppSecret());
		}
		String serializeURL = assembleUrlWithParams(this.serviceURL, this.urlParams);			
		switch (this.httpMethod) {
			case GET:	
				if (RequestType.AUTHENTICATION.equals(this.requestType))
				{
					asyncHttpClient.addHeader("Content-Type", "application/x-www-form-urlencoded");
				} 				
				else 
				{
					asyncHttpClient.addHeader("Content-Type", "application/json");
				}
				asyncHttpClient.get(serializeURL, this.requestDelegate);				
				break;				
			case POST:	
				this.postParams = paramData;
				if (paramData == null) {	
					asyncHttpClient.post(serializeURL, this.requestDelegate);					
				} else {
					String contentType = null;
					if (RequestType.AUTHENTICATION.equals(this.requestType))
					{
						contentType = "application/x-www-form-urlencoded";
						asyncHttpClient.addHeader("Content-Type", contentType);
						asyncHttpClient.post(this.ownerContext, serializeURL, paramData.getStringEntity(false), contentType, this.requestDelegate);
					}
					else if (RequestType.MULTIPART.equals(this.requestType))
					{
						contentType = "multipart/form-data;boundary=" + asyncHttpClient.getMultipartSeparatorLine();
						asyncHttpClient.addHeader("Content-Type", contentType);
						asyncHttpClient.post(this.ownerContext, serializeURL, paramData.getEntity(), contentType, this.requestDelegate);
					}
					else 
					{	
						contentType = "application/json";
						asyncHttpClient.addHeader("Content-Type", contentType);
						asyncHttpClient.post(this.ownerContext, serializeURL, paramData.getStringEntity(true), contentType, this.requestDelegate);
					}
				}			
				break;				
			case PUT:
				this.postParams = paramData;
				if (paramData == null) {					
					asyncHttpClient.put(serializeURL, this.requestDelegate);					
				} else {
					String contentType = null;
					if (RequestType.AUTHENTICATION.equals(this.requestType))
					{
						contentType = "application/x-www-form-urlencoded";
						asyncHttpClient.addHeader("Content-Type", contentType);
						asyncHttpClient.put(this.ownerContext, serializeURL, paramData.getStringEntity(false), contentType, this.requestDelegate);
					}
					else if (RequestType.MULTIPART.equals(this.requestType))
					{
						contentType = "multipart/form-data";
						asyncHttpClient.addHeader("Content-Type", contentType);
						asyncHttpClient.put(this.ownerContext, serializeURL, paramData.getEntity(), contentType, this.requestDelegate);
					}
					else 
					{	
						contentType = "application/json";
						asyncHttpClient.addHeader("Content-Type", contentType);
						asyncHttpClient.put(this.ownerContext, serializeURL, paramData.getStringEntity(true), contentType, this.requestDelegate);
					}
				}	
				break;						
			case DELETE:
				asyncHttpClient.delete(serializeURL, this.requestDelegate);
				break;					
			default:				
			}				
		return this;
	}
	
	/**
	 * Makes an asynchronous request using an HTTP POST method to upload multiple attachments.
	 *
	 * @param postData The multipart form data which contains both "fileInfo" JSON and file streams.
	 * 									 Note fileInfo's JSON contains keys "serviceProviderCode","fileName","type",and "description".
	 * @param attachments The attachments to be uploaded in the request. Values mapping: Key => File Path.
	 * @param requestDelegate The request's delegate.
	 *
	 * @return The AMRequest object corresponding to this API call.
	 *
	 * @since 3.0
	 */
	 
	public AMRequest sendRequest(RequestParams postData, Map<String, String> attachments, AMRequestDelegate requestDelegate) {
		for (Map.Entry<String, String> entry : attachments.entrySet()) {	
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
		this.requestType = RequestType.MULTIPART;
		if (this.isSynchronous) {
			this.fetch(postData);
		} else {
			this.sendRequest(postData, requestDelegate);			
		}
		
		return this;
	}
	
	/**
	 * Uploads multiple binary files together with JSON data represented as an asynchronous operation or synchronous operation.
	 * That depends on the value of isSynchronous boolean variable.	 * 
	 *
	 * @param postData The multipart form data which contains both "fileInfo" JSON and file streams.
	 * 									 Note fileInfo's JSON contains keys "serviceProviderCode","fileName","type",and "description".
	 * @param attachmentInfo The file collection of key-value pairs.
	 * 									 Note the key name is "fileName", and the value is file's full path.	 
	 * @param requestDelegate The delegate for asynchronous request, or null for synchronous request.
	 * 									  Note this parameter is used only for asynchronous request (this.isSynchronous = false).
	 * 
	 * @return The AMRequest object corresponding to this API call.
	 * 
	 * @since 4.0
	 */
	public AMRequest uploadAttachments(RequestParams postData, Map<String, String> attachmentInfo, AMRequestDelegate requestDelegate) {
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
		this.requestType = RequestType.MULTIPART;
		if (this.isSynchronous) {
			this.fetch(postData);
		} else {
			this.sendRequest(postData, requestDelegate);			
		}
		
		return this;
	}

	/**
	 * Download a binary file as an asynchronous operation or synchronous operation.
	 * That depends on the value of isSynchronous boolean variable.
	 * 
	 * @param localFile The local file path with which the downloaded file will be created.	
	 * 	@param requestDelegate The delegate for asynchronous request, or null for synchronous request.
	 * 									  Note this parameter is used only for asynchronous request (this.isSynchronous = false).
	 * 
	 * @return The AMRequest object corresponding to this API call.
	 * 
	 * @since 1.0
	 */
	public AMRequest downloadAttachment(String localFile, AMRequestDelegate requestDelegate) {		
		return downloadAttachment(localFile, null, requestDelegate);
	}
	
	
	/**
	 * Download a binary file as an asynchronous operation.
	 * 
	 * @param localFile The local file path with which the downloaded file will be created.	
	 * @param paramData The collection of parameters associated with post request body.
	 * @param requestDelegate The delegate for asynchronous request, or null for synchronous request.
	 * 
	 * @return The AMRequest object corresponding to this API call.
	 * 
	 * @since 1.0
	 */
	public AMRequest downloadAttachment(String localFile,RequestParams paramData,AMRequestDelegate requestDelegate) {		
		this.amDownloadDestinationPath = localFile;		
		this.requestType = RequestType.DOWNLOAD;
		if (this.isSynchronous) {
			this.fetch(null);
		} else {
			this.sendRequest(paramData, requestDelegate);			
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
	 * Get the value of property amDownloadDestinationPath.
	 * 
	 * @return The value of property amDownloadDestinationPath.
	 * 
	 * @since 1.0
	 */
	public String getAmDownloadDestinationPath() {
		return this.amDownloadDestinationPath;
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
	 * Get the value of property asyncHttpClient.
	 * 
     * @return The value of property asyncHttpClient.
	 * 
	 * @since 1.0
	 */
	public AsyncHttpClient  getAsyncHttpClient() {
		return this.asyncHttpClient;
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
	 * Get the value of property syncHttpClient.
	 * 
	 * @return The value of property syncHttpClient.
	 * 
	 * @since 1.0
	 */
	public SyncHttpClient  getSyncHttpClient() {
		return this.syncHttpClient;
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
	 * Indicates whether the request is in progress.
	 *
	 * @return true when this request is in progress; otherwise, false.
	 *
	 * @since 1.0
	 */
	public Boolean isLoading() {
		boolean result = false;
		if (isSynchronous && (syncHttpClient != null)) {
			result = syncHttpClient.isLoading();			
		} else if (!isSynchronous && (asyncHttpClient != null)) { // is asynchronous
			result = asyncHttpClient.isLoading();
		}
		return result;
	}
	
	
	
	/**
	 * Set the value of property accelaMobile.
	 * 
	 * @param accelaMobile The new value to be assigned.
	 * 
     * @return Void.
	 * 
	 * @since 3.0
	 */
	public void setAccelaMobile(AccelaMobile accelaMobile) {
		this.accelaMobile = accelaMobile;
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
	public void setHttpHeader(HashMap<String, String> httpHeaders) {
		if (isSynchronous) {
			if (syncHttpClient == null) {
				syncHttpClient = createSyncHttpClient();
			}
			for (String header : httpHeaders.keySet()) {
				syncHttpClient.addHeader(header, httpHeaders.get(header));
			}			
		} else {  // is asynchronous
			if (asyncHttpClient == null) {
				asyncHttpClient = createAsyncHttpClient();
			}
			for (String header : httpHeaders.keySet()) {
				asyncHttpClient.addHeader(header, httpHeaders.get(header));
			}
		}
	}	
	
	/**
	 * 
	 * Set value for property isSynchronous.
	 * 
	 * @param isSynchronous true or false
	 * 
	 * @return Void.
	 * 
	 * @since 3.0
     */
	public void setIsSynchronous(Boolean isSynchronous) {
		this.isSynchronous = isSynchronous;
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
	 * Wait until the current request is finished.
	 *
	 * @return Void.
	 *
	 * @since 3.0
	 */
	public void waitUntilFinished() {
		if (isSynchronous && (syncHttpClient != null)) {
			while (syncHttpClient.isLoading()) {
				try {
					this.wait(3000); // Wait 3 seconds
				} catch (InterruptedException e) {
					AMLogger.logError("In AMRequest.waitUntilFinished()-syncHttpClient: InterruptedException " + stringLoader.getString("Log_Exception_Occured"), e.getMessage());
				}				
			}			
		} else if (!isSynchronous && (asyncHttpClient != null)) { // is asynchronous
			while (asyncHttpClient.isLoading()) {
				try {
					this.wait(3000); // Wait 3 seconds
				} catch (InterruptedException e) {
					AMLogger.logError("In AMRequest.waitUntilFinished()-asyncHttpClient: InterruptedException " + stringLoader.getString("Log_Exception_Occured"), e.getMessage());
				}				
			}
		}
	}	

	/**
	 * Private method, used to initialize an AsyncHttpClient instance.
	 */	
	private AsyncHttpClient createAsyncHttpClient() {		
		AsyncHttpClient asyncHttpClient = new AsyncHttpClient();		 
		if (RequestType.DOWNLOAD.equals(this.requestType))  {
			 asyncHttpClient.addHeader("Accept", "application/octet-stream");	
		} else {
			 asyncHttpClient.addHeader("Accept", "application/json");
		}		
		return asyncHttpClient;
	}
	
	/**
	 * Private method, used to initialize a SyncHttpClient instance.
	 */	
	private SyncHttpClient createSyncHttpClient() {		
		SyncHttpClient syncHttpClient = new SyncHttpClient();		 
		if (RequestType.DOWNLOAD.equals(this.requestType))  {
			syncHttpClient.addHeader("Accept", "application/octet-stream");				
		} else {
			 syncHttpClient.addHeader("Accept", "application/json");
		}	 
		return syncHttpClient;
	}
	/**
	 * Private method, used to get version name from AndroidManifest.xml.
	 */	
	private String getAppVersion(){
		PackageInfo pkg = null;
        String appVersion = "1.0";
        try {
        	pkg = ownerContext.getPackageManager().getPackageInfo(ownerContext.getPackageName(), 0);
            appVersion = pkg.versionName;            
        } catch (NameNotFoundException e) {
        	AMLogger.logError("In AMRequest.getAppVersion(): NameNotFoundException " + stringLoader.getString("Log_Exception_Occured"), e.getMessage());
        }         
        return appVersion;        
     }	
	/**
	 * Private method, used to get platform information from device.
	 */	
	private String getAppPlatform(){
		boolean isTablet = (ownerContext.getResources().getConfiguration().screenLayout
	            & Configuration.SCREENLAYOUT_SIZE_MASK)
	            >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	    String osType = (isTablet) ? "Android Tablet" : "Android Phone";
		String osVersion = android.os.Build.VERSION.RELEASE; // e.g. osVersion = "1.6"
		String deviceName = android.os.Build.MODEL;        
        return osType + "|" + osVersion + "|" + deviceName;        
     }	
	
	/**
	 * Private method, used to assemble URL with query string..
	 */	
	private String assembleUrlWithParams(String url, RequestParams urlParams) {
		String languageCode = String.format("%s_%s", Locale.getDefault().getLanguage(),Locale.getDefault().getCountry());
    	if ((urlParams != null) && (!urlParams.hasKey("lang"))) {			
    		urlParams.put("lang", languageCode);
		} else if (urlParams == null) {
			urlParams = new RequestParams("lang", languageCode);
		}    	
    	this.urlParams = urlParams;
    	String paramString = urlParams.getParamString();
		url += "?" + paramString;	
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
		public void onFailure(Throwable error) {
			amRequestDidReceiveResponse(AMRequest.this);
			AMError amError = new AMError(String.valueOf(AMError.ERROR_CODE_Internal_Server_Error), error.getMessage(), null);
			this.amRequestDidFailWithError(AMRequest.this, amError);
		}
	};	
}

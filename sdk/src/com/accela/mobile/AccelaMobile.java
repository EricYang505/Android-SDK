/** 
  * Copyright 2014 Accela, Inc. 
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.json.JSONObject;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.text.TextUtils;

import com.accela.mobile.AMBatchResponse.AMBatchRequestDelegate;
import com.accela.mobile.AMRequest.HTTPMethod;
import com.accela.mobile.http.RequestParams;


/**
 *  AccelaMobile is the main object of the Accela SDK for Android.
 *  
 * 	@since 1.0
 */

public class AccelaMobile {	
	
	/**
	 * The enum of authorization status
	 * 
	 * @since 3.0
	 */
	public enum AuthorizationStatus
	{
		AUTHORIZED, LOGGEDIN, NONE
	}	
	
	/**
	 * Environment enumerations.
	 * 
	 * @since 3.0
	 */
	public enum Environment
	{
		PROD, TEST, DEV, STAGE, CONFIG, SUPP
	}	
	
	/**
	 * Authorization Status enumerations.
	 * 
	 * @since 3.0
	 */
	
	/**
	 * The static instance.
	 * 
	 * @since 3.0
	 */
	private static AccelaMobile instance;

	/**
	 * The URL of cloud authorization host. For examle: https://auth.accela.com
	 * 
	 * @since 3.0
	 */
	protected String amAuthHost = AMSetting.AM_OAUTH_HOST;
	
	/**
	 * The URL of cloud API host. For examle: https://apis.accela.com
	 * 
	 * @since 3.0
	 */
	protected String amApisHost = AMSetting.AM_API_HOST;
	
	/**
	 * The AuthorizationManager instance which manages session state.
	 * 
	 * @since 3.0
	 */
	protected AuthorizationManager authorizationManager;	
	
	/**
	 * The flag which indicates whether user profile should be saved to local storage after successful login.
	 * 
	 * @since 1.0
	 */	 
	protected Boolean amIsRemember = true;	
	
	/**
	 * The session delegate which methods are called during the progress of user authentication.
	 * 
	 * @since 1.0
	 */
	protected AMSessionDelegate sessionDelegate;	
	
	/**
	 * The authorization status
	 * 
	 * @since 3.0
	 */
	protected AuthorizationStatus authorizationStatus;
	
	/**
	 * The Android context (usually an Activity) which creates the current AccelaMobile instance.
	 * 
	 * @since 1.0
	 */
	protected Context ownerContext;
	
	/**
	 * The string loader which loads localized text strings.
	 * 
	 * @since 3.0
	 */
	protected  ResourceBundle stringLoader = AMSetting.getStringResourceBundle();
	
	/**
	 * The URL schema used by the callback of user authorization or other intents which are returned from web view.
	 * 
	 * @since 1.0
	 */
	private String urlSchema;
	
	/**
	 * The ID string of the application registered on developer portal.
	 * 
	 * @since 1.0
	 */
	private String appId;
	
	/**
	 * The security string of the application registered on developer portal.
	 * 
	 * @since 3.0
	 */
	private String appSecret ;
	
	
	/**
	 * The agency to which user logs in.
	 * 
	 * @since 1.0
	 */
	protected String agency ;

	/**
	 * The environment to which user logs in.
	 * 
	 * @since 3.0
	 */
	protected Environment environment = Environment.PROD ;	// Default value	
	
	protected HashMap<String, String> customHttpHeader = new HashMap<String, String>();

	
	/**
	 * 
	 * Get a default static instance of the current class.
	 * 
	 * @return An initialized AccelaMobile instance.
	 * 
	 * @since 3.0
	 */
	public static AccelaMobile defaultInstance() {
    	if (instance == null)  {
    		instance = new AccelaMobile();
    	}
    	return instance;
    }	
	
	public AuthorizationManager getAuthorizationManager() {
		return this.authorizationManager;
	}
	
	/**
	 * 
	 * Start a batch request.
	 * 
	 * @return An initialized AMBatchSession instance.
	 * 
	 * @since 4.0
	 */
	public static AMBatchSession batchBegin(){
		return new AMBatchSession();
	}
	
	
	/**
	 * 
	 * Commit the currently started batch request.
	 * 
	 * @param session The batch session instance.
	 * @param batchRequestDelegate The delegate of batch session.
	 * 
	 * @return An initialized AccelaMobile instance.
	 * 
	 * @since 4.0
	 */
	public static void batchCommit(AMBatchSession session, AMBatchRequestDelegate batchRequestDelegate){
		final AMBatchSession batchSession = session;
		final AMBatchRequestDelegate batchRequestDelegate1 = batchRequestDelegate;
		AMRequestDelegate requestDelegate = new AMRequestDelegate() {			
			@Override
			public void onStart() {}
			
			@Override
			public void onSuccess(JSONObject result) {			
				AMBatchResponse response = new AMBatchResponse(result);		
				List<JSONObject> childResponses = response.getResult();
				List<AMRequest> requests = batchSession.getRequests();
				
				if(response == null || childResponses.size()!= requests.size()){
					batchRequestDelegate1.onSuccessful();
					return;
				}
				
				for(int index = 0; index < requests.size(); index++){				
					AMRequest request = requests.get(index);
					JSONObject childResponse = childResponses.get(index);
					AMRequestDelegate rstDelegate = request.getRequestDelegate();			
					rstDelegate.onSuccess(childResponse);				
				}
				
				if(batchRequestDelegate1 != null){
					batchRequestDelegate1.onSuccessful();
				}
			}
			
			@Override
			public void onFailure(AMError error) {
				if(batchRequestDelegate1 != null){
					batchRequestDelegate1.onFailed(error);
				}
			}
		};
		
		session.executeAsync(requestDelegate);
	}

		
	/**
	 * 
	 * Constructor with the given activity, application Id, and application secret.
	 * 
	 * @param ownerContext The Android context which creates the current AccelaMobile instance.
	 * @param appId The Id string of the current application.
	 * @param appSecret The secret string of the current application.
	 * 
	 * @return An initialized AccelaMobile instance.
	 * 
	 * @since 3.0
	 */
	public AccelaMobile(Context ownerContext, String appId, String appSecret) {
		// Initialize instance properties.
		this.ownerContext = ownerContext;
		this.appId = appId;		
		this.appSecret = appSecret;
		this.authorizationManager = new AuthorizationManager(this);
		// Remember the current instance if the default instance is null.
		if (instance == null) {
			instance = this;		
		}
	}
	
	/**
	 * 
	 * Constructor with the given activity, application Id, application secret, and session delegate.
	 * 
	 * @param ownerContext The Android context which creates the current AccelaMobile instance.
	 * @param appId The Id string of the current application.
	 * @param appSecret The secret string of the current application.
	 * @param sessionDelegate The receiever's delegate or null if it doesn't have a delegate.  See {@link AMSessionDelegate} for more information.
	 * 
	 * @return An initialized AccelaMobile instance.
	 * 
	 * @since 3.0
	 */
	public AccelaMobile(Context ownerContext, String appId, String appSecret, AMSessionDelegate sessionDelegate) {
		// Initialize instance properties.
		this(ownerContext,appId, appSecret);
		this.sessionDelegate = (sessionDelegate != null) ? sessionDelegate : defaultSessionDelegate;				
	}
	
	/**
	 * 
	 * Constructor with the given context, application Id, session delegate, authorization server URL, and API server URL.
	 * 
	 * @param ownerContext The Android context which creates the current AccelaMobile instance.
	 * @param appId The Id string of the current application.
	 * @param appSecret The secret string of the current application.
	 * @param sessionDelegate The receiever's delegate or null if it doesn't have a delegate.  See {@link AMSessionDelegate} for more information.
	 * @param authHost The URL of cloud server for user authorization.
	 * @param apisHost The URL of cloud server for service API calling.
	 * 
	 * @return An initialized AccelaMobile instance.
	 * 
	 * @since 4.0
	 */				
	public AccelaMobile(Context ownerContext, String appId, String appSecret, AMSessionDelegate sessionDelegate, String authHost, String apisHost) {
		this(ownerContext, appId, appSecret, sessionDelegate);
		this.amAuthHost = (authHost !=null) ? authHost : this.amAuthHost;
		this.amApisHost = (apisHost !=null) ? apisHost : this.amApisHost;
	}
	
	/**
	 * 
	 * Set the URL of cloud server for user authorization and service API 
	 * 
	 * @param amAuthHost The URL of cloud server for user authorization.
	 * @param amApisHost The URL of cloud server for service API calling.
	 * 
	 * @return Void.
	 * 
	 * @since 4.0
	 */		
	public void setHostUrl(String authHost, String apisHost) {
		this.amAuthHost = authHost;
		this.amApisHost = apisHost;
	}
	
	/**
	 * 
	 * Get the URL of cloud server for user authorization
	 * 
	 * @return The URL of cloud server for user authorization.
	 * 
	 * @since 4.0
	 */	
	public String getAuthHost() {
		return this.amAuthHost;
	}
	
	/**
	 * 
	 * Get the URL of cloud server for service API calling.
	 * 
	 * @return The URL of cloud server for service API calling.
	 * 
	 * @since 4.0
	 */	
	public String getApisHost() {
		return this.amApisHost;
	}
	
	/**
	 *
	 * Turn on / off debugging logs.
	 *
	 * @param isOn true or false.
	 * 
	 * @return Void.
	 *
	 * @since 4.0
	 */
	public void setDebug(boolean isOn) {
		 AMSetting.DebugMode = isOn;	
	}	
	
	/**
	 * Authorizes user through web view with the given permissions array.  
	 *
	 * @param permissions The array of access permissions. For example, search_records get_single_record  create_record, and etc.
	 *
	 * @return Void.
	 * 
	 * @since 3.0
	 */	
	public void authorize(String[] permissions) {	
		authorize(permissions, null);
	}
	
	/**
	 * Authorizes user through web view with the given agency and permissions array.    
	 *	 
	 * @param permissions The array of access permissions. For example, search_records get_single_record  create_record, and etc.
	 * @param agency The agency name.
	 *
	 * @return Void.
	 * 
	 * @since 3.0
	 */	
	public void authorize(String[] permissions, String agency) {
		showAuthorizationWebView(permissions, agency, false);
	}	
	
	/**
	 * Authorizes the user with the given agency, through the login web view wrapped in a native login dialog.
	 * The native login dialog will be presented for the user if he / she has not valid token.
	 * Otherwise, the user will be authorized directly without the native login dialog.
	 *
	 * @param permissions The array of access permissions. For example, search_records get_single_record  create_record, and etc.
	 *
	 * @return Void.
	 * 
	 * @since 3.0
	 */	
	public void authorize2(String[] permissions) {		
		authorize2(permissions, null);
	}
	
	/**
	 * Authorizes the user with default agency defined in cloud, through the login web view wrapped in a native login dialog.
	 * The native login dialog will be presented for the user if he / she has not valid token.
	 * Otherwise, the user will be authorized directly without the native login dialog.
	 *
	 * @param permissions The array of access permissions. For example, search_records get_single_record  create_record, and etc.
	 * @param agency The agency name.
	 *
	 * @return Void.
	 * 
	 * @since 3.0
	 */	
	public void authorize2(String[] permissions, String agency) {		
		showAuthorizationWebView(permissions, agency, true); 			
	}		
	
	/**
	 * Synchronously get resource identified by the Accela Mobile Cloud API endpoint with the given parameters using HTTP GET method.
	 *
	 * @param path The path to the Accela Mobile Cloud API endpoint.
	 * @param urlParams The collection of parameters associated with the specific URL.
	 *
	 * @return A simple JSON object or a complex JSON object which contains child JSON objects.
	 *
	 * @since 1.0
	 */
	public JSONObject fetch(String path, RequestParams urlParams) {
		return this.fetch(path, urlParams, HTTPMethod.GET, null);
	}
	
	/**
	 * Synchronously get resource identified by the Accela Mobile Cloud API endpoint with the given parameters using the given HTTP method.
	 *	
	 * @param path The path to the Accela Mobile Cloud API endpoint.
	 * @param urlParams The collection of parameters associated with the specific URL.
	 * @param httpMethod The HTTP data transfer method (such as GET, POST, PUT or DELETE).
	 * @param postData The content sent with the corresponding request(only used in POST or PUT method).
	 *
	 * @return The resulting object is a simple JSON object or a complex JSON object which contains child JSON objects.
	 *
	 * @since 1.0
	 */
	public JSONObject fetch(String path, RequestParams urlParams, HTTPMethod httpMethod, RequestParams postData) {	
		AMRequest amRequest = new AMRequest(this, this.amApisHost + path, urlParams, httpMethod);
		amRequest.setAccelaMobile(this);
		amRequest.setHttpHeader(customHttpHeader);
					
		return amRequest.fetch(postData);
	}
	
	/**
	 *
	 * Get the value of property agency.
	 *
	 * @return The value of property agency.
	 *
	 * @since 1.0
	 */
	public String getAgency() {
		if (TextUtils.isEmpty(this.agency)) {
			if (this.authorizationManager != null
					&& !TextUtils.isEmpty(this.authorizationManager.getAgency())
					&& !this.authorizationManager.getAgency().equalsIgnoreCase(this.agency)) {
				this.agency = this.authorizationManager.getAgency();
			}
		}
		return this.agency;
	}
	
	/**
	 * public method, used to get version name from AndroidManifest.xml.
	 */	
	public String getAppVersion(){
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
	 * public method, used to get platform information from device.
	 */	
	public String getAppPlatform(){
		boolean isTablet = (ownerContext.getResources().getConfiguration().screenLayout
	            & Configuration.SCREENLAYOUT_SIZE_MASK)
	            >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	    String osType = (isTablet) ? "Android Tablet" : "Android Phone";
		String osVersion = android.os.Build.VERSION.RELEASE; // e.g. osVersion = "1.6"
		String deviceName = android.os.Build.MODEL;        
        return osType + "|" + osVersion + "|" + deviceName;        
     }	
	
	/**
	 *
	 * Get the value of property appId.
	 *
	 * @return The value of property appId.
	 *
	 * @since 1.0
	 */
	public String getAppId() {
		return this.appId;	
	}	  
	
	/**
	 *
	 * Get the value of property appSecret.
	 *
	 * @return The value of property appSecret.
	 *
	 * @since 3.0
	 */
	public String getAppSecret() {
		return this.appSecret;	
	}
	
	/**
	 *
	 * Get the flag whether the current user has been authorized.	
	 *
	 * @return true if user has been authorized; otherwise false.
	 *
	 * @since 3.0
	 */
	public AuthorizationStatus getAuthorizationStatus() {
		return this.authorizationStatus;
	}	

	/**
	 *
	 * Get the value of property environment.
	 *
	 * @return The value of property environment.
	 *
	 * @since 3.0
	 */
	public Environment getEnvironment() {
		if (TextUtils.isEmpty(this.agency)) {
			if (this.authorizationManager != null
					&& !TextUtils.isEmpty(this.authorizationManager.getEnvironment())
					&& !this.authorizationManager.getEnvironment().equalsIgnoreCase(this.environment.name())) {
				this.environment = Enum.valueOf(Environment.class, this.authorizationManager.getEnvironment());
			}
		}
		return this.environment;
	}
	
	/**
	 *
	 * Get the value of property sessionDelegate.
	 *
	 * @return The value of property sessionDelegate.
	 *
	 * @since 1.0
	 */
	public AMSessionDelegate getSessionDelegate() {
		return this.sessionDelegate;	
	}
	
	
	/**
	 *
	 * Get the value of property urlSchema.
	 *
	 * @return The value of property urlSchema.
	 *
	 * @since 2.1
	 */
	public String getUrlSchema() {
		return this.urlSchema;	
	}	
	
	private HashMap<String, String> getCustomHttpHeader(){
		return this.customHttpHeader;
	}
	
	/**
	 * 
	 * Checks if the current session is valid or not.
	 * 
	 * @return true if user session is valid; otherwise false.
	 * 
	 * @since 1.0
	 */
	public Boolean isSessionValid() {		
		return (this.authorizationManager != null) && (this.authorizationManager.getAccessToken() != null);
	}

	/**
	 * 
	 * Refresh token.
	 *
	 * @param sessionDelegate The session delegate which handle the success or failure resule of token refreshing.
	 * @return The request which processes the token refreshing.
	 * 
	 * @since 4.0
	 */
	public AMRequest refreshToken(AMSessionDelegate sessionDelegate) {			
		AMRequest tokenRequest = null;
		if (this.authorizationManager !=null) {
			tokenRequest = authorizationManager.fetchAccessTokenByRefreshToken(sessionDelegate);
		}
		return tokenRequest;
	}
	
	/**
	 * 
	 * Logs out the user.
	 * 
	 * @return Void.
	 * 
	 * @since 1.0
	 */
	public void logout() {
		// Clear token.
		if (authorizationManager != null) {
			authorizationManager.clearAuthorizationAndToken(true);
		}
		// Call session delegate.
		if (this.sessionDelegate != null) {
			this.sessionDelegate.amDidLogout();			
		}
	}

	/**
	 * Makes a request to the Accela Mobile Cloud API endpoint with the given parameters using HTTP GET method as an asynchronous operation.
	 *
	 * @param path The path to the Accela Mobile Cloud API endpoint.
	 * @param urlParams The collection of parameters associated with the specific URL.
	 * @param requestDelegate The request's delegate or null if it doesn't have a delegate.  See {@link AMRequestDelegate} for more information.
	 *
	 * @return The AMRequest object corresponding to this API call.
	 *
	 * @since 1.0
	 */
	public AMRequest request(String path, RequestParams urlParams, AMRequestDelegate requestDelegate) {
		return this.request(path, urlParams, HTTPMethod.GET, null, requestDelegate);
	}
	
	public AMRequest request(AMBatchSession batchSession, String path, RequestParams urlParams, AMRequestDelegate requestDelegate) {
		AMRequest request = new AMRequest(this, path, urlParams,HTTPMethod.GET, requestDelegate);
		batchSession.add(request);
		return request;
	}
	
	/**
	 * Makes a request to the Accela Mobile Cloud API endpoint with the given parameters using the given HTTP method as an asynchronous operation.
	 *
	 * @param path The path to the Accela Mobile Cloud API endpoint.
	 * @param urlParams The collection of parameters associated with the specific URL.
	 * @param httpMethod The HTTP data transfer method (such as GET, POST, PUT or DELETE).
	 * @param postData The content sent with the corresponding request(only used in POST or PUT method).
	 * @param requestDelegate The request's delegate or null if it doesn't have a delegate.  See {@link AMRequestDelegate} for more information.
	 *
	 * @return The AMRequest object corresponding to this Accela Mobile Cloud API endpoint call.
	 *
	 * @since 1.0
	 */
	public AMRequest request(String path, RequestParams urlParams, HTTPMethod httpMethod, RequestParams postData, AMRequestDelegate requestDelegate) {
		AMRequest amRequest = new AMRequest(this, this.amApisHost + path, urlParams, httpMethod);
		amRequest.setAccelaMobile(this);
		return amRequest.sendRequest(postData, requestDelegate);
	}
	
	/**
	 * Makes a request to the Accela Mobile Cloud API endpoint with the given parameters using the given HTTP method as an asynchronous operation.
	 *
	 * @param path The path to the Accela Mobile Cloud API endpoint.
	 * @param urlParams The collection of parameters associated with the specific URL.
	 * @param httpMethod The HTTP data transfer method (such as GET, POST, PUT or DELETE).
	 * @param postData The content sent with the corresponding request(only used in POST or PUT method).
	 * @param requestDelegate The request's delegate or null if it doesn't have a delegate.  See {@link AMRequestDelegate} for more information.
	 *
	 * @return The AMRequest object corresponding to this Accela Mobile Cloud API endpoint call.
	 *
	 * @since 1.0
	 */
	public AMRequest request(AMBatchSession batchSession,String path, RequestParams urlParams, HTTPMethod httpMethod, RequestParams postData, AMRequestDelegate requestDelegate) {
		AMRequest request = new AMRequest(this, path, urlParams, HTTPMethod.POST, requestDelegate);
		batchSession.add(request);
		return request;
	}
	
	/**
	 * 
	 * Makes a request to upload multiple attachments as an asynchronous operation.
	 *
	 * @param path The path to the Accela Mobile Cloud API endpoint.
	 * @param urlParams The collection of parameters associated with the specific URL.
	 * @param httpMethod The HTTP data transfer method (such as GET, POST, PUT or DELETE).
	 * @param postData The content sent with the corresponding request(only used in POST or PUT method).
	 * @param attachments The attachments to be uploaded in the request. Values mapping: Key => File Path.
	 * @param requestDelegate The request's delegate or null if it doesn't have a delegate.  See {@link AMRequestDelegate} for more information.
	 *	 
	 * @return The AMRequest object corresponding to this Accela Mobile Cloud API endpoint call.
	 *
	 * @since 3.0
	 */
	 
	public AMRequest request(String path, RequestParams urlParams, HTTPMethod httpMethod, RequestParams postData, Map<String, String> attachments, AMRequestDelegate requestDelegate) {
		AMRequest amRequest = new AMRequest(this, this.amApisHost  + path, urlParams, httpMethod);
		amRequest.setAccelaMobile(this);
		return amRequest.sendRequest(postData, attachments, requestDelegate);
	}
	
	
	/**
	 *
	 * Set the value of property amIsRemember.
	 *
	 * @param remember true or false.
	 *
	 * @return Void.
	 *
	 * @since 1.0
	 */
	public void setAmIsRemember(Boolean remember) {	
		this.amIsRemember = remember;	
	}
	
	
	/**
	 *
	 * Set the value of property authorizationManager.
	 *
	 * @param authorizationManager The new value to be assigned.
	 *
	 * @return Void.
	 *
	 * @since 3.0
	 */
	public void setAuthorizationManager(AuthorizationManager authorizationManager) {
		this.authorizationManager = authorizationManager;	
	}	
	
	/**
	 *
	 * Set the value of property environment.
	 *
	 * @param environmentType The new value to be assigned.
	 * 
	 * @return Void.
	 *
	 * @since 3.0
	 */
	public void setEnvironment(Environment environment) {
		 this.environment = environment;	
	}
	

	/**
	 *
	 * Set the value of property sessionDelegate.
	 *
	 * @param sessionDelegate The new value to be assigned.
	 *
	 * @return Void.
	 *
	 * @since 1.0
	 */
	public void setSessionDelegate(AMSessionDelegate sessionDelegate) {
		this.sessionDelegate = sessionDelegate;	
	}	
	
	/**
	 *
	 * Set the value of property urlSchema.
	 *
	 * @param urlSchema The new value to be assigned.
	 *
	 * @return Void.
	 *
	 * @since 2.1
	 */
	public void setUrlSchema(String urlSchema) {
		this.urlSchema = urlSchema;	
	}	
	
	public void setCustomHttpHeader(HashMap<String, String> customHttpHeader){
		this.customHttpHeader = customHttpHeader;
	}	
	
	/**
	 * 
	 * Uploads a set of binary files as an asynchronous operation. 
	 *
	 * @param path The path to the Accela Mobile Cloud API endpoint.
	 * @param postData The array of file's JSON object is posted together with attachments.
	 * 									 Note file's JSON object contains keys "serviceProviderCode","fileName","type",and "description".
	 * @param files The file collection of key-value pairs.
	 * 									 Note the key name is "fileName", and the value is file's full path.	
	 * @param requestDelegate The request's delegate or null if it doesn't have a delegate.  See {@link AMRequestDelegate} for more information. 
	 *
	 * @return The AMRequest object corresponding to this Accela Mobile Cloud API endpoint call.
	 *
	 * @since 1.0
	 */
	public AMRequest uploadAttachments(String path, RequestParams postData, Map<String, String> fileInformation,  AMRequestDelegate requestDelegate) {
		AMRequest amRequest = new AMRequest(this, this.amApisHost + path,  null, HTTPMethod.POST);	
		amRequest.setAccelaMobile(this);
		return amRequest.uploadAttachments(postData, fileInformation, requestDelegate);
	}


	public AMRequest downloadAttachment(String path, RequestParams postParams,  String localFile, AMRequestDelegate requestDelegate) {
		AMRequest amRequest = new AMRequest(this, this.amApisHost + path, null, HTTPMethod.POST);	
		amRequest.setAccelaMobile(this);
		return amRequest.downloadAttachment(localFile, postParams, requestDelegate);
	}	
	
	public AMRequest downloadAttachment(String path, String localFile, AMRequestDelegate requestDelegate){
		AMRequest amRequest = new AMRequest(this, this.amApisHost + path, null, HTTPMethod.GET);	
		amRequest.setAccelaMobile(this);
		return amRequest.downloadAttachment(localFile, requestDelegate);
	}
	
	public String getToken(){
		return authorizationManager.getAccessToken();
	}
	
	/**
	 * Private constructor, called by the defaultInstance() static method.
	 */	 
	private AccelaMobile() {}	 
	
	/**
	 * Private method, used to show login web view in an independent web browser or a native dialog.
	 */	 
	private  void showAuthorizationWebView(String[] permissions, String agency, boolean isWrappedWebView) {		
		// Initialize authorization manager if it is null
		this.authorizationManager = (this.authorizationManager !=null) ? this.authorizationManager : new AuthorizationManager(this);
		// Return directly if the authorization manager has access token (loaded from local store)
		if ((authorizationManager.getAccessToken() != null) && (sessionDelegate != null))
		{
			sessionDelegate.amDidLogin();
			return;
		}
		// Return directly if internet permission is not granted in AndroidManifest.xml file.
		else if (this.ownerContext.checkCallingOrSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
			new AlertDialog.Builder(ownerContext)
			.setTitle(null)
			.setMessage(stringLoader.getString("Error_Require_Internet_Permission"))
			.setNegativeButton(stringLoader.getString("Button_OK"), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// Do nothing except closing the alert dialog.
				}
			}).create().show();
			return;
		}
			
		// Otherwise, show the login dialog which embeds the HTML login view.		
		this.authorizationManager.setClientInfo(this.appId, this.appSecret, this.environment.name(), agency, this.amAuthHost, this.amApisHost);	
		this.authorizationManager.setIsRememberToken(this.amIsRemember);
		this.authorizationManager.setSessionDelegate(this.sessionDelegate);		
		
		if (isWrappedWebView) {  // Login through a dialog which wraps HTML login web view.
			String redirectUrl = urlSchema + "://authorize";		
		 	if (agency != null) {
		 		redirectUrl += "&agency=" + agency;
		 	}	
			String authorizationURL = this.authorizationManager.getAuthorizeUrl4WebLogin(redirectUrl, permissions);
			AMLoginDialogWrapper amLoginDialogWrapper = new AMLoginDialogWrapper(this, authorizationURL);
			this.authorizationManager.getAuthorizeCode4Public(amLoginDialogWrapper, redirectUrl, permissions);
		} else {   // Login through an independent web browser.
			HashMap<String, Object> actionBundle = new HashMap<String, Object>();			
			actionBundle.put("permissions", permissions);
			if (agency != null) {
				actionBundle.put("agency_name", agency);
			}
			AuthorizationActivity.accelaMobile = this;	
			Intent intent = new Intent(this.ownerContext, AuthorizationActivity.class);
			intent.putExtra("isWrappedWebView", isWrappedWebView);	
			intent.putExtra("actionBundle", actionBundle);	
			try {
				((Activity) this.ownerContext).startActivity(intent);
			} catch (ActivityNotFoundException e) {			
				AMLogger.logError("In AccelaMobile.authorize(String agency, String[] permissions): ActivityNotFoundException " + stringLoader.getString("Log_Exception_Occured"), e.getMessage());
				AMLogger.logError(stringLoader.getString("Log_AuthorizationActivity_NOT_Declared"));			
			} 	
		}
	}
	/**
	 * Private variable, used as the default value of sessionDelegate property if its value is not initialized.
	 */	 
	private AMSessionDelegate defaultSessionDelegate = new AMSessionDelegate() {
		
		public void amDidCancelLogin() {
			AMLogger.logInfo(stringLoader.getString("Log_AMSessionDelegate_amDidCancelLogin"));	
		}
		public void amDidLogin() {	
			AMLogger.logInfo(stringLoader.getString("Log_AMSessionDelegate_amDidLogin"));
		}
		public void amDidLoginFailure(AMError error) {	
			AMLogger.logInfo(stringLoader.getString("Log_AMSessionDelegate_amDidLoginFailure"), error.toString());	
		}
		
		public void amDidLogout() {
			AMLogger.logInfo(stringLoader.getString("Log_AMSessionDelegate_amDidLogout"));	
		}			
		public void amDidSessionInvalid(AMError error) {
			AMLogger.logInfo(stringLoader.getString("Log_AMSessionDelegate_amDidSessionInvalid"), error.toString());	
		}		
	};	 	
	
}

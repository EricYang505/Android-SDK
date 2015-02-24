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
import java.util.ResourceBundle;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;

import com.accela.mobile.AMRequest.HTTPMethod;
import com.accela.mobile.AMRequest.RequestType;
import com.accela.mobile.AccelaMobile.AuthorizationStatus;
import com.accela.mobile.http.RequestParams;


/**
 *  Authorization Manager object, which fetch and manage session data.
 * 
 * 	@since 3.0
 */

public class AuthorizationManager {
	static final String SESSION_STORE_PREF_FILE = "SessionStorePrefsFile";

	/**
	 * The key of environment name stored in local SharedPreferences file.
	 * 
	 * @since 3.0
	 */
	static final String ENVIRONMENT_KEY_IN_PREF_FILE = "environment";

	/**
	 * The key of agency name stored in local SharedPreferences file.
	 * 
	 * @since 3.0
	 */
	static final String AGENCY_KEY_IN_PREF_FILE = "agency";

	/**
	 * The key of user name stored in local SharedPreferences file.
	 * 
	 * @since 3.0
	 */
	static final String USER_KEY_IN_PREF_FILE = "user";

	/**
	 * The key of access token stored in local SharedPreferences file.
	 * 
	 * @since 3.0
	 */
	private static final String TOKEN_KEY_IN_PREF_FILE = "access_token";
	
	/**
	 * The key of refresh token stored in local SharedPreferences file.
	 * 
	 * @since 4.0
	 */
	private static final String REFRESH_TOKEN_KEY_IN_PREF_FILE = "refresh_token";

	/**
	 * The SharedPreferences instance used to save login information.
	 * 
	 * @since 3.0
	 */
	private SharedPreferences sessionStorePrefs;

	/**
	 * The session delegate which manage session life cycle.
	 * 
	 * @since 3.0
	 */
	private AMSessionDelegate sessionDelegate;

	/**
	 * The authorization code got from server.
	 * 
	 * @since 3.0
	 */
	private String authorizationCode;

	/**
	 * The access token got from server.
	 * 
	 * @since 3.0
	 */
	private String accessToken;
	
	/**
	 * The refresh token got from server.
	 * 
	 * @since 3.0
	 */
	private String refreshToken;

	/**
	 * The URL of authorization server.
	 * 
	 * @since 3.0
	 */
	private String authorizationServer;

	/**
	 * The URL of api server.
	 * 
	 * @since 3.0
	 */
	private String apisServer;

	/**
	 * The environment for authorization.
	 * 
	 * @since 3.0
	 */
	private String environment;

	/**
	 * The agency for authorization.
	 * 
	 * @since 3.0
	 */
	private String agency;

	/**
	 * The user's name (or civic ID) for authorization.
	 * 
	 * @since 3.0
	 */
	private String user;

	/**
	 * The user's password for authorization.
	 * 
	 * @since 3.0
	 */
	private String password;

	/**
	 * The client Id (App Id) for authorization.
	 * 
	 * @since 3.0
	 */
	private String clientId;

	/**
	 * The client secret for authorization.
	 * 
	 * @since 3.0
	 */
	private String clientSecret;

	/**
	 * The authorization state for authorization.
	 * 
	 * @since 3.0
	 */
	private String authorizationState;

	/**
	 * The array of access permissions. For example, search_records
	 * get_single_record create_record, and etc.
	 * 
	 * @since 3.0
	 */
	private String[] permissions;

	/**
	 * The AccelaMobile instance which creates the request.
	 * 
	 * @since 3.0
	 */
	private AccelaMobile accelaMobile;

	/**
	 * The activity instance which processes authorization request.
	 * 
	 * @since 3.0
	 */
	private Context ownerContext;

	/**
	 * The native SDK login dialog which is invoked for private authorization.
	 * 
	 * @since 3.0
	 */
	private AMLoginView loginDialog;

	/**
	 * The AuthorizationActivity which authorize user through independent web
	 * browser.
	 * 
	 * @since 3.0
	 */
	private AuthorizationActivity authorizationActivity;

	/**
	 * The AMLoginDialogWrapper dialog which wraps the HTML login view.
	 * 
	 * @since 4.0
	 */
	private AMLoginDialogWrapper loginDialogWrapper;

	/**
	 * The request instance which processes HTTP / HTTPS request to get
	 * authorization code or access token.
	 * 
	 * @since 3.0
	 */
	private AMRequest currentRequest;

	/**
	 * The flag which indicates whether the current authorization is processed
	 * for native or not(public).
	 * 
	 * @since 3.0
	 */
	private Boolean isNativeAuthorization = false;

	/**
	 * The flag which indicates whether the access token is saved to local
	 * SharedPreferences file or not.
	 * 
	 * @since 3.0
	 */
	private Boolean isRememberToken = true;

	/**
	 * The holder view which presents waiting indicator while authorization /
	 * token request is being processed.
	 * 
	 * @since 3.0
	 */
	// private ViewGroup processIndicatorHolderView;

	/**
	 * The redirect URL which is returned from authorization request.
	 * 
	 * @since 3.0
	 */
	private String redirectUrl;	

	/**
	 * The string loader which loads localized text.
	 * 
	 * @since 3.0
	 */
	protected ResourceBundle stringLoader = AMSetting.getStringResourceBundle();

	

	/**
	 * Constructor.
	 * 
	 * @return An initialized AuthorizationManager instance.
	 * 
	 * @since 3.0
	 */
	AuthorizationManager(AccelaMobile accelaMobile) {
		// Set property values.
		this.accelaMobile = accelaMobile;
		this.ownerContext = accelaMobile.ownerContext;
		this.authorizationState = "inspection";
		// Initialize SharedPreferences instance.
		sessionStorePrefs = this.ownerContext.getSharedPreferences(SESSION_STORE_PREF_FILE, 0);
		// Load access token stored locally.
		this.accessToken = sessionStorePrefs.getString(TOKEN_KEY_IN_PREF_FILE,null);
		this.refreshToken = sessionStorePrefs.getString(REFRESH_TOKEN_KEY_IN_PREF_FILE,null);
		this.agency = sessionStorePrefs.getString(AGENCY_KEY_IN_PREF_FILE, null);
		this.user = sessionStorePrefs.getString(USER_KEY_IN_PREF_FILE, null);
		this.environment = sessionStorePrefs.getString(ENVIRONMENT_KEY_IN_PREF_FILE, null);
	}

	/**
	 * 
	 * Clear the authorization code and access token got in the previous
	 * request.
	 * 
	 * @param clearSessionStore
	 *            Flag which indicates whether to clear local session store or not.
	 * 
	 * @return Void.
	 * 
	 * @since 3.0
	 */
	public void clearAuthorizationAndToken(Boolean clearSessionStore) {
		// Clear the values of authorization code and access token.
		this.authorizationCode = null;
		this.accessToken = null;
		this.refreshToken = null;
		accelaMobile.authorizationStatus = AuthorizationStatus.NONE;
		// Clear the values in local session store.
		if (clearSessionStore) {
			SharedPreferences.Editor prefsWriter = sessionStorePrefs.edit();
			prefsWriter.remove(TOKEN_KEY_IN_PREF_FILE);
			prefsWriter.remove(REFRESH_TOKEN_KEY_IN_PREF_FILE);
			prefsWriter.commit();
		}
	}
	
	/**
	 * 
	 * Get the value of property environment.
	 * 
	 * @return The value of property environment.
	 * 
	 * @since 3.0
	 */
	public String getEnvironment() {
		return this.environment;
	}

	/**
	 * 
	 * Get the value of property agency.
	 * 
	 * @return The value of property agency.
	 * 
	 * @since 3.0
	 */
	public String getAgency() {
		return this.agency;
	}

	/**
	 * 
	 * Get the value of property user.
	 * 
	 * @return The value of property user.
	 * 
	 * @since 3.0
	 */
	public String getUser() {
		return this.user;
	}

	/**
	 * 
	 * Get the value of property authorizationCode.
	 * 
	 * @return The value of property authorizationCode.
	 * 
	 * @since 3.0
	 */
	String getAuthorizationCode() {
		return this.authorizationCode;
	}
	

	/**
	 * 
	 * Get the value of property accessToken.
	 * 
	 * @return The value of property accessToken.
	 * 
	 * @since 3.0
	 */
	public String getAccessToken() {
		return this.accessToken;
	}
	
	/**
	 * 
	 * Get the value of property refreshToken.
	 * 
	 * @return The value of property accessToken.
	 * 
	 * @since 4.0
	 */
	public String getRefreshToken() {
		return this.refreshToken;
	}

	/**
	 * 
	 * Get the value of property isNativeAuthorization.
	 * 
	 * @return The value of property isNativeAuthorization.
	 * 
	 * @since 3.0
	 */
	Boolean isNativeAuthorization() {
		return this.isNativeAuthorization;
	}

	/**
	 * 
	 * Set the value of property isRememberToken.
	 * 
	 * @param isRemember
	 *            true or false.
	 * 
	 * @return Void.
	 * 
	 * @since 3.0
	 */
	public void setIsRememberToken(Boolean isRemember) {
		this.isRememberToken = isRemember;
	}

	/**
	 * 
	 * Set the value of property sessionDelegate.
	 * 
	 * @param sessionDelegate
	 *            The session delegate to be assigned.
	 * 
	 * @return Void.
	 * 
	 * @since 3.0
	 */
	public void setSessionDelegate(AMSessionDelegate sessionDelegate) {
		this.sessionDelegate = sessionDelegate;
	}

	/**
	 * 
	 * Set client's basic information.
	 * 
	 * @param clientId The clientId value to be assigned.
	 * @param clientSecret The clientSecret value to be assigned.
	 * @param environment The environment value to be assigned.
	 * @param agency The agency value to be assigned.
	 * @param authServer The URL of authorization server.
	 * @param apisServer The URL of api server.
	 * 
	 * @return Void.
	 * 
	 * @since 3.0
	 */
	public void setClientInfo(String clientId, String clientSecret,
			String environment, String agency, String authServer,
			String apisServer) {
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.agency = agency;
		this.environment = environment;
		this.authorizationServer = authServer;
		this.apisServer = apisServer;
	}

	/**
	 * 
	 * Send request to authorize user through private API(invoked by native SDK login view).
	 * 
	 * @param loginDialog The login dialog view which processes the request.
	 * @param agency The agency to which the user belongs.
	 * @param user The user's name.
	 * @param password The user's password.
	 * @param permissions The array of access permissions. 
	 * For example, search_records,get_single_record create_record
	 * @param requestDelegate The request's delegate.
	 * @param is4Civic Flag which indicates whether the authorization is for civic
	 *            user or not(agency user).
	 * 
	 * @return The AMRequest object corresponding to this API call.
	 * 
	 * @since 3.0
	 */
	AMRequest getAuthorizeCode4Private(AMLoginView loginDialog, String agency,
			String user, String password, String[] permissions, Boolean is4Civic) {
		this.loginDialog = loginDialog;
		this.agency = agency;
		this.user = user;
		this.password = password;
		this.permissions = permissions;
		this.isNativeAuthorization = true;
		// Clear the previous token (not clear the token saved in local
		// SharedPreferences file).
		clearAuthorizationAndToken(false);
		// this.processIndicatorHolderView = (ViewGroup)((Activity)
		// this.ownerContext).findViewById(android.R.id.content).getRootView();
		// Send request to get token.
		return fetchAccessToken();
	}

	/**
	 * 
	 * Open web view to authorize user through public API(in oAuth way).
	 * 
	 * @param authorizationActivity
	 *            The view object which presents the authorization web view.
	 * @param redirectUrl
	 *            The redirect URL which will be returned to client app from the
	 *            web view.
	 * @param permissions
	 *            The array of access permissions. For example, search_records
	 *            get_single_record create_record
	 * 
	 * @return Void.
	 * 
	 * @since 3.0
	 */
	void getAuthorizeCode4Public(Object authorizationView, String redirectUrl,
			String[] permissions) {
		// Clear the previous token (not clear the token saved in local
		// SharedPreferences file).
		clearAuthorizationAndToken(false);
		// Show login web view.
		if (authorizationView.getClass() == AuthorizationActivity.class) { // Login
																			// through
																			// an
																			// independent
																			// web
																			// browser.
			this.authorizationActivity = (AuthorizationActivity) authorizationView;
			String authorizeUrl = this.getAuthorizeUrl4WebLogin(redirectUrl,
					permissions);
			Intent intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(authorizeUrl));
			this.authorizationActivity.startActivity(intent);
		} else if (authorizationView.getClass() == AMLoginDialogWrapper.class) { // Login
																					// through
																					// a
																					// dialog
																					// which
																					// wraps
																					// HTML
																					// login
																					// web
																					// view.
			this.loginDialogWrapper = (AMLoginDialogWrapper) authorizationView;
			this.loginDialogWrapper.show();
		}
	}

	/**
	 * 
	 * Populate the URL of authorization based on the given parameters.
	 * 
	 * @param redirectUrl
	 *            The redirect URL which will be returned to client app from the
	 *            web view.
	 * @param permissions
	 *            The array of access permissions. For example, search_records
	 *            get_single_record create_record
	 * 
	 * @return Void.
	 * 
	 * @since 3.0
	 */
	String getAuthorizeUrl4WebLogin(String redirectUrl, String[] permissions) {
		this.redirectUrl = redirectUrl;
		this.permissions = permissions;
		this.isNativeAuthorization = false;
		// Clear the previous token (not clear the token saved in local
		// SharedPreferences file).
		clearAuthorizationAndToken(false);
		// Populate HTTP parameters
		HashMap<String, String> authorizationInfo = new HashMap<String, String>();
		authorizationInfo.put("environment", this.environment);
		authorizationInfo.put("client_id", this.clientId);
		authorizationInfo.put("redirect_uri", this.redirectUrl);
		authorizationInfo.put("state", this.authorizationState);
		authorizationInfo.put("scope",
				convertStringArray2StringWithSpaceSeparator(this.permissions));
		authorizationInfo.put("response_type", "code");
		if (this.agency != null) {
			authorizationInfo.put("agency_name", this.agency);
		}
		RequestParams params = new RequestParams(authorizationInfo);
		if (AMSetting.DebugMode) {
			AMLogger.logVerbose(
					"In AuthorizationManager.getAuthorizeCode4Public(): params = %s.",
					params.toString());
		}
		String authorizeUrl = this.authorizationServer
				+ AMSetting.PUBLIC_AUTHORIZE_URI + "?" + params.toString();
		return authorizeUrl;
	}



	

	/**
	 * 
	 * Get a key's value from the session data stored locally.
	 * 
	 * @param key
	 *            The name of a key in the session store.
	 * 
	 * @return The key's value.
	 * 
	 * @since 3.0
	 */
	String getValueFromSessionStore(String key) {
		return sessionStorePrefs.getString(key, null);
	}

	/**
	 * 
	 * Handle the intent returned from the web view opened for public
	 * authorization.
	 * 
	 * @param webIntent
	 *            The intent returned from authorization web view.
	 * 
	 * @return Void.
	 * 
	 * @since 3.0
	 */
	public void handleOpenURL(Intent webIntent) {
		// Parse the intent passed from civic user authorization web view.
		Uri uri = webIntent.getData();
		String action = uri.getHost();
		this.authorizationCode = uri.getQueryParameter("code");
		this.agency = uri.getQueryParameter("agency_name");
		this.environment = uri.getQueryParameter("environment");
		// Send request to get access token.
		if (("authorize".equalsIgnoreCase(action)) && (this.authorizationCode != null)) {
			accelaMobile.authorizationStatus = AuthorizationStatus.AUTHORIZED;
			// Dismiss the login view.
			if (this.authorizationActivity != null) { // Login through an independent web browser.
				Intent intent4Back = new Intent(this.authorizationActivity,
						this.ownerContext.getClass());
				intent4Back.putExtra("isAuthorized", true);
				this.authorizationActivity
						.dismissAMLoginDialogWrapperIfExists();
				this.authorizationActivity.startActivity(intent4Back);
				this.authorizationActivity.finish();
			} else if (this.loginDialogWrapper != null) { // Login through a
															// dialog which
															// wraps HTML login
															// web view.
				this.loginDialogWrapper.dismiss();
			}
			// Continue to send request to get access token.
			fetchAccessToken();
		}
	}
	
	public void setUser(String user){
		this.user = user;
	}
	
	public void setRedirectUrl(String url){
		this.redirectUrl = url;
	}
	
	public void setAuthorizationCode(String code){
		this.authorizationCode = code;
	}
	
	public void setAuthorizationState(String state){
		this.authorizationState = state;
	}

	/**
	 * 
	 * Save key value pairs to the session data stored locally.
	 * 
	 * @return Void.
	 * 
	 * @since 3.0
	 */
	public void saveUserProfile2LocalStore() {
		SharedPreferences.Editor prefsWriter = sessionStorePrefs.edit();
		if (this.environment != null) {
			prefsWriter.putString(ENVIRONMENT_KEY_IN_PREF_FILE,
					this.environment);
		}
		if (this.agency != null) {
			prefsWriter.putString(AGENCY_KEY_IN_PREF_FILE, this.agency);
		}
		if (this.user != null) {
			prefsWriter.putString(USER_KEY_IN_PREF_FILE, this.user);
		}
		if (this.accessToken != null) {
			prefsWriter.putString(TOKEN_KEY_IN_PREF_FILE, this.accessToken);
		}
		if (this.refreshToken != null) {
			prefsWriter.putString(REFRESH_TOKEN_KEY_IN_PREF_FILE, this.refreshToken);
		}
		prefsWriter.commit();
	}
	
	//save the username from webview login
	public void saveUserName(String user){
		this.user = user;
		SharedPreferences.Editor prefsWriter = sessionStorePrefs.edit();
		if (this.user != null) {
			prefsWriter.putString(USER_KEY_IN_PREF_FILE, this.user);
		}
		prefsWriter.commit();
	}

	/**
	 * 
	 * Get new access token by refresh token.
	 * 
	 * @return The request object which get token.
	 * 
	 * @since 4.0
	 */	
	AMRequest fetchAccessTokenByRefreshToken(AMSessionDelegate sessionDelegate) {
		if (sessionDelegate != null) {
			this.sessionDelegate = sessionDelegate;
		}
		String hostUrl = this.apisServer + AMSetting.ACCESS_TOKEN_URI;
		RequestParams urlParams = new RequestParams();
		RequestParams postParams = new RequestParams();
		postParams.put("client_id", this.clientId);
		postParams.put("client_secret", this.clientSecret);
		postParams.put("refresh_token", this.refreshToken);

		if (isNativeAuthorization) {
			postParams.put("grant_type", "password");			
		} else { // For authorization done through web view
			postParams.put("grant_type", "authorization_code");			
		}
		if (AMSetting.DebugMode) {
			AMLogger.logVerbose("In AuthorizationManager.fetchAccessTokenByRefreshToken(): postParams = %s.",postParams.toString());
		}
		//Clear the stored token
		clearAuthorizationAndToken(false);
		//Send request to get refreshed token.
		AMRequest amRequest = new AMRequest(this.accelaMobile, hostUrl,urlParams, HTTPMethod.POST);
		amRequest.setRequestType(RequestType.AUTHENTICATION);
		this.currentRequest = amRequest;
		return amRequest.sendRequest(postParams, this.tokenRequestDelegate);
	}

	/**
	 * public method, used to get access token for native authorization or web
	 * authorization.
	 */
	public AMRequest fetchAccessToken() {
//		String hostUrl = this.apisServer + AMSetting.ACCESS_TOKEN_URI;
		String hostUrl = "https://testconstructauth.cloudapp.net" + AMSetting.ACCESS_TOKEN_URI;
		RequestParams urlParams = new RequestParams();
		RequestParams postParams = new RequestParams();
		postParams.put("client_id", this.clientId);
		postParams.put("client_secret", this.clientSecret);

		if (isNativeAuthorization) {
			postParams.put("grant_type", "password");
			postParams.put("username", this.user);
			postParams.put("password", this.password);
			if (this.permissions != null) { // Optional
				postParams.put("scope", convertStringArray2StringWithSpaceSeparator(this.permissions));
			}
			postParams.put("client_id", this.clientId);
			postParams.put("client_secret", this.clientSecret);
			if (this.agency != null) { // Optional for Civic ID
				postParams.put("agency_name", this.agency);
			}
			postParams.put("environment", this.environment);
		} else { // For authorization done through web view
			postParams.put("grant_type", "authorization_code");
			postParams.put("redirect_uri", this.redirectUrl);
			postParams.put("code", this.authorizationCode);
			postParams.put("state", this.authorizationState);
		}
		if (AMSetting.DebugMode) {
			AMLogger.logVerbose(
					"In AuthorizationManager.fetchAccessTokenWithCode(): postParams = %s.",
					postParams.toString());
		}
		AMRequest amRequest = new AMRequest(this.accelaMobile, hostUrl,
				urlParams, HTTPMethod.POST);
		amRequest.setRequestType(RequestType.AUTHENTICATION);
		this.currentRequest = amRequest;
		// this.processIndicatorHolderView = (ViewGroup)((Activity)
		// this.ownerContext).findViewById(android.R.id.content).getRootView();
		return amRequest.sendRequest(postParams, this.tokenRequestDelegate);
	}
	
	/**
	 * Private method, used to convert a string array to a string separated by
	 * space char.
	 */
	private String convertStringArray2StringWithSpaceSeparator(
			String[] stringArray) {
		if ((stringArray == null) || (stringArray.length == 0)) {
			return null;
		}
		String permissionsStr = "";
		for (String permission : stringArray) {
			permissionsStr = permissionsStr + " " + permission;
		}
		permissionsStr = permissionsStr.substring(1); // Skip the 1st space char
		return permissionsStr;
	}

	/**
	 * Private variable, defined the request delegate for access token request
	 * sent by the native Login view(private) or the web intent(public) .
	 */
	private AMRequestDelegate tokenRequestDelegate = new AMRequestDelegate() {
		private boolean isLoginErrorHandled = false;

		@Override
		public void onTimeout() {
			if ((sessionDelegate != null) && (!isLoginErrorHandled)) {
				AMError exceptionError = new AMError(AMError.ERROR_CODE_Unauthorized,
						AMError.ERROR_CODE_TOKEN_EXPIRED,null, "Request times out.", null);
				sessionDelegate.amDidLoginFailure(exceptionError);
			}
		};

		@Override
		public void amRequestDidReceiveResponse(AMRequest request) {
			super.amRequestDidReceiveResponse(currentRequest);
			if (errorMessage != null) {
				AMError error = new AMError(AMError.ERROR_CODE_Unauthorized,
						AMError.ERROR_CODE_TOKEN_EXPIRED,traceId, errorMessage, null);
				if (sessionDelegate != null) {
					sessionDelegate.amDidLoginFailure(error);
					isLoginErrorHandled = true;
				}
			}
		}

		public void amRequestDidTimeout(AMRequest request) {
			super.amRequestDidReceiveResponse(currentRequest);
			// Dismiss the process waiting view
			ProgressDialog progressDialog = currentRequest
					.getRequestWaitingView();
			if ((progressDialog != null) && (progressDialog.isShowing())) {
				progressDialog.dismiss();
			}
			AMError error = new AMError(AMError.ERROR_CODE_Unauthorized,
					AMError.ERROR_CODE_TOKEN_EXPIRED,traceId, errorMessage, null);
			if (sessionDelegate != null) {
				sessionDelegate.amDidLoginFailure(error);
				isLoginErrorHandled = true;
			}
		}

		@Override
		public void onFailure(AMError error) {
			amRequestDidReceiveResponse(currentRequest);
			// Dismiss the process waiting view
			ProgressDialog progressDialog = currentRequest
					.getRequestWaitingView();
			if ((progressDialog != null) && (progressDialog.isShowing())) {
				progressDialog.dismiss();
			}
			// Invoke session delegate
			if ((sessionDelegate != null) && (!isLoginErrorHandled)) {				
				sessionDelegate.amDidLoginFailure(error);
			}
		}

		@Override
		public void onStart() {
			// Invoke request delegate
			amRequestStarted(currentRequest);
			// Reset the previous token data.
			accessToken = null;
			refreshToken = null;
			// Show progress waiting view
			// currentRequest.setOwnerView(processIndicatorHolderView,
			// stringLoader.getString("Msg_Request_AccessToken"));
		}

		@Override
		public void onSuccess(JSONObject response) {
			amRequestDidLoad(currentRequest, response);
			// Dismiss the process waiting view
			ProgressDialog progressDialog = currentRequest
					.getRequestWaitingView();
			if ((progressDialog != null) && (progressDialog.isShowing())) {
				progressDialog.dismiss();
			}
			// Get value of access token.
			if (response.has("access_token")) { // Found the token value
				// Get access token
				try {
					accessToken = response.getString("access_token");
					refreshToken = response.getString("refresh_token");
					accelaMobile.authorizationStatus = AuthorizationStatus.LOGGEDIN;
					if (AMSetting.DebugMode) {
						AMLogger.logVerbose(
								"In AuthorizationManager.tokenRequestDelegate: accessToken = %s.",
								accessToken);
					}
				} catch (JSONException e) {
					AMError error = new AMError(AMError.ERROR_CODE_Unauthorized,
							AMError.ERROR_CODE_TOKEN_EXPIRED,null, e.getMessage(),null);
					onFailure(error);
				}

				// Save user profile including access token to local storage
				if (isRememberToken) {
					saveUserProfile2LocalStore();
				}
				// Close the native authorization dialog (or authorization web
				// browser)
				if ((loginDialog != null) && (loginDialog.isShowing())) // For
																		// native
																		// authorization.
				{
					// Invoke login view delegate.
					if (loginDialog.amLoginViewDelegate != null) {
						loginDialog.amLoginViewDelegate.amDialogLogin(loginDialog);
					}
					loginDialog.dismiss();
				}
				// Invoke request delegate
				amRequestDidLoad(currentRequest, response);
				// Invoke session delegate
				if (sessionDelegate != null) {
					sessionDelegate.amDidLogin();
				}

				// Send broadcast message
				Intent broadcastIntent = new Intent(
						AMSetting.BROARDCAST_ACTION_LOGGED_IN);
				if (environment != null) {
					broadcastIntent.putExtra(ENVIRONMENT_KEY_IN_PREF_FILE,
							environment);
				}
				if (agency != null) {
					broadcastIntent.putExtra(AGENCY_KEY_IN_PREF_FILE, agency);
				}
				broadcastIntent.putExtra(USER_KEY_IN_PREF_FILE, user);
				broadcastIntent.putExtra(TOKEN_KEY_IN_PREF_FILE, accessToken);
				LocalBroadcastManager.getInstance(ownerContext).sendBroadcast(
						broadcastIntent);
			}
		}
	};
}

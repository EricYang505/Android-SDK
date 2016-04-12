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

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.accela.mobile.AMRequest.HTTPMethod;
import com.accela.mobile.AMRequest.RequestType;
import com.accela.mobile.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;


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
     * The flag which indicates whether user profile should be saved to local storage after successful login.
     *
     * @since 1.0
     */
    protected Boolean amIsRemember = true;

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
	AMSessionDelegate sessionDelegate;

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
	 * The native SDK login dialog which is invoked for private authorization.
	 * 
	 * @since 3.0
	 */
	private AMLoginView loginDialog;

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

    String mUrlSchema;
	private Map<String, String> customTokenPostParam;

	/**
	 * Constructor.
	 * 
	 * @return An initialized AuthorizationManager instance.
	 * 
	 * @since 3.0
	 */
	AuthorizationManager() {
		// Set property values.
		this.accelaMobile = AccelaMobile.getInstance();
		this.authorizationState = "inspection";
		// Initialize SharedPreferences instance.
		sessionStorePrefs = accelaMobile.ownerContext.getSharedPreferences(SESSION_STORE_PREF_FILE, 0);
		// Load access token stored locally.
		this.accessToken = sessionStorePrefs.getString(TOKEN_KEY_IN_PREF_FILE,null);
		this.refreshToken = sessionStorePrefs.getString(REFRESH_TOKEN_KEY_IN_PREF_FILE,null);
		accelaMobile.agency = sessionStorePrefs.getString(AGENCY_KEY_IN_PREF_FILE, null);
		this.user = sessionStorePrefs.getString(USER_KEY_IN_PREF_FILE, null);
		if (accelaMobile.environment==null && sessionStorePrefs.getString(ENVIRONMENT_KEY_IN_PREF_FILE, null)!=null)
			accelaMobile.environment = AccelaMobile.Environment.valueOf(sessionStorePrefs.getString(ENVIRONMENT_KEY_IN_PREF_FILE, null));
	}

	public AMRequest authenticate(String agency, String user, String password, AccelaMobile.Environment environment, String[] permissions, Map<String, String> customPostParam) {
		this.customTokenPostParam = customPostParam;
		return authenticate(agency, user, password, environment, permissions);
	}

    public AMRequest authenticate(String agency, String user, String password, AccelaMobile.Environment environment, String[] permissions) {
        accelaMobile.agency = agency;
        this.setClientInfo(agency, accelaMobile.amAuthHost, accelaMobile.amApisHost);
        this.setIsRememberToken(amIsRemember);
        this.setSessionDelegate(this.sessionDelegate);
		accelaMobile.environment = environment;
        return getAuthorizeCode4Private(this.loginDialog, agency, user, password, permissions, false);
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
		accelaMobile.reset();
        clearAuthorizationAndToken(true);

        // Call session delegate.
        if (this.sessionDelegate != null) {
            this.sessionDelegate.amDidLogout();
        }
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
        AMRequest tokenRequest = fetchAccessTokenByRefreshToken(sessionDelegate);
        return tokenRequest;
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
		// Clear the values in local session store.
		if (clearSessionStore) {
			SharedPreferences.Editor prefsWriter = sessionStorePrefs.edit();
			prefsWriter.remove(TOKEN_KEY_IN_PREF_FILE);
			prefsWriter.remove(REFRESH_TOKEN_KEY_IN_PREF_FILE);
			prefsWriter.commit();
		}
	}

    /**
     * public method, used to show login web view in an independent web browser or a native dialog.
     */
    public void showAuthorizationWebView(String[] permissions, String urlSchema, String agency, boolean isWrappedWebView) {
        // Return directly if the authorization manager has access token (loaded from local store)
        if ((getAccessToken() != null) && (sessionDelegate != null))
        {
            sessionDelegate.amDidLogin();
            return;
        }
        // Return directly if internet permission is not granted in AndroidManifest.xml file.
        else if (accelaMobile.ownerContext.checkCallingOrSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
			if (stringLoader!=null) {
				new AlertDialog.Builder(accelaMobile.ownerContext)
						.setTitle(null)
						.setMessage(stringLoader.getString("Error_Require_Internet_Permission"))
						.setNegativeButton(stringLoader.getString("Button_OK"), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								// Do nothing except closing the alert dialog.
							}
						}).create().show();
			}
            return;
        }

        // Otherwise, show the login dialog which embeds the HTML login view.
        this.mUrlSchema = urlSchema;
        this.setClientInfo(agency, accelaMobile.amAuthHost, accelaMobile.amApisHost);
        this.setIsRememberToken(amIsRemember);
        this.setSessionDelegate(this.sessionDelegate);

        String redirectUrl = urlSchema + "://authorize";
        if (agency != null) {
            redirectUrl += "&agency=" + agency;
        }
        String authorizationURL = this.getAuthorizeUrl4WebLogin(redirectUrl, permissions);
        AMLoginDialogWrapper amLoginDialogWrapper = new AMLoginDialogWrapper(authorizationURL);
        getAuthorizeCode4Public(amLoginDialogWrapper, redirectUrl, permissions);

    }
//	/**
//	 * Force login with Civic ID
//	 * @param permissions
//	 *
//	 * @since 4.1
//	 */
//	public void authorizeCivicId(String[] permissions, AMLoginViewDelegate loginViewDelegate) {
//		this.loginDialog = (this.loginDialog == null) ?
//				new CivicLoginDialog(this.accelaMobile, permissions) :
//					this.loginDialog;
//
//		this.loginDialog.amLoginViewDelegate = loginViewDelegate;
//		// Show the login view
//		View parentView = ((Activity) this.ownerContext).findViewById(android.R.id.content).getRootView();
//		this.loginDialog.showAtLocation(parentView, Gravity.CENTER, 0, 0);
//		this.loginDialog.setFocusable(true);
//	}
//
//	/**
//	 * Force login with agent authentication
//	 * @param permissions
//	 *
//	 * @since 4.1
//	 */
//	public void authorizeAgent(String[] permissions, AMLoginViewDelegate loginViewDelegate) {
//		this.loginDialog = (this.loginDialog == null) ?
//							new AgencyLoginDialog(this.accelaMobile, permissions) :
//								this.loginDialog;
//		this.loginDialog.amLoginViewDelegate = loginViewDelegate;
//
//		// Show the login view
//		View parentView = ((Activity) this.ownerContext).findViewById(android.R.id.content).getRootView();
//		this.loginDialog.showAtLocation(parentView, Gravity.CENTER, 0, 0);
//		this.loginDialog.setFocusable(true);
//	}
	


	/**
	 * 
	 * Get the value of property agency.
	 * 
	 * @return The value of property agency.
	 * 
	 * @since 3.0
	 */
	public String getAgency() {
		return accelaMobile.agency;
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
	 * @param agency The agency value to be assigned.
	 * @param authServer The URL of authorization server.
	 * @param apisServer The URL of api server.
	 * 
	 * @return Void.
	 * 
	 * @since 3.0
	 */
	public void setClientInfo(String agency, String authServer, String apisServer) {
		accelaMobile.agency = agency;
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
	 * @param is4Civic Flag which indicates whether the authorization is for civic
	 *       ‰     user or not(agency user).
	 * 
	 * @return The AMRequest object corresponding to this API call.
	 * 
	 * @since 3.0
	 */
	private AMRequest getAuthorizeCode4Private(AMLoginView loginDialog, String agency,
			String user, String password, String[] permissions, Boolean is4Civic) {
		this.loginDialog = loginDialog;
		accelaMobile.agency = agency;
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
        if (authorizationView.getClass() == AMLoginDialogWrapper.class) {
			this.loginDialogWrapper = (AMLoginDialogWrapper) authorizationView;
			this.loginDialogWrapper.show();
		}else{
            AMLogger.logWarn("authorizationView not valid, need to be AMLoginDialogWrapper.class!");
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
		authorizationInfo.put("environment", accelaMobile.environment.name());
		authorizationInfo.put("client_id", this.accelaMobile.appId);
		authorizationInfo.put("redirect_uri", this.redirectUrl);
		authorizationInfo.put("state", this.authorizationState);
		authorizationInfo.put("scope",
				convertStringArray2StringWithSpaceSeparator(this.permissions));
		authorizationInfo.put("response_type", "code");
		if (accelaMobile.agency != null) {
			authorizationInfo.put("agency_name", accelaMobile.agency);
		}
		RequestParams params = new RequestParams();
        params.setUrlParams(authorizationInfo);
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
		accelaMobile.agency = uri.getQueryParameter("agency_name");
		accelaMobile.environment = AccelaMobile.Environment.valueOf(uri.getQueryParameter("environment"));
		// Send request to get access token.
		if (("authorize".equalsIgnoreCase(action)) && (this.authorizationCode != null)) {
			if (this.loginDialogWrapper != null) {
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
		if (accelaMobile.environment != null) {
			prefsWriter.putString(ENVIRONMENT_KEY_IN_PREF_FILE,
					accelaMobile.environment.name());
		}
		if (accelaMobile.agency != null) {
			prefsWriter.putString(AGENCY_KEY_IN_PREF_FILE, accelaMobile.agency);
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
	private AMRequest fetchAccessTokenByRefreshToken(AMSessionDelegate sessionDelegate) {
		if (sessionDelegate != null) {
			this.sessionDelegate = sessionDelegate;
		}
		String hostUrl = this.apisServer + AMSetting.ACCESS_TOKEN_URI;
		RequestParams postParams = new RequestParams();
		Map<String, String> authBody = new HashMap<String, String>();
		authBody.put("client_id", this.accelaMobile.appId);
		authBody.put("client_secret", this.accelaMobile.appSecret);
		authBody.put("refresh_token", this.refreshToken);

		if (isNativeAuthorization) {
			authBody.put("grant_type", "refresh_token");
		} else { // For authorization done through web view
			authBody.put("grant_type", "refresh_token");
		}
		if (AMSetting.DebugMode) {
			AMLogger.logVerbose("In AuthorizationManager.fetchAccessTokenByRefreshToken(): postParams = %s.", authBody.toString());
		}
		//Clear the stored token
		clearAuthorizationAndToken(false);
        postParams.setAuthBody(authBody);
		//Send request to get refreshed token.
		AMRequest amRequest = new AMRequest(hostUrl,null, postParams, HTTPMethod.POST);
		amRequest.setRequestType(RequestType.AUTHENTICATION);
		this.currentRequest = amRequest;
        try {
            return amRequest.sendRequest(this.tokenRequestDelegate);
        } catch (JSONException e) {
            AMLogger.logError(e.toString());
        }
        return amRequest;
    }

	/**
	 * public method, used to get access token for native authorization or web
	 * authorization.
	 */
	public AMRequest fetchAccessToken() {
		String hostUrl = this.apisServer + AMSetting.ACCESS_TOKEN_URI;
		RequestParams requestPostParams = new RequestParams();
		if (customTokenPostParam==null)
			customTokenPostParam = new HashMap<String, String>();
		customTokenPostParam.put("client_id", this.accelaMobile.appId);
		customTokenPostParam.put("client_secret", this.accelaMobile.appSecret);

		if (isNativeAuthorization) {
			customTokenPostParam.put("grant_type", "password");
			customTokenPostParam.put("username", this.user);
			customTokenPostParam.put("password", this.password);



            if (this.permissions != null) { // Optional
				customTokenPostParam.put("scope", convertStringArray2StringWithSpaceSeparator(this.permissions));
			}
			customTokenPostParam.put("client_id", this.accelaMobile.appId);
			customTokenPostParam.put("client_secret", this.accelaMobile.appSecret);
			if (accelaMobile.agency != null) { // Optional for Civic ID
				customTokenPostParam.put("agency_name", accelaMobile.agency);
			}
			customTokenPostParam.put("environment", accelaMobile.environment.name());
		} else { // For authorization done through web view
			customTokenPostParam.put("grant_type", "authorization_code");
			customTokenPostParam.put("redirect_uri", this.redirectUrl);
			customTokenPostParam.put("code", this.authorizationCode);
			customTokenPostParam.put("state", this.authorizationState);
		}
		if (AMSetting.DebugMode) {
			AMLogger.logVerbose(
					"In AuthorizationManager.fetchAccessTokenWithCode(): postParams = %s.",
					customTokenPostParam.toString());
		}
		// this.processIndicatorHolderView = (ViewGroup)((Activity)
		// this.ownerContext).findViewById(android.R.id.content).getRootView();
        requestPostParams.setAuthBody(customTokenPostParam);
        AMRequest amRequest = new AMRequest(hostUrl, null, requestPostParams, HTTPMethod.POST);
        amRequest.setRequestType(RequestType.AUTHENTICATION);
        this.currentRequest = amRequest;
        try {
            return amRequest.sendRequest(this.tokenRequestDelegate);
        } catch (JSONException e) {
            AMLogger.logError(e.toString());
        }
        return amRequest;
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
				if (accelaMobile.environment != null) {
					broadcastIntent.putExtra(ENVIRONMENT_KEY_IN_PREF_FILE,
							accelaMobile.environment);
				}
				if (accelaMobile.agency != null) {
					broadcastIntent.putExtra(AGENCY_KEY_IN_PREF_FILE, accelaMobile.agency);
				}
				broadcastIntent.putExtra(USER_KEY_IN_PREF_FILE, user);
				broadcastIntent.putExtra(TOKEN_KEY_IN_PREF_FILE, accessToken);
//				LocalBroadcastManager.getInstance(ownerContext).sendBroadcast(
//						broadcastIntent);
			}
		}
	};
}

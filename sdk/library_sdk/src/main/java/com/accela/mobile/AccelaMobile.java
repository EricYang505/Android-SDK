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

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.text.TextUtils;
import java.util.ResourceBundle;


/**
 *  AccelaMobile is the main object of the Accela SDK for Android.
 *
 * 	@since 1.0
 */

public class AccelaMobile {

    private AMRequestSender requestSender;

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
	 * The URL of authorization host. For example: https://auth.accela.com
	 *
	 * @since 3.0
	 */
	protected String amAuthHost = AMSetting.AM_OAUTH_HOST;

	/**
	 * The URL of API host. For example: https://apis.accela.com
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

	/**
	 * Used to support multiple agency
	 *
	 * @since 4.0
	 */
	public static final String IS_ALL_AGENCIES = "is_all_agencies";
	public static final String AGENCY_NAME = "agency_name";
	public static final String ENVIRONMENT_NAME = "environment_name";

	/**
	 *
	 * Get a default static instance of the current class.
	 *
	 * @return An initialized AccelaMobile instance.
	 *
	 * @since 3.0
	 */
	public synchronized static AccelaMobile getInstance() {
    	if (instance == null)  {
    		instance = new AccelaMobile();
    	}
    	return instance;
    }


	/**
	 * Private constructor, called by the defaultInstance() static method.
	 */
	private AccelaMobile() {}


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
	public void initialize(Context ownerContext, String appId, String appSecret) {
		// Initialize instance properties.
        initialize(ownerContext,appId, appSecret, null);
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
	public void initialize(Context ownerContext, String appId, String appSecret, AMSessionDelegate sessionDelegate) {
        this.initialize(ownerContext, appId, appSecret, sessionDelegate, null, null);
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
	public void initialize(Context ownerContext, String appId, String appSecret, AMSessionDelegate sessionDelegate, String authHost, String apisHost) {
		this.amAuthHost = (authHost !=null) ? authHost : AMSetting.AM_OAUTH_HOST;
		this.amApisHost = (apisHost !=null) ? apisHost : AMSetting.AM_API_HOST;
        // Initialize instance properties.
        this.ownerContext = ownerContext;
        this.appId = appId;
        this.appSecret = appSecret;

        this.authorizationManager = new AuthorizationManager();
        this.authorizationManager.setSessionDelegate(sessionDelegate==null ? defaultSessionDelegate : sessionDelegate);

        this.requestSender = new AMRequestSender();
	}

	public AuthorizationManager getAuthorizationManager() {
		return this.authorizationManager;
	}

    public AMRequestSender getRequestSender(){
        return this.requestSender;
    }

	/**
	 *
	 * Set the URL of cloud server for user authorization and service API
	 *
	 * @param authHost The URL of cloud server for user authorization.
	 * @param apisHost The URL of cloud server for service API calling.
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
	 * Get the value of property urlSchema.
	 *
	 * @return The value of property urlSchema.
	 *
	 * @since 2.1
	 */
	public String getUrlSchema() {
		return this.urlSchema;
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
	 *
	 * Set the value of property environment.
	 *
	 * @param environment The new value to be assigned.
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

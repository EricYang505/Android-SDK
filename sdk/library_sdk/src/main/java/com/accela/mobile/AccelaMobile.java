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
	public String amAuthHost = AMSetting.AM_OAUTH_HOST;

	/**
	 * The URL of API host. For example: https://apis.accela.com
	 *
	 * @since 3.0
	 */
	public String amApisHost = AMSetting.AM_API_HOST;


	/**
	 * The AuthorizationManager instance which manages session state.
	 *
	 * @since 3.0
	 */
	protected AuthorizationManager authorizationManager;

    private AMRequestSender requestSender;

	/**
	 * The Android context (usually an Activity) which creates the current AccelaMobile instance.
	 *
	 * @since 1.0
	 */
	Context ownerContext;

	/**
	 * The string loader which loads localized text strings.
	 *
	 * @since 3.0
	 */
	protected  ResourceBundle stringLoader = AMSetting.getStringResourceBundle();

	/**
	 * The ID string of the application registered on developer portal.
	 *
	 * @since 1.0
	 */
	public String appId;

	/**
	 * The security string of the application registered on developer portal.
	 *
	 * @since 3.0
	 */
	public String appSecret ;


	/**
	 * The agency to which user logs in.
	 *
	 * @since 1.0
	 */
	private String agency ;

	/**
	 * The environment to which user logs in.
	 *
	 * @since 3.0
	 */
	public Environment environment = Environment.PROD ;	// Default value

//	/**
//	 * Used to support multiple agency
//	 *
//	 * @since 4.0
//	 */
//	public static final String IS_ALL_AGENCIES = "is_all_agencies";
//	public static final String AGENCY_NAME = "agency_name";
//	public static final String ENVIRONMENT_NAME = "environment_name";

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
	public void initialize(Context ownerContext, String appId, String appSecret, Environment environment) {
		// Initialize instance properties.
        initialize(ownerContext,appId, appSecret, environment, null);
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
	public void initialize(Context ownerContext, String appId, String appSecret, Environment environment, AMSessionDelegate sessionDelegate) {
        this.initialize(ownerContext, appId, appSecret, environment, sessionDelegate, null, null);
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
	public void initialize(Context ownerContext, String appId, String appSecret, Environment environment, AMSessionDelegate sessionDelegate, String authHost, String apisHost) {
		this.amAuthHost = (authHost !=null) ? authHost : AMSetting.AM_OAUTH_HOST;
		this.amApisHost = (apisHost !=null) ? apisHost : AMSetting.AM_API_HOST;
        // Initialize instance properties.
        this.ownerContext = ownerContext;
        this.appId = appId;
        this.appSecret = appSecret;
        this.environment = environment;
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

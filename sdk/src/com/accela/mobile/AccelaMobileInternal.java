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


import java.util.ResourceBundle;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;


/**
 *  The internal AccelaMobile object, used by native authorization dialogs.
 *  
 * 	@since 3.0
 */
public class AccelaMobileInternal extends AccelaMobile {	
	/**
	 * The login dialog.
	 * 
	 * @since 3.0
	 */
	private AMLoginView loginDialog;
	
	/**
	 * The string loader which load localized text.
	 * 
	 * @since 3.0
	 */
	private ResourceBundle stringLoader = AMSetting.getStringResourceBundle();
	
	/**
	 * Private variable, used as the default login view delegate if it is not specified.
	 */	 
	private AMLoginViewDelegate defaultLoginViewDelegate = new AMLoginViewDelegate() {
		public void amDialogFetch(AMLoginView loginView) {
			AMLogger.logInfo(stringLoader.getString("Log_AMLoginViewDelegate_amDialogFetch"), loginView.getClass().getName());
		}
		public void amDialogLogin(AMLoginView loginView) {
			AMLogger.logInfo(stringLoader.getString("Log_AMLoginViewDelegate_amDialogLogin"), loginView.getClass().getName());
		}
		public void amDialogNotLogin(boolean cancelled) {
			AMLogger.logInfo(stringLoader.getString("Log_AMLoginViewDelegate_amDialogNotLogin"), Boolean.toString(cancelled));
		}
		public void amDialogLoginFailure(AMError error) {
			AMLogger.logInfo(stringLoader.getString("Log_AMLoginViewDelegate_amDialogLoginFailure"), error.toString());
		}			
	};
	
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
	 * @return An initialized AccelaMobileInternal instance.
	 * 
	 * @since 3.0
	 */				
	public AccelaMobileInternal(Context ownerContext, String appId, String appSecret, AMSessionDelegate sessionDelegate, String authHost, String apisHost) {
		super(ownerContext, appId, appSecret, sessionDelegate,authHost,apisHost);
	}
	
	/**
	 * Authenticates an agency user through native agency login popup window.
	 * 
	 * @param agency The agency name of the Accela Automation.
	 * @param user The name of the agency user to be validated.
	 * @param password The password for the specified agency user.
	 * @param permissions The array of access permissions. For example, search_records get_single_record  create_record, and etc.
	 * @param requestDelagate The request's delegate or null if it doesn't have a delegate.
	 *           	 See {@link AMRequestDelegate} for more information.
	 * 
	 * @return The AMRequest object corresponding to this API call.
	 * 
	 * @since 3.0
	 */
	public AMRequest authenticate(String agency, String user, String password, String[] permissions) {			
		this.agency = agency;
		this.authorizationManager.setClientInfo(this.getAppId(), this.getAppSecret(), this.getEnvironment().name(), agency, this.amAuthHost, this.amApisHost);
		this.authorizationManager.setIsRememberToken(this.amIsRemember);
		this.authorizationManager.setSessionDelegate(this.sessionDelegate);		
		return authorizationManager.getAuthorizeCode4Private(this.loginDialog, agency, user, password, permissions, false);
	}
	
	/**
	 * Authorize agency app with the given permissions.
	 * 
	 * @param permissions The array of access permissions. For example, search_records get_single_record  create_record, and etc.
	 *
	 * @return Void.
	 * 
	 * @since 3.0
	 */	
	public void authorizeAgencyApp(String[] permissions) {
		// Initialize authorization manager if it is null
		this.authorizationManager = (this.authorizationManager !=null) ? this.authorizationManager : new AuthorizationManager(this);
		// Return directly if the authorization manager has access token (loaded from local store)
		if ((authorizationManager.getAccessToken() != null) && (sessionDelegate != null))
		{
			sessionDelegate.amDidLogin();
			return;
		}
		// Otherwise, create the native Agency Login view.
		this.authorizationManager.setClientInfo(null, null, null, null, this.amAuthHost, this.amApisHost);
		this.authorizationManager.setIsRememberToken(this.amIsRemember);
		this.loginDialog = (this.loginDialog != null) ? this.loginDialog : new AgencyLoginDialog(this, permissions, sessionDelegate);
		this.loginDialog.amLoginViewDelegate = defaultLoginViewDelegate;
					
		// Show the login view
		View parentView = ((Activity) this.ownerContext).findViewById(android.R.id.content).getRootView(); 
		this.loginDialog.showAtLocation(parentView, Gravity.CENTER, 0, 0);
		this.loginDialog.setFocusable(true);
	}
	
	/**
	 * Authorize agency app with the given permissions and login dialog delegate.
	 * 
	 * @param permissions The array of access permissions. For example, search_records get_single_record  create_record, and etc.
	 * @param loginViewDelegate The delegate which manager login dialog's lifecycle.
	 *
	 * @return Void.
	 * 
	 * @since 4.0
	 */	
	public void authorizeAgencyApp(String[] permissions, AMLoginViewDelegate loginViewDelegate) {
		this.authorizeAgencyApp(permissions);
		if (this.loginDialog != null) {
			this.loginDialog.amLoginViewDelegate = loginViewDelegate;
		}
	}
}

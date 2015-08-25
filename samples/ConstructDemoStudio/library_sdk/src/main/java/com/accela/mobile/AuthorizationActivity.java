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

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;

/**
 *  The activity which presents the authorization web view, handle the data returned from the authorization web view.
 *  
 *  Notes:
 *  This activity must be declared in your Android project's AndroidManifest.xml file because it is required in user authorization.
 *  And the value of android:scheme should be assigned to the urlSchema property when AccelaMobile class is initialized.
 *  Here is the complete content of the activity definition which should be added in AndroidManifest.xml file.
 *  	<activity android:name="com.accela.mobile.AuthorizationActivity" android:windowSoftInputMode="stateHidden" > 
 *           <intent-filter>
 *               <action android:name="android.intent.action.VIEW"></action>
 *				<category android:name="android.intent.category.DEFAULT"></category>
 *				<category android:name="android.intent.category.BROWSABLE"></category>
 *				<data android:scheme="amtest" android:host="authorize"></data>				
 *           </intent-filter>            
 *		</activity>
 * 
 * 	@since 3.0
 */
public class AuthorizationActivity extends Activity {	
	
	/**
	 * The static AccelaMobile instance binded to the current activity.
	 * 
	 * @since 3.0
	 */
	public static AccelaMobile accelaMobile;
	

	/**
	 * The array of access permissions to be assigned for the authorization.
	 * For example, search_records get_single_record  create_record, and etc..
	 * 
	 * @since 3.0
	 */
	private String[] permissions;
	
	/**
	 * The agency name.
	 * 
	 * @since 3.0
	 */
	private String agency;
	
	/**
	 * The dialog which wraps HTML login view.
	 * 
	 * @since 3.0
	 */
	private AMLoginDialogWrapper amLoginDialogWrapper;
	
	
	@Override
	 public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);			
		 // Set the content view to transparent.
		 ViewGroup mainLayout = (ViewGroup)this.findViewById(android.R.id.content).getRootView(); 
		 mainLayout.setBackgroundColor(Color.TRANSPARENT);				 	 		 
	}	

	@Override
	@SuppressWarnings("unchecked")
	protected void onStart() {
		super.onStart();		
		Intent currentIntent = getIntent();		 
		 // It means the current activity is created from AccelaMobile.authorize() method.
		  if ( (accelaMobile != null) && (currentIntent.getExtras() != null) 
		  && (currentIntent.getExtras().get("actionBundle") != null) && (!currentIntent.getExtras().getBoolean("isWrappedWebView"))) {	
			  	HashMap<String, Object> actionBundle = (HashMap<String, Object>) currentIntent.getExtras().get("actionBundle");	
			  	permissions = (String[])actionBundle.get("permissions");
			  	agency = (String)actionBundle.get("agency");			  	
			 	String redirectUrl = accelaMobile.getUrlSchema() + "://authorize";		
			 	if (agency != null) {
			 		redirectUrl += "&agency=" + agency;
			 	}
			 	accelaMobile.authorizationManager.getAuthorizeCode4Public(this, redirectUrl, permissions);				 	
		  }  		  
		  // It means the current activity is activated by the predefined schema intent coming from independent web browser(working with AccelaMobile.authorize() method).
		  else if ((accelaMobile != null) && (currentIntent.getData() != null))
		  {			 
			Uri webIntentUri = currentIntent.getData();					
			String action = currentIntent.getAction();	
			String schema = webIntentUri.getScheme();
			if ((Intent.ACTION_VIEW.equals(action)) && (accelaMobile != null) 
					&& (schema.equalsIgnoreCase(accelaMobile.getUrlSchema())) && (accelaMobile.authorizationManager != null))  { 
				// Send request to get access token.
				accelaMobile.authorizationManager.handleOpenURL(currentIntent);	
			}			 
		  }	
		// Finish the activity.		
		this.finish();
	}	
	
	protected void dismissAMLoginDialogWrapperIfExists() {
		if (amLoginDialogWrapper != null) {
			amLoginDialogWrapper.dismiss();
		}
	}
}

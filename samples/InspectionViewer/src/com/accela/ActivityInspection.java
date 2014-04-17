package com.accela;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.accela.mobile.*;
import com.accela.mobile.AccelaMobile.AuthorizationStatus;
import com.accela.mobile.http.RequestParams;

public class ActivityInspection extends ListActivity implements OnClickListener {
	private static String SERVICE_URI_INSPECTION_LIST = "/v4/records/{recordId}/inspections/";
	private AppContext appContext;
	private Button btnSignIn, btnSignOut, btnShow;
	private RelativeLayout mainLayout;
	private AMRequest currentRequest;
	private ProgressDialog accessTokenProgressDialog;
	private List<HashMap<String, String>> inspectionItems;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		this.mainLayout = (RelativeLayout) this.findViewById(R.id.mainLayout);		
		// Register button events
		this.btnSignIn = (Button) this.findViewById(R.id.btnLogin);
		this.btnSignOut = (Button) this.findViewById(R.id.btnSignOut);
		this.btnShow = (Button) this.findViewById(R.id.btnShow);
		this.btnSignIn.setOnClickListener(this);
		this.btnSignOut.setOnClickListener(this);
		this.btnShow.setOnClickListener(this);		
		// Initialize app context
        appContext = (AppContext)this.getApplicationContext(); 
        // Set button status.
        enableBusinessButtons(false);
	}		 
    
	// Show progress dialog if resume the current activity from authorization web view..
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {		
		super.onWindowFocusChanged(hasFocus);					
		if ( (hasFocus) && (appContext.accelaMobile != null)
		  && (appContext.accelaMobile.getAuthorizationStatus() == AuthorizationStatus.AUTHORIZED)) {
			accessTokenProgressDialog = ProgressDialog.show(this, null, this.getResources().getString(R.string.msg_login), true, true);
		} 
	}
	
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.btnLogin:
				createAccelaMobileInstance();
				String[] permissions4Authorization  = new String[] {"get_record_inspections"};
				//String[] permissions4Authorization  = new String[] {"search_records", "get_records", "get_record", "get_record_inspections", "get_inspections", "get_inspection", "get_record_documents", "get_document", "create_record","create_record_document", "get_gis_settings" };
				appContext.accelaMobile.authorize(permissions4Authorization);
				break;
				
			case R.id.btnSignOut:				
				// Log out
				appContext.accelaMobile.logout();
				// Clear inspection list view
				this.setListAdapter(null);							
				break;
				
			case R.id.btnShow:				
				String servicePath = SERVICE_URI_INSPECTION_LIST.replace("{recordId}", "14CAP-00000-0003E");	
				RequestParams requestParams = new RequestParams();
				requestParams.put("limit", "10");		
				requestParams.put("offset", "0");		
				currentRequest = appContext.accelaMobile.request(servicePath, requestParams, requestDelegate);			
			}
	}
	
	// Create and show an alert dialog
	private void createAlertDialog(String title, String message) {
		new AlertDialog.Builder(this)
				.setTitle(title)
				.setMessage(message)
				.setNegativeButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				}).create().show();
	}
	
	// Set button status based on session status.
	private void enableBusinessButtons(Boolean enabled) {
		this.btnSignIn.setEnabled(!enabled);
		this.btnSignOut.setEnabled(enabled);
		this.btnShow.setEnabled(enabled);		
	}
	
	// Create the AccelaMobile instance in app context.
	private void createAccelaMobileInstance() {			
		//Override the URLs of default authorization server and api server defined in Accela SDK package.
      	String authServer = "https://apps-auth.dev.accela.com";
      	String apiServer = "https://apps-apis.dev.accela.com";
		// Create an AccelaMobile instance with the App ID and App Secret of the registered app.
	   appContext.accelaMobile = new AccelaMobile(this, "com.accela.inspector", "2012122222212102", sessionDelegate,authServer,apiServer);         
	   // Set the environment.
	   appContext.accelaMobile.setEnvironment(AccelaMobile.Environment.PROD);
	   // Set the URL schema.
	   // NOTE: The assigned value should be same as the value of "android:scheme" under "AuthorizationActivity" in the project's AndroidManifest.xml.
	   appContext.accelaMobile.setUrlSchema("aminspectionviewer");  		
	}
	
	/**
	 * Private variable, defines the request delegate used by inspection searching.
	 */
	private AMRequestDelegate requestDelegate = new AMRequestDelegate() {
		@Override
		public void onStart() {
			amRequestStarted(currentRequest);
			// Show progress waiting view			
			currentRequest.setOwnerView(ActivityInspection.this.mainLayout, ActivityInspection.this.getResources().getString(R.string.msg_search_inspections));
		}

		@Override
		public void onSuccess(JSONObject responseJson) {
			amRequestDidReceiveResponse(currentRequest);
			// Dismiss the process waiting view
			ProgressDialog progressDialog = currentRequest.getRequestWaitingView();
			if ((progressDialog != null) && (progressDialog.isShowing())) {
				progressDialog.dismiss();
			}
			// Parse json data
			JSONArray inspectionsArray = null;
			try {
				inspectionsArray = responseJson.getJSONArray("result");
			} catch (JSONException e) {
				AMLogger.logError("Error in parsing inspections json array: %s", e.getMessage());
			}
			if ((inspectionsArray != null) && (inspectionsArray.length() > 0)) {
				ActivityInspection.this.inspectionItems = new ArrayList<HashMap<String, String>>();
				for (int i = 0; i < inspectionsArray.length(); i++) {					
					String inspectionType = "Unknown Type";
					String inspectionId = "";
					String inspectionStatus = "Unknown Status";
					String scheduleDate = "Unknown Date";
					try {
						JSONObject inspectionJson = inspectionsArray.getJSONObject(i);
						inspectionType = inspectionJson.getJSONObject("type").getString("text");
						inspectionId = inspectionJson.getString("id");
						inspectionStatus = inspectionJson.getJSONObject("status").getString("text");
						scheduleDate = inspectionJson.getString("scheduleDate");
					} catch (JSONException e) {
						AMLogger.logError("Error in parsing single inspection [%s]: %s", inspectionType, e.getMessage());
					}
					HashMap<String, String> inspectionMap = new HashMap<String, String>();
					inspectionMap.put("type", inspectionType + " - " + inspectionId);
					inspectionMap.put("status", inspectionStatus);
					inspectionMap.put("scheduleDate", scheduleDate);
					ActivityInspection.this.inspectionItems.add(inspectionMap);
				}
				// Refresh the inspection list view with data.
				SimpleAdapter inspectionListAdapter = new SimpleAdapter(ActivityInspection.this, inspectionItems, R.layout.inspection_list_item, new String[] { "type", "status",
				"scheduleDate" }, new int[] { R.id.lbInspType, R.id.lbInspStatus, R.id.lbInspScheduledDate });
				ActivityInspection.this.setListAdapter(inspectionListAdapter);
				// Invoke request delegate
				amRequestDidLoad(currentRequest, responseJson);
			} else {
				createAlertDialog(null, ActivityInspection.this.getResources().getString(R.string.error_no_inspection_found));
			}
		}

		@Override
		public void onFailure(Throwable error) {
			amRequestDidReceiveResponse(currentRequest);
			// Dismiss the process waiting view
			ProgressDialog progressDialog = currentRequest.getRequestWaitingView();
			if ((progressDialog != null) && (progressDialog.isShowing())) {
				progressDialog.dismiss();
			}
			// Show dialog with the returned error
			createAlertDialog(ActivityInspection.this.getResources().getString(R.string.error_request_title), ActivityInspection.this.getResources().getString(R.string.error_request_message) + ": \n" + error.getLocalizedMessage());
		}
	};
	
	// Session delegate for Accela Mobile
	private AMSessionDelegate sessionDelegate = new AMSessionDelegate() {
		public void amDidLogin() {
			// Dismiss progress dialog.
			if ((accessTokenProgressDialog != null) && (accessTokenProgressDialog.isShowing())) {
				accessTokenProgressDialog.dismiss();	
			}
			// Refresh button status based on session.
		 	boolean sessionValid = (appContext.accelaMobile != null) && (appContext.accelaMobile.isSessionValid());
		 	enableBusinessButtons(sessionValid);			
			// Show message.
			Toast.makeText(ActivityInspection.this, ActivityInspection.this.getResources().getString(R.string.msg_logged_in), Toast.LENGTH_SHORT).show();
			AMLogger.logInfo("In ActivityInspection: Session Delegate.amDidLogin() invoked...");
		}		
		
		public void amDidLoginFailure(AMError error) {	
			Toast.makeText(ActivityInspection.this, ActivityInspection.this.getResources().getString(R.string.error_request_message), Toast.LENGTH_SHORT).show();
			AMLogger.logInfo("In ActivityInspection: Session Delegate.amDidLoginFailure() invoked: %s", error.toString());
		}		
	
		public void amDidCancelLogin() {
			AMLogger.logInfo("In ActivityInspection: Session Delegate.amDidCancelLogin() invoked...");
		}
		
		public void amDidSessionInvalid(AMError error) {
			AMLogger.logInfo("In ActivityInspection: Session Delegate.amDidSessionInvalid() invoked: %s", error.toString());			
		}		
	
		public void amDidLogout() {	
			// Refresh button status.
			ActivityInspection.this.enableBusinessButtons(false);	
			// Show message.	
			AMLogger.logInfo("In ActivityInspection: Session Delegate.amDidLogout() invoked...");
			Toast.makeText(ActivityInspection.this, ActivityInspection.this.getResources().getString(R.string.msg_logged_out), Toast.LENGTH_SHORT).show();			
		}
	};
}
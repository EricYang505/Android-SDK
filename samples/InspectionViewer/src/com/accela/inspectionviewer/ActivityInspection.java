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
package com.accela.inspectionviewer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

import com.accela.R;
import com.accela.mobile.AMError;
import com.accela.mobile.AMLogger;
import com.accela.mobile.AMRequest;
import com.accela.mobile.AMRequestDelegate;
import com.accela.mobile.AMSessionDelegate;
import com.accela.mobile.AMSetting;
import com.accela.mobile.AccelaMobile;
import com.accela.mobile.AccelaMobile.AuthorizationStatus;
import com.accela.mobile.http.RequestParams;

public class ActivityInspection extends ListActivity implements OnClickListener {
	private static String SERVICE_URI_INSPECTION_LIST = "/v4/inspections";
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
				String[] permissions4Authorization  = new String[] {"search_records", "get_records", "get_record", "get_record_inspections", "get_inspections", "get_inspection", "get_record_documents", "get_document", "create_record","create_record_document", "get_gis_settings" };
				appContext.accelaMobile.authorize(permissions4Authorization);
				break;

			case R.id.btnSignOut:
				// Log out
				appContext.accelaMobile.logout();
				// Clear inspection list view
				this.setListAdapter(null);
				break;

			case R.id.btnShow:
				String servicePath = SERVICE_URI_INSPECTION_LIST;
				RequestParams requestParams = new RequestParams();

				Date dateToday = new Date();
				Date scheduledDateTo = dateToday;

				Calendar now = Calendar.getInstance();
				now.setTime(dateToday);
				now.set(Calendar.DATE, now.get(Calendar.DATE) - 600);
				Date scheduledDateFrom = now.getTime();

				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				//search inspections of 600 days before till now
				requestParams.put("scheduledDateFrom", dateFormat.format(scheduledDateFrom));
				requestParams.put("scheduledDateTo", dateFormat.format(scheduledDateTo));
				//requestParams.put("inspectorIds", inspectorId);
				requestParams.put("limit", "10");
				requestParams.put("offset", "0");
				//asynchronous request, put requestDelegate for handling results
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
      	String authServer = AMSetting.AM_OAUTH_HOST;
      	String apiServer = AMSetting.AM_API_HOST;
		// Create an AccelaMobile instance with the App ID and App Secret of the registered app.
	   appContext.accelaMobile = new AccelaMobile(this, "635439815877444193", "133bb8d3991a4d8483cf55e3ccf25070", sessionDelegate,authServer,apiServer);
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
		public void onFailure(AMError error) {
			amRequestDidReceiveResponse(currentRequest);
			// Dismiss the process waiting view
			ProgressDialog progressDialog = currentRequest.getRequestWaitingView();
			if ((progressDialog != null) && (progressDialog.isShowing())) {
				progressDialog.dismiss();
			}
			// Show dialog with the returned error
			createAlertDialog(ActivityInspection.this.getResources().getString(R.string.error_request_title), ActivityInspection.this.getResources().getString(R.string.error_request_message) + ": \n" + error.getMessage());
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
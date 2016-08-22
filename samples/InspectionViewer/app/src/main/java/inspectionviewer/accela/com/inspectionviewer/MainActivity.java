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
package inspectionviewer.accela.com.inspectionviewer;

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

import com.accela.mobile.AMError;
import com.accela.mobile.AMLogger;
import com.accela.mobile.AMRequest;
import com.accela.mobile.AMRequestDelegate;
import com.accela.mobile.AMSessionDelegate;
import com.accela.mobile.AccelaMobile;
import com.accela.mobile.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends ListActivity implements OnClickListener {
	private static String SERVICE_URI_INSPECTION_LIST = "/v4/inspections";
	private final String URLSCHEMA = "aminspectionviewer";
	private final String AGENCY = "ISLANDTON";
	private Button btnSignIn, btnSignOut, btnShow;
	private RelativeLayout mainLayout;
	private AMRequest currentRequest;
	private ProgressDialog accessTokenProgressDialog;
	private List<HashMap<String, String>> inspectionItems;
	private AccelaMobile accelaMobile = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.mainLayout = (RelativeLayout) this.findViewById(R.id.mainLayout);
		// Register button events
		this.btnSignIn = (Button) this.findViewById(R.id.btnLogin);
		this.btnSignOut = (Button) this.findViewById(R.id.btnSignOut);
		this.btnShow = (Button) this.findViewById(R.id.btnShow);
		this.btnSignIn.setOnClickListener(this);
		this.btnSignOut.setOnClickListener(this);
		this.btnShow.setOnClickListener(this);
        // Set button status.
        enableBusinessButtons(false);
		this.accelaMobile = AccelaMobile.getInstance();
		this.accelaMobile.initialize(getApplicationContext(), "635439815877444193", "133bb8d3991a4d8483cf55e3ccf25070", AccelaMobile.Environment.PROD, sessionDelegate);

	}


	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.btnLogin:
				String[] permissions4Authorization  = new String[] {"search_records", "get_records", "get_record", "get_record_inspections", "get_inspections", "get_inspection", "get_record_documents", "get_document", "create_record","create_record_document", "get_gis_settings" };
				accelaMobile.getAuthorizationManager().showAuthorizationWebView(this, permissions4Authorization, URLSCHEMA, AGENCY);
				break;

			case R.id.btnSignOut:
				// Log out
				accelaMobile.getAuthorizationManager().logout();
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
				now.set(Calendar.DATE, now.get(Calendar.DATE) - 60);
				Date scheduledDateFrom = now.getTime();

				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				//search inspections of 60 days before till now
				requestParams.put("scheduledDateFrom", dateFormat.format(scheduledDateFrom));
				requestParams.put("scheduledDateTo", dateFormat.format(scheduledDateTo));
				//requestParams.put("inspectorIds", inspectorId);
				requestParams.put("limit", "10");
				requestParams.put("offset", "0");
				//asynchronous request, put requestDelegate for handling results
				currentRequest = accelaMobile.getRequestSender().sendRequest(servicePath, requestParams, null, requestDelegate);
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

	/**
	 * Private variable, defines the request delegate used by inspection searching.
	 */
	private AMRequestDelegate requestDelegate = new AMRequestDelegate() {
		@Override
		public void onStart() {
			amRequestStarted(currentRequest);
			// Show progress waiting view
			currentRequest.setOwnerView(MainActivity.this.mainLayout, MainActivity.this.getResources().getString(R.string.msg_search_inspections));
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
				MainActivity.this.inspectionItems = new ArrayList<HashMap<String, String>>();
				for (int i = 0; i < inspectionsArray.length(); i++) {
					String inspectionType = "Unknown Type";
					String inspectionId = "";
					String inspectionStatus = "Unknown Status";
					String resultType = "Unknown Result Type";
					try {
						JSONObject inspectionJson = inspectionsArray.getJSONObject(i);
						inspectionType = inspectionJson.getJSONObject("type").getString("text");
						inspectionId = inspectionJson.getString("id");
						inspectionStatus = inspectionJson.getJSONObject("status").getString("text");
						resultType = inspectionJson.getString("resultType");
					} catch (JSONException e) {
						AMLogger.logError("Error in parsing single inspection [%s]: %s", inspectionType, e.getMessage());
					}
					HashMap<String, String> inspectionMap = new HashMap<String, String>();
					inspectionMap.put("type", inspectionType + " - " + inspectionId);
					inspectionMap.put("status", inspectionStatus);
					inspectionMap.put("resultType", resultType);
					MainActivity.this.inspectionItems.add(inspectionMap);
				}
				// Refresh the inspection list view with data.
				SimpleAdapter inspectionListAdapter = new SimpleAdapter(MainActivity.this, inspectionItems, R.layout.inspection_list_item, new String[] { "type", "status",
				"resultType" }, new int[] { R.id.lbInspType, R.id.lbInspStatus, R.id.lbInspResultType});
				MainActivity.this.setListAdapter(inspectionListAdapter);
				// Invoke request delegate
				amRequestDidLoad(currentRequest, responseJson);
			} else {
				createAlertDialog(null, MainActivity.this.getResources().getString(R.string.error_no_inspection_found));
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
			createAlertDialog(MainActivity.this.getResources().getString(R.string.error_request_title), MainActivity.this.getResources().getString(R.string.error_request_message) + ": \n" + error.getMessage());
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
		 	boolean sessionValid = (accelaMobile != null) && (accelaMobile.isSessionValid());
		 	enableBusinessButtons(sessionValid);
			// Show message.
			Toast.makeText(MainActivity.this, MainActivity.this.getResources().getString(R.string.msg_logged_in), Toast.LENGTH_SHORT).show();
			AMLogger.logInfo("In MainActivity: Session Delegate.amDidLogin() invoked...");
		}

		public void amDidLoginFailure(AMError error) {
			Toast.makeText(MainActivity.this, MainActivity.this.getResources().getString(R.string.error_request_message), Toast.LENGTH_SHORT).show();
			AMLogger.logInfo("In MainActivity: Session Delegate.amDidLoginFailure() invoked: %s", error.toString());
		}

		public void amDidCancelLogin() {
			AMLogger.logInfo("In MainActivity: Session Delegate.amDidCancelLogin() invoked...");
		}

		public void amDidSessionInvalid(AMError error) {
			AMLogger.logInfo("In MainActivity: Session Delegate.amDidSessionInvalid() invoked: %s", error.toString());
		}

		public void amDidLogout() {
			// Refresh button status.
			MainActivity.this.enableBusinessButtons(false);
			// Show message.
			AMLogger.logInfo("In MainActivity: Session Delegate.amDidLogout() invoked...");
			Toast.makeText(MainActivity.this, MainActivity.this.getResources().getString(R.string.msg_logged_out), Toast.LENGTH_SHORT).show();
		}
	};
}
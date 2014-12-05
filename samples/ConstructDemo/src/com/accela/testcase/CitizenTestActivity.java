package com.accela.testcase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.view.ViewGroup;

import com.accela.mobile.AMError;
import com.accela.mobile.AMLogger;
import com.accela.mobile.AMRequest;
import com.accela.mobile.AMSessionDelegate;
import com.accela.mobile.AMSetting;
import com.accela.mobile.AccelaMobile.AuthorizationStatus;
import com.accela.mobile.AMRequest.HTTPMethod;
import com.accela.mobile.http.RequestParams;
import com.accela.mobile.AMRequestDelegate;
import com.accela.mobile.AccelaMobile;

public class CitizenTestActivity extends Activity implements OnClickListener {
	private static String SERVICE_URI_RECORD_LIST = "/v4/records/";
	private static String SERVICE_URI_RECORD_CREATE = "/v4/records/";
	private static String SERVICE_URI_RECORD_SEARCH = "/v4/search/records/";

	private ProgressDialog accessTokenProgressDialog = null;
	private Button btnCivicWebLogin, btnCivicEmbeddedWebLogin, btnGetRecords,
			btnCreateRecord, btnSearchRecord,
			btnCivicLogout, btnBack;
	private AppContext appContext = null;
	private ViewGroup mainLayout = null;
	private AMRequest currentRequest = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Initialize content view.
		setContentView(R.layout.citizen_test_view);
		initContentView();
	}

	// Show progress dialog if resume the current activity from authorization
	// web view..
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if ((hasFocus)
				&& (appContext.accelaMobile4Citizen != null)
				&& (appContext.accelaMobile4Citizen.getAuthorizationStatus() == AuthorizationStatus.AUTHORIZED)) {
			accessTokenProgressDialog = ProgressDialog.show(this, null, this
					.getResources().getString(R.string.msg_login), true, true);
		}
	}

	@Override
	public void onBackPressed() {
		startActivity(new Intent(this, HomeActivity.class));
		super.onBackPressed();
	}

	public void onClick(View view) {
		String[] permissions4Authorization = new String[] { "search_records",
				"get_records", "get_record", "get_record_inspections",
				"get_inspections", "get_inspection", "get_record_documents",
				"get_document", "create_record", "create_record_document",
				"a311citizen_create_record" };
		String servicePath = null;
		RequestParams requestParams = new RequestParams();

		switch (view.getId()) {
		case R.id.btnCivicWebLogin:
			appContext.accelaMobile4Citizen.authorize(
					permissions4Authorization, "ISLANDTON");
			break;
		case R.id.btnCivicEmbeddedWebLogin:
			appContext.accelaMobile4Citizen.authorize2(
					permissions4Authorization, "ISLANDTON");
			break;
		case R.id.btnCivicGetRecords:
			if (isSessionValid()) {
				servicePath = SERVICE_URI_RECORD_LIST;
				requestParams.put("limit", "10");
				requestParams.put("offset", "0");
				currentRequest = appContext.accelaMobile4Citizen.request(
						servicePath, requestParams, requestDelegate);
			}
			break;
		case R.id.btnCivicCreateRecord:
			if (isSessionValid()) {
				servicePath = SERVICE_URI_RECORD_CREATE;
				JSONObject recordJson = populateRecordJson();
				requestParams = new RequestParams(recordJson);
				currentRequest = appContext.accelaMobile4Citizen.request(
						servicePath, null, HTTPMethod.POST, requestParams,
						requestDelegate);
			}
			break;
		case R.id.btnCivicSearchRecord:
			if (isSessionValid()) {
				servicePath = SERVICE_URI_RECORD_SEARCH;
				requestParams = new RequestParams();
				requestParams.put("limit", "10");
				requestParams.put("offset", "0");
				requestParams.put("expand", "Addresses");
				currentRequest = appContext.accelaMobile4Citizen.request(
						servicePath, null, HTTPMethod.POST, requestParams,
						requestDelegate);
			}
			break;
		case R.id.btnCivicLogout:
			if (isSessionValid()) {
				appContext.accelaMobile4Citizen.logout();
			}
			break;
		case R.id.btnCivicBack:
			this.onBackPressed();
		}
	}

	private void initContentView() {
		// Initialize UI elements.
		this.mainLayout = (ViewGroup) this.findViewById(android.R.id.content)
				.getRootView();
		this.btnCivicWebLogin = (Button) this
				.findViewById(R.id.btnCivicWebLogin);
		this.btnCivicEmbeddedWebLogin = (Button) this
				.findViewById(R.id.btnCivicEmbeddedWebLogin);
		this.btnGetRecords = (Button) this
				.findViewById(R.id.btnCivicGetRecords);
		this.btnCreateRecord = (Button) this
				.findViewById(R.id.btnCivicCreateRecord);

		this.btnSearchRecord = (Button) this
				.findViewById(R.id.btnCivicSearchRecord);
		this.btnCivicLogout = (Button) this.findViewById(R.id.btnCivicLogout);
		this.btnBack = (Button) this.findViewById(R.id.btnCivicBack);
		// Set events for buttons.
		this.btnCivicWebLogin.setOnClickListener(this);
		this.btnCivicEmbeddedWebLogin.setOnClickListener(this);
		this.btnGetRecords.setOnClickListener(this);
		this.btnCreateRecord.setOnClickListener(this);
		this.btnSearchRecord.setOnClickListener(this);
		this.btnCivicLogout.setOnClickListener(this);
		this.btnBack.setOnClickListener(this);
		// Initialize app context.
		appContext = (AppContext) this.getApplicationContext();
		// Clear the AccelaMobile instance created for agency.
		appContext.accelaMobile4Agency = null;
		appContext.accelaMobileInternal4Agency = null;

		// Override the URLs of default authorization server and api server
		// defined in Accela SDK package.
		String authServer = AMSetting.AM_OAUTH_HOST;
		String apiServer = AMSetting.AM_API_HOST;
		// Create an AccelaMobile instance with the App ID and App Secret of the
		// registered app.
		appContext.accelaMobile4Citizen = new AccelaMobile(this,
				"635442545965802935", "e7b22310882f4e5185c9ca339aa1a67c",
				sessionDelegate, authServer, apiServer);
		// Set the environment.
		appContext.accelaMobile4Citizen
				.setEnvironment(AccelaMobile.Environment.PROD);
		// Set the URL schema.
		// NOTE: The assigned value should be same as the value of
		// "android:scheme" under "AuthorizationActivity" in the project's
		// AndroidManifest.xml.
		appContext.accelaMobile4Citizen.setUrlSchema("amtest");
		// Enable debugging
		// appContext.accelaMobile4Citizen.setDebug(true);
	}

	// Create and show an alert dialog
	private void createAlertDialog(String title, String message) {
		new AlertDialog.Builder(this).setTitle(title).setMessage(message)
				.setNegativeButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Do nothing except closing itself.
					}
				}).create().show();
	}

	// Check whether the current user session is valid or not.
	private boolean isSessionValid() {
		Boolean isValid = (appContext.accelaMobile4Citizen != null)
				&& (appContext.accelaMobile4Citizen.isSessionValid());
		if (!isValid) {
			Toast.makeText(this,
					this.getResources().getString(R.string.msg_not_logged_in),
					Toast.LENGTH_SHORT).show();
		}
		return isValid;
	}

	// Session delegate for Accela Mobile
	private AMSessionDelegate sessionDelegate = new AMSessionDelegate() {
		public void amDidLogin() {
			// Dismiss progress dialog.
			if ((accessTokenProgressDialog != null)
					&& (accessTokenProgressDialog.isShowing())) {
				accessTokenProgressDialog.dismiss();
			}
			// Show message.
			Toast.makeText(
					CitizenTestActivity.this,
					CitizenTestActivity.this.getResources().getString(
							R.string.msg_logged_in), Toast.LENGTH_SHORT).show();
			AMLogger.logInfo("In CitizenTestActivity: Session Delegate.amDidLogin() invoked...");
		}

		public void amDidLoginFailure(AMError error) {
			Toast.makeText(
					CitizenTestActivity.this,
					CitizenTestActivity.this.getResources().getString(
							R.string.msg_login_failed), Toast.LENGTH_SHORT)
					.show();
			AMLogger.logInfo(
					"In CitizenTestActivity: Session Delegate.amDidLoginFailure() invokded: %s",
					error.toString());
		}

		public void amDidCancelLogin() {
			AMLogger.logInfo("In CitizenTestActivity: Session Delegate.amDidCancelLogin() invoked...");
		}

		public void amDidSessionInvalid(AMError error) {
			AMLogger.logInfo(
					"In CitizenTestActivity: Session Delegate.amDidSessionInvalid() invoked: %s",
					error.toString());
		}

		public void amDidLogout() {
			Toast.makeText(
					CitizenTestActivity.this,
					CitizenTestActivity.this.getResources().getString(
							R.string.msg_logged_out), Toast.LENGTH_SHORT)
					.show();
			AMLogger.logInfo("In CitizenTestActivity: Session Delegate.amDidLogout() invoked...");
		}
	};

	/**
	 * private variable, defined the request delegate to be used by normal
	 * request.
	 */
	private AMRequestDelegate requestDelegate = new AMRequestDelegate() {
		@Override
		public void onStart() {
			amRequestStarted(currentRequest);
			// Show progress waiting view
			currentRequest.setOwnerView(
					CitizenTestActivity.this.mainLayout,
					CitizenTestActivity.this.getResources().getString(
							R.string.msg_request_being_processed));
		}

		@Override
		public void onTimeout() {
			amRequestDidTimeout(currentRequest);
			// Dismiss the process waiting view
			ProgressDialog progressDialog = currentRequest
					.getRequestWaitingView();
			if ((progressDialog != null) && (progressDialog.isShowing())) {
				progressDialog.dismiss();
			}
			// Show alert dialog.
			createAlertDialog(
					CitizenTestActivity.this.getResources().getString(
							R.string.error_request_timeout_title),
					CitizenTestActivity.this.getResources().getString(
							R.string.error_request_timeout_message));
		}

		@Override
		public void onSuccess(JSONObject responseJson) {
			amRequestDidReceiveResponse(currentRequest);
			amRequestDidLoad(currentRequest, responseJson);
			// Dismiss the process waiting view
			ProgressDialog progressDialog = currentRequest
					.getRequestWaitingView();
			if ((progressDialog != null) && (progressDialog.isShowing())) {
				progressDialog.dismiss();
			}
			// Show dialog with the retrured Json data
			createAlertDialog(
					CitizenTestActivity.this.getResources().getString(
							R.string.msg_request_completed_title),
					CitizenTestActivity.this.getResources().getString(
							R.string.msg_request_completed_message)
							+ ": \n " + responseJson.toString());
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
			AMError amError = new AMError(error.getStatus(), errorMessage,
					traceId, null, null);
			// Show dialog with the returned error
			createAlertDialog(CitizenTestActivity.this.getResources()
					.getString(R.string.error_request_failed_title),
					amError.toString());
		}
	};

	/**
	 * private method, used to populate the JSON data for the request which
	 * creates record.
	 */
	private JSONObject populateRecordJson() {
		// Contact data
		String contactPhonesJsonStr = "[\"(801) 879-3789\"]";
		String contactRoleJsonStr = "{\"id\":\"Applicant\",\"display\":\"Applicant\"}";
		String contactMailingAddressJsonStr = "{\"streetName\":\"2633 Camino Ramon\",\"unit\":\"Suite 120\",\"city\":\"San Ramon\",\"state\":\"CA\",\"postalCode\":\"94583\"}";
		JSONObject contactJson = new JSONObject();
		try {
			contactJson.put("entityState", "Added");
			contactJson.put("givenName", "Kris");
			contactJson.put("faimilyName", "Trujillo");
			contactJson.put("tels", new JSONArray(contactPhonesJsonStr));
			contactJson.put("contactRole", new JSONObject(contactRoleJsonStr));
			contactJson.put("mainlingAddress", new JSONObject(
					contactMailingAddressJsonStr));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		// ASI data
		String asiItemJsonStr = "[{\"entityState\":\"Added\",\"display\":\"Trip Name\",\"id\":\"Trip Name\",\"value\":\"My Trip\"}]";
		JSONObject asiItemJson = new JSONObject();
		JSONObject asiSubgroupJson = new JSONObject();
		JSONObject asiJson = new JSONObject();
		try {
			asiItemJson.put("items", new JSONArray(asiItemJsonStr));
			asiSubgroupJson.put("subGroups", new JSONArray().put(asiItemJson));
			asiSubgroupJson.put("action", "Added");
			asiSubgroupJson.put("display", "STANDARD");
			asiSubgroupJson.put("id", "STANDARD");
			asiJson.put("asis", new JSONArray().put(asiSubgroupJson));
			asiJson.put("display", "LIC_FISH_RPT");
			asiJson.put("id", "LIC_FISH_RPT");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		// ASI Table data
		String column1JsonStr = "{\"entityState\":\"Added\",\"display\":\"Trip Name\",\"id\":\"Trip Name\",\"name\":\"Trip Name\",\"type\":\"String\"}";
		String column2JsonStr = "{\"entityState\":\"Added\",\"display\":\"Trip Location\",\"id\":\"Trip Location\",\"name\":\"Trip Location\",\"type\":\"String\"}";
		String column3JsonStr = "{\"entityState\":\"Added\",\"display\":\"Trip Start Date\",\"id\":\"Trip Start Date\",\"name\":\"Trip Start Date\",\"type\":\"String\"}";
		String column4JsonStr = "{\"entityState\":\"Added\",\"display\":\"Trip End Date\",\"id\":\"Trip End Date\",\"name\":\"Trip End Date\",\"type\":\"String\"}";
		String column5JsonStr = "{\"entityState\":\"Added\",\"display\":\"Fish Type\",\"id\":\"Fish Type\",\"name\":\"Fish Type\",\"type\":\"String\"}";
		String column6JsonStr = "{\"entityState\":\"Added\",\"display\":\"Fish Weight\",\"id\":\"Fish Weight\",\"name\":\"Fish Weight\",\"type\":\"String\"}";
		String column7JsonStr = "{\"entityState\":\"Added\",\"display\":\"Fish Length\",\"id\":\"Fish Length\",\"name\":\"Fish Length\",\"type\":\"String\"}";
		String column8JsonStr = "{\"entityState\":\"Added\",\"display\":\"Fish Count\",\"id\":\"Fish Count\",\"name\":\"Fish Count\",\"type\":\"String\"}";
		String column9JsonStr = "{\"entityState\":\"Added\",\"display\":\"Fish Location\",\"id\":\"Fish Location\",\"name\":\"Fish Location\",\"type\":\"String\"}";
		String value1JsonStr = "{\"entityState\":\"Added\",\"id\":\"Trip Name\",\"value\":\"My Trip\"}";
		String value2JsonStr = "{\"entityState\":\"Added\",\"id\":\"Trip Location\",\"value\":\"-111.000235, 33.000000\"}";
		String value3JsonStr = "{\"entityState\":\"Added\",\"id\":\"Trip Start Date\",\"value\":\"2/1/2012\"}";
		String value4JsonStr = "{\"entityState\":\"Added\",\"id\":\"Trip End Date\",\"value\":\"2/29/2012\"}";
		String value5JsonStr = "{\"entityState\":\"Added\",\"id\":\"Fish Type\",\"value\":\"Paddlefish\"}";
		String value6JsonStr = "{\"entityState\":\"Added\",\"id\":\"Fish Weight\",\"value\":\"5 lbs\"}";
		String value7JsonStr = "{\"entityState\":\"Added\",\"id\":\"Fish Length\",\"value\":\"40 inches\"}";
		String value8JsonStr = "{\"entityState\":\"Added\",\"id\":\"Fish Count\",\"value\":\"1\"}";
		String value9JsonStr = "{\"entityState\":\"Added\",\"id\":\"Fish Location\",\"value\":\"-111.00000,-33.00000\"}";

		JSONArray columnsArrayJson = new JSONArray();
		JSONArray valuesArrayJson = new JSONArray();

		JSONObject asitRowJson = new JSONObject();
		JSONObject asitJson = new JSONObject();

		try {
			columnsArrayJson.put(new JSONObject(column1JsonStr));
			columnsArrayJson.put(new JSONObject(column2JsonStr));
			columnsArrayJson.put(new JSONObject(column3JsonStr));
			columnsArrayJson.put(new JSONObject(column4JsonStr));
			columnsArrayJson.put(new JSONObject(column5JsonStr));
			columnsArrayJson.put(new JSONObject(column6JsonStr));
			columnsArrayJson.put(new JSONObject(column7JsonStr));
			columnsArrayJson.put(new JSONObject(column8JsonStr));
			columnsArrayJson.put(new JSONObject(column9JsonStr));

			valuesArrayJson.put(new JSONObject(value1JsonStr));
			valuesArrayJson.put(new JSONObject(value2JsonStr));
			valuesArrayJson.put(new JSONObject(value3JsonStr));
			valuesArrayJson.put(new JSONObject(value4JsonStr));
			valuesArrayJson.put(new JSONObject(value5JsonStr));
			valuesArrayJson.put(new JSONObject(value6JsonStr));
			valuesArrayJson.put(new JSONObject(value7JsonStr));
			valuesArrayJson.put(new JSONObject(value8JsonStr));
			valuesArrayJson.put(new JSONObject(value9JsonStr));

			asitRowJson.put("values", valuesArrayJson);
			asitRowJson.put("action", "Add");
			asitRowJson.put("display", "STANDARD");
			asitRowJson.put("id", "STANDARD");

			asitJson.put("columns", columnsArrayJson);
			asitJson.put("rows", new JSONArray().put(asitRowJson));
			asitJson.put("display", "LIC_FISH_RPT/STANDARD");
			asitJson.put("id", "LIC_FISH_RPT");
			asitJson.put("subId", "STANDARD");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		// Record Basic data
		JSONObject recordJson = new JSONObject();
		JSONObject recordTypeJson = new JSONObject();
		try {
			recordTypeJson.put("id", "ServiceRequest-Graffiti-Graffiti-NA");
			recordTypeJson.put("value", "ServiceRequest/Graffiti/Graffiti/NA");

			recordJson.put("type", recordTypeJson);
			recordJson.put("text", "Create from Catch Report");
			// recordJson.put("contacts", new JSONArray().put(contactJson));
			// recordJson.put("additionalInfo", new JSONArray().put(asiJson));
			// recordJson.put("additionalTableInfo", new
			// JSONArray().put(asitJson));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return recordJson;
	}

	// Copy image file from resource folder to application folder.
	private void copyImageFile2App(String localImagePath,
			String applicationImagePath) {
		// Create the image file under application's directory before uploading
		// it
		InputStream inputStream = this.getClass().getResourceAsStream(
				localImagePath);
		// Get the size of the resource image file.
		int imageFileSize = 0;
		try {
			imageFileSize = inputStream.available();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		// Create the image file in application directory.
		int read = 0;
		byte[] bytes = new byte[imageFileSize];
		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(new File(applicationImagePath));
			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
			outputStream.flush();
			inputStream.close();
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
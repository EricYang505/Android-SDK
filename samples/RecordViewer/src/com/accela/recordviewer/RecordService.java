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
package com.accela.recordviewer;


import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import com.accela.mobile.AMError;
import com.accela.mobile.AMLogger;
import com.accela.mobile.AMRequest;
import com.accela.mobile.AMRequestDelegate;
import com.accela.mobile.AccelaMobile;
import com.accela.mobile.AMRequest.HTTPMethod;
import com.accela.mobile.http.RequestParams;
import com.accela.recordviewer.model.AddressModel;
import com.accela.recordviewer.model.RecordModel;

public class RecordService {


	public static interface RecordServiceDeleagte {
		public void onLoadStart();
		public void onLoadSuccess();
		public void onLoadFailed();

	}


	private final static String SERVICE_URI_RECORD_LIST = "/v4/search/records";

	private RecordServiceDeleagte delegate;
	private List<RecordModel> listRecord = new ArrayList<RecordModel>();

	private AccelaMobile accelaMobile;
	private AMRequest currentRequest;


	public RecordService(AccelaMobile accelaMobile) {
		this.accelaMobile = accelaMobile;
	}

	public void loadRecordAsyn(boolean clearExistRecord) {

		String servicePath = SERVICE_URI_RECORD_LIST;
		RequestParams requestParams = new RequestParams();
		if(clearExistRecord) {
			listRecord.clear();
		}
		//requestParams.put("inspectorIds", inspectorId);
		requestParams.put("limit", "15");
		requestParams.put("offset", String.format("%d", listRecord.size()));
		requestParams.put("expand","addresses");

		RequestParams postParams = new RequestParams();
		//postParams.put("module", "Building");
		postParams.put("openedDateFrom",  "2013-01-01");
		postParams.put("openedDateTo",  "2014-12-01");

		//RecordService.request(String path, RequestParams urlParams, HTTPMethod httpMethod, RequestParams postData, AMRequestDelegate requestDelegate)
		//asynchronous request, put requestDelegate for handling results
		currentRequest = accelaMobile.request(servicePath, requestParams, HTTPMethod.POST, postParams, requestDelegate);

	}

	public List<RecordModel> getRecordList() {
		return listRecord;
	}

	public void setDelegate(RecordServiceDeleagte delegate) {
		this.delegate = delegate;
	}

	/**
	 * Private variable, defines the request delegate used by inspection searching.
	 */
	private AMRequestDelegate requestDelegate = new AMRequestDelegate() {
		@Override
		public void onStart() {
			amRequestStarted(currentRequest);
			if(delegate!=null) {
				delegate.onLoadStart();
			}
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
			JSONArray recordsArray = null;
			try {
				recordsArray = responseJson.getJSONArray("result");
			} catch (JSONException e) {
				AMLogger.logError("Error in parsing inspections json array: %s", e.getMessage());
			}
			if ((recordsArray != null) && (recordsArray.length() > 0)) {
				for (int i = 0; i < recordsArray.length(); i++) {
					RecordModel recordModel = new RecordModel();

					try {
						JSONObject recordJson = recordsArray.getJSONObject(i);
						recordModel.type = recordJson.getJSONObject("type").getString("text");
						recordModel.id = recordJson.getString("id");
						recordModel.status = recordJson.getJSONObject("status").getString("text");
						recordModel.openedDate = recordJson.getString("openedDate");
						recordModel.description = recordJson.optString("description");
						if(recordJson.has("addresses")) {
							recordModel.address = getFirstAddress(recordJson.getJSONArray("addresses"));
						}

					} catch (JSONException e) {
						AMLogger.logError("Error in parsing single record [%s]", e.getMessage());
					}
					listRecord.add(recordModel);
				}
				// Refresh the inspection list view with data.
				amRequestDidLoad(currentRequest, responseJson);
				if(delegate!=null) {
					delegate.onLoadSuccess();
				}
			} else {
				if(delegate!=null) {
					delegate.onLoadFailed();
				}
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
			if(delegate!=null) {
				delegate.onLoadFailed();
			}
		}
	};

	private AddressModel getFirstAddress(JSONArray addressJsonArray) throws JSONException {
		if(addressJsonArray.length()==0) {
			return null;
		}

		AddressModel address = new AddressModel();
		JSONObject addressJson = addressJsonArray.getJSONObject(0);
		address.streetStart = addressJson.getString("streetStart");
		address.streetName = addressJson.getString("streetName");
		address.city = addressJson.getString("city");
		address.state = addressJson.getJSONObject("state").getString("text");
		address.postalCode = addressJson.getString("postalCode");
		address.xCoordinate = addressJson.getDouble("xCoordinate");
		address.yCoordinate = addressJson.getDouble("yCoordinate");

		return address;
	}


}

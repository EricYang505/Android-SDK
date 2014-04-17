package com.accela.mobile;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.accela.mobile.AMRequest.HTTPMethod;
import com.accela.mobile.http.RequestParams;

/**
 * <pre>
 * 
 *  Accela Amobile
 *  File: AMBatchSession.java
 * 
 *  Accela, Inc.
 *  Copyright (C): 2014
 * 
 *  Description:
 *  Session manager of batch request.
 * 
 *  Notes:
 * 
 *  Revision History  
 * 
 * 	@since 4.0
 * 
 * </pre>
 */

public class AMBatchSession {
	private String path = "/batch";
	private List<AMRequest> requests;

	/**
	 * Constructor.
	 * 
	 * @return An initialized AMBatchSession instance.
	 * 
	 * @since 4.0
	 */
	public AMBatchSession() {
		requests = new ArrayList<AMRequest>();
	}

	/**
     * Add a request.
     * 
     * @param request An instance of AMRequest.
     * 
     * @return Void.
	 * 
	 * @since 4.0
	 */
	public void add(AMRequest request) {
		requests.add(request);
	}

	/**
     * Get the list of all requests.
     * 
     * 
     * @return List of requests.
	 * 
	 * @since 4.0
	 */
	public List<AMRequest> getRequests() {
		return requests;
	}

	/**
     * Execute the batch request in synchronous way.
     * 
     * 
     * @return The Json object returned by the request.
	 * 
	 * @since 4.0
	 */
	public JSONObject execute() {
		RequestParams params = new RequestParams();
		List<AMBatchRequestModel> models = new ArrayList<AMBatchRequestModel>();
		for (AMRequest request : requests) {
			String url = request.getServiceURL();
			HTTPMethod httpMethod = request.getHttpMethod();
			String method = httpMethod.toString();

			AMBatchRequestModel model = new AMBatchRequestModel();
			model.setUrl(url);
			model.setMethod(method);
			if ((httpMethod == HTTPMethod.POST || httpMethod == HTTPMethod.PUT)
					&& request.getPostParams() != null) {
				model.setBody(request.getPostParams());
			}

			models.add(model);
		}

		String json = toJsonArray(models);
		params.put("json", json);
		AccelaMobile accelaMobile = AccelaMobileInternal.defaultInstance();
		JSONObject result = accelaMobile.fetch(path, null, HTTPMethod.POST,
				params);
		return result;
	}

	/**
     * Execute the batch request in asynchronous way.
     * 
     * @param requestDelegate The delegate which handles the request's callbacks.
     * 
     * @return Void.
	 * 
	 * @since 4.0
	 */
	public void executeAsync(AMRequestDelegate requestDelegate) {
		RequestParams params = new RequestParams();
		List<AMBatchRequestModel> models = new ArrayList<AMBatchRequestModel>();
		for (AMRequest request : requests) {
			String url = request.getServiceURL();
			HTTPMethod httpMethod = request.getHttpMethod();
			String method = httpMethod.toString();
			AMBatchRequestModel model = new AMBatchRequestModel();
			model.setUrl(url);
			model.setMethod(method);
			if ((httpMethod == HTTPMethod.POST || httpMethod == HTTPMethod.PUT)
				&& request.getPostParams() != null) {
				model.setBody(request.getPostParams());
			}

			models.add(model);
		}

		String json = toJsonArray(models);
		params.put("json", json);
		AccelaMobile accelaMobile = AccelaMobileInternal.defaultInstance();
		accelaMobile.request(path, null, HTTPMethod.POST, params,requestDelegate);
	}
	
	/**
	 * Private method, used to convert batch request models to JSON array.
	 */	
	private String toJsonArray(List<AMBatchRequestModel> models) {
		if (models == null) return null;
		
		JSONArray jsonArray = new JSONArray();
		for (AMBatchRequestModel model : models) {
			jsonArray.put(model.toJsonObject());
		}
		return jsonArray.toString();		
	}
}

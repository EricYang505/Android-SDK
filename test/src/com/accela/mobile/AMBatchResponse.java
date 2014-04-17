package com.accela.mobile;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * <pre>
 * 
 *  Accela Amobile
 *  File: AMBatchResponse.java
 * 
 *  Accela, Inc.
 *  Copyright (C): 2014
 * 
 *  Description:
 *  Response object of batch request.
 * 
 *  Notes:
 * 
 *  Revision History  
 * 
 * 	@since 4.0
 * 
 * </pre>
 */


public class AMBatchResponse {
	
	private JSONObject response;	
	private String status;
	private List<JSONObject> result;
	
	/**
	 * <pre>
	 * 
	 *  Accela Amobile
	 *  Interface: AMBatchRequestDelegate
	 * 
	 *  Description:
	 *  The delegate interface of batch request .
	 * 
	 * 	@since 4.0
	 * 
	 * </pre>
	 */
	public interface AMBatchRequestDelegate {		
		public void onCompleted();
	}

	/**
	 * Constructor.
	 * 
	 * @return An initialized AMBatchResponse instance.
	 * 
	 * @since 4.0
	 */
	public AMBatchResponse(JSONObject response){
		this.response = response;
		result = new ArrayList<JSONObject>();
		
		try {
			this.status = this.response.getString("status");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		try {
			JSONArray jsonArray = this.response.getJSONArray("result");
			int len = jsonArray.length();
			
			for(int index = 0; index <len; index++){
				JSONObject item = jsonArray.getJSONObject(index);
				result.add(item);
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 *
	 * Get the value of property status.
	 *
	 * @return The value of property status.
	 *
	 * @since 4.0
	 */
	public String getStatus() {
		return status;
	}

	/**
	 *
	 * Set the value of property status.
	 *
	 * @param status The new status value.
	 *
	 * @return Void.
	 *
	 * @since 4.0
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	
	/**
	 *
	 * Get the value of property result.
	 *
	 * @return The value of property result.
	 *
	 * @since 4.0
	 */
	public List<JSONObject> getResult() {
		return result;
	}

	/**
	 *
	 * Set the value of property result.
	 *
	 * @param result The new result value.
	 *
	 * @return Void.
	 *
	 * @since 4.0
	 */
	public void setResult(List<JSONObject> result) {
		this.result = result;
	}
}

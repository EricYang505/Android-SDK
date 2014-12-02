package com.accela.mobile;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * <pre>
 * 
 *  Accela Amobile
 *  File: AMBatchRequestModel.java
 * 
 *  Accela, Inc.
 *  Copyright (C): 2014
 * 
 *  Description:
 *  Model object of batch request.
 * 
 *  Notes:
 * 
 *  Revision History  
 * 
 * 	@since 4.0
 * 
 * </pre>
 */

public class AMBatchRequestModel {
	private String url;
	private String method;
	private Object body;
	
	/**
	 *
	 * Get the value of property url.
	 *
	 * @return The value of property url.
	 *
	 * @since 4.0
	 */
	public String getUrl() {
		return url;
	}
	
	/**
	 *
	 * Set the value of property url.
	 *
	 * @param status The new url value.
	 *
	 * @return Void.
	 *
	 * @since 4.0
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	
	/**
	 *
	 * Get the value of property method.
	 *
	 * @return The value of property method.
	 *
	 * @since 4.0
	 */
	public String getMethod() {
		return method;
	}
	
	/**
	 *
	 * Set the value of property method.
	 *
	 * @param status The new method value.
	 *
	 * @return Void.
	 *
	 * @since 4.0
	 */
	public void setMethod(String method) {
		this.method = method;
	}
	
	/**
	 *
	 * Get the value of property body.
	 *
	 * @return The value of property body.
	 *
	 * @since 4.0
	 */
	public Object getBody() {
		return body;
	}
	
	/**
	 *
	 * Set the value of property body.
	 *
	 * @param status The new body value.
	 *
	 * @return Void.
	 *
	 * @since 4.0
	 */
	public void setBody(Object body) {
		this.body = body;
	}
	
	/**
	 *
	 * Convert the model to JSON object.
	 *
	 *
	 * @return A Json object.
	 *
	 * @since 4.0
	 */
	public JSONObject toJsonObject() {
		JSONObject json = new JSONObject();
		try {
			json.put("url", url);
			json.put("method", method);
			json.put("body", body);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}	
}

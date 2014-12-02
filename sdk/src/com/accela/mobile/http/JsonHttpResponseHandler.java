package com.accela.mobile.http;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.accela.mobile.AMError;
import com.accela.mobile.AMLogger;
import com.accela.mobile.AMSetting;

/**
 * <pre>
 * 
 *  Accela Amobile
 *  File: JsonHttpResponseHandler.java
 * 
 *  Accela, Inc.
 *  Copyright (C): 2012
 * 
 *  Description:
 *  Json response callback handler object, called during an asynchronous request is being processed.
 * 
 *  Notes:
 * Used to intercept and handle the responses from requests made using 
 * {@link AsyncHttpClient}, with automatic parsing into a {@link JSONObject}
 * or {@link JSONArray}.
 * <p>
 * This class is designed to be passed to get, post, put and delete requests
 * with the {@link #onSuccess(JSONObject)} or {@link #onSuccess(JSONArray)}
 * methods anonymously overridden.
 * <p>
 * Additionally, you can override the other event methods from the 
 * parent class.
 * 
 *  Revision History
 *  
 * 
 * 	@since 1.0
 * 
 * </pre>
 */
public abstract class JsonHttpResponseHandler extends AsyncHttpResponseHandler {

	public JSONObject rawResponseJson;
	
    /**
     * Fired when a request returns successfully and contains a json object
     *at the base of the response string. Override to handle in your own code.
     * 
     * @param response The parsed json object found in the server response (if any).
     * 
     * @return Void.
	 * 
	 * @since 1.0
	 */
    public abstract void onSuccess(JSONObject response);
    
    /**
     * Fired when a request fails to complete,
     *override to handle in your own code.
     * 
     * @param error The underlying cause of the failure.
     * 
     * @return Void.
	 * 
	 * @since 1.0
	 */
    public abstract void onFailure(AMError error);


    /**
     * Fired when a request returns successfully and contains a json array
     *at the base of the response string. Override to handle in your own code.
     * 
     * @param response The parsed json array found in the server response (if any).
     * 
     * @return Void.
	 * 
	 * @since 1.0
	 */
    public void onSuccess(JSONArray response){};
   
    
    /**
	 * Override the handleSuccessMessage method inherited from its parent class.
	*/
    @Override
    protected void handleSuccessMessage(String responseBody) {
        super.handleSuccessMessage(responseBody);       
        try {
            Object jsonResponse = parseResponse(responseBody);
           
            if(jsonResponse instanceof JSONObject) {     
            	onSuccess((JSONObject)jsonResponse);
            } else if(jsonResponse instanceof JSONArray) {
            	onSuccess((JSONArray)jsonResponse);
            }
        } catch(JSONException e) {
        	AMError error = new AMError(0,null,null, e.getMessage(),null);
            onFailure(error);
        }
    }

    /**
	 * Protected method, used to convert response string to JSON object.
	*/
    protected JSONObject parseResponse(String responseBody) throws JSONException { 
    	try {
    		rawResponseJson = (JSONObject) new JSONTokener(responseBody).nextValue();        
    	} catch(ClassCastException e) {
    		AMLogger.logError("JsonHttpResponseHandler.parseResponse(): Error occured: %s", e.getMessage());
			if (AMSetting.DebugMode) {
				e.printStackTrace();
			 } 
        }
        return rawResponseJson;
    }
}
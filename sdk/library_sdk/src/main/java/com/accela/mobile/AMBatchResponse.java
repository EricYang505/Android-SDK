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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 *  Response object of batch request.
 * 
 * 	@since 4.0
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
		public void onSuccessful();
		public void onFailed(AMError e);
	}

	/**
	 * Constructor.
	 * 
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
	 *
	 * @since 4.0
	 */
	public void setResult(List<JSONObject> result) {
		this.result = result;
	}
}

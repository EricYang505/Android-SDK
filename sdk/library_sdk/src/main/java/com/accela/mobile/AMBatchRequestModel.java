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

import org.json.JSONException;
import org.json.JSONObject;

/**
 *  Model object of batch request.
 * 
 * 	@since 4.0
 */

public class AMBatchRequestModel {
	private String relativeUrl;
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
	public String getRelativeUrl() {
		return relativeUrl;
	}
	
	/**
	 *
	 * Set the value of property url.
	 *
	 *
	 * @since 4.0
	 */
	public void setRelativeUrl(String url) {
		this.relativeUrl = url;
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
	 * @param method The new method value.
	 *
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
	 * @param body The new body value.
	 *
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
			json.put("relativeUrl", relativeUrl);
			json.put("method", method);
			json.put("body", body);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}	
}

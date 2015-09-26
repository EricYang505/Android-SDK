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
import org.json.JSONObject;

import android.graphics.Bitmap;
//import android.support.v4.content.LocalBroadcastManager;


public abstract class AMRequestDelegate extends HttpResponseLogHandler {
	/**
	 * Fired when the request is started, override to handle in your own code.
	 *
	 * @return Void.
	 *
	 * @since 1.0
	 */
	public abstract void onStart();

	public void onSuccess(JSONObject content) {}

	public void onSuccess(Bitmap bitmap) {}

	/**
	 * Fired when a request fails to complete, override to handle in your own
	 * code.
	 *
	 * @param error
	 *            The underlying cause of the failure.
	 *
	 * @return Void.
	 *
	 * @since 1.0
	 */
	public void onFailure(AMError error) {
	}

}

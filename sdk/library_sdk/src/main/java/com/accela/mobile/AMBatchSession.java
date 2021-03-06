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

import com.accela.mobile.AMRequest.HTTPMethod;
import com.accela.mobile.http.RequestParams;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *  Session manager of batch request.
 * 
 * 	@since 4.0
 */

public class AMBatchSession {
	private String path = "/v4/batch";
	private List<AMRequest> requests;

	/**
	 * Constructor.
	 * 
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
     * Execute the batch request in asynchronous way.
     * 
     * @param requestDelegate The delegate which handles the request's callbacks.
     * 
	 *
	 * @since 4.0
	 */
	public void executeAsync(Map<String, String> customHttpHeader, AMRequestDelegate requestDelegate) {
		List<AMBatchRequestModel> models = new ArrayList<AMBatchRequestModel>();
		for (AMRequest request : requests) {
			String url = request.getServiceURL();
			HTTPMethod httpMethod = request.getHttpMethod();
			String method = httpMethod.toString();
			AMBatchRequestModel model = new AMBatchRequestModel();
			model.setRelativeUrl(url);
			model.setMethod(method);
			if ((httpMethod == HTTPMethod.POST || httpMethod == HTTPMethod.PUT)
				&& request.getPostParams() != null) {
				model.setBody(request.getPostParams());
			}else if(httpMethod == HTTPMethod.GET) {		
				model.setRelativeUrl(request.assembleUrlWithParams(url, request.getUrlParams()));
			}

			models.add(model);
		}

		String json = toJsonArray(models);
        RequestParams params = new RequestParams(json);
        AccelaMobile.getInstance().getRequestSender().sendRequest(path, null, customHttpHeader, HTTPMethod.POST, params,requestDelegate);
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

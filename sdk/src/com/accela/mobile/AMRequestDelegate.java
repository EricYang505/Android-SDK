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

import java.util.Map;
import java.util.ResourceBundle;

import org.apache.http.Header;
import org.json.JSONObject;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.accela.mobile.AMRequest.HTTPMethod;
import com.accela.mobile.http.AsyncHttpClient;
import com.accela.mobile.http.JsonHttpResponseHandler;
import com.accela.mobile.http.RequestParams;

/**
 *  Request delegate, defines the methods which will be called during the lifecycle of a request. 
 * 
 * 	@since 1.0
 */

public abstract class AMRequestDelegate extends JsonHttpResponseHandler {

	/**
	 * The AMError instance binded to the current delegate.
	 * 
	 * @since 3.0
	 */
	protected AMError amError;

	/**
	 * The error code got from the JSON returned by API.
	 * 
	 * @since 3.0
	 */
	protected String errorCode;

	/**
	 * The trace ID got from the JSON returned by API.
	 * 
	 * @since 3.0
	 */
	protected String traceId;

	/**
	 * The message got from the JSON returned by API.
	 * 
	 * @since 3.0
	 */
	protected String errorMessage;

	/**
	 * The string loader which loads localized text.
	 * 
	 * @since 3.0
	 */
	private ResourceBundle stringLoader = AMSetting.getStringResourceBundle();
	
	/**
	 * Called just before the request is sent to the server.
	 * 
	 * @param request The AMRequest instance which is processing the current request.
	 * 
	 * @return Void.
	 * 
	 * @since 1.0
	 */
	public void amRequestStarted(AMRequest request) {
		if (!AMSetting.DebugMode)
			return;

		// Get the headers of the current request.
		Map<String, String> requestMap = null;
		AsyncHttpClient asyncHttpClient = request.getAsyncHttpClient();
		if (asyncHttpClient != null) {
			requestMap = asyncHttpClient.getHeader();
		} else {
			requestMap = request.getSyncHttpClient().getHeader();
		}
		// Print the request URL and header in log.
		String serviceURL = request.getServiceURL();
		RequestParams urlParams = request.getUrlParams();
		if (urlParams != null) {
			serviceURL += "?" + urlParams.toString();
		}
		HTTPMethod httpMethod = request.getHttpMethod();
		AMLogger.logInfo(stringLoader.getString("Log_AMRequestDelegate_amRequestStarted_URL"),request.getTag(), httpMethod, serviceURL);
		AMLogger.logInfo(stringLoader.getString("Log_AMRequestDelegate_amRequestStarted_Header"),request.getTag(), httpMethod, requestMap.toString());
		if ((AMRequest.HTTPMethod.POST.equals(httpMethod)) || (AMRequest.HTTPMethod.PUT.equals(httpMethod))) {
			RequestParams postParams = request.getPostParams();
			String postParamsString = (postParams != null) ? postParams.getJsonString() : "null";
			AMLogger.logInfo(stringLoader.getString("Log_AMRequestDelegate_amRequestStarted_Body"),request.getTag(), httpMethod, postParamsString);
		}
	}
	
	/**
	 * Called when the server responds and begins to send back data.
	 * 
	 * @param request The AMRequest instance which is processing the current request.
	 * 
	 * @return Void.
	 * 
	 * @since 1.0
	 */
	public void amRequestDidReceiveResponse(AMRequest request) {
		// Parse headers
		if (this.httpResponse != null) {
			Header traceIdHeader = this.httpResponse.getFirstHeader(AMRequest.HEADER_X_ACCELA_TRACEID);
			Header messageHeader = this.httpResponse.getFirstHeader(AMRequest.HEADER_X_ACCELA_RESP_MESSAGE);
			traceId = (traceIdHeader != null) ? traceIdHeader.getValue() : null;
			errorMessage = (messageHeader != null) ? messageHeader.getValue() : null;
		}
		// Handle session for error code 401 and 403 specially
		if (this.responseStatusCode == AMError.ERROR_CODE_Unauthorized) {// HTTP error 401
			// Clear token
			AuthorizationManager authorizationManager = request.getAccelaMobile().authorizationManager;
			if (authorizationManager != null) {
				authorizationManager.clearAuthorizationAndToken(true);
			}
			// Populate error
			this.amError = new AMError(AMError.ERROR_CODE_Unauthorized,
					AMError.ERROR_CODE_TOKEN_EXPIRED,
					traceId, errorMessage,stringLoader.getString("Error_AMRequestDelegate_Unauthorized"));

		} else if (this.responseStatusCode == AMError.ERROR_CODE_Forbidden) {// HTTP error 403
			// Populate error
			this.amError = new AMError(AMError.ERROR_CODE_Forbidden,
					AMError.ERROR_CODE_ACCESS_FORBIDDEN,traceId, errorMessage,stringLoader.getString("Error_AMRequestDelegate_Forbidden"));
		} else if (this.responseStatusCode >= AMError.ERROR_CODE_HTTP_MINIMUM) {// Other HTTP errors
			// Populate error
			this.amError = new AMError(responseStatusCode,
					AMError.ERROR_CODE_OTHER_ERROR,traceId, errorMessage,stringLoader.getString("Error_AMRequestDelegate_Forbidden"));
		}
		// Send broadcast for error
		if (this.amError != null) {
			Intent broadcastIntent = new Intent(AMSetting.BROARDCAST_ACTION_SESSION_INVALID);
			String username = request.getAccelaMobile().authorizationManager.getUser();
			if (username != null) {
				broadcastIntent.putExtra("user", username);
			}
			LocalBroadcastManager.getInstance(request.getAccelaMobile().ownerContext).sendBroadcast(broadcastIntent);
		}
		// Print the response header and body in log.
		if (AMSetting.DebugMode) {
			Header[] responseHeaders;
			String responseBody;
			AsyncHttpClient asyncHttpClient = request.getAsyncHttpClient();
			if (asyncHttpClient != null) {
				responseHeaders = this.responseHeaders;
				responseBody = this.rawResponseBody;
			} else {
				responseHeaders = request.getSyncHttpClient().getHttpResponseHeader();
				responseBody = request.getSyncHttpClient().getHttpResponse();
			}
			HTTPMethod httpMethod = request.getHttpMethod();
			AMLogger.logInfo(stringLoader.getString("Log_AMRequestDelegate_amRequestDidReceiveResponse_Header"),request.getTag(), httpMethod,getHeadersString(responseHeaders));
			AMLogger.logInfo(stringLoader.getString("Log_AMRequestDelegate_amRequestDidReceiveResponse_Body"),request.getTag(), httpMethod, responseBody);
		}
	}

	/**
	 * Called when an request times out after waiting the connection time
	 * interval defined in {@link AMSetting}.
	 * 
	 * @param request The AMRequest instance which is processing the current request.
	 * 
	 * @return Void.
	 * 
	 * @since 3.0
	 */
	public void amRequestDidTimeout(AMRequest request) {
		if (AMSetting.DebugMode) {
			AMLogger.logError(stringLoader.getString("Log_AMRequestDelegate_amRequestDidTimeout_Body"),request.getTag(), request.getHttpMethod(),AMSetting.HTTP_CONNECTION_TIMEOUT);
		}
	}

	/**
	 * Called when an error prevents the request from completing successfully.
	 * 
	 * @param request The AMRequest instance which is processing the current request.
	 * @param error The AMError instance which contains the error information.
	 * 
	 * @return Void.
	 * 
	 * @since 1.0
	 */
	public void amRequestDidFailWithError(AMRequest request, AMError error) {
		if (AMSetting.DebugMode) {
			AMLogger.logError(stringLoader.getString("Log_AMRequestDelegate_amRequestDidFailWithError_Body"),request.getTag(), request.getHttpMethod(),error.getMessage());
		}
	}

	/**
	 * Called when a request returns and its response has been parsed into an
	 * object. The resulting object format may be a dictionary or an array. Note
	 * that the result is null if the amDownloadDestinationPath property bas
	 * been set to a valid value.
	 * 
	 * @param request The AMRequest instance which is processing the current request.
	 * @param result The JSON object which contains the result data.
	 * 
	 * @return Void.
	 * 
	 * @since 1.0
	 */
	public void amRequestDidLoad(AMRequest request, JSONObject result) {
		if (AMSetting.DebugMode) {
			AMLogger.logInfo(stringLoader.getString("Log_AMRequestDelegate_amRequestDidLoad_Body"),request.getTag(), request.getHttpMethod(),result.toString());
		}
	}
	
	/**
	 * Called when the request receives some data.
	 * 
	 * Note that the total is 0 if there is no Content-Length variable in the
	 * HTTP headers.
	 * 
	 * @param request The AMRequest instance which is processing the current request.
	 * @param bytes The bytes of data which has been received.
	 * @param total The total bytes of data which will be received.
	 * 
	 * @since 1.0
	 */
	public void amRequestReceivedBytes(AMRequest request, long bytes, long total) {
		if (AMSetting.DebugMode) {
			AMLogger.logInfo(stringLoader.getString("Log_AMRequestDelegate_amRequestReceivedBytes_Body"),request.getTag(), request.getHttpMethod(), bytes, total);
		}
	}

	/**
	 * Called when the request sends some data.
	 * 
	 * @param request The AMRequest instance which is processing the current request.
	 * @param bytes The bytes of data which has been sent.
	 * 
	 * @since 1.0
	 */

	public void amRequestSendBytes(AMRequest request, long bytes) {
		if (AMSetting.DebugMode) {
			AMLogger.logInfo(stringLoader.getString("Log_AMRequestDelegate_amRequestSendBytes_Body"),request.getTag(), request.getHttpMethod(), bytes);
		}
	}

	/**
	 * private method, used to populate response's headers into a string.
	 */
	private String getHeadersString(Header[] headers) {
		if ((headers == null) || (headers.length == 0)) {
			return null;
		}

		String headersString = "";
		for (Header header : headers) {
			headersString += header.getName() + "=" + header.getValue() + ";";
		}
		// Remove the last char
		headersString = headersString.substring(0, headersString.length() - 2);
		// Return the string with JSON format.
		return "{" + headersString + "}";
	}
}

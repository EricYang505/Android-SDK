package com.accela.mobile.http;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.entity.BufferedHttpEntity;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.accela.mobile.AMError;
import com.accela.mobile.AMLogger;
import com.accela.mobile.AMSetting;

/**
 * <pre>
 * 
 *  Accela Amobile
 *  File: AsyncHttpResponseHandler.java
 * 
 *  Accela, Inc.
 *  Copyright (C): 2013
 * 
 *  Description:
 *  Generic response callback handler object, called during an asynchronous request is being processed.
 * 
 * Used to intercept and handle the responses from requests made using 
 * {@link AsyncHttpClient}. The {@link #onSuccess(String)} method is 
 * designed to be anonymously overridden with your own response handling code.
 * <p>
 * Additionally, you can override the {@link #onFailure(Throwable)},
 * {@link #onStart()}, and {@link #onFinish()} methods as required.
 * <p>
 * For example:
 * <p>
 * <pre>
 * AsyncHttpClient client = new AsyncHttpClient();
 * client.get("http://www.google.com", new AsyncHttpResponseHandler() {
 *     &#064;Override
 *     public void onStart() {
 *         // Initiated the request
 *     }
 *
 *     &#064;Override
 *     public void onSuccess(String response) {
 *         // Successfully got a response
 *     }
 * 
 *     &#064;Override
 *     public void onFailure(Throwable e) {
 *         // Response failed :(
 *     }
 *
 *     &#064;Override
 *     public void onFinish() {
 *         // Completed the request (either success or failure)
 *     }
 * });
 * 
 * 
 *  Revision History
 *  
 * 
 * 	@since 1.0
 * 
 * </pre>
 */
abstract class AsyncHttpResponseHandler {
	
	/**
	 * The response object.
	 * 
	 * @since 3.0
	 */
	public HttpResponse httpResponse;
	
	/**
	 * The response headers.
	 * 
	 * @since 1.0
	 */
	public Header[] responseHeaders;
	
	/**
	 * The text content of response body..
	 * 
	 * @since 1.0
	 */
	public String rawResponseBody;	
	protected int responseStatusCode;
	private static final int TIMEOUT_MESSAGE = -1;
	private static final int SUCCESS_MESSAGE = 0;
	private static final int FAILURE_MESSAGE = 1;
	private static final int START_MESSAGE = 2;
	private static final int FINISH_MESSAGE = 3;
	private static final int FILE_MESSAGE = 4;
	private Handler handler;
	private String contentType;
	private boolean isTimeoutError = false;
	private boolean isEmseAfterError = false;

	/**
	 * Constructor without parameters.
	 * 
	 * @return An initialized AsyncHttpResponseHandler instance
	 * 
	 * @since 1.0
	 */
	@SuppressLint("HandlerLeak")
	public AsyncHttpResponseHandler() {
		// Set up a handler to post events back to the correct thread if possible
		if (Looper.myLooper() != null) {
			handler = new Handler() {
				public void handleMessage(Message msg) {
					AsyncHttpResponseHandler.this.handleMessage(msg);
				}
			};
		}
	}

	/**
	 * Fired when the request is started, override to handle in your own code.
	 * 
	 * @return Void.
	 * 
	 * @since 1.0
	 */
	public abstract void onStart();



	/**
	 * Fired in all cases when the request is finished, after both success and
	 * failure, override to handle in your own code
	 * 
	 * @return Void.
	 * 
	 * @since 1.0
	 */
	public void onFinish() {}

	/**
	 * Fired when a request returns a string response successfully,
	 * override to handle in your own code.
	 * 
	 * @param content
	 *            The string body of the HTTP response from the server.
	 * 
	 * @return Void.
	 * 
	 * @since 1.0
	 */
	public void onSuccess(String content) {}

	/**
	 * Fired when a request returns a byte array response successfully,
	 * override to handle in your own code.
	 * 
	 * @param content
	 *            The byte array body of the HTTP response from the server.
	 * 
	 * @return Void.
	 * 
	 * @since 1.0
	 */
	public void onSuccess(byte[] content) {}

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


	/**
	 * Fired when a request times out, override to handle in your own code.
	 * 
	 * @param error
	 *            The underlying cause of the failure.
	 * 
	 * @return Void.
	 * 
	 * @since 4.0
	 */
	public void onTimeout() {}	


	/**
	 * Protected method, used to send timeout message.
	 */
	protected void sendTimeoutMessage(AMError e) {
		sendMessage(obtainMessage(TIMEOUT_MESSAGE, e));
	}

	/**
	 * Protected method, used to send success message.
	 */
	protected void sendSuccessMessage(byte[] responseBody) {
		sendMessage(obtainMessage(SUCCESS_MESSAGE, responseBody));
	}
	
//	protected void sendFileSuccessMessage(DiskFile file){
//		sendMessage(obtainMessage(FILE_MESSAGE, file));
//	}

	/**
	 * Protected method, used to send failure message.
	 */
	protected void sendFailureMessage(AMError e) {
		sendMessage(obtainMessage(FAILURE_MESSAGE, e));
	}

	/**
	 * Protected method, used to send start message.
	 */
	protected void sendStartMessage() {
		sendMessage(obtainMessage(START_MESSAGE, null));
	}




	

	/**
	 * Protected method, used to send finish message.
	 */
	protected void sendFinishMessage() {
		sendMessage(obtainMessage(FINISH_MESSAGE, null));
	}
	/**
	 * Protected method, used to handle pre-processing of messages (in original
	 * calling thread, typically the UI thread)
	 */
	protected void handleSuccessMessage(byte[] responseBody) {
		onSuccess(responseBody);
		try {
			rawResponseBody = new String(responseBody, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			rawResponseBody = new String(responseBody);
		}
		// Handle response body only when the content type is not object stream
		// (typically in file downloading)
		if (!"application/octet-stream".equalsIgnoreCase(this.contentType)) {
			handleSuccessMessage(rawResponseBody);
		}
	}
	

	/**
	 * Protected method, used to handle success message.
	 */
	protected void handleSuccessMessage(String responseBody) {
		rawResponseBody = responseBody;
		onSuccess(responseBody);
	}

	

	/**
	 * Protected method, used to handle failure message.
	 */
	protected void handleFailureMessage(AMError error) {
		onFailure(error);
	}

	/**
	 * Protected method, used to emulate android's Handler and Message methods.
	 */
	protected void handleMessage(Message msg) {
		switch (msg.what) {
		case TIMEOUT_MESSAGE:
			onTimeout();
			break;
		case SUCCESS_MESSAGE:
			handleSuccessMessage((byte[]) msg.obj);
			break;
		case FAILURE_MESSAGE:
			handleFailureMessage((AMError) msg.obj);
			break;
		case START_MESSAGE:
			onStart();
			break;
		case FINISH_MESSAGE:
			onFinish();
			break;
		case FILE_MESSAGE:
//			onSuccess((FormFile) msg.obj);
			break;
		}
	}

	/**
	 * Protected method, used to send message.
	 */
	protected void sendMessage(Message msg) {
		if (handler != null) {
			handler.sendMessage(msg);
		} else {
			handleMessage(msg);
		}
	}	

	/**
	 * Protected method, used to obtain message.
	 */
	protected Message obtainMessage(int responseMessage, Object response) {
		Message msg = null;
		if (handler != null) {
			msg = this.handler.obtainMessage(responseMessage, response);
		} else {
			msg = new Message();
			msg.what = responseMessage;
			msg.obj = response;
		}
		return msg;
	}

	/**
	 * Protected method, used to send response message.
	 */
	protected void sendResponseMessage(HttpResponse response) {

		this.httpResponse = response;
		// Assign value to responseHeaders property
		this.responseHeaders = response.getAllHeaders();
		// Get content type and assign it to contentType property
		String contentTypeStr = null;

		Header header = response.getFirstHeader("Content-Type");
		if(header!=null) {
			contentTypeStr = header.getValue();
		}
		String [] types = null;
		if(contentTypeStr!=null) {
			types = contentTypeStr.split(";");
		}
		this.contentType = types!=null? types[0]: "";
		// Get status code and assign it to responseStatusCode property
		StatusLine status = response.getStatusLine();
		this.responseStatusCode = status.getStatusCode();
		if (AMSetting.DebugMode)
			AMLogger.logInfo(
					"In AsyncHttpResponseHandler.sendResponseMessage(): status.getStatusCode() = %d, status.getReasonPhrase() = %s",
					status.getStatusCode(), status.getReasonPhrase());
		// Send failure message if HTTP request returns an invalid status code
		// (>=300)
		if (status.getStatusCode() >= AMError.ERROR_CODE_HTTP_MINIMUM) {
			HttpEntity httpEntity = response.getEntity();
			AMError error = getContent4SpecialError(httpEntity);
			if (error != null) { // Consider this as successful case
				if (isEmseAfterError) {
					if (AMSetting.DebugMode) AMLogger.logInfo("***************** In sendResponseMessage(isEmseAfterError):%s", error.toString());
					byte[] contents = error.getMore().getBytes();
					sendSuccessMessage(contents);
				} else if (isTimeoutError) {
					sendTimeoutMessage(error);
					if (AMSetting.DebugMode) AMLogger.logInfo("***************** In sendResponseMessage(isTimeoutError)");
				} else {
					if (AMSetting.DebugMode) AMLogger.logInfo("***************** In sendResponseMessage(Other error)");
					sendFailureMessage(error);
				}
			} else {
				if (AMSetting.DebugMode) AMLogger.logInfo("***************** In sendResponseMessage(Empty error)");
				error = new AMError(0, null, null,"Failed to get response from server", null);				
				sendFailureMessage(error);
			}			
		} else {
			try {
				HttpEntity entity = null;
				HttpEntity temp = response.getEntity();
				if (temp != null) {
					entity = new BufferedHttpEntity(temp);
				}
				
				// Send response as a byte array and assume listener knows
				// what
				// to expect.
				ByteArrayInputStream stream = (ByteArrayInputStream) entity
						.getContent();
				// Cast long to int. 2GB+ of data between threads.
				byte[] contents = new byte[(int) entity.getContentLength()];
				while (stream.read(contents) != -1) {
				}

				rawResponseBody = new String(contents);
				sendSuccessMessage(contents);
			} catch (OutOfMemoryError e) {
				AMError error = new AMError(0, AMError.ERROR_CODE_OUT_OF_MEMORY, null, e.toString(), null);
				sendFailureMessage(error);
			} catch (Throwable e) {
				AMError error = new AMError(0, null, null, e.getMessage(), null);
				sendFailureMessage(error);
			}
		}
	}
	
	protected void handleStreamFile(InputStream stream){
		
	}
	

	protected Exception getException(String responseBody) {
		JSONObject jo;
		try {
			jo = new JSONObject(rawResponseBody);
			if (jo != null) {
				int responseStatus = jo.optInt("status", 200);
				// int responseCode = jo.optInt("code");
				String responseMsg = jo.optString("message");
				if (responseStatus >= AMError.ERROR_CODE_HTTP_MINIMUM) {
					return new HttpResponseException(responseStatus,
							responseMsg);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Private method, used to check whether the response contains error of EMSE
	 * After event or not.
	 */
	private AMError getContent4SpecialError(HttpEntity responseEntity) {		
		AMError error = null;
		String errorCode = null;
		try {
			InputStream inputStream = responseEntity.getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String line = reader.readLine();
			JSONObject errorJson = new JSONObject(line);
			reader.close();			
			
			AMLogger.logInfo("***************** In getContent4SpecialError(1): Errorine = %s",line);
			
			if (errorJson.has("code")) {
				errorCode = errorJson.optString("code");
				int status = errorJson.optInt("status");	
				String traceId = errorJson.optString("traceId");				
				String errorMessage = errorJson.optString("message");
				String moreMessage = errorJson.toString();
				error = new AMError(status, errorCode, traceId, errorMessage, moreMessage);
			}
			AMLogger.logInfo("***************** In getContent4SpecialError(2): errorJson = %s",errorJson.toString());
		} catch (IllegalStateException e) {
			AMLogger.logError("IllegalStateException occured: %s.",e.getMessage());
		} catch (JSONException e) {
			AMLogger.logError("JSONException occured: %s.",e.getMessage());
		} catch (IOException e) {
			AMLogger.logError("IOException occured: %s.", e.getMessage());
		}
		try {
			isTimeoutError = ((errorCode != null) && (errorCode.equalsIgnoreCase(AMError.ERROR_CODE_REQUEST_TIMEOUT)));
			isEmseAfterError = ((errorCode != null) && (errorCode.equalsIgnoreCase(AMError.ERROR_CODE_EMSE_FAILURE)));
		} catch (NumberFormatException e) {
			//Do nothing
		}
		return error;
	}
}
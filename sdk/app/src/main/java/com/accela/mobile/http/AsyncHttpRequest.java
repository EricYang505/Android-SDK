
package com.accela.mobile.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.HttpContext;

import com.accela.mobile.AMError;
import com.accela.mobile.AMLogger;
import com.accela.mobile.AMSetting;


/**
 * <pre>
 * 
 *  Accela Amobile
 *  File: AsyncHttpRequest.java
 * 
 *  Accela, Inc.
 *  Copyright (C): 2012
 * 
 *  Description:
 *  Request wrapper object, used to process HTTP request and handle response in a separate thread.
 * 
 *  Notes:
 * 
 * 
 *  Revision History
 *  
 * 
 * 	@since 1.0
 * 
 * </pre>
 */

class AsyncHttpRequest implements Runnable {
    private final AbstractHttpClient client;
    private final HttpContext context;
    private final HttpUriRequest request;
    private final AsyncHttpResponseHandler responseHandler;
    private int executionCount = 0;

    /**
	 * Constructor.
	 * 
	 * @param client The AbstractHttpClient instance which processes the request.	
	 * @param context The current HttpContext instance. 
	 * @param request The current HttpUriRequest instance. 
	 * @param responseHandler The callback handler which methods will be called during the request is processed. 
	 * 
	 * @return An initialized AsyncHttpRequest instance
	 * 
	 * @since 1.0
	 */
    public AsyncHttpRequest(AbstractHttpClient client, HttpContext context, HttpUriRequest request, AsyncHttpResponseHandler responseHandler) {
        this.client = client;
        this.context = context;
        this.request = request;
        this.responseHandler = responseHandler;
    }

    /**
	 * Run the request.
	 * 
	 * @return Void.
	 * 
	 * @since 1.0
	 */
    public void run() {
        try {
            if(responseHandler != null){
                responseHandler.sendStartMessage();
            }

            makeRequestWithRetries();

            if(responseHandler != null) {
                responseHandler.sendFinishMessage();
            }
        } catch (IOException e) {
            if(responseHandler != null) {
                responseHandler.sendFinishMessage();
				AMError error = new AMError(0, null, null, e.getMessage(), null);
                responseHandler.sendFailureMessage(error);
            }
        }
    }
    
    /**
	 * Private method, used to handle HTTP response after the request is completed.
	 */	
    private void makeRequest() throws IOException {
		if (!Thread.currentThread().isInterrupted()) {
			try {
			    HttpResponse response = client.execute(request, context);
			    if (!Thread.currentThread().isInterrupted()) {
			    	if (responseHandler != null) {
			    		responseHandler.sendResponseMessage(response);
			    	}
			    } else {
			    	sendFailedMessage4Timeout(null);
			    }
			} catch (SocketTimeoutException e) {			
				sendFailedMessage4Timeout(e);
			} catch (ConnectTimeoutException e) {
				sendFailedMessage4Timeout(e);
			} catch (UnknownHostException e) {
				sendFailedMessage4UnavailableHost();
			}
		}
    }

    /**
	 * Private method, used to retry request if it fails.
	 */	
    private void makeRequestWithRetries() throws ConnectException {
        boolean retry = true;
        IOException cause = null;
        HttpRequestRetryHandler retryHandler = client.getHttpRequestRetryHandler();
        while (retry) {
            try {
                makeRequest();
                return;
            } catch (IOException e) {
                cause = e;
                retry = retryHandler.retryRequest(cause, ++executionCount, context);
            } catch (NullPointerException e) {
                cause = new IOException("NPE in HttpClient: " + e.getMessage());
                retry = retryHandler.retryRequest(cause, ++executionCount, context);
    	    } catch (Exception e) {
    			cause = new IOException("Exception in HttpClient: " + e.getMessage());;
    			retry = retryHandler.retryRequest(cause, ++executionCount,context);
    		} 
        }
        
        ConnectException ex = new ConnectException();
        ex.initCause(cause);
        throw ex;
    }
    
    /**
     * Private method, used to send failed message for network timeout.
     */
    private void sendFailedMessage4Timeout(Exception exception) {
    	String errorMsg;
    	if (exception != null) {
    		errorMsg = exception.getMessage();
    	} else {
    		errorMsg = "Request times out.";
    	}
    	BasicStatusLine failedStatusLine = new BasicStatusLine(new ProtocolVersion("AM", 3, 1), 500, null); // HTTP status code 500
    	BasicHttpResponse failedResponse = new BasicHttpResponse(failedStatusLine);	
    	String timeoutErrorStr = "{\"code\":\"request_timeout\",\"status\":\"500\",\"code\":\"" + AMError.ERROR_CODE_REQUEST_TIMEOUT + "\",\"message\":\"" + errorMsg + "\"}";
    	BasicHeader header1 = new BasicHeader("Accept","application/json");
    	BasicHeader header2 = new BasicHeader("Content-Type","application/json");
    	failedResponse.setHeaders(new BasicHeader[]{header1,header2});
    	try {
			failedResponse.setEntity(new StringEntity(timeoutErrorStr));
			responseHandler.sendResponseMessage(failedResponse);
		} catch (UnsupportedEncodingException e) {
			AMLogger.logError("AsyncHttpRequest.sendFailedMessage4Timeout(): Error occured: %s", e.getMessage());
			if (AMSetting.DebugMode) {
				e.printStackTrace();
			 } 
		}
    }
    
    /**
     * Private method, used to send failed message for network timeout.
     */
    private void sendFailedMessage4UnavailableHost() {
    	BasicStatusLine failedStatusLine = new BasicStatusLine(new ProtocolVersion("AM", 3, 1), 500, null); // HTTP status code 500
    	BasicHttpResponse failedResponse = new BasicHttpResponse(failedStatusLine);	
    	String timeoutErrorStr = "{\"error\":\"unavailable_server\",\"status\":\"500\",\"code\":\"" + AMError.ERROR_CODE_UNAVAIABLE_HOST + "\",\"message\":\"Failed to connect to server, please try again later.\"}";
    	BasicHeader header1 = new BasicHeader("Accept","application/json");
    	BasicHeader header2 = new BasicHeader("Content-Type","application/json");
    	failedResponse.setHeaders(new BasicHeader[]{header1,header2});
    	try {
			failedResponse.setEntity(new StringEntity(timeoutErrorStr));
			responseHandler.sendResponseMessage(failedResponse);
		} catch (UnsupportedEncodingException e) {
			AMLogger.logError("AsyncHttpRequest.sendFailedMessage4UnavailableHost(): Error occured: %s", e.getMessage());
			if (AMSetting.DebugMode) {
				e.printStackTrace();
			 } 
		}
    }
}
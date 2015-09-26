package com.accela.mobile;

/**
 * Created by eyang on 8/20/15.
 */


import org.json.JSONObject;

import android.content.Intent;

import com.accela.mobile.AMError;
import com.accela.mobile.AMLogger;
import com.accela.mobile.AMRequest;
import com.accela.mobile.AMSetting;
import com.accela.mobile.AuthorizationManager;
import com.accela.mobile.http.RequestParams;
import com.accela.mobile.http.volley.AuthFailureError;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;


public abstract class HttpResponseLogHandler {

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
        Map<String, String> requestHeader = new HashMap<String, String>();

        // Print the request URL and header in log.
        String serviceURL = request.getServiceURL();
        RequestParams urlParams = request.getUrlParams();
        if (urlParams != null) {
            try {
                requestHeader = request.mRequest.getHeaders();
            } catch (AuthFailureError authFailureError) {
                authFailureError.printStackTrace();
            }
            serviceURL += "?" + urlParams.toString();
        }
        AMRequest.HTTPMethod httpMethod = request.getHttpMethod();
        AMLogger.logInfo(stringLoader.getString("Log_AMRequestDelegate_amRequestStarted_URL"), request.getTag(), httpMethod, serviceURL);
        AMLogger.logInfo(stringLoader.getString("Log_AMRequestDelegate_amRequestStarted_Header"), request.getTag(), httpMethod, requestHeader.toString());
        if ((AMRequest.HTTPMethod.POST.equals(httpMethod)) || (AMRequest.HTTPMethod.PUT.equals(httpMethod))) {
            RequestParams postParams = request.getPostParams();
            String postParamsString = (postParams != null) ? postParams.getStringBody() : "null";
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
        if (request.mRequest==null)
            return;
        Map<String, String> httpResponse = request.mRequest.getResponseHeader();
        int responseStatusCode = request.mRequest.getResponseStatus();
        if (httpResponse != null) {
            String traceIdHeader = httpResponse.get(AMRequest.HEADER_X_ACCELA_TRACEID);
            String messageHeader = httpResponse.get(AMRequest.HEADER_X_ACCELA_RESP_MESSAGE);
            traceId = (traceIdHeader != null) ? traceIdHeader : null;
            errorMessage = (messageHeader != null) ? messageHeader : null;
        }
        // Handle session for error code 401 and 403 specially
        if (responseStatusCode == AMError.ERROR_CODE_Unauthorized) {// HTTP error 401
            // Clear token
            AuthorizationManager authorizationManager = request.getAccelaMobile().authorizationManager;
            if (authorizationManager != null) {
                authorizationManager.clearAuthorizationAndToken(true);
            }
            // Populate error
            this.amError = new AMError(AMError.ERROR_CODE_Unauthorized,
                    AMError.ERROR_CODE_TOKEN_EXPIRED,
                    traceId, errorMessage,stringLoader.getString("Error_AMRequestDelegate_Unauthorized"));

        } else if (responseStatusCode == AMError.ERROR_CODE_Forbidden) {// HTTP error 403
            // Populate error
            this.amError = new AMError(AMError.ERROR_CODE_Forbidden,
                    AMError.ERROR_CODE_ACCESS_FORBIDDEN,traceId, errorMessage,stringLoader.getString("Error_AMRequestDelegate_Forbidden"));
        } else if (responseStatusCode >= AMError.ERROR_CODE_HTTP_MINIMUM) {// Other HTTP errors
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
//			LocalBroadcastManager.getInstance(request.getAccelaMobile().ownerContext).sendBroadcast(broadcastIntent);
        }
        // Print the response header and body in log.
        if (AMSetting.DebugMode) {
            String responseBody;
            AMRequest.HTTPMethod httpMethod = request.getHttpMethod();
            AMLogger.logInfo(stringLoader.getString("Log_AMRequestDelegate_amRequestDidReceiveResponse_Header"),request.getTag(), httpMethod, httpResponse.toString());
//			AMLogger.logInfo(stringLoader.getString("Log_AMRequestDelegate_amRequestDidReceiveResponse_Body"),request.getTag(), httpMethod, responseBody);
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

}
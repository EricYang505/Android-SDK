package com.accela.mobile;

import android.widget.ImageView;

import com.accela.mobile.http.AMDocDownloadRequest;
import com.accela.mobile.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Created by eyang on 8/31/15.
 */
public class AMRequestSender {

    AMRequestSender(){
    }
    /**
     *
     * @param batchSession
     * @param path
     * @param urlParams
     * @param customHttpHeader
     * @param requestDelegate
     * @return
     *
     * @since 4.0
     */
    public AMRequest sendRequest(AMBatchSession batchSession, String path, RequestParams urlParams, Map<String, String> customHttpHeader, AMRequestDelegate requestDelegate) {
        AMRequest amRequest = new AMRequest(path, urlParams, AMRequest.HTTPMethod.GET, requestDelegate);
        amRequest.setHttpHeader(customHttpHeader);
        batchSession.add(amRequest);
        return amRequest;
    }

    /**
     * Makes a request to the Accela Construct API endpoint with the given parameters using HTTP GET method as an asynchronous operation.
     *
     * @param path The path to the Accela Construct API endpoint.
     * @param urlParams The collection of parameters associated with the specific URL.
     * @param customHttpHeader The HTTP header fields in key value pairs.
     * @param requestDelegate The request's delegate or null if it doesn't have a delegate.  See {@link AMRequestDelegate} for more information.
     * @param customHttpHeader The HTTP header fields in key value pairs.
     * @return The AMRequest object corresponding to this API call.
     *
     * @since 1.0
     */
    public AMRequest sendRequest(String path, RequestParams urlParams, Map<String, String> customHttpHeader, AMRequestDelegate requestDelegate) {
        return this.sendRequest(path, urlParams, customHttpHeader, AMRequest.HTTPMethod.GET, null, requestDelegate);
    }

    /**
     * Makes a request to the Accela Construct API endpoint with the given parameters using the given HTTP method as an asynchronous operation.
     *
     * @param path The path to the Accela Construct API endpoint.
     * @param urlParams The collection of parameters associated with the specific URL.
     * @param httpMethod The HTTP data transfer method (such as GET, POST, PUT or DELETE).
     * @param postData The content sent with the corresponding request(only used in POST or PUT method).
     * @param requestDelegate The request's delegate or null if it doesn't have a delegate.  See {@link AMRequestDelegate} for more information.
     *
     * @return The AMRequest object corresponding to this Accela Construct API endpoint call.
     *
     * @since 1.0
     */
    public AMRequest sendRequest(String path, RequestParams urlParams, AMRequest.HTTPMethod httpMethod, RequestParams postData, AMRequestDelegate requestDelegate) {
        return sendRequest(path, urlParams, null, httpMethod, postData, requestDelegate);
    }

    /**
     * Makes a request to the Accela Construct API endpoint with the given parameters using the given HTTP method as an asynchronous operation.
     *
     * @param path The path to the Accela Construct API endpoint.
     * @param urlParams The collection of parameters associated with the specific URL.
     * @param customHttpHeader The HTTP header fields in key value pairs.
     * @param httpMethod The HTTP data transfer method (such as GET, POST, PUT or DELETE).
     * @param postData The content sent with the corresponding request(only used in POST or PUT method).
     * @param requestDelegate The request's delegate or null if it doesn't have a delegate.  See {@link AMRequestDelegate} for more information.
     *
     * @return The AMRequest object corresponding to this Accela Construct API endpoint call.
     *
     * @since 4.1
     */
    public AMRequest sendRequest(String path, RequestParams urlParams, Map<String, String> customHttpHeader, AMRequest.HTTPMethod httpMethod, RequestParams postData, AMRequestDelegate requestDelegate) {
        AMRequest amRequest = new AMRequest(AccelaMobile.getInstance().amApisHost + path, urlParams, httpMethod);
        amRequest.setHttpHeader(customHttpHeader);
        try {
            return amRequest.sendRequest(postData, requestDelegate);
        } catch (JSONException e) {
            AMLogger.logError(e.toString());
        }
        return amRequest;
    }

    /**
     * Makes a request to the Accela Construct API end point with the given parameters using the given HTTP method as an asynchronous operation.
     *
     * @param path The path to the Accela Construct API end point.
     * @param urlParams The collection of parameters associated with the specific URL.
     * @param httpMethod The HTTP data transfer method (such as GET, POST, PUT or DELETE).
     * @param postData The content sent with the corresponding request(only used in POST or PUT method).
     * @param requestDelegate The request's delegate or null if it doesn't have a delegate.  See {@link AMRequestDelegate} for more information.
     *
     * @return The AMRequest object corresponding to this Accela Construct API end point call.
     *
     * @since 4.0
     */
    public AMRequest sendRequest(AMBatchSession batchSession,String path, RequestParams urlParams, AMRequest.HTTPMethod httpMethod, RequestParams postData, AMRequestDelegate requestDelegate) {
        return sendRequest(batchSession, path, urlParams, null, httpMethod, postData, requestDelegate);
    }

    /**
     * Makes a request to the Accela Construct API end point with the given parameters using the given HTTP method as an asynchronous operation.
     *
     * @param path The path to the Accela Construct API end point.
     * @param urlParams The collection of parameters associated with the specific URL.
     * @param customHttpHeader The HTTP header fields in key value pairs.
     * @param httpMethod The HTTP data transfer method (such as GET, POST, PUT or DELETE).
     * @param postData The content sent with the corresponding request(only used in POST or PUT method).
     * @param requestDelegate The request's delegate or null if it doesn't have a delegate.  See {@link AMRequestDelegate} for more information.
     *
     * @return The AMRequest object corresponding to this Accela Construct API end point call.
     *
     * @since 4.1
     */
    public AMRequest sendRequest(AMBatchSession batchSession,String path, RequestParams urlParams, Map<String, String> customHttpHeader, AMRequest.HTTPMethod httpMethod, RequestParams postData, AMRequestDelegate requestDelegate) {
        AMRequest amRequest = new AMRequest(path, urlParams, AMRequest.HTTPMethod.POST, requestDelegate);
        amRequest.setHttpHeader(customHttpHeader);
        batchSession.add(amRequest);
        return amRequest;
    }


    public AMRequest loadImage(String path, RequestParams urlParams, Map<String, String> customHttpHeader, AMRequestDelegate requestDelegate, int maxWidth, int maxHeight, ImageView.ScaleType scaleType) {
        AMRequest amRequest = new AMRequest(AccelaMobile.getInstance().amApisHost + path, urlParams, AMRequest.HTTPMethod.GET);
        amRequest.setHttpHeader(customHttpHeader);
        amRequest.setRequestType(AMRequest.RequestType.IMAGE);
        amRequest.loadImage(requestDelegate, maxWidth, maxHeight, scaleType);
        return amRequest;
    }

    public AMRequest loadImage(String path, RequestParams urlParams, Map<String, String> customHttpHeader, AMRequestDelegate requestDelegate, int maxWidth, int maxHeight) {
        AMRequest amRequest = new AMRequest(AccelaMobile.getInstance().amApisHost + path, urlParams, AMRequest.HTTPMethod.GET);
        amRequest.setHttpHeader(customHttpHeader);
        amRequest.setRequestType(AMRequest.RequestType.IMAGE);
        amRequest.loadImage(requestDelegate, maxWidth, maxHeight);
        return amRequest;
    }

    public AMRequest loadImage(String path, RequestParams urlParams, Map<String, String> customHttpHeader, AMRequestDelegate requestDelegate) {
        AMRequest amRequest = new AMRequest(AccelaMobile.getInstance().amApisHost + path, urlParams, AMRequest.HTTPMethod.GET);
        amRequest.setHttpHeader(customHttpHeader);
        amRequest.setRequestType(AMRequest.RequestType.IMAGE);
        amRequest.loadImage(requestDelegate);
        return amRequest;
    }


    /**
     *
     * Uploads a set of binary files as an asynchronous operation.
     *
     * @param path The path to the Accela Construct API endpoint.
     * @param postData The array of file's JSON object is posted together with attachments.
     * 									 Note file's JSON object contains keys "serviceProviderCode","fileName","type",and "description".
     * @param fileInformation The file collection of key-value pairs.
     * 									 Note the key name is "fileName", and the value is file's full path.
     * @param requestDelegate The request's delegate or null if it doesn't have a delegate.  See {@link AMRequestDelegate} for more information.
     *
     * @return The AMRequest object corresponding to this Accela Construct API endpoint call.
     *
     * @since 1.0
     */
    public AMRequest uploadAttachments(String path, RequestParams postData, Map<String, String> fileInformation, AMRequestDelegate requestDelegate) {
        return uploadAttachments(path, postData, fileInformation, requestDelegate);
    }

    /**
     *
     * Uploads a set of binary files as an asynchronous operation.
     *
     * @param path The path to the Accela Construct API endpoint.
     * @param postData The array of file's JSON object is posted together with attachments.
     * 									 Note file's JSON object contains keys "serviceProviderCode","fileName","type",and "description".
     * @param fileInformation The file collection of key-value pairs.
     * 									 Note the key name is "fileName", and the value is file's full path.
     * @param customHttpHeader The HTTP header fields in key value pairs.
     * @param requestDelegate The request's delegate or null if it doesn't have a delegate.  See {@link AMRequestDelegate} for more information.
     *
     * @return The AMRequest object corresponding to this Accela Construct API endpoint call.
     *
     * @since 1.0
     */
    public AMRequest uploadAttachments(String path, RequestParams postData, Map<String, String> fileInformation,  Map<String, String> customHttpHeader, AMRequestDelegate requestDelegate) {
        AMRequest amRequest = new AMRequest(AccelaMobile.getInstance().amApisHost + path,  null, AMRequest.HTTPMethod.POST);
        amRequest.setHttpHeader(customHttpHeader);
        return amRequest.uploadAttachments(postData, fileInformation, requestDelegate);
    }

    /**
     *
     * @param path The path to the Accela Construct API endpoint.
     * @param urlParams The collection of parameters associated with the specific URL.
     * @param localFile The path for file.
     * @return The AMRequest object corresponding to this Accela Construct API endpoint call.
     * @since 1.0
     */
    public AMRequest downloadAttachment(String path, RequestParams urlParams,  String localFile, AMDocDownloadRequest.AMDownloadDelegate downloadDelegate) {
        return this.downloadAttachment(path, urlParams, null, localFile, downloadDelegate);
    }


    /**
     *
     * @param path The path to the Accela Construct API endpoint.
     * @param urlParams The collection of parameters associated with the specific URL.
     * @param localFile The path for file.
     * @return The AMRequest object corresponding to this Accela Construct API endpoint call.
     * @since 4.1
     */
    public AMRequest downloadAttachment(String path, RequestParams urlParams, Map<String, String> customHttpHeader, String localFile, AMDocDownloadRequest.AMDownloadDelegate downloadDelegate) {
        AMRequest amRequest = new AMRequest(AccelaMobile.getInstance().amApisHost + path, urlParams, AMRequest.HTTPMethod.GET);
        amRequest.setHttpHeader(customHttpHeader);
        return amRequest.downloadDocument(urlParams, localFile, downloadDelegate);
    }


    /**
     *
     * Start a batch request.
     *
     * @return An initialized AMBatchSession instance.
     *
     * @since 4.0
     */
    public static AMBatchSession batchBegin(){
        return new AMBatchSession();
    }


    /**
     *
     * Commit the currently started batch request.
     *
     * @param session The batch session instance.
     * @param batchRequestDelegate The delegate of batch session.
     *
     * @return An initialized AccelaMobile instance.
     *
     * @since 4.0
     */
    public static void batchCommit(AMBatchSession session, Map<String, String> customParams, AMBatchResponse.AMBatchRequestDelegate batchRequestDelegate){
        final AMBatchSession batchSession = session;
        final AMBatchResponse.AMBatchRequestDelegate batchRequestDelegate1 = batchRequestDelegate;
        AMRequestDelegate requestDelegate = new AMRequestDelegate() {
            @Override
            public void onStart() {}

            @Override
            public void onSuccess(JSONObject result) {
                AMBatchResponse response = new AMBatchResponse(result);
                List<JSONObject> childResponses = response.getResult();
                List<AMRequest> requests = batchSession.getRequests();

                if(response == null || childResponses.size()!= requests.size()){
                    batchRequestDelegate1.onSuccessful();
                    return;
                }

                for(int index = 0; index < requests.size(); index++){
                    AMRequest request = requests.get(index);
                    JSONObject childResponse = childResponses.get(index);
                    AMRequestDelegate rstDelegate = request.getRequestDelegate();
                    rstDelegate.onSuccess(childResponse);
                }

                if(batchRequestDelegate1 != null){
                    batchRequestDelegate1.onSuccessful();
                }
            }

            @Override
            public void onFailure(AMError error) {
                if(batchRequestDelegate1 != null){
                    batchRequestDelegate1.onFailed(error);
                }
            }
        };

        session.executeAsync(customParams, requestDelegate);
    }
}

package com.accela.mobile.http;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.accela.mobile.AMError;
import com.accela.mobile.AMLogger;
import com.accela.mobile.AMRequestDelegate;
import com.accela.mobile.http.volley.NetworkResponse;
import com.accela.mobile.http.volley.Response;
import com.accela.mobile.http.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by eyang on 8/20/15.
 */
public class AMRequestFactory {

    public static AMHttpRequest createJsonRequest(String url, int method, HashMap<String, String> customHttpHeader, String requestBody, boolean shouldCache,
                                              final AMRequestDelegate requestDelegate){
        AMHttpRequest jsonRequest = new AMJsonRequest
                (method, url, customHttpHeader, requestBody, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Object jsonResponse = response;
                        if(jsonResponse instanceof JSONObject) {
                            requestDelegate.onSuccess((JSONObject) jsonResponse);
                        } else if(jsonResponse instanceof JSONArray) {
                            requestDelegate.onSuccess((JSONArray) jsonResponse);
                        } else{
                            requestDelegate.onFailure(new AMError(200, null, null, "JsonHttpResponseHandler: unknown json type!", null));
                        }                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse response = error.networkResponse;
                        String traceId = "";
                        if (response==null){
                            requestDelegate.onFailure(new AMError(0, null, null, error.getLocalizedMessage(), null));
                            return;
                        }
                        if (response.headers!=null){
                            traceId = response.headers.get("x-accela-traceId");
                        }
                        requestDelegate.onFailure(new AMError(response.statusCode, null, traceId, error.getLocalizedMessage(), response.headers.toString()));
                    }
                });
        jsonRequest.setShouldCache(shouldCache);
        jsonRequest.setShouldCache(shouldCache);

        return jsonRequest;
    }

    public static AMHttpRequest createLoginRequest(String url, int method, HashMap<String, String> customHttpHeader, String requestBody, boolean shouldCache, final AMRequestDelegate requestDelegate) {
            AMHttpRequest loginRequest = new AMLoginRequest
                    (method, url, customHttpHeader, requestBody, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            Object jsonResponse = response;
                            if(jsonResponse instanceof JSONObject) {
                                requestDelegate.onSuccess((JSONObject) jsonResponse);
                            } else if(jsonResponse instanceof JSONArray) {
                                requestDelegate.onSuccess((JSONArray) jsonResponse);
                            } else{
                                requestDelegate.onFailure(new AMError(0, null, null, "JsonHttpResponseHandler: unknown json type!", null));
                            }                    }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            NetworkResponse response = error.networkResponse;
                            if (response==null){
                                requestDelegate.onFailure(new AMError(0, null, null, error.getLocalizedMessage(), null));
                                return;
                            }
                            if (response.headers==null){
                                requestDelegate.onFailure(new AMError(response.statusCode, null, null, error.getLocalizedMessage(), null));
                                return;
                            }
                            String traceId = response.headers.get("x-accela-traceId");
                            requestDelegate.onFailure(new AMError(response.statusCode, null, traceId, error.getLocalizedMessage(), response.headers.toString()));

                        }
                    });
            loginRequest.setShouldCache(shouldCache);
            loginRequest.setShouldCache(shouldCache);

            return loginRequest;

    }

    public static AMDocDownloadRequest createAMDocDownloadRequest(String url, HashMap<String, String> customHttpHeader, RequestParams requestParams, String localFilePath, final AMDocDownloadRequest.AMDownloadDelegate downloadDelegate){
        AMDocDownloadRequest request = null;
        try {
            String stringBody = null;
            if (requestParams!=null)
                    stringBody = requestParams.getStringBody();
            request = new AMDocDownloadRequest(url, customHttpHeader, stringBody, localFilePath, downloadDelegate);
        } catch (MalformedURLException e) {
            AMLogger.logError(e.toString());
        }
        return request;
    }

    public static AMMultiPartRequest createAMMultiPartRequests(String url, HashMap<String, String> customHttpHeader, RequestParams requestParams, final AMRequestDelegate requestDelegate){
        AMMultiPartRequest request = null;
        try {
            request = new AMMultiPartRequest(url, customHttpHeader, requestParams.getEntity(), requestDelegate);
        } catch (MalformedURLException e) {
            AMLogger.logError(e.toString());
        }
        return request;
    }

    static AMImageRequest createImageRequest(String url, Map<String, String> customHttpHeader, Response.Listener<Bitmap> listener, int maxWidth, int maxHeight, ImageView.ScaleType scaleType, Bitmap.Config decodeConfig,
                                                    Response.ErrorListener errorListener){
        AMImageRequest imageRequest = new AMImageRequest(url, customHttpHeader, listener, maxWidth, maxHeight, scaleType, decodeConfig, errorListener);
        return imageRequest;
    }
}

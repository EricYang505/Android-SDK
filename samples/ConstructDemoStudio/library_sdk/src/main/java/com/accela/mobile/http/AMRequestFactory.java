package com.accela.mobile.http;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.accela.mobile.AMError;
import com.accela.mobile.AMRequestDelegate;
import com.accela.mobile.http.volley.Request;
import com.accela.mobile.http.volley.Response;
import com.accela.mobile.http.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONObject;

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
                            requestDelegate.onFailure(new AMError(0, null, null, "JsonHttpResponseHandler: unknown json type!", null));
                        }                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                        requestDelegate.onFailure(new AMError(0, null, null, error.getLocalizedMessage(), null));
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

                            requestDelegate.onFailure(new AMError(0, null, null, error.getLocalizedMessage(), null));
                        }
                    });
            loginRequest.setShouldCache(shouldCache);
            loginRequest.setShouldCache(shouldCache);

            return loginRequest;

    }

    static AMImageRequest createImageRequest(String url, Map<String, String> customHttpHeader, Response.Listener<Bitmap> listener, int maxWidth, int maxHeight, ImageView.ScaleType scaleType, Bitmap.Config decodeConfig,
                                                    Response.ErrorListener errorListener){
        AMImageRequest imageRequest = new AMImageRequest(url, customHttpHeader, listener, maxWidth, maxHeight, scaleType, decodeConfig, errorListener);
        return imageRequest;
    }
}

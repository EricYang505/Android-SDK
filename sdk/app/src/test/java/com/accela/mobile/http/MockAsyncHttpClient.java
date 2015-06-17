package com.accela.mobile.http;

import android.content.Context;

import com.accela.mobile.AMLogger;
import com.accela.mobile.AMSetting;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jzhong on 6/17/15.
 */
public class MockAsyncHttpClient extends AsyncHttpClient {


    public MockAsyncHttpClient() {
        AMLogger.logInfo("Create MockAsyncHttpClient");

    }

    @Override
    protected void sendRequest(DefaultHttpClient client, HttpContext httpContext, HttpUriRequest uriRequest, String contentType, AsyncHttpResponseHandler responseHandler, Context context) {
        if (contentType != null) {
            uriRequest.addHeader("Content-Type", contentType);
        }
        if (AMSetting.DebugMode) {
            AMLogger.logInfo("In AsyncHttpClient.sendRequest(): uriRequest = %s",uriRequest.getURI());
        }

    }


    protected JSONObject sendRequest(DefaultHttpClient client, HttpUriRequest uriRequest, String contentType) {
        String status = "200";
        JSONObject jsonResponse = new JSONObject();
        JSONArray array = new JSONArray();
        try {
            for (int i = 0; i < 5; i++) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(String.format("%d", i), "json-object3");
                array.put(i, jsonObject);
            }
            jsonResponse.put("status", status);
            jsonResponse.put("result", array);
        } catch (JSONException e) {

        }
        return jsonResponse;
    }
}

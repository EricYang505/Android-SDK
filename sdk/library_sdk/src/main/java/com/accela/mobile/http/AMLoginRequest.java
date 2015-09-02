package com.accela.mobile.http;

import com.accela.mobile.http.volley.Response;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by eyang on 8/24/15.
 */
class AMLoginRequest extends AMHttpRequest {


    /** Content type for request. */
    private static final String PROTOCOL_CONTENT_TYPE = "application/x-www-form-urlencoded";


    public AMLoginRequest(int method, String url, HashMap<String, String> customHttpHeader, String stringRequestBody, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, customHttpHeader, stringRequestBody, listener, errorListener);
    }


    @Override
    public String getBodyContentType() {
        return PROTOCOL_CONTENT_TYPE;
    }

}

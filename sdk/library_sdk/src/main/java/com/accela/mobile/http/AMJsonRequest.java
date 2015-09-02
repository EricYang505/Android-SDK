package com.accela.mobile.http;

import com.accela.mobile.http.volley.Response;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by eyang on 8/20/15.
 */
class AMJsonRequest extends AMHttpRequest {

    /** Default charset for JSON request. */

    /** Content type for request. */
    private static final String PROTOCOL_CONTENT_TYPE = "application/json";


    public AMJsonRequest(int method, String url, HashMap<String, String> customHttpHeader, String stringRequestBody, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, customHttpHeader, stringRequestBody, listener, errorListener);
    }

    @Override
    public String getBodyContentType() {
        return PROTOCOL_CONTENT_TYPE;
    }
}


//Request.Method
//        int DEPRECATED_GET_OR_POST = -1;
//        int GET = 0;
//        int POST = 1;
//        int PUT = 2;
//        int DELETE = 3;
//        int HEAD = 4;
//        int OPTIONS = 5;
//        int TRACE = 6;
//        int PATCH = 7;
package com.accela.mobile.http;

import com.accela.mobile.http.volley.AuthFailureError;
import com.accela.mobile.http.volley.Cache;
import com.accela.mobile.http.volley.NetworkResponse;
import com.accela.mobile.http.volley.ParseError;
import com.accela.mobile.http.volley.Response;
import com.accela.mobile.http.volley.VolleyLog;
import com.accela.mobile.http.volley.toolbox.HttpHeaderParser;
import com.accela.mobile.http.volley.toolbox.JsonRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

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
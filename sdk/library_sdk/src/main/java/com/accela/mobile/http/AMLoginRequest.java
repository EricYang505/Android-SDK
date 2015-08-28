package com.accela.mobile.http;

import com.accela.mobile.http.volley.AuthFailureError;
import com.accela.mobile.http.volley.Cache;
import com.accela.mobile.http.volley.NetworkResponse;
import com.accela.mobile.http.volley.ParseError;
import com.accela.mobile.http.volley.Request;
import com.accela.mobile.http.volley.Response;
import com.accela.mobile.http.volley.VolleyLog;
import com.accela.mobile.http.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

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

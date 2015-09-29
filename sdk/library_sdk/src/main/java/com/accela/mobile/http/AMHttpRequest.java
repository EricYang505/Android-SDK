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
public class AMHttpRequest extends Request<JSONObject> {

    /** Default charset for JSON request. */
    protected static final String PROTOCOL_CHARSET = "utf-8";

    private final Response.Listener<JSONObject> mListener;

    private final String mRequestBody;

    private HashMap<String, String> customHttpHeader;

    private int responseStatus;

    private Map<String, String> responseHeader;


    public AMHttpRequest(int method, String url, HashMap<String, String> customHttpHeader, String stringRequestBody, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.customHttpHeader = customHttpHeader;
        mRequestBody = stringRequestBody;
        mListener = listener;
        this.setRetryPolicy(new AMRetryPolicy());
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return customHttpHeader;
    }


    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            responseHeader = response.headers;
            Cache.Entry entry = HttpHeaderParser.parseCacheHeaders(response);
            this.responseStatus = response.statusCode;
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
            return Response.success(new JSONObject(jsonString), entry);
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }

    public int getResponseStatus(){
        return responseStatus;
    }

    public Map<String, String> getResponseHeader() {
        return responseHeader;
    }

    @Override
    protected void deliverResponse(JSONObject response) {
        mListener.onResponse(response);
    }


    @Override
    public byte[] getBody() {
        try {
            return mRequestBody == null ? null : mRequestBody.getBytes(PROTOCOL_CHARSET);
        } catch (UnsupportedEncodingException uee) {
            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s",
                    mRequestBody, PROTOCOL_CHARSET);
            return null;
        }
    }

}

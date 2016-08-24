/**
 * Copyright 2015 Accela, Inc.
 *
 * You are hereby granted a non-exclusive, worldwide, royalty-free license to
 * use, copy, modify, and distribute this software in source code or binary
 * form for use in connection with the web services and APIs provided by
 * Accela.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 */
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
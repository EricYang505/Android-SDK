package com.accela.mobile.http;

/**
 * Created by eyang on 8/20/15.
 */
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.accela.mobile.AMError;
import com.accela.mobile.AMLogger;
import com.accela.mobile.AMSetting;




public abstract class AsyncHttpResponseHandler {

    /**
     * Fired when the request is started, override to handle in your own code.
     *
     * @return Void.
     *
     * @since 1.0
     */
    public abstract void onStart();



    /**
     * Fired in all cases when the request is finished, after both success and
     * failure, override to handle in your own code
     *
     * @return Void.
     *
     * @since 1.0
     */
    public void onFinish() {}

    public void onSuccess(JSONObject content) {}

    public void onSuccess(JSONArray content) {}

    public void onSuccess(Bitmap bitmap) {}
    /**
     * Fired when a request returns a string response successfully,
     * override to handle in your own code.
     *
     * @param content
     *            The string body of the HTTP response from the server.
     *
     * @return Void.
     *
     * @since 1.0
     */
    public void onSuccess(String content) {}

    /**
     * Fired when a request returns a byte array response successfully,
     * override to handle in your own code.
     *
     * @param content
     *            The byte array body of the HTTP response from the server.
     *
     * @return Void.
     *
     * @since 1.0
     */
    public void onSuccess(byte[] content) {}

    /**
     * Fired when a request fails to complete, override to handle in your own
     * code.
     *
     * @param error
     *            The underlying cause of the failure.
     *
     * @return Void.
     *
     * @since 1.0
     */
    public void onFailure(AMError error) {
    }


    /**
     * Fired when a request times out, override to handle in your own code.
     *
     * @param error
     *            The underlying cause of the failure.
     *
     * @return Void.
     *
     * @since 4.0
     */
    public void onTimeout() {}

}
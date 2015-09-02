package com.accela.mobile.http;

/**
 * Created by eyang on 8/20/15.
 */


import org.json.JSONArray;
import org.json.JSONObject;

import android.graphics.Bitmap;

import com.accela.mobile.AMError;


public abstract class AsyncHttpResponseHandler {

    /**
     * Fired when the request is started, override to handle in your own code.
     *
     * @return Void.
     *
     * @since 1.0
     */
    public abstract void onStart();

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

//    /**
//     * Fired when a request returns a byte array response successfully,
//     * override to handle in your own code.
//     *
//     * @param content
//     *            The byte array body of the HTTP response from the server.
//     *
//     * @return Void.
//     *
//     * @since 1.0
//     */
//    public void onSuccess(byte[] content) {}

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

}
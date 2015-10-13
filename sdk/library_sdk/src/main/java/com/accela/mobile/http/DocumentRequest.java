package com.accela.mobile.http;

import android.os.AsyncTask;

import com.accela.mobile.http.volley.NetworkResponse;
import com.accela.mobile.http.volley.ServerError;

import java.io.IOException;

/**
 * Created by eyang on 8/28/15.
 */
public interface DocumentRequest {

    public NetworkResponse request(AsyncTask asyncTask) throws IOException, ServerError;

    public void handleResponse(NetworkResponse networkResponse);

    public void cancel();
}

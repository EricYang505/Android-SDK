package com.accela.mobile.http;

import com.accela.mobile.http.volley.NetworkResponse;
import com.accela.mobile.http.volley.ServerError;

import java.io.IOException;

/**
 * Created by eyang on 8/28/15.
 */
public interface DocumentRequest {

    public NetworkResponse request() throws IOException, ServerError;

    public void handleResponse(NetworkResponse networkResponse);
}

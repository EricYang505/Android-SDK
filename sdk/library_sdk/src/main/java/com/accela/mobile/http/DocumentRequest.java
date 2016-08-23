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

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
package com.accela.mobile;

import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemClock;

import com.accela.mobile.http.DocumentRequest;
import com.accela.mobile.http.volley.NetworkResponse;
import com.accela.mobile.http.volley.ServerError;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by eyang on 8/26/15.
 */

/**
 *  Document request management class for binary files download and upload.
 *
 * @since 4.1
 */

public class AMDocRequestManager {
    public final static int IOEXCEPTION_ERROR = 1;
    public final static int SERVEREXCEPTION_ERROR = 2;
    private final static int DOWNLOAD_STATUS_LOADING = 128;
    private final static int DOWNLOAD_STATUS_IDLE = 256;
    private int mStatus = DOWNLOAD_STATUS_IDLE;
    private static AMDocRequestManager mInstance;
    private final BlockingQueue<DocumentRequest> mBlockingQueue = new ArrayBlockingQueue(128);

    private AMDocRequestManager(){}

    static synchronized AMDocRequestManager getAMDocumentManager(Context context) {
        if (mInstance == null) {
            mInstance = new AMDocRequestManager();
        }
        return mInstance;
    }
    /**
     *
     * Add task to the queue for further execute
     *
     * @param task based on DocumentRequest interface
     *
     * @return AMDocRequestManager.
     *
     * @since 4.1
     */
    AMDocRequestManager addRequest(DocumentRequest task){
        mBlockingQueue.add(task);
        return this;
    }
    /**
     *
     * Pulling task from BlockingQueue and execute one by one
     *
     *
     * @since 4.1
     */
    void startRequest(){
        if (mStatus == DOWNLOAD_STATUS_LOADING)
            return;
        if (!mBlockingQueue.isEmpty()){
            DocumentRequest task = mBlockingQueue.poll();
            new DocumentTask().execute(task);
        }
    }


    private class DocumentTask extends AsyncTask<DocumentRequest, Object, NetworkResponse> {
        DocumentRequest mTask;
        @Override
        protected NetworkResponse doInBackground(DocumentRequest... task) {
            mStatus = DOWNLOAD_STATUS_LOADING;
            NetworkResponse response = null;
            if (task!=null) {
                long requestStart = SystemClock.elapsedRealtime();
                try {
                    this.mTask = task[0];
                    response = mTask.request(this);
                } catch (IOException e) {
                    response = new NetworkResponse(IOEXCEPTION_ERROR, null, null, false,
                            SystemClock.elapsedRealtime() - requestStart);
                } catch (ServerError serverError) {
                    response = new NetworkResponse(SERVEREXCEPTION_ERROR, null, null, false,
                            SystemClock.elapsedRealtime() - requestStart);                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(NetworkResponse networkResponse) {
            mTask.handleResponse(networkResponse);
            mStatus = DOWNLOAD_STATUS_IDLE;
            startRequest(); // after finish a task, start to poll a new task again.
        }

    }


}

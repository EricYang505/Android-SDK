package com.accela.mobile;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Looper;
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
public class AMDocRequestManager {
    public final static int IOEXCEPTION_ERROR = 1;
    public final static int SERVEREXCEPTION_ERROR = 2;
    private final static int DOWNLOAD_STATUS_LOADING = 128;
    private final static int DOWNLOAD_STATUS_IDLE = 256;
    private int mStatus = DOWNLOAD_STATUS_IDLE;
    private static AMDocRequestManager mInstance;
    private final BlockingQueue<DocumentRequest> mBlockingQueue = new ArrayBlockingQueue(100);

    private AMDocRequestManager(){}

    public static synchronized AMDocRequestManager getAMDocumentManager(Context context) {
        if (mInstance == null) {
            mInstance = new AMDocRequestManager();
        }
        return mInstance;
    }

    public AMDocRequestManager addRequest(DocumentRequest task){
        mBlockingQueue.add(task);
        return this;
    }

    public void startRequest(){
//        if (Looper.myLooper() != Looper.getMainLooper())
//            throw new RuntimeException("Please send request on Main Thread!");
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
            startRequest();
        }

    }


}

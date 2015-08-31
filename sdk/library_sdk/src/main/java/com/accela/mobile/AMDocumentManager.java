package com.accela.mobile;

import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemClock;

import com.accela.mobile.http.AMMultiPartRequest;
import com.accela.mobile.http.DocumentRequest;
import com.accela.mobile.http.volley.Cache;
import com.accela.mobile.http.volley.Legacy.HttpStatus;
import com.accela.mobile.http.volley.NetworkResponse;
import com.accela.mobile.http.volley.ServerError;
import com.accela.mobile.http.volley.toolbox.HttpHeaderParser;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by eyang on 8/26/15.
 */
public class AMDocumentManager {
    public final static int IOEXCEPTION_ERROR = 1;
    public final static int SERVEREXCEPTION_ERROR = 2;

    private static AMDocumentManager mInstance;
    private final BlockingQueue<DocumentRequest> mBlockingQueue = new ArrayBlockingQueue(100);

    private AMDocumentManager(){}

    public static synchronized AMDocumentManager getAMDocumentManager(Context context) {
        if (mInstance == null) {
            mInstance = new AMDocumentManager();
        }
        return mInstance;
    }

    public AMDocumentManager addRequest(DocumentRequest task){
        mBlockingQueue.add(task);
        return this;
    }

    public void startRequest(){
        while (!mBlockingQueue.isEmpty()){
            DocumentRequest task = mBlockingQueue.poll();
            new DocumentTask().execute(task);
        }
    }


    private class DocumentTask extends AsyncTask<DocumentRequest, Object, NetworkResponse> {
        DocumentRequest mTask;
        @Override
        protected NetworkResponse doInBackground(DocumentRequest... task) {
            NetworkResponse response = null;
            if (task!=null) {
                long requestStart = SystemClock.elapsedRealtime();
                try {
                    this.mTask = task[0];
                    response = mTask.request();
                } catch (IOException e) {
                    new NetworkResponse(IOEXCEPTION_ERROR, null, null, false,
                            SystemClock.elapsedRealtime() - requestStart);
                } catch (ServerError serverError) {
                    new NetworkResponse(SERVEREXCEPTION_ERROR, null, null, false,
                            SystemClock.elapsedRealtime() - requestStart);                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(NetworkResponse networkResponse) {
            mTask.handleResponse(networkResponse);
        }

    }


}

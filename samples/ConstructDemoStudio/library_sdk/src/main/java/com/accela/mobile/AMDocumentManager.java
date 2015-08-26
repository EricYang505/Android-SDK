package com.accela.mobile;

import android.content.Context;
import android.os.AsyncTask;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by eyang on 8/26/15.
 */
public class AMDocumentManager {
    private static AMDocumentManager mInstance;
    private final BlockingQueue<Runnable> mBlockingQueue = new ArrayBlockingQueue(100);

    private AMDocumentManager(){}

    public static synchronized AMDocumentManager getAMDocumentManager(Context context) {
        if (mInstance == null) {
            mInstance = new AMDocumentManager();
        }
        return mInstance;
    }

    public AMDocumentManager addRequest(Runnable task){
        mBlockingQueue.add(task);
        return this;
    }

    public void performRequest(){
        if (!mBlockingQueue.isEmpty()){
            Runnable task = mBlockingQueue.poll();
            DocumentTask
        }
    }


    private class DocumentTask extends AsyncTask<Runnable, Object, Boolean> {


        protected Boolean doInBackground(Runnable... task) {

            return true;
        }

        protected void onPostExecute(Long result) {
            showDialog("Downloaded " + result + " bytes");
        }
    }


}

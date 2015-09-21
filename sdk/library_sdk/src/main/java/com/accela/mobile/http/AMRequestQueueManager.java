package com.accela.mobile.http;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.accela.mobile.http.volley.Cache;
import com.accela.mobile.http.volley.Network;
import com.accela.mobile.http.volley.Request;
import com.accela.mobile.http.volley.RequestQueue;
import com.accela.mobile.http.volley.toolbox.BasicNetwork;
import com.accela.mobile.http.volley.toolbox.DiskBasedCache;
import com.accela.mobile.http.volley.toolbox.HurlStack;
import com.accela.mobile.http.volley.toolbox.ImageLoader;
import com.accela.mobile.http.volley.toolbox.NoCache;

/**
 * Created by eyang on 8/20/15.
 */
public class AMRequestQueueManager {
    private static AMRequestQueueManager mInstance;
    private RequestQueue mRequestQueue;

    /** Number of network request dispatcher threads to start. */
    private static final int DEFAULT_NETWORK_THREAD_POOL_SIZE = 4;
    private static int mPoolSize = -1;
    private static int mDiskCacheSize = -1;

    private AMRequestQueueManager() {
        // getApplicationContext() is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        mRequestQueue = getRequestQueue();

        mRequestQueue.start();
    }

    public static synchronized AMRequestQueueManager buildAMRequestQueue(int netWorkThreadPoolSize, int diskCacheSize) {
        mPoolSize = netWorkThreadPoolSize;
        mDiskCacheSize = diskCacheSize;
        return buildAMRequestQueue();
    }


    public static synchronized AMRequestQueueManager buildAMRequestQueue() {
        if (mInstance == null) {
            mInstance = new AMRequestQueueManager();
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            Cache cache = new NoCache();
            Network network = new BasicNetwork(new HurlStack());
            // Instantiate the RequestQueue with the cache and network.
            mRequestQueue = new RequestQueue(cache, network, mPoolSize>0 ? mPoolSize : DEFAULT_NETWORK_THREAD_POOL_SIZE);
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

}

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

/**
 * Created by eyang on 8/20/15.
 */
public class AMRequestQueue {
    private static AMRequestQueue mInstance;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private static Context mCtx;

    /** Default maximum disk usage in bytes. */
    private static final int DEFAULT_DISK_USAGE_BYTES = 5 * 1024 * 1024;


    private AMRequestQueue(Context context) {
        // getApplicationContext() is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        mCtx = context.getApplicationContext();
        mRequestQueue = getRequestQueue();

        mImageLoader = new ImageLoader(mRequestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<String, Bitmap>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });
        mRequestQueue.start();
    }

    public static synchronized AMRequestQueue getAMRequestQueue(Context context) {
        if (mInstance == null) {
            mInstance = new AMRequestQueue(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            Cache cache = new DiskBasedCache(mCtx.getCacheDir(), DEFAULT_DISK_USAGE_BYTES);
            Network network = new BasicNetwork(new HurlStack());
            // Instantiate the RequestQueue with the cache and network.
            mRequestQueue = new RequestQueue(cache, network);
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }
}

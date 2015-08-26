package com.accela.mobile.http;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.util.LruCache;

import com.accela.mobile.http.volley.toolbox.ImageLoader;

/**
 * Created by eyang on 8/25/15.
 */
class AMLruCache extends LruCache<String, Bitmap> implements ImageLoader.ImageCache {

    public AMLruCache(int maxSize) {
        super(maxSize);
    }

    public AMLruCache(Context ctx) {
        this(getCacheSize(ctx));
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        return value.getRowBytes() * value.getHeight();
    }

    @Override
    public Bitmap getBitmap(String url) {
        return get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        put(url, bitmap);
    }

    // Returns a cache size equal to approximately three screens worth of images.
    public static int getCacheSize(Context ctx) {
        final DisplayMetrics displayMetrics = ctx.getResources().getDisplayMetrics();
        final int screenWidth = displayMetrics.widthPixels;
        final int screenHeight = displayMetrics.heightPixels;
        // 4 bytes per pixel
        final int screenBytes = screenWidth * screenHeight * 4;

        return screenBytes * 3;
    }
}

//        RequestQueue mRequestQueue; // assume this exists.
//        ImageLoader mImageLoader = new ImageLoader(mRequestQueue, new LruBitmapCache(
//        LruBitmapCache.getCacheSize()));
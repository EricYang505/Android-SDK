package com.accela.mobile.http;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.accela.mobile.AMError;
import com.accela.mobile.AMRequestDelegate;
import com.accela.mobile.http.volley.Request;
import com.accela.mobile.http.volley.RequestQueue;
import com.accela.mobile.http.volley.Response;
import com.accela.mobile.http.volley.VolleyError;
import com.accela.mobile.http.volley.toolbox.ImageLoader;

import java.util.Map;

/**
 * Created by eyang on 8/25/15.
 */
public class AMImageLoader extends ImageLoader {
    private static AMImageLoader mImageLoader;
    private Map<String, String> customHttpHeader;
    /**
     * Constructs a new ImageLoader.
     *
     * @param queue      The RequestQueue to use for making image requests.
     * @param imageCache The cache to use as an L1 cache.
     */
    private AMImageLoader(RequestQueue queue, ImageCache imageCache) {
        super(queue, imageCache);
    }

    public static synchronized AMImageLoader getAMImageLoader(Context context){
        if (mImageLoader==null){
            ImageCache imageCache = new AMLruCache(AMLruCache.getCacheSize(context));
            mImageLoader = new AMImageLoader(AMRequestQueueManager.buildAMRequestQueue().getRequestQueue(), imageCache);
        }
        return mImageLoader;
    }


    public void loadImage(String requestUrl, Map<String, String> customHttpHeader, final AMRequestDelegate amRequestDelegate){
        ImageListener imageListener = new ImageListener() {
            @Override
            public void onResponse(ImageContainer response, boolean isImmediate) {
                if(response.getBitmap()!=null)
                    amRequestDelegate.onSuccess(response.getBitmap());
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                amRequestDelegate.onFailure(new AMError(0, null, null, error.getMessage(), error.toString()));
            }
        };
        this.customHttpHeader = customHttpHeader;
        super.get(requestUrl, imageListener);
    }

    public void loadImage(String requestUrl, Map<String, String> customHttpHeader, final AMRequestDelegate amRequestDelegate, int maxWidth, int maxHeight){
        ImageListener imageListener = new ImageListener() {
            @Override
            public void onResponse(ImageContainer response, boolean isImmediate) {
                amRequestDelegate.onSuccess(response.getBitmap());
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                amRequestDelegate.onFailure(new AMError(0, null, null, error.getMessage(), error.toString()));
            }
        };
        this.customHttpHeader = customHttpHeader;
        super.get(requestUrl, imageListener, maxWidth, maxHeight);
    }

    public void loadImage(String requestUrl, Map<String, String> customHttpHeader, final AMRequestDelegate amRequestDelegate, int maxWidth, int maxHeight, ImageView.ScaleType scaleType){
        ImageListener imageListener = new ImageListener() {
            @Override
            public void onResponse(ImageContainer response, boolean isImmediate) {
                amRequestDelegate.onSuccess(response.getBitmap());
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                amRequestDelegate.onFailure(new AMError(0, null, null, error.getMessage(), error.toString()));
            }
        };
        this.customHttpHeader = customHttpHeader;
        super.get(requestUrl, imageListener, maxWidth, maxHeight, scaleType);
    }

    @Override
    protected Request<Bitmap> makeImageRequest(String requestUrl, int maxWidth, int maxHeight,
                                               ImageView.ScaleType scaleType, final String cacheKey) {
        return AMRequestFactory.createImageRequest(requestUrl, this.customHttpHeader, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                onGetImageSuccess(cacheKey, response);
            }
        }, maxWidth, maxHeight, scaleType, Bitmap.Config.RGB_565, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onGetImageError(cacheKey, error);
            }
        });
    }
}

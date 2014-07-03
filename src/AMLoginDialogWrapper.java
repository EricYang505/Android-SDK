package com.accela.mobile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * <pre>
 * 
 *  Accela Amobile
 *  File: AMLoginDialogWrapper.java
 * 
 *  Accela, Inc.
 *  Copyright (C): 2013
 * 
 *  Description:
 *  The native which wraps HTML login web view.
 * 
 *  Notes:
 * 	
 * 
 *  Revision History
 *  
 * 
 * @since 3.0
 * </pre>
 */

public class AMLoginDialogWrapper extends Dialog {
	/**
	 * Private WebViewClient class, used by the web view control.
	 */	
    private class DialogWebViewClient extends WebViewClient {        
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);          
            spinner.dismiss();            
            contentFrameLayout.setBackgroundColor(Color.TRANSPARENT);
            webView.setVisibility(View.VISIBLE);
            crossImageView.setVisibility(View.VISIBLE);		
        }        
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
        	super.onPageStarted(view, url, favicon);  
        	spinner.show();
        }
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        	super.onReceivedError(view, errorCode, description, failingUrl);  
        	// Dismiss spinner
            spinner.dismiss();            
            contentFrameLayout.setBackgroundColor(Color.TRANSPARENT);
            // Invoke session delegate
            AMError amError = new AMError(AMError.STATUS_CODE_OTHER,String.valueOf(errorCode),String.valueOf(errorCode), description, failingUrl);
            accelaMobile.sessionDelegate.amDidLoginFailure(amError);
        }        
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view, handler, error);
            // Dismiss spinner
            spinner.dismiss();            
            contentFrameLayout.setBackgroundColor(Color.TRANSPARENT);
            // Invoke session delegate
            AMError amError = new AMError(AMError.STATUS_CODE_OTHER,null,String.valueOf(AMError.ERROR_CODE_Bad_Request), error.toString(), null);
            accelaMobile.sessionDelegate.amDidLoginFailure(amError);
        } 
        
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
        	String authorizeSchemaUrl = accelaMobile.getUrlSchema() + "://authorize";            		
            if (url.startsWith(authorizeSchemaUrl)) { // Redirect to the predefined authorization schema.  
            	// Dismiss spinner
            	spinner.dismiss();            
	            contentFrameLayout.setBackgroundColor(Color.TRANSPARENT);
	            // Handle the redirected URL
            	Uri data = Uri.parse(url); 
	            Intent currentIntent = new Intent(Intent.ACTION_VIEW);		           
	            currentIntent.setData(data);	           
	            accelaMobile.authorizationManager.handleOpenURL(currentIntent);		
            } else { //  Redirect to the normal login URL. 
            	webView.loadUrl(url);            	
            }
            return true;
        }
    }
    private AccelaMobile accelaMobile;   
    private String url;
    private WebView webView;
    private ProgressDialog spinner;
    private ImageView crossImageView; 

    private FrameLayout contentFrameLayout;       
    
    /**
     * Constructor which can be used to display a dialog with an already-constructed URL and a custom theme.
     *
     * @param accelaMobile The AccelaMobile instance which presents this dialog.
     * @param url    The URL of the Web Dialog to display; no validation is done on this URL, but it should
     *                be a valid URL.
     *                
     * @return An initialized AMLoginDialogWrapper instance.
	 *
	 * @since 3.0
     */
    public AMLoginDialogWrapper(AccelaMobile accelaMobile, String url) {
    	 super(accelaMobile.ownerContext, android.R.style.Theme_Translucent_NoTitleBar);     	 
    	 this.accelaMobile = accelaMobile;
    	 this.url = url;
    }
  

	/**
	 * Private method, used to initialize the cross image(close button).
	 */	
    private void createCrossImage() {
        crossImageView = new ImageView(getContext());
        crossImageView.setOnClickListener(new View.OnClickListener() {            
            public void onClick(View v) {            	
                AMLoginDialogWrapper.this.dismiss();
            }
        });      
	    DisplayMetrics displayMetrics = new DisplayMetrics();   
	    ((Activity) accelaMobile.ownerContext).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
	    String closeImageStr = (displayMetrics.widthPixels > 540) ? CivicLoginDialogImageResource.close2xImageForWebView : CivicLoginDialogImageResource.closeImageForWebView;
        crossImageView.setImageDrawable((createDrawableFromBase64String(closeImageStr)));
        crossImageView.setVisibility(View.INVISIBLE);
     }

	/**
	 * Private method, used to create a drawable from a Base64 encoded string.
	 */	
	private Drawable createDrawableFromBase64String(String base64Str) {
		
		byte[] bytes = Base64.decode(base64Str, Base64.DEFAULT);		
		
		for (int i = 0; i < bytes.length; ++i) {
			if (bytes[i] < 0) {
				bytes[i] += 256;
			}
		}				
		return new BitmapDrawable(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
	}

    public void dismiss() {
    	 if (spinner.isShowing()) {
             spinner.dismiss();
         }    	 
        if (webView != null) {
            webView.stopLoading();
        }       
        super.dismiss();
    }
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        spinner = new ProgressDialog(getContext());
        spinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
        spinner.setMessage("Loading ...");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        contentFrameLayout = new FrameLayout(getContext());
        createCrossImage();
        int crossWidth = crossImageView.getDrawable().getIntrinsicWidth();
        setUpWebView(crossWidth / 2);         
        webView.loadUrl(url);           
        contentFrameLayout.addView(crossImageView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        addContentView(contentFrameLayout,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }    
    
    /**
	 * Private method, used to initialize the web view control.
	 */	
    @SuppressLint("SetJavaScriptEnabled")
    private void setUpWebView(int margin) {
    	LinearLayout webViewContainer = new LinearLayout(getContext());
    	webView = new WebView(getContext());
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setWebViewClient(new DialogWebViewClient());        
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        webView.setVisibility(View.INVISIBLE);
        webView.getSettings().setSavePassword(false);       
        webViewContainer.setPadding(margin, margin, margin, margin);
        webViewContainer.addView(webView);
        contentFrameLayout.addView(webViewContainer);
    }
}

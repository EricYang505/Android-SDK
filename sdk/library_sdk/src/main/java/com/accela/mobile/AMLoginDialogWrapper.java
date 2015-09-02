/** 
  * Copyright 2014 Accela, Inc. 
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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
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
 *  The native which wraps HTML login web view.
 * 
 * @since 3.0
 */

public class AMLoginDialogWrapper extends Dialog {
	private int colorBackground = 0x88000000;
	/**
	 * Private WebViewClient class, used by the web view control.
	 */	
    private class DialogWebViewClient extends WebViewClient {        
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);          
            spinner.dismiss();            
        //    contentFrameLayout.setBackgroundColor(Color.TRANSPARENT);
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
        //    contentFrameLayout.setBackgroundColor(Color.TRANSPARENT);
            // Invoke session delegate
            AMError amError = new AMError(AMError.STATUS_CODE_OTHER,String.valueOf(errorCode),String.valueOf(errorCode), description, failingUrl);
            accelaMobile.getAuthorizationManager().sessionDelegate.amDidLoginFailure(amError);
        }        
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view, handler, error);
            // Dismiss spinner
            spinner.dismiss();            
         //   contentFrameLayout.setBackgroundColor(Color.TRANSPARENT);
            // Invoke session delegate
            AMError amError = new AMError(AMError.STATUS_CODE_OTHER,null,String.valueOf(AMError.ERROR_CODE_Bad_Request), error.toString(), null);
            accelaMobile.getAuthorizationManager().sessionDelegate.amDidLoginFailure(amError);
        } 
        
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
        	String authorizeSchemaUrl = accelaMobile.getAuthorizationManager().mUrlSchema + "://authorize";
            if (url.startsWith(authorizeSchemaUrl)) { // Redirect to the predefined authorization schema.  
            	// Dismiss spinner
            	spinner.dismiss();            
	       //     contentFrameLayout.setBackgroundColor(Color.TRANSPARENT);
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
    private DisplayMetrics displayMetrics;

    private FrameLayout contentFrameLayout;       
    
    /**
     * Constructor which can be used to display a dialog with an already-constructed URL and a custom theme.
     *
     * @param url    The URL of the Web Dialog to display; no validation is done on this URL, but it should
     *                be a valid URL.
     *                
     * @return An initialized AMLoginDialogWrapper instance.
	 *
	 * @since 3.0
     */
    public AMLoginDialogWrapper(String url) {
    	super(AccelaMobile.getInstance().ownerContext, android.R.style.Theme_Translucent_NoTitleBar);
        this.accelaMobile = AccelaMobile.getInstance();
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
	    
        crossImageView.setImageDrawable(getContext().getResources().getDrawable(android.R.drawable.ic_menu_close_clear_cancel));
        crossImageView.setVisibility(View.INVISIBLE);
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
    
    private int convertDptoPixel(int dp) {
    	return displayMetrics.densityDpi * dp /160;
    }
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        displayMetrics = new DisplayMetrics();   
	    ((Activity) accelaMobile.ownerContext).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        spinner = new ProgressDialog(getContext());
        spinner.setCancelable(false);
        spinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
        spinner.setMessage("Loading ...");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        contentFrameLayout = new FrameLayout(getContext());
        createCrossImage();
        int crossWidth = convertDptoPixel(48);
        setUpWebView(crossWidth / 2);         
        webView.loadUrl(url); 
        
        contentFrameLayout.addView(crossImageView, new ViewGroup.LayoutParams(crossWidth, crossWidth));
        addContentView(contentFrameLayout,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        contentFrameLayout.setBackgroundColor(colorBackground);
        
    }    
    
    /**
	 * Private method, used to initialize the web view control.
	 */	
    @SuppressLint("SetJavaScriptEnabled")
    private void setUpWebView(int margin) {
    	LinearLayout webViewContainer = new LinearLayout(getContext());
    	webViewContainer.setBackgroundColor(Color.LTGRAY);
    	webView = new WebView(getContext());
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setWebViewClient(new DialogWebViewClient());        
        webView.getSettings().setJavaScriptEnabled(true);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(margin, margin, margin, margin);
        webView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        //webView.setVisibility(View.INVISIBLE);
        webView.getSettings().setSavePassword(false);
        webViewContainer.setPadding(convertDptoPixel(1), convertDptoPixel(1), convertDptoPixel(1), convertDptoPixel(1));
        webViewContainer.addView(webView);
        layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(margin, margin, margin, margin);
        contentFrameLayout.addView(webViewContainer, layoutParams);
    }
}

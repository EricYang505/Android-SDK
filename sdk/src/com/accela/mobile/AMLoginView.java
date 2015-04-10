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


import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.widget.PopupWindow;

/**
 *  The base class of native login dialog views.
 * 
 * @since 3.0
 */

public class AMLoginView extends PopupWindow {		
	
	protected AMLoginViewDelegate amLoginViewDelegate;
	protected String[] permissions;	
	protected String agency;	
	protected String environment;	
	protected AccelaMobile accelaMobileInternal;   
	
	protected Boolean isUserProfileRemebered = false;
	protected AMRequest currentRequest;
	protected Boolean isDevicePad = false;
	protected int screenWidth, screenHeight;	
	
	/**
	 * 
	 * Default constructor .
	 * 
	 * @param accelaMobileInternal The AccelaMobile which creates the login dialog.
	 * 
	 * @return An initialized AMLoginView instance.
	 * 
	 * @since 3.0
	 */	
	AMLoginView(AccelaMobile accelaMobileInternal) {		
		this(accelaMobileInternal.ownerContext);
		this.accelaMobileInternal = accelaMobileInternal;
		// init data
		this.agency = this.accelaMobileInternal.agency;
		this.environment = this.accelaMobileInternal.environment.name();
		// Get screen height and width
		DisplayMetrics displayMetrics = new DisplayMetrics();   
		((Activity) accelaMobileInternal.ownerContext).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		screenWidth = displayMetrics.widthPixels;
		screenHeight = displayMetrics.heightPixels;	
		// Check whether the current device is Pad or not (Phone)
		isDevicePad = isRunningOnTabletDevice();
		
		this.isUserProfileRemebered = accelaMobileInternal.amIsRemember;
	}
	
	/**
	 * 
	 * Constructor for Android use internally.
	 * 
	 * @return An initialized AMLoginView instance.
	 * 
	 * @since 3.0
	 */	
	AMLoginView(Context context) {		
		super(context);
	}		
	
	/**
	 * Protected method, used to create Drawable from a Base64 encoded string.
	 */	
	protected Drawable createDrawableFromBase64String(String base64Str) {
		
		byte[] bytes = Base64.decode(base64Str, Base64.DEFAULT);		
		
		for (int i = 0; i < bytes.length; ++i) {
			if (bytes[i] < 0) {
				bytes[i] += 256;
			}
		}				
		return new BitmapDrawable(accelaMobileInternal.ownerContext.getResources(), BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
	}
	
	/**
	 * Protected method, used to create container shape with round corners.
	 */	
	protected Drawable geRoundCornerRowShape(int width,int height, int color) {
		DisplayMetrics displayMetrics = new DisplayMetrics();   
		((Activity) accelaMobileInternal.ownerContext).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);			
		GradientDrawable g = new GradientDrawable();
		g.setGradientType(GradientDrawable.RECTANGLE);         
		g.setBounds(10, 0, 10, 0);
		g.setSize(displayMetrics.widthPixels, 20);		
		g.setCornerRadius(10);
		g.setColor(color);
		g.setStroke(2, Color.rgb(204, 204, 204)); // Dark gray

      return g;
	}
	
	/**
	 * 
	 * Get value of the property accelaMobileInternal
	 * 
	 * @return The value of property accelaMobileInternal  .
	 * 
	 * @since 3.0
	 */
	AccelaMobile getAccelaMobileInternal() {
		return this.accelaMobileInternal;
	}	
	
	/**
	 * Private method, used to detect whether the running device is a Tablet or not.
	 */	
	private Boolean isRunningOnTabletDevice() {
	    return (accelaMobileInternal.ownerContext.getResources().getConfiguration().screenLayout
	            & Configuration.SCREENLAYOUT_SIZE_MASK)
	            >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}	
}
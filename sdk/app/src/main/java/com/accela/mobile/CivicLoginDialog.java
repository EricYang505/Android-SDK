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

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;



/**
 *  The native civic login dialog view, which process civic authentication request to get access token.
 * 
 * @since 3.0
 */

class CivicLoginDialog extends AMLoginView  {			
	// Request URLs
	private static final String FACEBOOK_LOGIN_URL = "https://m.facebook.com/dialog/permissions.request";
	private static final String TWITTER_LOGIN_URL = "http://twitter.com/oauth/authenticate";	
	private static final String NEW_ACCOUNT_URI = "/User/ClientRegister/ClientRegister";	
	private static final String RESET_PASSWORD_URI = "/User/Password/Forgot";
	// Input field controls, field values, and buttons.
	private EditText etEmail,etPassword ;	
	private String valEmail, valPassword;
	private Button btnSignin, btnTwitter, btnFacebook;
	private TextView linkNewAccount, linkResetPassword;
	private LinearLayout contentViewLayout;
	private ScrollView scrollView;
	
	private Boolean isViewScrolled = false;
	private EditText focusedEditText;
		
	/**
	 * 
	 * Default constructor
	 * 
	 * @return An initialized CivicLoginDialog instance..
	 * 
	 * @since 3.0
	 */	
	CivicLoginDialog(AccelaMobile accelaMobileInternal) {
		super(accelaMobileInternal);		
		
		// Set view content.		
		if (isDevicePad)
		{
			setContentView(createContentView4Pad());		
		} else {
			setContentView(createContentView4Phone());		
		}
		// Set view size.	
		this.setWidth(screenWidth);		
		this.setHeight(screenHeight);
		// Initialize button events.
		initializeControlEvents();		
		// Enable keyboard input
		this.setFocusable(true);
		// Set dismiss listener
		this.setOnDismissListener(dismissListener);		
	}	
	
	/**
	 * 
	 * Constructor with context and session delegate.
	 * 
	 * @param accelaMobileInternal The AccelaMobile which creates the login dialog.
	 * @param permissions The array of access permissions. For example, search_records get_single_record  create_record
	 * @param sessionDelegate The session delegate which manager session lifecycle.
	 * 
	 * @return An initialized CivicLoginDialog instance.
	 * 
	 * @since 3.0
	 */	
	CivicLoginDialog(AccelaMobile accelaMobileInternal, String[] permissions) {		
		this(accelaMobileInternal);			
		this.permissions = permissions;
	}
		
	/**
	 * Private constructor, for Android use internally.
	 */	
	private CivicLoginDialog(Context context) {		
		super(context);
	}
	
	/**
	 * Private method, used to set OnClickListener for buttons and links.
	 */	
	private void initializeControlEvents() {
		
		final Context ownerContext = accelaMobileInternal.ownerContext;
		final String token = accelaMobileInternal.authorizationManager.getAccessToken();
		
		// Facebook login button
		btnFacebook.setOnClickListener(new Button.OnClickListener() {			
			public void onClick(View v) {		
				String facebookLoginWebUrl = FACEBOOK_LOGIN_URL +   
														"?appID=" + accelaMobileInternal.getAppId() + "&agency=" + accelaMobileInternal.authorizationManager.getAgency() +  "&schema=" + accelaMobileInternal.getUrlSchema() + "&tempid=" + token;
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(facebookLoginWebUrl));
				ownerContext.startActivity(intent);			
			}			
		});
		// Twitter login button	
		btnTwitter.setOnClickListener(new Button.OnClickListener() {			
			public void onClick(View v) {		
				String facebookLoginWebUrl = TWITTER_LOGIN_URL +   
														"?appID=" + accelaMobileInternal.getAppId() + "&agency=" + accelaMobileInternal.authorizationManager.getAgency() +  "&schema=" + accelaMobileInternal.getUrlSchema() + "&tempid=" + token;
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(facebookLoginWebUrl));
				ownerContext.startActivity(intent);			
			}			
		});
		
		// Sign in button
		btnSignin.setEnabled(false);
		btnSignin.getBackground().setAlpha(100);
		btnSignin.setOnClickListener(new Button.OnClickListener() {			
			public void onClick(View v) {
				if (isLoginInfoFilled()) {	
					hideKeyboard();				
					// Progress authorization request.
					accelaMobileInternal.authorizationManager.setClientInfo(accelaMobileInternal.getAppId(), accelaMobileInternal.getAppSecret(), accelaMobileInternal.authorizationManager.getEnvironment(), accelaMobileInternal.authorizationManager.getAgency(), accelaMobileInternal.amAuthHost, accelaMobileInternal.amApisHost);
					accelaMobileInternal.authorizationManager.getAuthorizeCode4Private(CivicLoginDialog.this, agency, valEmail, valPassword, permissions, true);
				} 					
			}			
		});
		
		// New Account link
		linkNewAccount.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {	
				String hostUrl = (accelaMobileInternal.amApisHost != null) ? accelaMobileInternal.amApisHost : AMSetting.AM_API_HOST;
				String newAccountWebUrl = hostUrl + NEW_ACCOUNT_URI +   
													"?appID=" + accelaMobileInternal.getAppId() + "&agency=" + accelaMobileInternal.authorizationManager.getAgency() +  "&schema=" + accelaMobileInternal.getUrlSchema() + "&tempid=" + token;
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(newAccountWebUrl));
				ownerContext.startActivity(intent);
			}
		});
		
		// Forget Password link
		linkResetPassword.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {	
				String resetPasswordWebUrl = accelaMobileInternal.amApisHost + RESET_PASSWORD_URI +   
														"?appID=" + accelaMobileInternal.getAppId() + "&agency=" + accelaMobileInternal.authorizationManager.getAgency() +  "&schema=" + accelaMobileInternal.getUrlSchema() + "&tempid=" + token;
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(resetPasswordWebUrl));
				ownerContext.startActivity(intent);
			}
		});		
		
		// Hide keyboard if user taps on the other area(not the input fields) in login dialog		
		contentViewLayout.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				hideKeyboard();
				v.performClick();
				return true;
			}	       
	    });
	}
	
	/**
	 * Private method, used to create the login dialog view for phone device.
	 */	
	private LinearLayout createContentView4Phone()
	{				
		Context ownerContext = accelaMobileInternal.ownerContext;
		int contentViewFrameWidth = 480-20*2;  // Minus left margin and right margin
		int contentViewFrameHeight =  840;   // Minus height of title bar
		contentViewLayout = new LinearLayout(ownerContext);					
		LinearLayout.LayoutParams contentViewLayoutParams = new LinearLayout.LayoutParams(contentViewFrameWidth, contentViewFrameHeight);
		contentViewLayout.setOrientation(LinearLayout.VERTICAL);	
		int hPadding = (screenWidth-contentViewFrameWidth)/2;	
		contentViewLayoutParams.setMargins(hPadding, 20, hPadding, 0);
		contentViewLayoutParams.gravity = Gravity.CENTER;
		LinearLayout.LayoutParams singleLineLayoutParams = new LinearLayout.LayoutParams(contentViewFrameWidth, LinearLayout.LayoutParams.WRAP_CONTENT);			
		singleLineLayoutParams.setMargins(0, 10, 0, 10);	
		
		// Social buttons section		
		LinearLayout.LayoutParams socialButtonLayoutParams = new LinearLayout.LayoutParams(contentViewFrameWidth, 102);
		socialButtonLayoutParams.setMargins(0, 10, 0, 0);
		btnFacebook = new Button(ownerContext);			
		btnFacebook.setBackground(createDrawableFromBase64String(CivicLoginDialogImageResource.facebookButtonImageBase64EncodedtStr4Phone));	
		
		btnTwitter = new Button(ownerContext);		
		btnTwitter.setBackground(createDrawableFromBase64String(CivicLoginDialogImageResource.twitterButtonImageBase64EncodedtStr4Phone));	
		contentViewLayout.addView(btnFacebook, socialButtonLayoutParams);	
		contentViewLayout.addView(btnTwitter, socialButtonLayoutParams);
		
		// The Or separator line
		LinearLayout.LayoutParams orSeparatorLayoutParams = new LinearLayout.LayoutParams(contentViewFrameWidth, LinearLayout.LayoutParams.WRAP_CONTENT);	
		orSeparatorLayoutParams.setMargins(0, 10, 0, 10);
		ImageView orSeparatorImage = new ImageView(ownerContext);
		orSeparatorImage.setBackground(createDrawableFromBase64String(CivicLoginDialogImageResource.orSeparatorLineImageBase64EncodedtStr4Phone));	
		contentViewLayout.addView(orSeparatorImage, orSeparatorLayoutParams);
		
		// Input fields
		LinearLayout loginSectionLayout = new LinearLayout(ownerContext);		
		loginSectionLayout.setOrientation(LinearLayout.VERTICAL);				
		LinearLayout.LayoutParams loginSectionLayoutParams = new LinearLayout.LayoutParams(contentViewFrameWidth, LinearLayout.LayoutParams.WRAP_CONTENT);	
		loginSectionLayoutParams.setMargins(0, 10, 0, 0);		
		loginSectionLayout.setBackground(geRoundCornerRowShape(loginSectionLayoutParams.width, 50, Color.WHITE));
		// Email field
		etEmail = new EditText(ownerContext);	
		etEmail.setTextSize(16);	
		etEmail.setBackgroundColor(Color.TRANSPARENT);
		etEmail.setHint("Email");
		etEmail.setTypeface(Typeface.SANS_SERIF);
		etEmail.setText(valEmail);
		etEmail.setSingleLine(true);		
		etEmail.setInputType(InputType.TYPE_CLASS_TEXT |InputType.TYPE_TEXT_VARIATION_PASSWORD);
		etEmail.setOnFocusChangeListener(focusChangeListener);
		etEmail.addTextChangedListener(textWatcher);
		// Password field
		etPassword = new EditText(ownerContext);
		etPassword.setTextSize(16);	
		etPassword.setBackgroundColor(Color.TRANSPARENT);
		etPassword.setHint("Password");
		etPassword.setTypeface(Typeface.SANS_SERIF);
		etPassword.setSingleLine(true);			
		etPassword.setInputType(InputType.TYPE_CLASS_TEXT |InputType.TYPE_TEXT_VARIATION_PASSWORD);		
		etPassword.setSelectAllOnFocus(true);	
		etPassword.setOnFocusChangeListener(focusChangeListener);
		etPassword.addTextChangedListener(textWatcher); 	
		
		// Field separator line
		LinearLayout.LayoutParams fieldSeparatorLayoutParams = new LinearLayout.LayoutParams(contentViewFrameWidth, 2);	
		ImageView fieldSeparatorImage = new ImageView(ownerContext);
		Drawable fieldSeparatorImageeDrawable = createDrawableFromBase64String(CivicLoginDialogImageResource.fieldSeparatorLineImageBase64EncodedtStr4Phone);
		fieldSeparatorImage.setBackground(fieldSeparatorImageeDrawable);		
		
		// Add fields into layout
		LinearLayout.LayoutParams inputFieldLayoutParams = new LinearLayout.LayoutParams(contentViewFrameWidth, LinearLayout.LayoutParams.MATCH_PARENT);
		loginSectionLayout.addView(etEmail, inputFieldLayoutParams);
		loginSectionLayout.addView(fieldSeparatorImage, fieldSeparatorLayoutParams);
		loginSectionLayout.addView(etPassword, inputFieldLayoutParams);		
		loginSectionLayout.setBackground(geRoundCornerRowShape(loginSectionLayoutParams.width, loginSectionLayoutParams.height, Color.WHITE));		
		contentViewLayout.addView(loginSectionLayout, loginSectionLayoutParams);
		
		// Login button		
		LinearLayout.LayoutParams loginButtonLayoutParams = new LinearLayout.LayoutParams(contentViewFrameWidth, 70);
		loginButtonLayoutParams.setMargins(5, 10, 10, 0);
		btnSignin = new Button(ownerContext);	
		btnSignin.setFocusable(true);
		btnSignin.setBackground(createDrawableFromBase64String(CivicLoginDialogImageResource.loginButtonImageBase64EncodedtStr4Phone));	

		contentViewLayout.addView(btnSignin, loginButtonLayoutParams);		
		
		// Help links section		
		linkNewAccount = new TextView(ownerContext);
		linkNewAccount.setTextSize(14);		
		linkNewAccount.setTextColor(Color.BLACK);
		linkNewAccount.setPaintFlags(linkNewAccount.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
		linkNewAccount.setText("Create a new account");	
		linkNewAccount.setTypeface(Typeface.SANS_SERIF);
		linkNewAccount.setGravity(Gravity.LEFT);		
		linkResetPassword = new TextView(ownerContext);
		linkResetPassword.setTextSize(14);	
		linkResetPassword.setTextColor(Color.BLACK);
		linkResetPassword.setPaintFlags(linkResetPassword.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
		linkResetPassword.setText("Forgot password?");	
		linkResetPassword.setTypeface(Typeface.SANS_SERIF);
		linkResetPassword.setGravity(Gravity.RIGHT);		
		
		RelativeLayout  helpLinksSectionLayout = new RelativeLayout(ownerContext);		
		RelativeLayout.LayoutParams helpLinksSectionLayouttParams = new RelativeLayout.LayoutParams(contentViewFrameWidth, contentViewFrameHeight/3);
		helpLinksSectionLayouttParams.setMargins(0, 20, 0, 20);
		helpLinksSectionLayout.setId(1002);
		RelativeLayout.LayoutParams accountLinkLayoutParams = new RelativeLayout.LayoutParams((contentViewFrameWidth - 40)/2, RelativeLayout.LayoutParams.WRAP_CONTENT);
		accountLinkLayoutParams.setMargins(10, 0, 0, 0);
		RelativeLayout.LayoutParams passwordLinkLayoutParams = new RelativeLayout.LayoutParams((contentViewFrameWidth - 40)/2, RelativeLayout.LayoutParams.WRAP_CONTENT);
		passwordLinkLayoutParams.setMargins(0, 0, 10, 0);
		accountLinkLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, helpLinksSectionLayout.getId());		
		accountLinkLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL, helpLinksSectionLayout.getId());
		passwordLinkLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, helpLinksSectionLayout.getId());		
		passwordLinkLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL, helpLinksSectionLayout.getId());	
		helpLinksSectionLayout.addView(linkNewAccount, accountLinkLayoutParams);	
		helpLinksSectionLayout.addView(linkResetPassword, passwordLinkLayoutParams);	
		contentViewLayout.addView(helpLinksSectionLayout, helpLinksSectionLayouttParams);	
		
		// Header logo image
		LinearLayout headerLogoLayout = new LinearLayout(ownerContext); 
		headerLogoLayout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams headerLogoLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 90);
		Drawable headerImageeDrawable = createDrawableFromBase64String(CivicLoginDialogImageResource.logoHeaderImageBase64EncodedtStr4Phone);
		headerLogoLayout.setBackground(headerImageeDrawable);
		TextView tvTitle = new TextView(ownerContext);
		tvTitle.setText("Accela Gov Platform");
		tvTitle.setTextSize(20);
		tvTitle.setTextColor(Color.WHITE);	
		tvTitle.setTypeface(Typeface.SANS_SERIF);
		tvTitle.setTypeface(null, Typeface.BOLD);
		tvTitle.setGravity(Gravity.CENTER);
		LinearLayout.LayoutParams headerTextLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
		headerTextLayoutParams.gravity = Gravity.CENTER;
		headerLogoLayout.addView(tvTitle, headerTextLayoutParams);
		
		// Frame view
		LinearLayout contentViewFrameLayout = new LinearLayout(ownerContext); 
		contentViewFrameLayout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams frameLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
		contentViewFrameLayout.setLayoutParams(frameLayoutParams);	
		contentViewFrameLayout.addView(contentViewLayout, contentViewLayoutParams);
		
		LinearLayout.LayoutParams scrollViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);	
		scrollView = new ScrollView(ownerContext);	
		scrollView.setFillViewport(true);	
		scrollView.addView(contentViewFrameLayout, scrollViewParams);	
		scrollView.pageScroll(View.FOCUS_UP);
		scrollView.setScrollContainer(true);
		
		// The whole window view
		LinearLayout windowLayout = new LinearLayout(ownerContext); 
		windowLayout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams windowLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
		windowLayout.setLayoutParams(windowLayoutParams);
		windowLayout.setBackgroundColor(Color.WHITE);
		
		windowLayout.addView(headerLogoLayout, headerLogoLayoutParams);	
		windowLayout.addView(scrollView, scrollViewParams);	
		
		// Return the window view	
		return windowLayout;
	}	
	
	/**
	 * Private method, used to create the login dialog view for tablet device.
	 */	
	private LinearLayout createContentView4Pad()
	{		
		Context ownerContext = accelaMobileInternal.ownerContext;
		// Main content view		
		int contentViewFrameWidth = 480-20*2;  // Minus left margin and right margin
		int contentViewFrameHeight = 800-20*2 - 90; // Minus top margin and bottom margin, and the height of title bar
		contentViewLayout = new LinearLayout(ownerContext);			
		LinearLayout.LayoutParams contentViewLayoutParams = new LinearLayout.LayoutParams(contentViewFrameWidth, contentViewFrameHeight);	
		int hPadding = (screenWidth-contentViewFrameWidth)/2;	
		int vPadding = (screenHeight-contentViewFrameHeight)/2;	
		contentViewLayoutParams.setMargins(hPadding, vPadding, hPadding, vPadding);
		contentViewLayoutParams.gravity = Gravity.CENTER;
		LinearLayout.LayoutParams singleLineLayoutParams = new LinearLayout.LayoutParams(contentViewFrameWidth,  LinearLayout.LayoutParams.WRAP_CONTENT);			
		singleLineLayoutParams.setMargins(0, 10, 0, 10);
		contentViewLayout.setOrientation(LinearLayout.VERTICAL);	
		
		// Title lable
		TextView signTitle = new TextView(ownerContext);
		signTitle.setTextSize(14);	
		contentViewLayout.addView(signTitle, singleLineLayoutParams);
		
		// Social buttons section		
		LinearLayout socialButtonSectionLayout = new LinearLayout(ownerContext);			
		LinearLayout.LayoutParams socialButtonSectionLayoutParams = new LinearLayout.LayoutParams(contentViewFrameWidth, LinearLayout.LayoutParams.WRAP_CONTENT);		
		socialButtonSectionLayoutParams.setMargins(10, 10, 10, 0);
		LinearLayout.LayoutParams socialButtonLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 54);
		socialButtonLayoutParams.weight = 1;
		
		btnFacebook = new Button(ownerContext);			
		btnFacebook.setBackground(createDrawableFromBase64String(CivicLoginDialogImageResource.facebookButtonImageBase64EncodedtStr4Pad));	
		btnTwitter = new Button(ownerContext);		
		btnTwitter.setBackground(createDrawableFromBase64String(CivicLoginDialogImageResource.twitterButtonImageBase64EncodedtStr4Pad));	
				
		socialButtonSectionLayout.addView(btnFacebook, socialButtonLayoutParams);	
		socialButtonSectionLayout.addView(btnTwitter, socialButtonLayoutParams);
		contentViewLayout.addView(socialButtonSectionLayout);		
		
		// The Or separator line
		LinearLayout.LayoutParams orSeparatorLayoutParams = new LinearLayout.LayoutParams(contentViewFrameWidth, 16);	
		orSeparatorLayoutParams.setMargins(0, 10, 0, 10);
		ImageView orSeparatorImage = new ImageView(ownerContext);
		Drawable orSeparatorImageDrawable = createDrawableFromBase64String(CivicLoginDialogImageResource.orSeparatorLineImageBase64EncodedtStr4Pad);
		orSeparatorImage.setBackground(orSeparatorImageDrawable);	
		contentViewLayout.addView(orSeparatorImage, orSeparatorLayoutParams);
		
		// Input fields
		LinearLayout loginSectionLayout = new LinearLayout(ownerContext);		
		loginSectionLayout.setOrientation(LinearLayout.VERTICAL);			
		LinearLayout.LayoutParams loginSectionLayoutParams = new LinearLayout.LayoutParams(contentViewFrameWidth, LinearLayout.LayoutParams.WRAP_CONTENT);	
		loginSectionLayoutParams.setMargins(0, 10, 0, 0);
		loginSectionLayoutParams.weight = 1;
		loginSectionLayout.setBackground(geRoundCornerRowShape(loginSectionLayoutParams.width, 50, Color.WHITE));
		
		etEmail = new EditText(ownerContext);	
		etEmail.setBackgroundColor(Color.TRANSPARENT);
		etEmail.setHint("Email");
		etEmail.setTypeface(Typeface.SANS_SERIF);
		etEmail.setText(valEmail);
		etEmail.clearFocus();
		etEmail.setSingleLine(true);		
		etEmail.addTextChangedListener(textWatcher);
		// Password field
		etPassword = new EditText(ownerContext);
		etPassword.setBackgroundColor(Color.TRANSPARENT);
		etPassword.setHint("Password");
		etPassword.setTypeface(Typeface.SANS_SERIF);
		etPassword.setSingleLine(true);			
		etPassword.setInputType(InputType.TYPE_CLASS_TEXT |InputType.TYPE_TEXT_VARIATION_PASSWORD);
		etPassword.addTextChangedListener(textWatcher); 		
		
		// Field separator line
		LinearLayout.LayoutParams fieldSeparatorLayoutParams = new LinearLayout.LayoutParams(contentViewFrameWidth, 2);	
		ImageView fieldSeparatorImage = new ImageView(ownerContext);
		Drawable fieldSeparatorImageeDrawable = createDrawableFromBase64String(CivicLoginDialogImageResource.fieldSeparatorLineImageBase64EncodedtStr4Pad);
		fieldSeparatorImage.setBackground(fieldSeparatorImageeDrawable);		
		
		// Add fields into layout
		LinearLayout.LayoutParams inputFieldLayoutParams = new LinearLayout.LayoutParams(contentViewFrameWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
		inputFieldLayoutParams.weight = 1;
		loginSectionLayout.addView(etEmail, inputFieldLayoutParams);
		loginSectionLayout.addView(fieldSeparatorImage, fieldSeparatorLayoutParams);
		loginSectionLayout.addView(etPassword, inputFieldLayoutParams);		
		loginSectionLayout.setBackground(geRoundCornerRowShape(loginSectionLayoutParams.width, loginSectionLayoutParams.height, Color.WHITE));		
		contentViewLayout.addView(loginSectionLayout, loginSectionLayoutParams);
		
		// Sign in button		
		LinearLayout.LayoutParams loginButtonLayoutParams = new LinearLayout.LayoutParams(contentViewFrameWidth, 52);
		loginButtonLayoutParams.setMargins(0, 10, 0, 20);
		btnSignin = new Button(ownerContext);		
		btnSignin.setBackground(createDrawableFromBase64String(CivicLoginDialogImageResource.loginButtonImageBase64EncodedtStr4Pad));
		btnSignin.setText("Sign In");
		btnSignin.setTextSize(20);
		btnSignin.setTextColor(Color.WHITE);	
		btnSignin.setTypeface(Typeface.SANS_SERIF);
		btnSignin.setTypeface(null, Typeface.BOLD);	
		
		contentViewLayout.addView(btnSignin, loginButtonLayoutParams);
		
		// Help links section		
		linkNewAccount = new TextView(ownerContext);
		linkNewAccount.setTextSize(14);	
		linkNewAccount.setTextColor(Color.BLACK);
		linkNewAccount.setPaintFlags(linkNewAccount.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
		linkNewAccount.setText("Create a new account");	
		linkNewAccount.setTypeface(Typeface.SANS_SERIF);
		linkNewAccount.setGravity(Gravity.LEFT);		
		linkResetPassword = new TextView(ownerContext);
		linkResetPassword.setTextSize(14);	
		linkResetPassword.setTextColor(Color.BLACK);
		linkResetPassword.setPaintFlags(linkNewAccount.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
		linkResetPassword.setText("Forgot password?");	
		linkResetPassword.setTypeface(Typeface.SANS_SERIF);
		linkResetPassword.setGravity(Gravity.RIGHT);		
		
		RelativeLayout  helpLinksSectionLayout = new RelativeLayout(ownerContext);		
		RelativeLayout.LayoutParams helpLinksSectionLayouttParams = new RelativeLayout.LayoutParams(contentViewFrameWidth, contentViewFrameHeight/3);
		helpLinksSectionLayout.setLayoutParams(helpLinksSectionLayouttParams);
		helpLinksSectionLayout.setId(1002);
		RelativeLayout.LayoutParams accountLinkLayoutParams = new RelativeLayout.LayoutParams((contentViewFrameWidth - 40)/2, RelativeLayout.LayoutParams.WRAP_CONTENT);
		accountLinkLayoutParams.setMargins(10, 0, 0, 0);
		RelativeLayout.LayoutParams passwordLinkLayoutParams = new RelativeLayout.LayoutParams((contentViewFrameWidth - 40)/2, RelativeLayout.LayoutParams.WRAP_CONTENT);
		passwordLinkLayoutParams.setMargins(0, 0, 10, 0);
		accountLinkLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, helpLinksSectionLayout.getId());		
		accountLinkLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL, helpLinksSectionLayout.getId());
		passwordLinkLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, helpLinksSectionLayout.getId());		
		passwordLinkLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL, helpLinksSectionLayout.getId());	
		helpLinksSectionLayout.addView(linkNewAccount, accountLinkLayoutParams);	
		helpLinksSectionLayout.addView(linkResetPassword, passwordLinkLayoutParams);	
		contentViewLayout.addView(helpLinksSectionLayout, singleLineLayoutParams);	
		
		// Header logo image
		LinearLayout headerLogoLayout = new LinearLayout(ownerContext); 
		LinearLayout.LayoutParams headerLogoLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,90);
		headerLogoLayoutParams.gravity = Gravity.CENTER;
		headerLogoLayoutParams.setMargins(0, 10, 0, 0);		
		Drawable headerrImageDrawable = createDrawableFromBase64String(CivicLoginDialogImageResource.logoHeaderImageBase64EncodedtStr4Pad);
		headerLogoLayout.setBackground(headerrImageDrawable);
		TextView tvTitle = new TextView(ownerContext);
		tvTitle.setText("Accela Gov Platform");
		tvTitle.setTextSize(20);
		tvTitle.setTextColor(Color.WHITE);
		tvTitle.setTypeface(Typeface.SANS_SERIF);
		tvTitle.setTypeface(null, Typeface.BOLD);
		tvTitle.setGravity(Gravity.CENTER);
		
		LinearLayout.LayoutParams headerTextLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 90);
		headerTextLayoutParams.gravity = Gravity.CENTER;
		headerTextLayoutParams.setMargins(0, 15, 0, 0);	
		headerLogoLayout.addView(tvTitle, headerTextLayoutParams);	
		
		// Scroll view
		LinearLayout.LayoutParams scrollViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);	
		scrollView = new ScrollView(ownerContext);	
		scrollView.setFillViewport(true);	
		scrollView.addView(contentViewLayout, contentViewLayoutParams);	
		scrollView.pageScroll(View.FOCUS_UP);
		scrollView.setScrollContainer(true);
		
		// The whole window view
		LinearLayout windowLayout = new LinearLayout(ownerContext); 
		windowLayout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams windowLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
		windowLayout.setLayoutParams(windowLayoutParams);
		windowLayout.setBackgroundColor(Color.WHITE);
		
		windowLayout.addView(headerLogoLayout, headerLogoLayoutParams);	
		windowLayout.addView(scrollView, scrollViewParams);	
		
		// Return the main layout		
		return windowLayout;
	}	
	
	/**
	 * Private method, used to check whether all the three input fields have been filled with values.
	 */	
	private Boolean isLoginInfoFilled() {
		valEmail = etEmail.getText().toString();
		valPassword = etPassword.getText().toString();		
		return (valEmail.length() > 0) && (valPassword.length() > 0);		
	}	
	
	/**
	 * Private method, used to hide keyboard.
	 */	
	private void hideKeyboard() {
		// Hide keyboard if it is showing.
		InputMethodManager mgr = (InputMethodManager) accelaMobileInternal.ownerContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (mgr.isActive()) {
			mgr.hideSoftInputFromWindow(etPassword.getWindowToken(), 0);
		}		
	}
	
	private PopupWindow.OnDismissListener dismissListener = new PopupWindow.OnDismissListener() {

		public void onDismiss() {			
			int scrollHeight = (focusedEditText == etEmail) ? -75 : -150;			
			scrollView.scrollBy(0, scrollHeight);				
		}
		
	};
	
	private View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
		public void onFocusChange(View v, boolean hasFocus) {
			if (!isViewScrolled)
			{
				isViewScrolled = true;
				return;
			}
			
			focusedEditText = (EditText)v;
			int scrollHeight = 0;
			if (v == etEmail)
			{				
				scrollHeight = hasFocus ? 75 : -75;
			} else {
				scrollHeight = hasFocus ? 150 : -150;
			}
			
			scrollView.scrollBy(0, scrollHeight);		
			
		}
		
	};
	
	/**
	 * Private method, used to set text change watcher for the three input fields.
	 * Thus the login button will become active only when all the three input fields hold values.
	 */	
	private TextWatcher textWatcher = new TextWatcher() {  
  
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {  
            // Nothing to do for now      
        }    

        public void onTextChanged(CharSequence s, int start, int before,  int count) {              
            if (isLoginInfoFilled()) {
            	btnSignin.setEnabled(true); 
            	btnSignin.getBackground().setAlpha(255);
            } else {
            	btnSignin.setEnabled(false);     
            	btnSignin.getBackground().setAlpha(100);
            }
        }

		public void afterTextChanged(Editable arg0) {
			// Nothing to do for now					
		}            
    };	
    
    
}
  



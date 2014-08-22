package com.accela.mobile;

import java.util.ResourceBundle;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
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
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;


/**
 * <pre>
 * 
 *  Accela Amobile
 *  File: AgencyLoginDialog.java
 * 
 *  Accela, Inc.
 *  Copyright (C): 2013
 * 
 *  Description:
 *  The native agency login dialog view, which process agency authentication request to get access token.
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

class AgencyLoginDialog extends AMLoginView  {			

	private EditText etAgency, etUsername, etPassword, etEnvironment ;	
	private String savedEnvironment, savedAgency, savedUsername;
	private String valEnvironment, valAgency, valUsername, valPassword;
	private Button btnSignin;
	private RelativeLayout addtlSettingLinkViewLayout;
	private ImageView envSeparatorImage, envArrowImage;
	private LinearLayout environmentRowLayout;
	
	private ResourceBundle stringLoader = AMSetting.getStringResourceBundle();
	
	
	
	/**
	 * 
	 * Default constructor
	 * 
	 * @param accelaMobileInternal The AccelaMobile which creates the login dialog.
	 * 
	 * @return An initialized AgencyLoginDialog instance.
	 * 
	 * @since 2.1
	 */	
	AgencyLoginDialog(AccelaMobileInternal accelaMobileInternal) {
		super(accelaMobileInternal);	
		// Initialize login information
		loadSavedLoginInfo();
		// Set content view.
		setContentView(createContentView());
		this.setWidth(screenWidth);		
		this.setHeight(screenHeight);		
		// Initialize button events.
		initializeControlEvents();		
		// Enable keyboard input
		this.setFocusable(true);	
	}	
	
	/**
	 * 
	 * Constructor with the given parameters.
	 * 
	 * @param accelaMobileInternal The AccelaMobile which creates the login dialog.
	 * @param permissions The array of access permissions. For example, search_records get_single_record  create_record, and etc.
	 * @param sessionDelegate The session delegate which manager session lifecycle.
	 * 
	 * @return An initialized AgencyLoginDialog instance.
	 * 
	 * @since 2.1
	 */	
	AgencyLoginDialog(AccelaMobileInternal accelaMobileInternal, String[] permissions, AMSessionDelegate sessionDelegate) {
		this(accelaMobileInternal);				
		this.permissions = permissions;
		this.sessionDelegate = sessionDelegate;
	}
	
	/**
	 * 
	 * Constructor with the given parameters.
	 * 
	 * @param accelaMobileInternal The AccelaMobile which creates the login dialog.
	 * @param permissions The array of access permissions. For example, search_records get_single_record  create_record, and etc.
	 * @param sessionDelegate The delegate which manager session lifecycle.
	 * @param loginViewDelegate The delegate which manager login dialog's lifecycle.
	 * 
	 * @return An initialized AgencyLoginDialog instance.
	 * 
	 * @since 4.0
	 */	
	AgencyLoginDialog(AccelaMobileInternal accelaMobileInternal, String[] permissions, AMSessionDelegate sessionDelegate, AMLoginViewDelegate loginViewDelegate) {
		this(accelaMobileInternal,permissions,sessionDelegate);				
		this.amLoginViewDelegate = loginViewDelegate;		
	}
	
	/**
	 * Private constructor, for Android use internally.
	 */	
	private AgencyLoginDialog(Context context) {		
		super(context);
	}
	
	/**
	 * Private method, used to create the content view.
	 */	
	private ScrollView createContentView()
	{		
		Context ownerContext = accelaMobileInternal.ownerContext;
		// Main content view		
		int contentViewFrameWidth = 480-20*2;  // Minus left margin and right margin
		int contentViewFrameHeight = 800-20*2; // Minus top margin and bottom margin
		LinearLayout contentViewLayout = new LinearLayout(ownerContext);					
		LinearLayout.LayoutParams contentViewLayoutParams = new LinearLayout.LayoutParams(contentViewFrameWidth, contentViewFrameHeight);
		contentViewLayout.setOrientation(LinearLayout.VERTICAL);	
		int hPadding = (screenWidth-contentViewFrameWidth)/2;	
		int vPadding = (screenHeight-contentViewFrameHeight)/2;	
		contentViewLayoutParams.setMargins(hPadding, vPadding, hPadding, vPadding);
		contentViewLayoutParams.gravity = Gravity.CENTER;
		
		boolean isTablet = (accelaMobileInternal.ownerContext.getResources().getConfiguration().screenLayout
	            & Configuration.SCREENLAYOUT_SIZE_MASK)
	            >= Configuration.SCREENLAYOUT_SIZE_LARGE;
		String logoImageStr = (isTablet) ? AgencyLoginDialogImageResource.logo2xImageBase64EncodedtStr : AgencyLoginDialogImageResource.logoImageBase64EncodedtStr;
		ImageView logoImage = new ImageView(ownerContext);
		Drawable logoDrawable = createDrawableFromBase64String(logoImageStr);
		logoImage.setBackgroundDrawable(logoDrawable);	
		LinearLayout.LayoutParams logoLayoutParams = new LinearLayout.LayoutParams(265, 71);
		logoLayoutParams.setMargins(0, 80, 0, 30);					
		logoLayoutParams.gravity = Gravity.CENTER;
		contentViewLayout.addView(logoImage, logoLayoutParams);				
		
		// Input fields
		LinearLayout loginSectionLayout = new LinearLayout(ownerContext);		
		loginSectionLayout.setOrientation(LinearLayout.VERTICAL);			
		LinearLayout.LayoutParams loginSectionLayoutParams = new LinearLayout.LayoutParams(contentViewFrameWidth, LinearLayout.LayoutParams.WRAP_CONTENT);	
		loginSectionLayoutParams.setMargins(0, 10, 0, 0);		
		LinearLayout.LayoutParams labelLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);	
		labelLayoutParams.setMargins(10, 0, 0, 0);
		labelLayoutParams.weight = 2;
		LinearLayout.LayoutParams fieldLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);	
		fieldLayoutParams.setMargins(0, 0, 10, 0);
		fieldLayoutParams.weight = 1;		
		LinearLayout agencyFieldLayout = new LinearLayout(ownerContext);
		agencyFieldLayout.setOrientation(LinearLayout.HORIZONTAL);
		agencyFieldLayout.setLayoutParams(loginSectionLayoutParams);
		TextView  agencyFieldLabel = new TextView(ownerContext);
		agencyFieldLabel.setTextSize(14);
		agencyFieldLabel.setTypeface(Typeface.SANS_SERIF);
		agencyFieldLabel.setText(stringLoader.getString("AgencyLogin_FieldLabel_Agency"));		
		agencyFieldLabel.setTextColor(Color.BLACK);
		agencyFieldLabel.setGravity(Gravity.CENTER_VERTICAL);
		etAgency = new EditText(ownerContext);
		etAgency.setTextColor(Color.BLACK);
		etAgency.setTextSize(14);
		etAgency.setTypeface(Typeface.SANS_SERIF);
		etAgency.setBackgroundColor(Color.TRANSPARENT);
		etAgency.setHint(stringLoader.getString("AgencyLogin_FieldHint_Agency"));
		etAgency.setSingleLine(true);		
		etAgency.setGravity(Gravity.CENTER_VERTICAL);		
		etAgency.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		agencyFieldLayout.addView(agencyFieldLabel, labelLayoutParams);
		agencyFieldLayout.addView(etAgency, fieldLayoutParams);
		
		LinearLayout usernameFieldLayout = new LinearLayout(ownerContext);
		usernameFieldLayout.setOrientation(LinearLayout.HORIZONTAL);
		usernameFieldLayout.setLayoutParams(loginSectionLayoutParams);
		TextView  usernameFieldLabel = new TextView(ownerContext);
		usernameFieldLabel.setTextSize(14);
		usernameFieldLabel.setTypeface(Typeface.SANS_SERIF);
		usernameFieldLabel.setTextColor(Color.BLACK);
		usernameFieldLabel.setText(stringLoader.getString("AgencyLogin_FieldLabel_Username"));	
		usernameFieldLabel.setGravity(Gravity.CENTER_VERTICAL);
		etUsername = new EditText(ownerContext);	
		etUsername.setTextColor(Color.BLACK);
		etUsername.setTextSize(14);
		etUsername.setTypeface(Typeface.SANS_SERIF);
		etUsername.setBackgroundColor(Color.TRANSPARENT);
		etUsername.setHint(stringLoader.getString("AgencyLogin_FieldHint_Username"));		
		etUsername.setSingleLine(true);		
		etUsername.setGravity(Gravity.CENTER_VERTICAL);
		etUsername.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		usernameFieldLayout.addView(usernameFieldLabel, labelLayoutParams);
		usernameFieldLayout.addView(etUsername, fieldLayoutParams);
		
		LinearLayout passwordFieldLayout = new LinearLayout(ownerContext);
		passwordFieldLayout.setOrientation(LinearLayout.HORIZONTAL);
		passwordFieldLayout.setLayoutParams(loginSectionLayoutParams);
		TextView  passwordFieldLabel = new TextView(ownerContext);
		passwordFieldLabel.setTextSize(14);
		passwordFieldLabel.setTypeface(Typeface.SANS_SERIF);
		passwordFieldLabel.setTextColor(Color.BLACK);
		passwordFieldLabel.setText(stringLoader.getString("AgencyLogin_FieldLabel_Password"));	
		passwordFieldLabel.setGravity(Gravity.CENTER_VERTICAL);
		etPassword = new EditText(ownerContext);
		etPassword.setTextColor(Color.BLACK);
		etPassword.setTextSize(14);
		etPassword.setTypeface(Typeface.SANS_SERIF);
		etPassword.setBackgroundColor(Color.TRANSPARENT);
		etPassword.setSingleLine(true);	
		etPassword.setHint(stringLoader.getString("AgencyLogin_FieldHint_Password"));		
		etPassword.setInputType(InputType.TYPE_CLASS_TEXT |InputType.TYPE_TEXT_VARIATION_PASSWORD);
		etPassword.setSelectAllOnFocus(true);
		etPassword.setGravity(Gravity.CENTER_VERTICAL);
		passwordFieldLayout.addView(passwordFieldLabel, labelLayoutParams);
		passwordFieldLayout.addView(etPassword, fieldLayoutParams);			
		
		loginSectionLayout.setBackgroundDrawable(geRoundCornerRowShape(loginSectionLayoutParams.width, loginSectionLayoutParams.height, Color.WHITE));
				
		TextView  environmentFieldLabel = new TextView(ownerContext);
		environmentFieldLabel.setTextSize(14);
		environmentFieldLabel.setId(1002);
		environmentFieldLabel.setTypeface(Typeface.SANS_SERIF);
		environmentFieldLabel.setTextColor(Color.BLACK);
		environmentFieldLabel.setText("Environment");	
		environmentFieldLabel.setGravity(Gravity.CENTER_VERTICAL);
		
		etEnvironment = new EditText(ownerContext);
		etEnvironment.setTextColor(Color.BLACK);
		etEnvironment.setTextSize(14);
		etEnvironment.setTypeface(Typeface.SANS_SERIF);
		etEnvironment.setBackgroundColor(Color.TRANSPARENT);
		etEnvironment.setFocusable(false);		
		
		envArrowImage = new ImageView(ownerContext);
		envArrowImage.setId(1004);
		envArrowImage.setBackgroundDrawable(createDrawableFromBase64String(AgencyLoginDialogImageResource.environmentArrowImageBase64EncodedtStr));

		RelativeLayout envFieldLayout = new RelativeLayout(ownerContext);
		RelativeLayout.LayoutParams envFieldLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		envFieldLayoutParams.setMargins(0, 0, 0, 0);
		envFieldLayout.setLayoutParams(envFieldLayoutParams);
		envFieldLayout.setId(1001);
		RelativeLayout.LayoutParams relativeLayoutParams1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		relativeLayoutParams1.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, envFieldLayout.getId());			
		relativeLayoutParams1.addRule(RelativeLayout.CENTER_VERTICAL, envFieldLayout.getId());
		relativeLayoutParams1.setMargins(10, 0, 0, 0);
		RelativeLayout.LayoutParams relativeLayoutParams2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		relativeLayoutParams2.addRule(RelativeLayout.ALIGN_PARENT_LEFT, envFieldLayout.getId());	
		relativeLayoutParams2.addRule(RelativeLayout.CENTER_VERTICAL, envFieldLayout.getId());
		relativeLayoutParams2.setMargins(0, 0, 10, 0);		
		envFieldLayout.addView(envArrowImage, relativeLayoutParams1);	
		envFieldLayout.addView(etEnvironment, relativeLayoutParams2);
		// The environment row which contains label, field, and arrow icon (hidden by default)		
		environmentRowLayout = new LinearLayout(ownerContext);	
		environmentRowLayout.addView(environmentFieldLabel, labelLayoutParams);
		environmentRowLayout.addView(envFieldLayout, fieldLayoutParams);		
		environmentRowLayout.setVisibility(View.GONE);
		LinearLayout.LayoutParams environmentFieldLayoutParams = new LinearLayout.LayoutParams(contentViewFrameWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
		environmentFieldLayoutParams.setMargins(10, 20, 10, 20);				
		// Field separator line
		LinearLayout.LayoutParams fieldSeparatorLayoutParams = new LinearLayout.LayoutParams(contentViewFrameWidth, 2);
		Drawable fieldSeparatorImageeDrawable = createDrawableFromBase64String(AgencyLoginDialogImageResource.fieldSeparatorLineImageBase64EncodedtStr);
		ImageView field1SeparatorImage = new ImageView(ownerContext);
		field1SeparatorImage.setBackgroundDrawable(fieldSeparatorImageeDrawable);	
		ImageView field2SeparatorImage = new ImageView(ownerContext);
		field2SeparatorImage.setBackgroundDrawable(fieldSeparatorImageeDrawable);
		// Hide the field separator line by default
		envSeparatorImage = new ImageView(ownerContext);
		envSeparatorImage.setBackgroundDrawable(fieldSeparatorImageeDrawable);
		envSeparatorImage.setVisibility(View.GONE);
		// Add fields into layout
		LinearLayout.LayoutParams inputFieldLayoutParams = new LinearLayout.LayoutParams(contentViewFrameWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
		loginSectionLayout.addView(agencyFieldLayout, inputFieldLayoutParams);
		loginSectionLayout.addView(field1SeparatorImage, fieldSeparatorLayoutParams);
		loginSectionLayout.addView(usernameFieldLayout, inputFieldLayoutParams);
		loginSectionLayout.addView(field2SeparatorImage, fieldSeparatorLayoutParams);
		loginSectionLayout.addView(passwordFieldLayout, inputFieldLayoutParams);		
		loginSectionLayout.addView(envSeparatorImage, fieldSeparatorLayoutParams);
		loginSectionLayout.addView(environmentRowLayout, inputFieldLayoutParams);		
		loginSectionLayout.setBackgroundDrawable(geRoundCornerRowShape(loginSectionLayoutParams.width, loginSectionLayoutParams.height, Color.WHITE));		
		contentViewLayout.addView(loginSectionLayout, loginSectionLayoutParams);
		// Additional Setting link		
		addtlSettingLinkViewLayout = new RelativeLayout(ownerContext);
		addtlSettingLinkViewLayout.setLayoutParams(loginSectionLayoutParams);
		TextView  addtlLinkLabel = new TextView(ownerContext);
		addtlLinkLabel.setText(stringLoader.getString("AgencyLogin_Button_AddtlSettings"));
		addtlLinkLabel.setTypeface(Typeface.SANS_SERIF);
		addtlLinkLabel.setTypeface(null, Typeface.BOLD_ITALIC);
		addtlLinkLabel.setTextColor(Color.WHITE);
		addtlLinkLabel.setPaintFlags(addtlLinkLabel.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
		RelativeLayout.LayoutParams addtlSettingLinkParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		addtlSettingLinkParams.setMargins(0, 0, 20, 0);
		addtlSettingLinkParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, addtlSettingLinkViewLayout.getId());						
		LinearLayout.LayoutParams addtlSettingLinkParamsLayoutParams = new LinearLayout.LayoutParams(contentViewFrameWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
		addtlSettingLinkParamsLayoutParams.setMargins(0, 20, 0, 20);
		addtlSettingLinkViewLayout.addView(addtlLinkLabel, addtlSettingLinkParams);	
		contentViewLayout.addView(addtlSettingLinkViewLayout, addtlSettingLinkParamsLayoutParams);
		// Populate values into input fields.
		etEnvironment.setText(valEnvironment);
		
		if ((savedAgency != null) && (savedUsername != null))
		{
			etAgency.setText(savedAgency);
			etUsername.setText(savedUsername);	
		}
		
		if (savedEnvironment != null) {
			etEnvironment.setText(savedEnvironment);
			contentViewLayout.removeView(addtlSettingLinkViewLayout);
			envSeparatorImage.setVisibility(View.VISIBLE);
			environmentRowLayout.setVisibility(View.VISIBLE);
		} else {
			etEnvironment.setText(accelaMobileInternal.getEnvironment().name());
		}
		// Sign in button		
		LinearLayout.LayoutParams loginButtonLayoutParams = new LinearLayout.LayoutParams(contentViewFrameWidth, 70);
		loginButtonLayoutParams.setMargins(0, 20, 0, 20);
		btnSignin = new Button(ownerContext);	
		btnSignin.setBackgroundDrawable(createDrawableFromBase64String(AgencyLoginDialogImageResource.loginButtonImageBase64EncodedtStr));
		contentViewLayout.addView(btnSignin, loginButtonLayoutParams);	
		// Frame view
		LinearLayout contentViewFrameLayout = new LinearLayout(ownerContext); 
		contentViewFrameLayout.setOrientation(LinearLayout.VERTICAL);		
		contentViewFrameLayout.addView(contentViewLayout, contentViewLayoutParams);
		LinearLayout.LayoutParams contentViewFrameLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);	
		ScrollView mainView = new ScrollView(ownerContext);	
		mainView.setFillViewport(true);	
		mainView.addView(contentViewFrameLayout, contentViewFrameLayoutParams);		
		mainView.setBackgroundDrawable(createDrawableFromBase64String(AgencyLoginDialogImageResource.bgImageBase64EncodedtStr));
		mainView.pageScroll(View.FOCUS_UP);		
		// Return the main layout		
		return mainView;		
	}	
	
	/**
	 * Private method, used to create a popup list view for environment selection.
	 */	
    private Dialog createEnvironmentDialog() {
    	final String cancelItemText = stringLoader.getString("AgencyLogin_ValueDialogCancel_Environment");
    	String environmentList = stringLoader.getString("AgencyLogin_ValueDialogList_Environment") + "," + cancelItemText;
		final String[] menuTitles = environmentList.split(",");

		Dialog envDialog = new AlertDialog.Builder(accelaMobileInternal.ownerContext).setTitle(stringLoader.getString("AgencyLogin_ValueDialogTitle_Environment"))
 			.setItems(menuTitles, new DialogInterface.OnClickListener() { 
	 			public void onClick(DialogInterface dialog, int which) { 
	 				String menuTitle =  menuTitles[which];
	 				if (! cancelItemText.equals(menuTitle))
	 				{
	 					AgencyLoginDialog.this.etEnvironment.setText(menuTitle);		
	 					valEnvironment = menuTitle;
	 				}
	 			} 
	        }).create();
		
		return envDialog;
    }		
		
	/**
	 * Private method, used to hide soft keyboard.
	 */	
	private void hideKeyboard() {
		// Hide keyboard if it is showing.
		InputMethodManager mgr = (InputMethodManager) accelaMobileInternal.ownerContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (mgr.isActive()) {
			mgr.hideSoftInputFromWindow(etPassword.getWindowToken(), 0);
		}	
	}	
	
	/**
	 * Private method, used to set OnClickListener events for buttons and links in the dialog view.
	 */	
	private void initializeControlEvents() {		
		
		TextWatcher textWatcher = new TextWatcher(){  			  
	        public void afterTextChanged(Editable arg0) {}
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void onTextChanged(CharSequence s, int start, int before,  int count) {              
	            if (isLoginInfoFilled()) {
	            	btnSignin.setEnabled(true); 
	            	btnSignin.getBackground().setAlpha(255);
	            	btnSignin.setFocusable(true);
	            } else {
	            	btnSignin.setEnabled(false);    
	            	btnSignin.getBackground().setAlpha(100);
	            	btnSignin.setFocusable(false);
	            }
	        }            
	    };	
		// Set text change watcher for text fields
		etAgency.addTextChangedListener(textWatcher); 		
		etUsername.addTextChangedListener(textWatcher);
		etPassword.addTextChangedListener(textWatcher); 	
		
		// Environment text field	
		etEnvironment.setOnClickListener(new Button.OnClickListener() {			
			public void onClick(View v) {		
				createEnvironmentDialog().show(); 			
			}			
		});
		// Environment right arrow	
		envArrowImage.setOnClickListener(new Button.OnClickListener() {			
			public void onClick(View v) {		
				createEnvironmentDialog().show(); 			
			}			
		});
		// Sign in button
		btnSignin.setEnabled(false); 
		btnSignin.getBackground().setAlpha(100);
    	btnSignin.setOnClickListener(new Button.OnClickListener() {			
			public void onClick(View v) {		
				if (isLoginInfoFilled()) {			
					// Hide keyboard.
					hideKeyboard();
					// Progress authorization request.						
					accelaMobileInternal.authenticate(valAgency, valUsername, valPassword, permissions);
					// Invoke login dialog delegate.
					if (amLoginViewDelegate != null) {
						amLoginViewDelegate.amDialogFetch(AgencyLoginDialog.this);
					}
					
				} 						
			}			
		});
		
		// Additional Settings link	
		addtlSettingLinkViewLayout.setOnClickListener(new Button.OnClickListener() {			
			public void onClick(View v) {		
				addtlSettingLinkViewLayout.setVisibility(View.GONE);
				
				envSeparatorImage.setVisibility(View.VISIBLE);
				environmentRowLayout.setVisibility(View.VISIBLE);	
			}			
		});
		
		// Hide keyboard if user taps on the other area(excluding the interactive elements defined above).
		this.getContentView().setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {	
				hideKeyboard();
				v.performClick();
				return true;
			}	       
	    });
	}
	
	/**
	 * Private method, used to check whether all input fields are filled with values.
	 */	
	private Boolean isLoginInfoFilled() {
		valAgency = etAgency.getText().toString();
		valUsername = etUsername.getText().toString();
		valPassword = etPassword.getText().toString();	
		return (valAgency.length() > 0) && (valUsername.length() > 0)  && (valPassword.length() > 0);		
	}  
    
    /**
	 * Private method, used to load the login information saved in local SharedPreferences file.
	 */	
	private void loadSavedLoginInfo() {	
		AuthorizationManager authorizationManager = accelaMobileInternal.authorizationManager;		
		savedEnvironment = authorizationManager.getValueFromSessionStore(AuthorizationManager.ENVIRONMENT_KEY_IN_PREF_FILE);
		savedAgency = authorizationManager.getValueFromSessionStore(AuthorizationManager.AGENCY_KEY_IN_PREF_FILE);
		savedUsername = authorizationManager.getValueFromSessionStore(AuthorizationManager.USER_KEY_IN_PREF_FILE);	
	}
}
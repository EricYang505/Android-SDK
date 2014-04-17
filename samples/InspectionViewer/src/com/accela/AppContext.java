package com.accela;

import android.app.Application;

import com.accela.mobile.AccelaMobile;

public class AppContext extends Application {
	public AccelaMobile accelaMobile;
	
	@Override
	public void onCreate() {
		super.onCreate();		
	}		
}

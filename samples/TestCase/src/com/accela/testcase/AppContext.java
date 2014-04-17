package com.accela.testcase;

import android.app.Application;
import com.accela.mobile.AccelaMobile;
import com.accela.mobile.AccelaMobileInternal;

public class AppContext extends Application {
	public AccelaMobile accelaMobile4Agency, accelaMobile4Citizen;
	public AccelaMobileInternal  accelaMobileInternal4Agency;
	
	@Override
	public void onCreate() {
		super.onCreate();		
	}		
}

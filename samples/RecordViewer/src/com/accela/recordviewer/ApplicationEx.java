package com.accela.recordviewer;

import android.app.Application;
import android.content.Context;

import com.accela.mobile.AccelaMobile;

public class ApplicationEx extends Application {
	private AccelaMobile accelaMobile;
	private RecordService recordService;

	@Override
	public void onCreate() {
		super.onCreate();		
	}		
	
	public AccelaMobile getAccelaMobile(Context activityContext) {
		if(accelaMobile == null) {
			//register your APP at "http://developer.accela.com" and replace the APP ID and secret here.
			accelaMobile = new AccelaMobile(activityContext, 
					"635524499989735717", "97ee045a67d34757bb9934553a75b4d1");
			
			accelaMobile.setEnvironment(AccelaMobile.Environment.PROD);
		}
		return accelaMobile;
	}
	
	public RecordService getRecordService() {
		if(recordService== null) {
			recordService = new RecordService(accelaMobile);
		}
		return recordService;
	}
	
	
}

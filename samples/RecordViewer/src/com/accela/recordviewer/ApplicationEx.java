/**
  * Copyright 2015 Accela, Inc.
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

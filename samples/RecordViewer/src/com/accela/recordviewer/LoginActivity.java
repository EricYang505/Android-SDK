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

import com.accela.mobile.AMError;
import com.accela.mobile.AMSessionDelegate;
import com.accela.mobile.AccelaMobile;
import com.accela.recordviewer.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class LoginActivity extends Activity {

	AccelaMobile accelaMobile;
	@Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        this.setContentView(R.layout.activity_login);
	        initAccelaMobile();

	        Button buttonLogin = (Button) findViewById(R.id.buttonLogin);
	        buttonLogin.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					login();
				}
			});

	 }

	private void initAccelaMobile() {
		accelaMobile = ((ApplicationEx) getApplication()).getAccelaMobile(this);
		accelaMobile.setSessionDelegate(sessionDelegate);

	}

	private void login() {

		String[] permissions = {"search_records", "get_records", "get_record", "batch_request"};
		accelaMobile.authorize2(permissions);

	}

	private void startRecordListActivity() {
		Intent intent = new Intent(LoginActivity.this, RecordListActivity.class);
		startActivity(intent);
		finish();
	}

	private AMSessionDelegate sessionDelegate = new AMSessionDelegate() {

		@Override
		public void amDidLogin() {
			// start record view activity
			startRecordListActivity();

		}

		@Override
		public void amDidLoginFailure(AMError error) {
			// TODO Auto-generated method stub
			Toast.makeText(LoginActivity.this, getString(R.string.login_failed), Toast.LENGTH_SHORT)
				.show();
		}

		@Override
		public void amDidCancelLogin() {
			// TODO Auto-generated method stub

		}

		@Override
		public void amDidSessionInvalid(AMError error) {
			// TODO Auto-generated method stub

		}

		@Override
		public void amDidLogout() {
			// TODO Auto-generated method stub

		}

	};


}

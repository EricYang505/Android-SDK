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
package com.accela.constructdemo;

import com.accela.testcase.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class HomeActivity extends Activity implements OnClickListener {
	
	private Button btnCitizenTest, btnAgencyTest;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.home);    
		
		// Set button event
		this.btnCitizenTest = (Button) this.findViewById(R.id.btnCitizenAppTest);
		this.btnAgencyTest = (Button) this.findViewById(R.id.btnAgencyAppTest);
		this.btnCitizenTest.setOnClickListener(this);
		this.btnAgencyTest.setOnClickListener(this);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		
		case R.id.btnCitizenAppTest:				
			startActivity(new Intent(this, CitizenTestActivity.class));
			this.finish();
			break;		  
		case R.id.btnAgencyAppTest:	
			startActivity(new Intent(this, AgencyTestActivity.class));
			this.finish();
		}
	}	
}

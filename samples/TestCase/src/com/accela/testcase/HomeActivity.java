package com.accela.testcase;

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

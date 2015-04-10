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

import com.accela.recordviewer.R;
import com.accela.recordviewer.model.RecordModel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class RecordDetailsActivity extends Activity {



	@Override
	protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        this.setContentView(R.layout.activity_record_details);

	        Intent intent = getIntent();
	        RecordModel recordModel = (RecordModel) intent.getSerializableExtra("record");
	        if(recordModel==null) {
	        	Toast.makeText(this, getString(R.string.record_empty),Toast.LENGTH_SHORT)
	        		.show();
	        	finish();
	        	return;
	        }


	        showRecordDetails(recordModel);



	 }

	private void showRecordDetails(RecordModel recordModel) {
		ViewGroup rootContainer = (ViewGroup) findViewById(R.id.rootContainer);

		LayoutInflater lf = LayoutInflater.from(this);

		View view;
		TextView text;

		//Title
		text = (TextView) rootContainer.findViewById(R.id.textTitle);
		text.setText(recordModel.type);

		//record ID
		view = lf.inflate(R.layout.record_details_item, null);
		text = (TextView) view.findViewById(R.id.textFieldName);
		text.setText(getString(R.string.record_id));

		text = (TextView) view.findViewById(R.id.textFieldValue);
		text.setText(recordModel.id);

		rootContainer.addView(view);

		//record Name
		view = lf.inflate(R.layout.record_details_item, null);
		text = (TextView) view.findViewById(R.id.textFieldName);
		text.setText(getString(R.string.record_name));

		text = (TextView) view.findViewById(R.id.textFieldValue);
		text.setText(recordModel.name);

		rootContainer.addView(view);

		//record status
		view = lf.inflate(R.layout.record_details_item, null);
		text = (TextView) view.findViewById(R.id.textFieldName);
		text.setText(getString(R.string.status));

		text = (TextView) view.findViewById(R.id.textFieldValue);
		text.setText(recordModel.status);

		rootContainer.addView(view);

		//record Open date
		view = lf.inflate(R.layout.record_details_item, null);
		text = (TextView) view.findViewById(R.id.textFieldName);
		text.setText(getString(R.string.open_date));

		text = (TextView) view.findViewById(R.id.textFieldValue);
		text.setText(recordModel.openedDate);

		rootContainer.addView(view);

		//record Description
		view = lf.inflate(R.layout.record_details_item, null);
		text = (TextView) view.findViewById(R.id.textFieldName);
		text.setText(getString(R.string.description));

		text = (TextView) view.findViewById(R.id.textFieldValue);
		text.setText(recordModel.description);

		rootContainer.addView(view);

		//record address
		if(recordModel.address!=null) {
			view = lf.inflate(R.layout.record_details_item, null);
			text = (TextView) view.findViewById(R.id.textFieldName);
			text.setText(getString(R.string.address));

			text = (TextView) view.findViewById(R.id.textFieldValue);
			text.setText(recordModel.address.getAddress());

			rootContainer.addView(view);
		}


	}


}

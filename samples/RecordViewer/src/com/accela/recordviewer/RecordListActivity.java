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

import java.util.List;


import com.accela.recordviewer.R;
import com.accela.recordviewer.RecordService.RecordServiceDeleagte;
import com.accela.recordviewer.fragment.MapViewFragment;
import com.accela.recordviewer.fragment.MapViewFragment.OnMakerClickListener;
import com.accela.recordviewer.fragment.RecordListFragment;
import com.accela.recordviewer.model.RecordModel;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;


public class RecordListActivity extends Activity implements RecordServiceDeleagte
	, RecordListFragment.OnListSelectListener, OnMakerClickListener {

	private final static String FRAGMENT_TAG_LISTVIEW = "listview";
	private final static String FRAGMENT_TAG_MAPVIEW = "mapview";

	RecordService recordService;

	ProgressDialog loadingView;

	ViewGroup rootContainer;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        this.setContentView(R.layout.activity_recordlist);
	        rootContainer = (ViewGroup) findViewById(R.id.rootContainer);

	        showRecordListView();

	        recordService = ((ApplicationEx) getApplication()).getRecordService();
	        recordService.setDelegate(this);
	        recordService.loadRecordAsyn(true);

	 }


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.getMenuInflater().inflate(R.menu.main_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_listview:
			showRecordListView();
			break;
		case R.id.action_mapview:
			showRecordMapView();
			break;

		}
		return super.onOptionsItemSelected(item);
	}

	private void showRecordListView() {
		Fragment fg = getFragmentManager().findFragmentByTag(FRAGMENT_TAG_LISTVIEW);
		if(fg!=null && fg instanceof RecordListFragment) {
			((RecordListFragment) fg).refresh();
		} else {
			FragmentTransaction ft = this.getFragmentManager().beginTransaction();
			RecordListFragment recordFg = new RecordListFragment();
			recordFg.setOnListSelectListener(this);
	        ft.replace(R.id.rootContainer, recordFg , FRAGMENT_TAG_LISTVIEW);
	        ft.commit();
		}

	}

	private void showRecordMapView() {
		Fragment fg = getFragmentManager().findFragmentByTag(FRAGMENT_TAG_MAPVIEW);
		if(fg!=null && fg instanceof MapViewFragment) {
			((MapViewFragment) fg).refresh();
		} else {

			MapViewFragment mapFg = new MapViewFragment();
			mapFg.setOnMakerClickListener(this);
			FragmentTransaction ft = this.getFragmentManager().beginTransaction();
	        ft.replace(R.id.rootContainer, mapFg, FRAGMENT_TAG_MAPVIEW);
	        ft.commit();
		}
	}

	@Override
	public void onLoadStart() {

		loadingView = ProgressDialog.show(this.rootContainer.getContext(),
				null, getString(R.string.loading_record), false, false);

	}

	@Override
	public void onLoadSuccess() {
		loadingView.dismiss();
		Fragment fg = getFragmentManager().findFragmentByTag(FRAGMENT_TAG_LISTVIEW);
		if(fg!=null && fg instanceof RecordListFragment) {
			((RecordListFragment) fg).refresh();
		}

		fg = getFragmentManager().findFragmentByTag(FRAGMENT_TAG_MAPVIEW);
		if(fg!=null && fg instanceof MapViewFragment) {
			((MapViewFragment) fg).refresh();
		}
	}

	@Override
	public void onLoadFailed() {
		loadingView.dismiss();
		// TODO Auto-generated method stub
		AlertDialog dialog = new AlertDialog.Builder(this).create();
		dialog.setMessage(getText(R.string.load_record_failed));
		dialog.show();

	}


	@Override
	public void onItemSelected(int id) {
		showRecordDetails(id);
	}


	@Override
	public void onRecordSelected(int id) {
		// TODO Auto-generated method stub
		showRecordDetails(id);
	}

	private void showRecordDetails(int id) {
		List<RecordModel> list = recordService.getRecordList();
		if(id >= list.size() ) {
			return;
		}
		RecordModel record = list.get(id);
		Intent intent = new Intent(this, RecordDetailsActivity.class);
		intent.putExtra("record", record);

		startActivity(intent);
	}






}

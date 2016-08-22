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
package recordviewer.accela.com.recordviewer.fragment;


import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import recordviewer.accela.com.recordviewer.R;
import recordviewer.accela.com.recordviewer.RecordService;
import recordviewer.accela.com.recordviewer.model.RecordModel;


public class MapViewFragment extends Fragment implements
		GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {

	private MapView mapView;
	private GoogleMap map;
	private RecordService recordService;

	OnMakerClickListener makerClickListener;
	private ArrayList<Marker> markerList = new ArrayList<Marker>();

	 public interface  OnMakerClickListener{
	        /**
	         * Callback for when an item has been selected.
	         */
	        public void onRecordSelected(int id);
	 }


	Handler handler = new Handler() {

	};


    public MapViewFragment() {

    }

    public void setOnMakerClickListener(OnMakerClickListener l) {
    	makerClickListener = l;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mapview,container, false);



		this.recordService = RecordService.getInstance();
        mapView = (MapView) view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        setupMap();
        addMakerToMap();
        return view;
    }

    private boolean checkMap() {
    	if(map == null) {
    		map = mapView.getMap();
    		if(map!=null) {
    			MapsInitializer.initialize(this.getActivity());
    		}
    	}
    	return map!=null;

    }

    private void setupMap() {
    	if(!checkMap()) {
    		return;
    	}
    	map.getUiSettings().setZoomControlsEnabled(false);
    	map.setOnMarkerClickListener(this);
    	map.setOnInfoWindowClickListener(this);

    }

    private void addMakerToMap() {
    	if(!checkMap()) {
    		return;
    	}
    	map.clear();
    	markerList.clear();
    	final LatLngBounds.Builder builder = new LatLngBounds.Builder();
        int markerCount = 0;
    	List<RecordModel> listRecords = recordService.getRecordList();
    	for(RecordModel record: listRecords) {
    		if(record.address==null)
    			continue;
    		MarkerOptions options = new MarkerOptions();
    		LatLng latlng = new LatLng(record.address.xCoordinate, record.address.yCoordinate);
    		options.position(latlng);
    		options.title(record.type);
    		options.snippet(record.address.getAddress());

    		Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.mipmap.report_graffiti);
    		options.icon(BitmapDescriptorFactory.fromBitmap(bitmap
    				));

    		markerCount++;
    		Marker marker = map.addMarker(options);
    		markerList.add(marker);
    		builder.include(latlng);
    	}


    	if(markerCount>0) {
    		handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					map.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 200));
				}

        	}, 800);

    	}

    }

    @Override
	public void onInfoWindowClick(Marker marker) {
    	if(makerClickListener!=null) {
    		int index = markerList.indexOf(marker);
    		makerClickListener.onRecordSelected(index);
    	}

	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		// TODO Auto-generated method stub
		return false;
	}



	public void refresh() {
	//	setupMap();
        addMakerToMap();
	}

	@Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        checkMap();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
    	mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

}

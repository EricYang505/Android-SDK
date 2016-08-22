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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import recordviewer.accela.com.recordviewer.R;
import recordviewer.accela.com.recordviewer.RecordService;
import recordviewer.accela.com.recordviewer.model.RecordModel;


public class RecordListFragment extends Fragment  {

	private OnListSelectListener listSelectListener;
	private ListView listviewRecord;
	private AdapterRecordList adapter;
	private RecordService recordService;
    public interface  OnListSelectListener{
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(int id);
    }

    public RecordListFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recordlist,container, false);

        recordService = RecordService.getInstance();

        listviewRecord = (ListView) view.findViewById(R.id.listviewRecord);


        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, 120);

        FrameLayout layout = new FrameLayout(getActivity());

        layout.setLayoutParams(lp);
        Button buttonMore = new Button(this.getActivity());

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        buttonMore.setLayoutParams(params);
        buttonMore.setText("More...");
        layout.addView(buttonMore);

        buttonMore.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				recordService.loadRecordAsyn(false);
			}
		});
        listviewRecord.addFooterView(layout);

        /*
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, 150);

        TextView mHeaderView = new TextView(this.getActivity());
        mHeaderView.setTextColor(Color.WHITE);
        mHeaderView.setBackgroundColor(Color.BLUE);
        mHeaderView.setGravity(Gravity.CENTER);
        mHeaderView.setLayoutParams(lp);
        mHeaderView.setText("Header");

        TextView mFooterView = new TextView(this.getActivity());
        mFooterView.setTextColor(Color.WHITE);
        mFooterView.setBackgroundColor(Color.BLUE);
        mFooterView.setGravity(Gravity.CENTER);
        mFooterView.setLayoutParams(lp);
        mFooterView.setText("Footer");
*/

        //listviewRecord.addHeaderView(mHeaderView);
        //listviewRecord.addFooterView(mFooterView);


        listviewRecord.setAdapter(adapter = new AdapterRecordList());

        listviewRecord.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(listSelectListener!=null) {
					listSelectListener.onItemSelected(position);
				}

			}

		});
        return view;
    }



    public void setOnListSelectListener(OnListSelectListener l) {
    	listSelectListener = l;
    }


    class AdapterRecordList extends BaseAdapter {

    	@Override
    	public int getCount() {
    		// TODO Auto-generated method stub
    		return recordService.getRecordList().size();
    	}

    	@Override
    	public Object getItem(int position) {
    		// TODO Auto-generated method stub
    		return null;
    	}

    	@Override
    	public long getItemId(int position) {
    		// TODO Auto-generated method stub
    		return 0;
    	}

    	@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
    		// TODO Auto-generated method stub
    		if(convertView == null) {
    			convertView = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_record, null);
    		}
    		RecordModel record = recordService.getRecordList().get(position);
    		TextView textView = (TextView) convertView.findViewById(R.id.textViewIndex);
    		textView.setText(String.format("%d", position + 1));
    		textView = (TextView) convertView.findViewById(R.id.textViewType);
    		textView.setText(record.type);
    		textView = (TextView) convertView.findViewById(R.id.textViewAddress);
    		if(record.address!=null) {
    			textView.setText(record.address.getAddress());
    		} else {
    			textView.setText("Unknown address");
    		}

    		return convertView;
    	}

    }

	public void refresh() {
		if(adapter!=null) {
			adapter.notifyDataSetChanged();
		}
	}

}

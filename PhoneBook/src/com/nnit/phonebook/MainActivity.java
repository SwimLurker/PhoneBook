package com.nnit.phonebook;

import java.util.ArrayList;
import java.util.List;

import com.nnit.phonebook.data.IPBDataSet;
import com.nnit.phonebook.data.JSONPBDataSource;
import com.nnit.phonebook.data.PhoneBookField;
import com.nnit.phonebook.data.PhoneBookItem;
import com.nnit.phonebook.ui.PhoneBookListAdapter;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class MainActivity extends Activity {

	public static final String SELECTED_PBITEM = "com.nnit.phonebook.SELECTED_PBITEM";
	private List<PhoneBookItem> pbItems = null;
	public static boolean isDetailList = true;
	private ListView briefList = null;
	private ListView detailList = null;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        
        setContentView(R.layout.activity_main);
        
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
        
        
        ImageButton detailListBtn = (ImageButton) findViewById(R.id.imageBtn_ListDetail);
        detailListBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				isDetailList = true;
				updateLayout();
			}
        });
        
        ImageButton briefListBtn = (ImageButton) findViewById(R.id.imageBtn_ListBrief);
        briefListBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				isDetailList = false;
				updateLayout();
			}      	
        });
               
        briefListBtn.requestFocus();
        
        TextView tv = (TextView) findViewById(R.id.textview);
        ListView lv1 = (ListView) findViewById(R.id.brief_list);
        ListView lv2 = (ListView) findViewById(R.id.detail_list);
       
        
        try{
        	pbItems = getPhoneBook();
        	
        	tv.setText("PhoneBook record number:" + pbItems.size());
//        	ArrayList<String> a = new ArrayList<String>();
//        	int len = pbItems.size();
//        	for(int i=0;i<len ;i++){
//        		a.add(pbItems.get(i).getInitials());
//        	}
//        	ArrayAdapter listItemAdapter = new ArrayAdapter (this,
//        			R.layout.brief_list_item,
//        			R.id.textView1,
//        			a.toArray());
        	lv1.setAdapter(new PhoneBookListAdapter(this, pbItems));
        	
        	
        	lv2.setAdapter(new PhoneBookListAdapter(this, pbItems));
        	
        }catch(Exception exp){
        	exp.printStackTrace();
        	tv.setText("Load PhoneBook Error:" + exp.getMessage());
        }
       
        lv1.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				Intent intent = new Intent();
				intent.putExtra(SELECTED_PBITEM, pbItems.get(position));
				intent.setAction("com.nnit.phonebook.DetailActivity");
				startActivity(intent);
			}
        	
        });
        
        
        lv2.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				Intent intent = new Intent();
				intent.putExtra(SELECTED_PBITEM, pbItems.get(position));
				intent.setAction("com.nnit.phonebook.DetailActivity");
				startActivity(intent);
			}
        	
        });
        
        lv2.setVisibility(View.GONE);
        
    }
    

    private void showMyDialog(String str) {
        new AlertDialog.Builder(this).setIcon(R.drawable.ic_launcher).setTitle("PhoneBook").setMessage(str).setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,
                    int whichButton) {
               
            }
        }).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
       
        return true;
    }
    
    private List<PhoneBookItem> getPhoneBook() throws Exception{
    	JSONPBDataSource ds =  new JSONPBDataSource();
		ds.setJsonFilePath("iNNIT.json");
		List<PhoneBookItem> result = null;
		
		IPBDataSet set = ds.getDataSet();
		return set.getPBItems();
		//return set.filter(PhoneBookField.INITIALS, "cqdi").getPBItems();
		
    }
    
    private void updateLayout(){
    	if(isDetailList){
    		if(detailList == null){
    			detailList = (ListView) findViewById(R.id.detail_list);
    		}
    		detailList.setVisibility(View.VISIBLE);
    		detailList.setAdapter(new PhoneBookListAdapter(this, pbItems));
    		
    		if(briefList == null){
    			briefList = (ListView) findViewById(R.id.brief_list);
    		}
    		briefList.setVisibility(View.GONE);
    	}else{
    		if(briefList == null){
    			briefList = (ListView) findViewById(R.id.brief_list);
    		}
    		briefList.setVisibility(View.VISIBLE);
    		briefList.setAdapter(new PhoneBookListAdapter(this, pbItems));
    		
    		if(detailList == null){
    			detailList = (ListView) findViewById(R.id.detail_list);
    		}
    		detailList.setVisibility(View.GONE);
    	}
    }
    
}

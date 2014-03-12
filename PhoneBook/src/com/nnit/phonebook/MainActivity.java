package com.nnit.phonebook;

import java.util.ArrayList;
import java.util.List;

import com.nnit.phonebook.data.IPBDataSet;
import com.nnit.phonebook.data.JSONPBDataSource;
import com.nnit.phonebook.data.PhoneBookField;
import com.nnit.phonebook.data.PhoneBookItem;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class MainActivity extends ListActivity {

	private List<PhoneBookItem> pbItems = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        TextView tv = (TextView) findViewById(R.id.textview);
        ListView lv = (ListView) this.getListView();
        try{
        	pbItems = getPhoneBook();
        	tv.setText("PhoneBook record number:" + pbItems.size());
        	ArrayList<String> a = new ArrayList<String>();
        	int len = pbItems.size();
        	for(int i=0;i<len ;i++){
        		a.add(pbItems.get(i).getInitials());
        	}
        	ArrayAdapter listItemAdapter = new ArrayAdapter (this,
        			R.layout.listview_item,
        			R.id.textView1,
        			a.toArray());
        	lv.setAdapter(listItemAdapter);
        }catch(Exception exp){
        	exp.printStackTrace();
        	tv.setText("Load PhoneBook Error:" + exp.getMessage());
        }
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        showMyDialog("You click " + pbItems.get(position).getInitials());
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
		ds.setJsonFilePath("sdcard/iNNIT.json");
		List<PhoneBookItem> result = null;
		
		IPBDataSet set = ds.getDataSet();
		return set.getPBItems();
		//return set.filter(PhoneBookField.INITIALS, "cqdi").getPBItems();
		
    }
    
}

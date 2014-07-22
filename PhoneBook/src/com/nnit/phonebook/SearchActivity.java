package com.nnit.phonebook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

public class SearchActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_search);
		
		ImageButton closeBtn = (ImageButton)findViewById(R.id.searcher_closebtn);
		closeBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				SearchActivity.this.finish();
				//kill the process
		    	android.os.Process.killProcess(android.os.Process.myPid());
			}
			
		});
		
		
		
		ImageButton searchBtn = (ImageButton)findViewById(R.id.searcher_searchbtn);
		searchBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				TextView initialsTV = (TextView)findViewById(R.id.searcher_initials);
				String initials = initialsTV.getText().toString();
				
				Intent intent = new Intent();
				intent.putExtra(MainActivity.SEARCH_INITIALS, initials);
				intent.setAction("com.nnit.phonebook.MainActivity");
				startActivity(intent);
				
				SearchActivity.this.finish();
			}
			
		});
		
		
	}
	
	@Override
	public void onBackPressed() {  	
    	finish();
    	//kill the process
    	android.os.Process.killProcess(android.os.Process.myPid());
    }
}

package com.nnit.phonebook;

import com.nnit.phonebook.data.PhoneBookItem;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailActivity extends Activity{
	private PhoneBookItem pbItem = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		
		pbItem = (PhoneBookItem) getIntent().getSerializableExtra(MainActivity.SELECTED_PBITEM);
		
		String initials = pbItem.getInitials().toLowerCase();
		ImageView photoIV = (ImageView) findViewById(R.id.detail_photo);
		int id = getResources().getIdentifier(initials, "drawable", "com.nnit.phonebook");
		if(id != 0){
			photoIV.setImageResource(id);
		}
		
		TextView initialTV = (TextView) findViewById(R.id.detail_initials);
		initialTV.setText(pbItem.getInitials());
		
		TextView nameTV = (TextView) findViewById(R.id.detail_name);
		nameTV.setText(pbItem.getName());
		
		TextView localnameTV = (TextView) findViewById(R.id.detail_localname);
		localnameTV.setText(pbItem.getLocalName());
		
		TextView genderTV = (TextView) findViewById(R.id.detail_gender);
		String gender = "Unknown";
		if(pbItem.getGender() == PhoneBookItem.GENDER.MALE){
			gender = "Male";
		}else if(pbItem.getGender() == PhoneBookItem.GENDER.FEMALE){
			gender = "Female";
		}
		genderTV.setText(gender);
		
		TextView mobileTV = (TextView) findViewById(R.id.detail_mobile);
		mobileTV.setText(pbItem.getMobile());
		
		TextView phoneTV = (TextView) findViewById(R.id.detail_phone);
		phoneTV.setText(pbItem.getPhone());
		
		TextView titleTV = (TextView) findViewById(R.id.detail_title);
		titleTV.setText(pbItem.getTitle());
		
		TextView depNoTV = (TextView) findViewById(R.id.detail_departmentNo);
		depNoTV.setText(pbItem.getDepartmentNo());
		
		TextView depTV = (TextView) findViewById(R.id.detail_department);
		depTV.setText(pbItem.getDepartment());
		
		TextView managerTV = (TextView) findViewById(R.id.detail_manager);
		managerTV.setText(pbItem.getManager());
		
		Button closeBtn = (Button) findViewById(R.id.detail_closebtn);
		closeBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				finish();				
			}
			
		});
		
	}

}

package com.nnit.phonebook;

import com.nnit.phonebook.data.PhoneBookField;
import com.nnit.phonebook.data.PhoneBookItem;
import com.nnit.phonebook.data.SeatMapInfo;
import com.nnit.phonebook.db.SeatMapInfoDAO;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailActivity extends Activity{
	
	public static final String TARGET_INITIALS = "com.nnit.phonebook.TARGET_INITIALS";
	
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
		
		Button callBtn = (Button) findViewById(R.id.detail_callbtn);
		callBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Dialog dialog = new AlertDialog.Builder(DetailActivity.this)
		        	.setIcon(R.drawable.ic_launcher)
		        	.setTitle("Do you want to make the call?")
		        	.setPositiveButton("OK",new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent();
						    intent.setAction("android.intent.action.DIAL");
						    intent.setData(Uri.parse("tel:"+pbItem.getMobile()));
						    startActivity(intent);
						    dialog.dismiss();
						}
					})
		        	.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();							
						}
					})
		        	.show();
				
			}
			
		});
		
		Button mapBtn = (Button) findViewById(R.id.detail_mapbtn);
		mapBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				
				String initials = pbItem.getInitials();
				SeatMapInfo seatInfo = getSeatInfo(initials);
				if(seatInfo == null){
					Dialog dialog = new AlertDialog.Builder(DetailActivity.this)
		        	.setIcon(R.drawable.ic_launcher)
		        	.setTitle("Can not find map info for initial:" + initials)
		        	.setPositiveButton("Close",new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					})
		        	.show();
				}else{
					Intent intent = new Intent();
					intent.putExtra(TARGET_INITIALS, initials);
					intent.setAction("com.nnit.phonebook.MapActivity");
					startActivity(intent);
				}
				
			}
			
		});
	}
	
	private SeatMapInfo getSeatInfo(String initials) {	
		SeatMapInfoDAO dao = new SeatMapInfoDAO();
		return dao.querySeatMapInfo(initials);
	}

}

package com.nnit.phonebook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.nnit.phonebook.data.PhoneBookField;
import com.nnit.phonebook.data.PhoneBookItem;
import com.nnit.phonebook.data.PhotoManager;
import com.nnit.phonebook.data.SeatMapInfo;
import com.nnit.phonebook.db.SeatMapInfoDAO;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailActivity extends Activity{
	
	public static final String TARGET_INITIALS = "com.nnit.phonebook.TARGET_INITIALS";
	
	private PhoneBookItem pbItem = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		
		setContentView(R.layout.activity_detail);
		
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar_detail);
		
		pbItem = (PhoneBookItem) getIntent().getSerializableExtra(MainActivity.SELECTED_PBITEM);
		
		String initials = pbItem.getInitials().toLowerCase();
		ImageView photoIV = (ImageView) findViewById(R.id.detail_photo);
		

		FileInputStream fis = null;
		try{
			String photoFilename = PhotoManager.getInstance().getPhotoFilenameByInitials(initials);
			if(photoFilename == null){
				photoIV.setImageResource(R.drawable.photo);
			}
			File f = new File(photoFilename);
		
			if(f.exists() && f.isFile()){
				fis = new FileInputStream(f);
				Bitmap bitmap = BitmapFactory.decodeStream(fis);
				photoIV.setImageBitmap(bitmap);
			}else{
				photoIV.setImageResource(R.drawable.photo); 
			}			
			
		}catch(Exception exp){
			photoIV.setImageResource(R.drawable.photo); 
		}finally{
			if(fis != null){
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
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
		
		ImageButton closeBtn = (ImageButton) findViewById(R.id.imagebtn_closedetail);
		closeBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				finish();				
			}
			
		});
		
		ImageButton callBtn = (ImageButton) findViewById(R.id.imagebtn_call);
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
		
		ImageButton mapBtn = (ImageButton) findViewById(R.id.imagebtn_map);
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

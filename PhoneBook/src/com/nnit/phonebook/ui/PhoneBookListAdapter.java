package com.nnit.phonebook.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import com.nnit.phonebook.DetailActivity;
import com.nnit.phonebook.MainActivity;
import com.nnit.phonebook.R;
import com.nnit.phonebook.data.FavoriteManager;
import com.nnit.phonebook.data.PhoneBookItem;












import com.nnit.phonebook.data.PhotoManager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PhoneBookListAdapter extends BaseAdapter{
	private List<PhoneBookItem> pbItems = null;
	private LayoutInflater mInflater = null;
	private Context context = null;
	
	public PhoneBookListAdapter(Context c, List<PhoneBookItem> items){
		this.context = c;
		this.pbItems = items;
		mInflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	@Override
	public int getCount() {
		return pbItems == null?0: pbItems.size();
	}

	@Override
	public Object getItem(int position) {
		return pbItems == null? null : pbItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		PhoneBookItem pb = pbItems.get(position);
		if (MainActivity.isDetailList){
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.listitem_detaillist, parent, false);								
			}
			TextView tv1 = (TextView)convertView.findViewById(R.id.detailList_Initials);
			tv1.setText(pb == null? null: pb.getInitials() + "(" + pb.getName() + ")");
			TextView tv2 = (TextView)convertView.findViewById(R.id.detailList_Name);
			tv2.setText(pb == null? null: pb.getTitle() + "," + pb.getDepartmentNo() + " " + pb.getDepartment());
			ImageView iv = (ImageView)convertView.findViewById(R.id.detailList_Photo);
			
			ImageButton removeFavoriteBtn = (ImageButton)convertView.findViewById(R.id.detaillist_removefromfavorite);
			if(MainActivity.showFavorite){
				removeFavoriteBtn.setVisibility(View.VISIBLE);
			}else{
				removeFavoriteBtn.setVisibility(View.GONE);
			}
			
			final Context context = convertView.getContext();
			final String targetInitials = pb.getInitials();
			final ViewGroup vg = parent;
			removeFavoriteBtn.setOnClickListener(new OnClickListener(){
				
				@Override
				public void onClick(View arg0) {
					Dialog dialog = new AlertDialog.Builder(context)
		        	.setIcon(R.drawable.ic_launcher)
		        	.setTitle("Do you want to remove it from your favorite list?")
		        	.setPositiveButton("OK",new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if(!FavoriteManager.getInstance().removeFromFavoriteList(targetInitials)){
								Toast.makeText(context, "Save favorite list info failed", Toast.LENGTH_SHORT).show();
							}
							for(int i = 0; i<pbItems.size(); i++){
								PhoneBookItem pbi = pbItems.get(i);
								if(pbi.getInitials().equalsIgnoreCase(targetInitials)){
									pbItems.remove(i);
									break;
								}
							}
							PhoneBookListAdapter.this.notifyDataSetChanged();
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
			
			FileInputStream fis = null;
			Resources resources = context.getResources();
			String initials = (pb == null ? null: pb.getInitials().toLowerCase());
			
			try{
				String photoFilename = PhotoManager.getInstance().getPhotoFilenameByInitials(initials);
				if(photoFilename == null){
					iv.setImageResource(R.drawable.photo);
				}
				File f = new File(photoFilename);
			
				if(f.exists() && f.isFile()){
					fis = new FileInputStream(f);
					Bitmap bitmap = BitmapFactory.decodeStream(fis);
					iv.setImageBitmap(bitmap);
				}else{
					iv.setImageResource(R.drawable.photo); 
				}			
				
			}catch(Exception exp){
				iv.setImageResource(R.drawable.photo); 
			}finally{
				if(fis != null){
					try {
						fis.close();
					} catch (IOException e) {
					}
				}
			}
			
//			int id = context.getResources().getIdentifier(initials, "drawable", "com.nnit.phonebook");
//			if(id == 0){
//				iv.setImageResource(R.drawable.photo); 
//			}else{
//				iv.setImageResource(id);
//			}
			
			
		}else{
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.listitem_brieflist, parent, false);
			}
			TextView tv = (TextView)convertView.findViewById(R.id.briefList_Initial);
			tv.setText(pb == null? null: pb.getInitials() + "(" + pb.getName() +")");
		}
		
		return convertView;
	}

}

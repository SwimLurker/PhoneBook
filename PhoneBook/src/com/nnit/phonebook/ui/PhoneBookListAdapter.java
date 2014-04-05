package com.nnit.phonebook.ui;

import java.util.List;

import com.nnit.phonebook.MainActivity;
import com.nnit.phonebook.R;
import com.nnit.phonebook.data.PhoneBookItem;






import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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
			
			Resources resources = context.getResources();
			String initials = (pb == null ? null: pb.getInitials().toLowerCase());
			
			int id = context.getResources().getIdentifier(initials, "drawable", "com.nnit.phonebook");
			if(id == 0){
				iv.setImageResource(R.drawable.photo); 
			}else{
				iv.setImageResource(id);
			}
			
			
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

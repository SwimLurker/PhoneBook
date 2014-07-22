package com.nnit.phonebook.widget;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.nnit.phonebook.DetailActivity;
import com.nnit.phonebook.MainActivity;
import com.nnit.phonebook.R;
import com.nnit.phonebook.config.ConfigManager;
import com.nnit.phonebook.data.DataPackageManager;
import com.nnit.phonebook.data.FavoriteManager;
import com.nnit.phonebook.data.JSONPBDataSource;
import com.nnit.phonebook.data.PhoneBookItem;
import com.nnit.phonebook.data.PhotoManager;
import com.nnit.phonebook.service.UpdateContactCardWidgetService;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.RemoteViews;

public class ContactCardWidgetProvider extends AppWidgetProvider{
	public static final String SHOW_NEXT_PERSON_ACTION = "com.nnit.phonebook.widget.SHOW_NEXT_PERSON";
	
	private static List<PhoneBookItem> pbItems = null;
	private static int index = 0;
	private static int layoutIndex = 0;
	private static PhoneBookItem prePBI = null;
	
	
	public ContactCardWidgetProvider(){
		if(pbItems == null){
			pbItems = getPhoneBook();
		}
	}
	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds){
		super.onDeleted(context, appWidgetIds);
	}
	
	@Override
	public void onDisabled(Context context){
		super.onDisabled(context);
		//UpdateContactCardWidgetService.setStop();
	}
	
	@Override
	public void onEnabled(Context context){
		super.onEnabled(context);
		ConfigManager.getInstance().reloadConfigures();
		
		boolean bStartService = false;
		String startServiceStr = ConfigManager.getInstance().getConfigure(ConfigManager.CONFIG_START_WIDGETUPDATE_SERVICE);
		if(startServiceStr != null && (startServiceStr.equalsIgnoreCase("1") || startServiceStr.equalsIgnoreCase("true"))){
			bStartService = true;
		}
		if(bStartService){
			context.startService(new Intent(context, UpdateContactCardWidgetService.class));
		}
	}
	
	@Override
	public void onReceive(Context context, Intent intent){
		if(SHOW_NEXT_PERSON_ACTION.equals(intent.getAction())){
			RemoteViews updateViews = null;
			PhoneBookItem pbi = getNextFavoritePhoneBookItem();
			updateViews = updateAppWidget(context, pbi);		
			
			AppWidgetManager manager = AppWidgetManager.getInstance(context);
			ComponentName cName = new ComponentName(context, this.getClass());
			if(updateViews != null){
				manager.updateAppWidget(cName, updateViews);
			}
		}
		
		super.onReceive(context, intent);
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds){
		UpdateContactCardWidgetService.updateAppWidgetIds(appWidgetIds);
		
		RemoteViews updateViews = null;
		PhoneBookItem pbi = getNextFavoritePhoneBookItem();
		updateViews = updateAppWidget(context, pbi);
		
		if(updateViews != null){
			manager.updateAppWidget(appWidgetIds, updateViews);
		}
		
//		UpdateWidgetService.updateAppWidgetIds(appWidgetIds);
//		context.startService(new Intent(context, UpdateWidgetService.class));
//		for(int i =0; i<appWidgetIds.length; i++){
//			RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.layout.nnit_phonebook_widget);
//			if(favoriteList.size()>=1){
//				index = index % favoriteList.size();
//				Log.i("Widget", "index:"+index);
//				PhoneBookItem pb = favoriteList.get(index);
//				index ++;
//				remoteViews.setTextViewText(R.id.widget_line1, pb.getInitials().toUpperCase() + "(" + pb.getLocalName() +")");
//				remoteViews.setTextViewText(R.id.widget_line2, pb.getName());
//				remoteViews.setTextViewText(R.id.widget_line3, pb.getTitle() + "," + pb.getDepartmentNo() + " " + pb.getDepartment());
//				remoteViews.setTextViewText(R.id.widget_line4, pb.getPhone());
//				remoteViews.setTextViewText(R.id.widget_line5, pb.getInitials().toLowerCase() + "@nnit.com");
//				
//				String filename = PhotoManager.getInstance().getPhotoFilenameByInitials(pb.getInitials());
//				Bitmap image = getPhoto(filename);
//				if(image!= null){
//					remoteViews.setImageViewBitmap(R.id.widget_photo, image);
//				}
//				
//			}
//			
//			manager.updateAppWidget(new ComponentName(context, this.getClass()), remoteViews);
//		}
//		
//		super.onUpdate(context, manager, appWidgetIds);
	}
	
	public static PhoneBookItem getNextFavoritePhoneBookItem(){
		PhoneBookItem nextPBI = null;
		if(pbItems == null){
			pbItems = getPhoneBook();
		}
		
		FavoriteManager.getInstance().reload();
		List<PhoneBookItem> favoriteList = new ArrayList<PhoneBookItem>();
		for(PhoneBookItem pbi: pbItems){
			if(FavoriteManager.getInstance().isInFavoriteList(pbi.getInitials())){
				favoriteList.add(pbi);
			}
		}
		
		if(favoriteList.size()>=1){
			index = index % favoriteList.size();
			nextPBI = favoriteList.get(index);
			index ++;
		}
		
		return nextPBI;
	}
	
	
	public static RemoteViews updateAppWidget(Context context, PhoneBookItem pbi){
		RemoteViews remoteViews = null;
		if(layoutIndex == 0){
			remoteViews = new RemoteViews(context.getPackageName(),R.layout.widget_contact_card);
			layoutIndex = 1;
		}else{
			remoteViews = new RemoteViews(context.getPackageName(),R.layout.widget_contact_card2);
			layoutIndex = 0;
		}
		
		if(pbi != null){
			remoteViews.setTextViewText(R.id.widget_line1, pbi.getInitials().toUpperCase() + "(" + pbi.getLocalName() +")");
			remoteViews.setTextViewText(R.id.widget_line2, pbi.getName());
			remoteViews.setTextViewText(R.id.widget_line3, pbi.getTitle() + "," + pbi.getDepartmentNo() + " " + pbi.getDepartment());
			remoteViews.setTextViewText(R.id.widget_line4, pbi.getPhone());
			remoteViews.setTextViewText(R.id.widget_line5, pbi.getInitials().toLowerCase() + "@nnit.com");
			
			String filename = PhotoManager.getInstance().getPhotoFilenameByInitials(pbi.getInitials());
			Bitmap image = getPhoto(filename);
			if(image!= null){
				remoteViews.setImageViewBitmap(R.id.widget_photo, image);
			}
			
			if (prePBI != null){
				remoteViews.setTextViewText(R.id.widget_line1_back, prePBI.getInitials().toUpperCase() + "(" + prePBI.getLocalName() +")");
				remoteViews.setTextViewText(R.id.widget_line2_back, prePBI.getName());
				remoteViews.setTextViewText(R.id.widget_line3_back, prePBI.getTitle() + "," + prePBI.getDepartmentNo() + " " + prePBI.getDepartment());
				remoteViews.setTextViewText(R.id.widget_line4_back, prePBI.getPhone());
				remoteViews.setTextViewText(R.id.widget_line5_back, prePBI.getInitials().toLowerCase() + "@nnit.com");
				
				String filename1 = PhotoManager.getInstance().getPhotoFilenameByInitials(prePBI.getInitials());
				Bitmap image1 = getPhoto(filename1);
				if(image1!= null){
					remoteViews.setImageViewBitmap(R.id.widget_photo_back, image1);
				}
			}
			
			prePBI = pbi;
			
		}
		
		Intent intent = new Intent();
		intent.setAction(SHOW_NEXT_PERSON_ACTION);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);		
		remoteViews.setOnClickPendingIntent(R.id.widget_nextbtn, pendingIntent);
		if(pbi!=null){
			Intent appIntent = new Intent(context, DetailActivity.class);
			appIntent.putExtra(MainActivity.SELECTED_PBITEM, pbi);
			Log.i("WidgetProvider", "Save info:" +pbi.getInitials());
			PendingIntent appPendingIntent = PendingIntent.getActivity(context, 0, appIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			
			remoteViews.setOnClickPendingIntent(R.id.widget_photo, appPendingIntent);
		}else{
			Intent appIntent = new Intent(context, MainActivity.class);
			PendingIntent appPendingIntent = PendingIntent.getActivity(context, 0, appIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.widget_photo, appPendingIntent);
		}
		
		return remoteViews;
	}
	private static Bitmap getPhoto(String photoFilename) {
		FileInputStream fis = null;
		try{
			if(photoFilename != null){
				File f = new File(photoFilename);
		
				if(f.exists() && f.isFile()){
					fis = new FileInputStream(f);
					Bitmap bitmap = BitmapFactory.decodeStream(fis);
					return bitmap;
				}			
			}
		}catch(Exception exp){
			exp.printStackTrace();
		}finally{
			if(fis != null){
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
		}
		return null;
	}

	private static List<PhoneBookItem> getPhoneBook() {
		try{
			JSONPBDataSource ds =  new JSONPBDataSource();
			ds.setJsonFilePath(DataPackageManager.getInstance().getPhoneBookDataFileAbsolutePath());
		
			return ds.getDataSet().getPBItems();
		}catch(Exception exp){
			exp.printStackTrace();
			return new ArrayList<PhoneBookItem>();
		}
    }
}

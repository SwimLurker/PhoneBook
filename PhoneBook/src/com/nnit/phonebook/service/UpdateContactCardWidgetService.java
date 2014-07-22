package com.nnit.phonebook.service;

import java.util.LinkedList;
import java.util.Queue;

import com.nnit.phonebook.config.ConfigManager;
import com.nnit.phonebook.data.PhoneBookItem;
import com.nnit.phonebook.widget.ContactCardWidgetProvider;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.text.format.Time;
import android.widget.RemoteViews;

public class UpdateContactCardWidgetService extends Service implements Runnable {
	
	private static final String TAG = "UpdateContactCardWidgetService";
	private static Queue<Integer> appWidgetIds = new LinkedList<Integer>();
	public static final String ACTION_UPDATE_ALL = "com.nnit.phonebook.widget.UPDATE_ALL";
	private static boolean threadRunning = false;
	private static Object lock = new Object();
	private long updateInterval = 60000;
	
	@Override
	public void run() {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
		RemoteViews updateViews = null;
		
		while(hasMoreUpdates()){
			int appWidgetId = getNextWidgetId();
		
			PhoneBookItem pbi = ContactCardWidgetProvider.getNextFavoritePhoneBookItem();
			
			if(pbi != null){
				updateViews = ContactCardWidgetProvider.updateAppWidget(this, pbi);
			}
			if(updateViews != null){
				appWidgetManager.updateAppWidget(appWidgetId, updateViews);
			}
		}
		
		Intent updateIntent = new Intent(ACTION_UPDATE_ALL);
		updateIntent.setClass(this, UpdateContactCardWidgetService.class);
		PendingIntent pending = PendingIntent.getService(this, 0, updateIntent, 0);
		
		String startServiceStr = ConfigManager.getInstance().getConfigure(ConfigManager.CONFIG_START_WIDGETUPDATE_SERVICE);
		boolean bStopped = true;
		if(startServiceStr != null && (startServiceStr.equalsIgnoreCase("1") || startServiceStr.equalsIgnoreCase("true"))){
			bStopped = false;
		}
		if(!bStopped){
			AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			Time time = new Time();
			long now = System.currentTimeMillis();
			
			
			time.set(now + updateInterval);
			long updateTimes = time.toMillis(true);
			alarm.set(AlarmManager.RTC_WAKEUP, updateTimes, pending);
		}
		stopSelf();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public static void updateAppWidgetIds(int[] ids) {
		synchronized(lock){
			for(int appWidgetId: ids){
				appWidgetIds.add(appWidgetId);
			}
		}
	}
	
	public static int getNextWidgetId(){
		synchronized(lock){
			if(appWidgetIds.peek() == null){
				return AppWidgetManager.INVALID_APPWIDGET_ID;
			}else{
				return appWidgetIds.poll();
			}
		}
	}
	
	private static boolean hasMoreUpdates(){
		synchronized(lock){
			boolean hasMore = !appWidgetIds.isEmpty();
			if(!hasMore){
				threadRunning = false;
			}
			
			return hasMore;
		}
	}
	
	@Override
	public void onCreate(){
		super.onCreate();
	}
	
	@Override
	public void onStart(Intent intent, int startId){
		super.onStart(intent, startId);
		String updateIntervalStr = ConfigManager.getInstance().getConfigure(ConfigManager.CONFIG_WIDGETUPDATE_INTERVAL);
		if(updateIntervalStr !=null){
			updateInterval = Long.parseLong(updateIntervalStr);
		}
		
		if(null != intent){
			if(ACTION_UPDATE_ALL.equals(intent.getAction())){
				AppWidgetManager widget = AppWidgetManager.getInstance(this);
				updateAppWidgetIds(widget.getAppWidgetIds(new ComponentName(this, ContactCardWidgetProvider.class)));
			}
		}
		
		synchronized(lock){
			if(!threadRunning){
				threadRunning = true;
				new Thread(this).start();
			}
		}
	}
	
}

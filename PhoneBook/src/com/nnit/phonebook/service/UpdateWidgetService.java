package com.nnit.phonebook.service;

import java.util.LinkedList;
import java.util.Queue;

import com.nnit.phonebook.data.PhoneBookItem;
import com.nnit.phonebook.widget.NNITPhoneBookWidgetProvider;

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

public class UpdateWidgetService extends Service implements Runnable {
	
	private static final String TAG = "UpdateWidgetService";
	private static Queue<Integer> appWidgetIds = new LinkedList<Integer>();
	public static final String ACTION_UPDATE_ALL = "com.nnit.phonebook.widget.UPDATE_ALL";
	private static boolean threadRunning = false;
	private static Object lock = new Object();
	
	@Override
	public void run() {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
		RemoteViews updateViews = null;
		
		while(hasMoreUpdates()){
			int appWidgetId = getNextWidgetId();
		
			PhoneBookItem pbi = NNITPhoneBookWidgetProvider.getNextFavoritePhoneBookItem();
			
			if(pbi != null){
				updateViews = NNITPhoneBookWidgetProvider.updateAppWidget(this, pbi);
			}
			if(updateViews != null){
				appWidgetManager.updateAppWidget(appWidgetId, updateViews);
			}
		}
		
		Intent updateIntent = new Intent(ACTION_UPDATE_ALL);
		updateIntent.setClass(this, UpdateWidgetService.class);
		PendingIntent pending = PendingIntent.getService(this, 0, updateIntent, 0);
		
		AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Time time = new Time();
		long now = System.currentTimeMillis();
		time.set(now + 20000);
		long updateTimes = time.toMillis(true);
		alarm.set(AlarmManager.RTC_WAKEUP, updateTimes, pending);
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
		if(null != intent){
			if(ACTION_UPDATE_ALL.equals(intent.getAction())){
				AppWidgetManager widget = AppWidgetManager.getInstance(this);
				updateAppWidgetIds(widget.getAppWidgetIds(new ComponentName(this, NNITPhoneBookWidgetProvider.class)));
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

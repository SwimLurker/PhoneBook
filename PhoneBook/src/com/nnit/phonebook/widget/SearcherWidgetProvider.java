package com.nnit.phonebook.widget;

import com.nnit.phonebook.DetailActivity;
import com.nnit.phonebook.MainActivity;
import com.nnit.phonebook.R;
import com.nnit.phonebook.SearchActivity;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class SearcherWidgetProvider extends AppWidgetProvider{
	
	public static final String SHOW_SEARCHER_ACTION = "com.nnit.phonebook.widget.SHOW_SEARCHER";

	
	public SearcherWidgetProvider(){
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds){
		super.onDeleted(context, appWidgetIds);
	}

	@Override
	public void onDisabled(Context context){
		super.onDisabled(context);
	}

	@Override
	public void onEnabled(Context context){
		super.onEnabled(context);
	}

	@Override
	public void onReceive(Context context, Intent intent){
		super.onReceive(context, intent);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds){
		
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.layout.widget_searcher);
		
		Intent appIntent = new Intent(context, SearchActivity.class);
		PendingIntent appPendingIntent = PendingIntent.getActivity(context, 0, appIntent, 0);
		
		remoteViews.setOnClickPendingIntent(R.id.widget_searcher, appPendingIntent);
		
		ComponentName cName = new ComponentName(context, this.getClass());
		
		manager.updateAppWidget(cName, remoteViews);
		
		super.onUpdate(context, manager, appWidgetIds);
	}
}

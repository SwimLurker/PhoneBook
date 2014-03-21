package com.nnit.phonebook.ui;


import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;



import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.media.MediaPlayer;
import android.net.NetworkInfo;
import android.telephony.SignalStrength;
import android.view.KeyEvent;





public class FusionField
{
	/**
	 * 保存当前的Activity
	 */
	public static Activity currentActivity = null;
	

	/**
//	 * 屏幕分辨率
	 */
	public static float currentDensity;
//	public static int currentDensityDpi;
//	public static int currentWidthPixels;
//	public static int currentHeightPixels;
//	public static String currentscreenSize = null;

	

	/**
	 * 默认屏幕密度
	 */
	public static final float DEFAULT_DENSITY = 1.0f;

	/**
	 * 高屏幕密度
	 */
	public static final float HIGH_DENSITY = 1.5f;
	
	/**
	 * 640 * 960分辨率
	 */
	public static final float HIGH_BIG_DENSITY = 2.0f;
	
	/**
	 * 低屏幕密度
	 */
	public static final float LOW_DENSITY = 0.75f;	
	
	
	/**
     * 设置界面各个选项字体大小,菜单字体大小
     */
	public static float SET_TYPE_TEXT_SIZE = 16;
	

	
	
}
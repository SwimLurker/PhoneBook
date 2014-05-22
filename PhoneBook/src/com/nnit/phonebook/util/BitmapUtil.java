package com.nnit.phonebook.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.os.Environment;
import android.os.StatFs;

public class BitmapUtil {
	
	private static int FREE_SD_SPACE_NEEDED_TO_CACHE = 1;
	private static int MB = 1024 * 1024;
	public final static String DIR = "/sdcard/com/nnit/phonebook";
	
	public static Bitmap readBitmapById(Context context, int resId){
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		
		InputStream is = context.getResources().openRawResource(resId);
		return BitmapFactory.decodeStream(is);
	}

	public static Bitmap readBitmapById(Context context, int resId, int screenWidth, int screenHeight){
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
		opt.inPurgeable = true;
		opt.inInputShareable = true; 
		InputStream stream = context.getResources().openRawResource(resId);
		Bitmap bitmap = BitmapFactory.decodeStream(stream, null, opt);
		return getBitmap(bitmap, screenWidth, screenHeight);
	}
	
	public static Bitmap getBitmap(Bitmap bitmap, int screenWidth, int screenHeight){
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Matrix matrix = new Matrix();
		
		float scale = (float) screenWidth / w;
		float scale2 = (float) screenHeight / h;
		
		matrix.postScale(scale, scale);
		
		return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
	}
	
	public static void saveBmpToSd(Bitmap bm, String url, int quantity){
		if(FREE_SD_SPACE_NEEDED_TO_CACHE > freeSpaceOnSd()){
			return;
		}
		if(!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
			return;
		}
		String filename = url;
		File dirPath = new File(DIR);
		if(!dirPath.exists()){
			dirPath.mkdirs();
		}
		
		File file = new File(DIR + File.pathSeparator + filename);
		try{
			file.createNewFile();
			OutputStream os = new FileOutputStream(file);
			bm.compress(Bitmap.CompressFormat.PNG, quantity, os);
			os.flush();
			os.close();
		}catch(FileNotFoundException fnfExp){
			
		}catch(IOException ioExp){
			ioExp.printStackTrace();
		}
	}
	
	public static Bitmap getBitmap(String url, int quantity){
		InputStream is = null;
		String filename = "";
		Bitmap bm = null;
		URL url_Image = null;
		String LOCALURL = "";
		if(url == null){
			return null;
		}
		
		try{
			filename = url;
		}catch(Exception exp){
		}
		
		LOCALURL = URLEncoder.encode(filename);
		if(exist(DIR + File.pathSeparator + LOCALURL)){
			bm = BitmapFactory.decodeFile(DIR + File.pathSeparator + LOCALURL);
		}else{
			try{
				url_Image = new URL(url);
				is = url_Image.openStream();
				bm = BitmapFactory.decodeStream(is);
				if(bm != null){
					saveBmpToSd(bm, LOCALURL, quantity);
				}
				is.close();
			}catch(Exception exp){
				exp.printStackTrace();
				return null;
			}
		}
		return bm;
	}
	
	public static boolean exist(String url){
		File file = new File(DIR + url);
		return file.exists();
	}
	
	private static int freeSpaceOnSd(){
		StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
		double sdFreeMB = ((double)stat.getAvailableBlocks() * (double)stat.getBlockSize()) / MB;
		
		return (int)sdFreeMB;
	}
}

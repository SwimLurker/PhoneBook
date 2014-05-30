package com.nnit.phonebook.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.os.Environment;

public class PhotoFileManager {
	private static PhotoFileManager _instance = null;
	
	
	public static final String PACKAGE_NAME = "com.nnit.phonebook";
	public static final String PHOTOFILE_PATH = "/data" 
			+ Environment.getDataDirectory().getAbsolutePath()
			+ "/" + PACKAGE_NAME + "/photos/";
	
	public static final String PHOTO_ZIP_NAME = "photos.zip";
	
	private HashMap<String, String> photos = new HashMap<String, String>();
	
	private PhotoFileManager(){
		
	}
	
	public static PhotoFileManager getInstance(){
		if(_instance == null){
			_instance = new PhotoFileManager();
		}
		return _instance;
	}
	
	public String getPhotoFilenameByInitials(String initials){
		if(photos.containsKey(initials)){
			return photos.get(initials);
		}
		return null;
	}
	
	public void unpackPhotosFromAssets(Context context) throws IOException{
		File file = new File(PHOTOFILE_PATH);
		if(file.exists()){
			loadPhotosInfo(file);
			return;
		}
		unpackPhotos(context, context.getResources().getAssets().open(PHOTO_ZIP_NAME));
	}
	
	public void unpackPhotos(Context context, InputStream photoPackIS) throws IOException{
		
		File file = new File(PHOTOFILE_PATH);
		if(!file.exists()){
			file.mkdirs();
		}
		ZipInputStream zis = new ZipInputStream(photoPackIS);
		
		ZipEntry zipEntry = zis.getNextEntry();
		
		byte[] buf = new byte[1024*32];
		
		int count = 0;
		while(zipEntry != null){
			
			file = new File(PHOTOFILE_PATH +File.separator + zipEntry.getName());
			if(zipEntry.isDirectory()){
				file.mkdir();
			}else{
				file.createNewFile();
				FileOutputStream fos = new FileOutputStream(file);
				while((count=zis.read(buf))>0){
					fos.write(buf, 0 , count);
				}
				fos.close();
				
				String entryName = zipEntry.getName();
				String initials = getInitialsFromFilePath(entryName);
				if(initials != null){
					photos.put(initials.toLowerCase(), PHOTOFILE_PATH +File.separator + zipEntry.getName());
				}
			}
			
			zipEntry = zis.getNextEntry();
		}
		zis.close();
	}
	
	private void loadPhotosInfo(File rootDir) {
		File[] subFiles = rootDir.listFiles();
		for(File f: subFiles){
			if(f.isDirectory()){
				loadPhotosInfo(f);
			}else if(f.isFile()){
				String initials = getInitilas(f);
				if(initials != null){
					photos.put(initials.toLowerCase(), f.getAbsolutePath());
				}
			}	
		}
	}
	
	private String getInitilas(File file){
		int pos = -1;
		String filename = file.getName();
		if((pos = filename.lastIndexOf(".")) != -1){
			String initials = filename.substring(0, pos);
			return initials;
		}else{
			return filename;
		}
		
	}
	private String getInitialsFromFilePath(String filePath){
		String initials = null;
		int pos = -1;
		if((pos = filePath.lastIndexOf(".")) != -1){
			int pos2 = filePath.lastIndexOf(File.separator);
			initials = filePath.substring(pos2+1, pos);
			return initials;
		}else{
			return filePath;
		}
	}
	
	

}

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
import android.util.Log;

public class DataPackageManager {
	private static DataPackageManager _instance = null;

	public static final String PACKAGE_NAME = "com.nnit.phonebook";
	public static final String DATAPACKAGE_DIR = "/data"
			+ Environment.getDataDirectory().getAbsolutePath() + "/"
			+ PACKAGE_NAME + "/";

	public static final String DATA_PACKAGE_NAME = "data.zip";
	public static final String PHONEBOOK_DATA_FILENAME = "iNNIT.json";
	public static final String SEAT_DB_FILENAME = "iNNIT.db";
	public static final String PHOTO_DIR = "photos";
	public static final String MAP_DIR = "maps";
	public static final String BACKUP_DIR = "bak";
	
	
	private HashMap<String, String> photos = null;

	private DataPackageManager() {
		photos = new HashMap<String, String>();
	}

	public static DataPackageManager getInstance() {
		if (_instance == null) {
			_instance = new DataPackageManager();
		}
		return _instance;
	}

	
	public void unpackDataPackageFromAssets(Context context, boolean bOverwrite) throws IOException {
		unpackDataPackage(context.getResources().getAssets().open(DATA_PACKAGE_NAME), bOverwrite);
	}
	
	public void unpackDataPackageFromInputStream(InputStream dataPackageIS, boolean bOverwrite)throws IOException {
		backupDataFiles();
		unpackDataPackage(dataPackageIS, bOverwrite);		
	}
	
	private void backupDataFiles(){
		File f = new File(DATAPACKAGE_DIR);
		if(f.exists() && f.isDirectory()){
			File backDir = new File(DATAPACKAGE_DIR + BACKUP_DIR + File.separator);
			backDir.delete();
			backDir.mkdirs();
			
			File[] files = f.listFiles();
			for(File sf: files){
				if(sf.isDirectory()){
					if(!sf.getName().equalsIgnoreCase(BACKUP_DIR)){
						sf.renameTo(new File(DATAPACKAGE_DIR + BACKUP_DIR + File.separator + sf.getName()));
					}
				}else if(sf.isFile()){
					sf.renameTo(new File(DATAPACKAGE_DIR + BACKUP_DIR + File.separator + sf.getName()));
				}
			}
			
			File[] files2 = f.listFiles();
			for(File sf: files2){
				if(sf.isDirectory()&&(sf.getName().equalsIgnoreCase(BACKUP_DIR))){
						continue;
				}
				sf.delete();
			}
			
		}
		
	}

	private void unpackDataPackage(InputStream dataPackageIS, boolean bOverwrite)
			throws IOException {
		File pbfile = new File(getPhoneBookDataFileAbsolutePath());
		
		if (pbfile.exists()&&(!bOverwrite)) {
			return;
		}

		File file = new File(DATAPACKAGE_DIR);
		
		if (!file.exists()) {
			file.mkdirs();
		}
		ZipInputStream zis = new ZipInputStream(dataPackageIS);

		ZipEntry zipEntry = zis.getNextEntry();

		byte[] buf = new byte[1024 * 32];

		int count = 0;
		while (zipEntry != null) {

			file = new File(DATAPACKAGE_DIR + zipEntry.getName());
			if (zipEntry.isDirectory()) {
				file.mkdir();
			} else {
				file.createNewFile();
				FileOutputStream fos = new FileOutputStream(file);
				while ((count = zis.read(buf)) > 0) {
					fos.write(buf, 0, count);
				}
				fos.close();
			}

			zipEntry = zis.getNextEntry();
		}
		zis.close();
	}

	public String getPhoneBookDataFileAbsolutePath() {
		return DATAPACKAGE_DIR + PHONEBOOK_DATA_FILENAME;
	}
	
	public String getSeatDBFileAbsolutePath() {
		return DATAPACKAGE_DIR + SEAT_DB_FILENAME;
	}
	
	public String getPhotoDirAbsolutePath() {
		return DATAPACKAGE_DIR + PHOTO_DIR;
	}
	
	public String getMapDirAbsolutePath() {
		return DATAPACKAGE_DIR + MAP_DIR;
	}
	
}

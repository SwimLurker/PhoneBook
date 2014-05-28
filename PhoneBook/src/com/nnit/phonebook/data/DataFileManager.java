package com.nnit.phonebook.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.util.Log;


public class DataFileManager {
	private static DataFileManager _instance = null;
	
	
	public static final String PACKAGE_NAME = "com.nnit.phonebook";
	public static final String DATAFILE_PATH = "/data" 
			+ Environment.getDataDirectory().getAbsolutePath()
			+ "/" + PACKAGE_NAME;
	
	private HashMap<String, DataFileImporter> importerMap = new HashMap<String, DataFileImporter>();
	private Context context;
	
	private DataFileManager(){
		
	}
	
	public static DataFileManager getInstance(){
		if(_instance == null){
			_instance = new DataFileManager();
		}
		return _instance;
	}
	
	public void setContext(Context context){
		this.context = context;
	}
	
	public void addImporter(String datafileName, int datafileResourceID){
		DataFileImporter importer = new DataFileImporter(context);
		importer.setDatafileName(datafileName);
		importer.setDatafilePath(DATAFILE_PATH);
		importer.setDatafileResourceID(datafileResourceID);
		
		importerMap.put(datafileName, importer);
	}
	
	public void removeImporter(String datafileName){
		importerMap.remove(datafileName);
	}
	
	public void clearImporters(){
		importerMap.clear();
	}
	
	public String getDataFileAbsolutePath(String datafileName){
		if(!importerMap.containsKey(datafileName)){
			throw new RuntimeException("Data file not defined");
		}
		DataFileImporter importer = importerMap.get(datafileName);
		return importer.getDatafilePath() + "/" + importer.getDatafileName();
	}

	public void importAllFiles() {
		Collection<DataFileImporter> importers = importerMap.values();
		for(DataFileImporter importer: importers){
			importer.importDataFile();
		}
	}
	
    class DataFileImporter {

		private final int BUFFER_SIZE = 400000;
		
		private Context context;
		private String datafilePath = null;
		private String datafileName = null;
		private int datafileResourceID = -1;
		
		
		public DataFileImporter(Context ctx){
			this.context = ctx;
		}
		
		
		public String getDatafilePath() {
			return datafilePath;
		}


		public void setDatafilePath(String datafilePath) {
			this.datafilePath = datafilePath;
		}


		public String getDatafileName() {
			return datafileName;
		}


		public void setDatafileName(String datafileName) {
			this.datafileName = datafileName;
		}


		public int getDatafileResourceID() {
			return datafileResourceID;
		}


		public void setDatafileResourceID(int datafileResourceID) {
			this.datafileResourceID = datafileResourceID;
		}


		@SuppressLint("NewApi")
		public void importDataFile(){
			String datafile = datafilePath + "/" + datafileName;
			
			try{
				if(!(new File(datafile).exists())){
					InputStream is = this.context.getResources().openRawResource(datafileResourceID);
					FileOutputStream fos =  new FileOutputStream(datafile);
					byte[] buffer = new byte[BUFFER_SIZE];
					int count = 0;
					while((count = is.read(buffer)) > 0){
						fos.write(buffer, 0, count);
					}
					fos.close();
					is.close();
				}
				
			}catch(FileNotFoundException e){
				Log.e("DataFileImporter", "File not found");
				throw new RuntimeException("can not import data file", e);
			}catch(IOException e){
				Log.e("DataFileImporter", "IO exception");
				throw new RuntimeException("can not import data file", e);
			}
		}
		
		@SuppressLint("NewApi")
		public void updateDataFile(InputStream is){
			String datafile = datafilePath + "/" + datafileName;
			String bakDatafile = datafilePath + "/" + datafileName + ".bak";
			
			boolean needRollback = false;
			boolean updateFailed = false;
			
			File f = new File(datafile);
			if(f.exists()){
				f.renameTo(new File(bakDatafile));
				needRollback = true;
				f.delete();
			}
			
			try{
				FileOutputStream fos =  new FileOutputStream(datafile);
				byte[] buffer = new byte[BUFFER_SIZE];
				int count = 0;
				while((count = is.read(buffer)) > 0){
					fos.write(buffer, 0, count);
				}
				fos.close();
				is.close();
				
			
			}catch(Exception e){
				Log.e("DataFileImporter", "Update exception");
				updateFailed = true;
				throw new RuntimeException("can not update data file", e);
			}finally{
				if(updateFailed && needRollback){
					rollback();
				}
			}
		}
		
		private void rollback(){
			String datafile = datafilePath + "/" + datafileName;
			String bakDatafile = datafilePath + "/" + datafileName + ".bak";
			
			File f = new File(datafile);
			f.delete();
			File bakf = new File(bakDatafile);
			bakf.renameTo(f);
			
		}
	}

	public boolean updateDataFile(String datafileName, InputStream is) {
		DataFileImporter importer = importerMap.get(datafileName);
		if(importer == null){
			return false;
		}
		importer.updateDataFile(is);
		
		return true;
	}

}

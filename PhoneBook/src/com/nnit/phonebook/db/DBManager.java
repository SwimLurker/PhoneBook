package com.nnit.phonebook.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.nnit.phonebook.R;
import com.nnit.phonebook.data.DataFileManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

public class DBManager {
	
	private SQLiteDatabase database;
	
	public void openDatabase(){
		String dbfile = DataFileManager.getInstance().getDataFileAbsolutePath("iNNIT.db");
		checkDatabase(dbfile);
		this.database =  SQLiteDatabase.openOrCreateDatabase(dbfile, null);
		if(this.database == null){
			throw new SQLException("Can not open database:" + dbfile);
		}
	}
	
	public void closeDatabase(){
		if(this.database != null){
			this.database.close();
			this.database = null;
		}
	}
	
	public Cursor query(String sql, String[] selectionArgs){
		return this.database.rawQuery(sql, selectionArgs);
	}
	
	@SuppressLint("NewApi")
	private void checkDatabase(String dbfile){
		if(!(new File(dbfile).exists())){
			throw new RuntimeException("database file not found");
		}
	}
	
	
	

}

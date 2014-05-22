package com.nnit.phonebook.db;

import com.nnit.phonebook.data.SeatMapInfo;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.RectF;
import android.util.Log;

public class SeatMapInfoDAO {
	
	private DBManager dbManager;
	
	public SeatMapInfoDAO(){
		dbManager = new DBManager();
	}
	
	public SeatMapInfo querySeatMapInfo(String initials){
		SeatMapInfo result = null;
		Cursor cursor = null;
		try{
			dbManager.openDatabase();
			cursor = dbManager.query("select Map.Floor, Map.Filename, Seat.X, Seat.Y, Seat.Width, Seat.Height, Seat.Direction from Map,Seat where Seat.Initials=? and Seat.MapID=Map.ID", new String[]{initials});
				
			if(cursor.moveToNext()){
				int floorNo = cursor.getInt(0);
				String mapFilename = cursor.getString(1);
				int x = cursor.getInt(2);
				int y = cursor.getInt(3);
				int w = cursor.getInt(4);
				int h = cursor.getInt(5);
				int d = cursor.getInt(6);
						
				result = new SeatMapInfo();
				result.setInitials(initials);
				result.setFloorNo(floorNo);
				result.setMapFilename(mapFilename);
				result.setDirection(d);
				result.setSeatRect(new RectF(x, y, x+w, y+h));
			}
			
		}catch(SQLException e){
			Log.e("SeatMapInfoDAO", "Query seat info error");
			e.printStackTrace();
		}finally{
			if(cursor != null){
				cursor.close();
				cursor = null;
			}
			dbManager.closeDatabase();
		}
	
		return result;
	}

}

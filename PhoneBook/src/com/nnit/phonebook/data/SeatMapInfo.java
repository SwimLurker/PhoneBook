package com.nnit.phonebook.data;

import java.io.Serializable;

import android.graphics.RectF;

public class SeatMapInfo{
	
	private String initials;
	private RectF seatRect;
	private int direction;
	private int floorNo;
	private String mapFilename;
	public String getInitials() {
		return initials;
	}
	public void setInitials(String initials) {
		this.initials = initials;
	}
	public RectF getSeatRect() {
		return seatRect;
	}
	public void setSeatRect(RectF seatRect) {
		this.seatRect = seatRect;
	}
	public int getDirection() {
		return direction;
	}
	public void setDirection(int direction) {
		this.direction = direction;
	}
	public int getFloorNo() {
		return floorNo;
	}
	public void setFloorNo(int floorNo) {
		this.floorNo = floorNo;
	}
	public String getMapFilename() {
		return mapFilename;
	}
	public void setMapFilename(String mapFilename) {
		this.mapFilename = mapFilename;
	}
	
}

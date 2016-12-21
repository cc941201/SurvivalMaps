package com.oose2016.group4.server;

import java.util.Date;

public class Crime {
	private int date, linkid;
	private String address, type;
	private double latitude, longitude;
	
	/**
	 * Constructor for Crime
	 * @param date the date the crime was committed in seconds
	 * @param address the approximate address of the crime committed 
	 * @param type the type of crime committed (assault, murder, rape, etc.)
	 * @param latitude the latitude of the location where the crime was committed
	 * @param longitude the longitude of the location where the crime was committed
	 * @param linkid the linkId of the location where the crime was committed
	 */
	public Crime(int date, String address, String type, double latitude, double longitude, int linkid) {
		this.date = date;
		this.address = address;
		this.type = type;
		this.latitude = latitude;
		this.longitude = longitude;
		this.linkid = linkid;
	}
	
	/**
	 * Constructor for Crime where the address, type and linkId are defaulted to empty because
	 * the object is only concerned with the date, latitude and longitude
	 * @param date the date the crime was committed in seconds
	 * @param latitude the latitude of the location where the crime was committed
	 * @param longitude the longitude of the location where the crime was committed
	 */
	public Crime(int date, double latitude, double longitude) {
		this.date = date;
		this.address = "";
		this.type = "";
		this.latitude = latitude;
		this.longitude = longitude;
		this.linkid = 0;
	}
	
	/**
	 * Get the date
	 * @return date
	 */
	public int getDate() {
		return date;
	}
	
	/**
	 * Get the address
	 * @return address
	 */
	public String getAddress() {
		return address;
	}
	
	/**
	 * Get the latitude
	 * @return latitude 
	 */
	public double getLat() {
		return latitude;
	}
	
	/**
	 * Get the longitude
	 * @return longitude
	 */
	public double getLng() {
		return longitude;
	}
	
	/**
	 * Get the type
	 * @return type
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Get the linkId
	 * @return linkId
	 */
	public int getLinkId() {
		return linkid;
	}
	
	/**
	 * Convert Crime to string.
	 * @return Crime in the form of a string
	 */
	public String toString() {
		return "date: " + date 
				+ " address: " + address
				+ " latitude: " + latitude
				+ " longitutude: " + longitude
				+ " type: " + type
				+ " linkId: " + linkid;
	}
}

package com.accela.recordviewer.model;

import java.io.Serializable;


public class RecordModel implements Serializable
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String id = "";
	public String name = "N/A";
	public String status = "Unknown Status";;
	public String type = "Unknown Type";
	public String openedDate = "Unknown Date";
	public String description = "N/A";
	public AddressModel address;
	
}

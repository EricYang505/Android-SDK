package com.accela.recordviewer.model;

import java.io.Serializable;


public class AddressModel implements Serializable
{

	private static final long serialVersionUID = 1L;
	
	public String streetStart = "";
	public String streetName = "";
	public String city = "";
	public String postalCode = "";
	public String state = "";
	
	public double xCoordinate = 0;
	public double yCoordinate = 0;
	
	public String getAddress() {
		return streetStart + streetName + " , " + city + " , " + state + postalCode;
	}
}

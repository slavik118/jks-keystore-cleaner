package com.cert.util;

/**
Collected constants of general utility.
*/
public final class Constants {

	public static final String CSV_FILE = "assets/csv/jksfiles.csv";
	
	/**
	   The caller references the constants using <tt>Consts.EMPTY_STRING</tt>, 
	   and so on. Thus, the caller should be prevented from constructing objects of 
	   this class, by declaring this private constructor. 
	  */
	private Constants() {
		//this prevents even the native class from 
	    //calling this constructor as well :
	    throw new AssertionError();
	}
}

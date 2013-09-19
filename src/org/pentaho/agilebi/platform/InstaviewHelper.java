package org.pentaho.agilebi.platform;

public class InstaviewHelper {

	private static IInstaviewHelper instaviewHelper;
	
	public static void setInstaviewHelper(IInstaviewHelper helper ) {
		instaviewHelper = helper;
	}
	
	public static IInstaviewHelper getInstaviewHelper() {
		return instaviewHelper;
	}
	
}

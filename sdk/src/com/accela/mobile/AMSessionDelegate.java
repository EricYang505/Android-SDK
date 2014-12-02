package com.accela.mobile;


/**
 * <pre>
 * 
 *  Accela Amobile
 *  File: AMSessionDelegate.java
 * 
 *  Accela, Inc.
 *  Copyright (C): 2013
 * 
 *  Description:
 *  Session delegate, defines the methods which will be called during the lifecycle of user session.
 * 
 *  Notes:
 * 
 * 
 *  Revision History
 *  
 * 
 * 	@since 1.0
 * 
 * </pre>
 */


public interface AMSessionDelegate {
	
	/**
	 * 
	 * Called when access token returns successfully.
	 * 
	 * @return Void.
	 * 
	 * @since 1.0
	 */
	public abstract void amDidLogin();	
	
	/**
	 * 
	 * Called when login fails.
	 * 
	 * @param error The AMError instance which holds the error information.
	 * 
	 * @return Void.
	 * 
	 * @since 3.0
	 */
	public abstract void amDidLoginFailure(AMError error);
	
	/**
	 * 
	 * Called when login is canceled.
	 * 
	 * @return Void.
	 * 
	 * @since 3.0
	 */
	public abstract void amDidCancelLogin();	
		
	/**
	 * 
	 * Called when session becomes invalid.
	 * 
	 * @param error The AMError instance which holds the error information.
	 * 
	 * @return Void.
	 * 
	 * @since 1.0
	 */
	public abstract void amDidSessionInvalid(AMError error);
	
	/**
	 * 
	 * Called when session logs out.
	 * 
	 * @return Void.
	 * 
	 * @since 1.0
	 */
	public abstract void amDidLogout();	

}

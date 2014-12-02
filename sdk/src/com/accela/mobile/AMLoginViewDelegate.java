package com.accela.mobile;

/**
 * <pre>
 * 
 *  Accela Amobile
 *  File: AMLoginViewDelegate.java
 * 
 *  Accela, Inc.
 *  Copyright (C): 2013
 * 
 *  Description:
 *  Login view delegate, defines the callback methods for the delegate of a login view. 
 * 
 *  Notes:
 * 
 * 
 *  Revision History
 *  
 * 
 * 	@since 3.0
 * 
 * </pre>
 */

public interface AMLoginViewDelegate {
		
	/**
	 * Called when when the dialog starts to fetch authorization.
	 * Typically it means the time point at which Login or Sign in button is clicked.
	 * 
	 * @param loginView The login dialog view.
	 * 
	 * @return Void.
	 * 
	 * @since 4.0
	 */
	public abstract void amDialogFetch(AMLoginView loginView);
	
	/**
	 * Called when when the user successfully logs in.
	 * 
	 * @param loginView The login dialog view.
	 * 
	 * @return Void.
	 * 
	 * @since 4.0
	 */
	public abstract void amDialogLogin(AMLoginView loginView);
	
	/**
	 * Called when user cancels authorization.
	 * 
	 * @param cancelled true or false.
	 * 
	 * @return Void.
	 * 
	 * @since 4.0
	 */
	public abstract void amDialogNotLogin(boolean cancelled);
	
	/**
	 * Called when there is error during authorization.
	 * 
	 * @param error The AMError object which contains error details.	
	 * 
	 * @return Void.
	 * 
	 * @since 4.0
	 */
	public abstract void amDialogLoginFailure(AMError error);	
	
}

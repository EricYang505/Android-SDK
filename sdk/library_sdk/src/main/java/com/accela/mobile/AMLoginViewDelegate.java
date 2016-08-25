/** 
  * Copyright 2014 Accela, Inc. 
  * 
  * You are hereby granted a non-exclusive, worldwide, royalty-free license to 
  * use, copy, modify, and distribute this software in source code or binary 
  * form for use in connection with the web services and APIs provided by 
  * Accela. 
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
  * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
  * DEALINGS IN THE SOFTWARE. 
  * 
  */
package com.accela.mobile;

/**
 *  Login view delegate, defines the callback methods for the delegate of a login view. 
 * 
 * 	@since 3.0
 */

public interface AMLoginViewDelegate {
		
	/**
	 * Called when when the dialog starts to fetch authorization.
	 * Typically it means the time point at which Login or Sign in button is clicked.
	 * 
	 * @param loginView The login dialog view.
	 * 
	 *
	 * @since 4.0
	 */
	public abstract void amDialogFetch(AMLoginView loginView);
	
	/**
	 * Called when when the user successfully logs in.
	 * 
	 * @param loginView The login dialog view.
	 * 
	 *
	 * @since 4.0
	 */
	public abstract void amDialogLogin(AMLoginView loginView);
	
	/**
	 * Called when user cancels authorization.
	 * 
	 * @param cancelled true or false.
	 * 
	 *
	 * @since 4.0
	 */
	public abstract void amDialogNotLogin(boolean cancelled);
	
	/**
	 * Called when there is error during authorization.
	 * 
	 * @param error The AMError object which contains error details.	
	 * 
	 *
	 * @since 4.0
	 */
	public abstract void amDialogLoginFailure(AMError error);	
	
}

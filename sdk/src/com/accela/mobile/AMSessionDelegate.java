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
 *  Session delegate, defines the methods which will be called during the lifecycle of user session.
 * 
 * 	@since 1.0
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

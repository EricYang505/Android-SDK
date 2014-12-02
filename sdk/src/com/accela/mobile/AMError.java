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
 *  Error object wrapper 
 * 
 * @since 1.0
 */

public class AMError {


	/**
	 * A customized error code, which means timeout in HTTP request.
	 * 
	 * @since 4.0
	 */
	public static final String ERROR_CODE_REQUEST_TIMEOUT = "request_timeout_error";

	/**
	 * A customized error code, which returned from cloud API when an
	 * operation(e.g. result inspection) fails due to EMSE event.
	 * 
	 * @since 4.0
	 */
	public static final String ERROR_CODE_EMSE_FAILURE = "emse_failure_error";

	/**
	 * A customized error code, which returned from cloud API when an
	 * operation(e.g. result inspection) fails due to expired token or invalid token.
	 * 
	 * @since 4.0
	 */
	public static final String ERROR_CODE_TOKEN_INVALID = "invalid_token";	
	public static final String ERROR_CODE_TOKEN_EXPIRED = "token_expired";	
	public static final String ERROR_CODE_ACCESS_FORBIDDEN = "access_forbidden";
	public static final String ERROR_CODE_OTHER_ERROR = "other_error";
	public static final String ERROR_CODE_OUT_OF_MEMORY = "out_of_memory";
	
	public static final int STATUS_CODE_OTHER = 0;
	
	/**
	 * A customized error code, which means unavaiable host in HTTP request.
	 * 
	 * @since 4.0
	 */
	public static final String ERROR_CODE_UNAVAIABLE_HOST = "server_unavailable_error";

	/**
	 * A customized error code, which means error in JSON parsing.
	 * 
	 * @since 4.0
	 */
	public static String ERROR_CODE_JSON_PARSING = "json_parsing_error";

	/**
	 * A customized error code, which means error in business processing.
	 * 
	 * @since 4.0
	 */
	public static String ERROR_CODE_BUSINESS = "business_error";

	/**
	 * A customized error code, which means unexpected error(e.g. exceptions)
	 * 
	 * @since 4.0
	 */
	public static final int ERROR_CODE_EXCEPTION = 9999;

	/**
	 * The minimum status code for HTTP errors. Any code which is greater than
	 * this number means HTTP error.
	 * 
	 * @since 4.0
	 */
	public static final int ERROR_CODE_HTTP_MINIMUM = 300;

	/**
	 * HTTP standard error code, which indicates error in HTTP / HTTPS request
	 * syntax.
	 * 
	 * @since 3.0
	 */
	public static int ERROR_CODE_Bad_Request = 400;

	/**
	 * HTTP standard error code, which indicates error in client's authorization
	 * to service APIs.
	 * 
	 * @since 3.0
	 */
	public static int ERROR_CODE_Unauthorized = 401;

	/**
	 * HTTP standard error code, which indicates error in client's access to
	 * service APIs.
	 * 
	 * @since 3.0
	 */
	public static int ERROR_CODE_Forbidden = 403;

	/**
	 * HTTP standard error code, which indicates error in the requested URI of
	 * service APIs.
	 * 
	 * @since 3.0
	 */
	public static int ERROR_CODE_Not_Found = 404;

	/**
	 * HTTP standard error code, which indicates error in cloud server when
	 * calling service APIs.
	 * 
	 * @since 3.0
	 */
	public static int ERROR_CODE_Internal_Server_Error = 500;
	
	/**
	 * Error status.
	 */
	private int status;
	
	/**
	 * Error code.
	 */
	private String code;
	
	/**
	 * Trace Id for debugging the error on mobile server.
	 */
	private String traceId;

	/**
	 * Error message string.
	 */
	private String message;

	/**
	 * The more detailed error message string.
	 */
	private String more;

	/**
	 * Constructor with the given parameters.
	 * 
	 * @param statuc The error status.
	 * @param code The error code.
	 * @param traceId The trace Id for debugging the error on mobile server.
	 * @param message The error message string.
	 * @param more The more detailed error message string.
	 * 
	 * @return An initialized AMError object.
	 * 
	 * @since 1.0
	 */
	public AMError(int status,String code,String traceId, String message, String more) {
		this.status = status;
		this.code = code;
		this.traceId = traceId;
		this.message = message;
		this.more = more;
	}
	
	/**
	 * Get status of the current error.
	 * 
	 * @return The status.
	 * 
	 * @since 4.0
	 */
	public int getStatus() {
		return status;
	}
	
	/**
	 * Get code of the current error.
	 * 
	 * @return The code.
	 * 
	 * @since 4.0
	 */
	public String getCode() {
		return code;
	}
	
	/**
	 * Get trace Id of the current error.
	 * 
	 * @return The trace Id.
	 * 
	 * @since 1.0
	 */
	public String getTraceId() {
		return traceId;
	}

	/**
	 * Get error message of the current error.
	 * 
	 * @return The message content string.
	 * 
	 * @since 1.0
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Get more detailed error message of the current error.
	 * 
	 * @return The more detailed error message string.
	 * 
	 * @since 1.0
	 */
	public String getMore() {
		return more;
	}

	

	/**
	 * Convert the current error object to a string.
	 * 
	 * @return A string which contains the current error object's property
	 *         values.
	 * 
	 * @since 1.0
	 */
	public String toString() {
		StringBuilder buf = new StringBuilder();
		if (status != 0)
			buf.append("tatus: ").append(status).append(".\n");
		if (code != null)
			buf.append("Code: ").append(status).append(".\n");
		if (traceId != null)
			buf.append("Trace ID: ").append(traceId).append(".\n");
		if (message != null)
			buf.append("Error Message: ").append(message).append("\n");
		if (more != null)
			buf.append("More Detailed Error Message : ").append(more);
		return buf.toString();
	}

}

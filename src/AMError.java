package com.accela.mobile;

/**
 * <pre>
 * 
 *  Accela Amobile
 *  File: AError.java
 * 
 *  Accela, Inc.
 *  Copyright (C): 2013
 * 
 *  Description:
 *  Error object wrapper 
 * 
 *  Notes:
 * 	
 * 
 *  Revision History
 * 
 * 
 * @since 1.0
 * 
 * </pre>
 */

public class AMError {


	/**
	 * A customized error code, which means timeout in HTTP request.
	 * 
	 * @since 4.0
	 */
	public static final int ERROR_CODE_REQUEST_TIMEOUT = 1000;

	/**
	 * A customized error code, which returned from cloud API when an
	 * operation(e.g. result inspection) fails due to EMSE event.
	 * 
	 * @since 4.0
	 */
	public static final int ERROR_CODE_EMSE_FAILURE = 11000;

	/**
	 * A customized error code, which means unavaiable host in HTTP request.
	 * 
	 * @since 4.0
	 */
	public static final int ERROR_CODE_UNAVAIABLE_HOST = 11001;

	/**
	 * A customized error code, which means error in JSON parsing.
	 * 
	 * @since 4.0
	 */
	public static int ERROR_CODE_JSON_PARSING = 1008;

	/**
	 * A customized error code, which means error in business processing.
	 * 
	 * @since 4.0
	 */
	public static int ERROR_CODE_BUSINESS = 1009;

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
	 * Trace Id for debugging the error on mobile server.
	 */
	private String traceId;

	/**
	 * Error message string.
	 */
	private String errorMessage;

	/**
	 * The more detailed error message string.
	 */
	private String more;

	/**
	 * Constructor with the given parameters.
	 * 
	 * @param traceId
	 *            The trace Id for debugging the error on mobile server.
	 * @param errorMessage
	 *            The error message string.
	 * @param more
	 *            The more detailed error message string.
	 * 
	 * @return An initialized AMError object.
	 * 
	 * @since 1.0
	 */
	public AMError(String traceId, String errorMessage, String more) {
		this.traceId = traceId;
		this.errorMessage = errorMessage;
		this.more = more;
	}

	/**
	 * Get error message of the current error.
	 * 
	 * @return The message content string.
	 * 
	 * @since 1.0
	 */
	public String getErrorMessage() {
		return errorMessage;
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
	 * Convert the current error object to a string.
	 * 
	 * @return A string which contains the current error object's property
	 *         values.
	 * 
	 * @since 1.0
	 */
	public String toString() {
		StringBuilder buf = new StringBuilder();
		if (traceId != null)
			buf.append("Trace ID: ").append(traceId).append(".\n");
		if (errorMessage != null)
			buf.append("Error Message: ").append(errorMessage).append("\n");
		if (more != null)
			buf.append("More Detailed Error Message : ").append(more);
		return buf.toString();
	}

}

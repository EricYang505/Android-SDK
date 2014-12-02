package com.accela.mobile;

import android.util.Log;

/**
 * <pre>
 * 
 *  Accela Amobile
 *  File: AMLogger.java
 * 
 *  Accela, Inc.
 *  Copyright (C): 2013
 * 
 *  Description:
 *  Logger object, used to print or save log entities based on various log levels.
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
public class AMLogger {
	
	/**
	 *  Log level enumerations
	 */
	private enum AMLogLevel
	{
		AMLogError, AMLogWarn, AMLogInfo, AMLogVerbose
	}
	
	/**
	 * Tag name for logging
	 */
	private static final String LOG_TAG = "AMLogger";
	
	/**
	 * Log level
	 */
	private static AMLogLevel logLevel;		
	
	/**
	 * Private method, used to inflate a string with variable placeholders
	 */
	private static String inflateLogMessage(String format, Object... args) {	
		
		String logMessage = null;
		int argsNum = format.split("%").length - 1;
		
		if (args.length ==0) {
			logMessage = format;
		} else if (argsNum == args.length) {
			logMessage = String.format(format, args);		
		} else {
			logMessage = "Error: Arguments doesn't match format!";
		}
		return  logMessage;
	}
	
	/**
	 * 
	 * Create a formatted log message with Error level
	 * 
	 * @param format A string which contains placeholders which will be replaced by the variables listed in the second parameter args.
	 * 						  For example: "Got  %d records with type %s "
	 * @param args A list of variables.
	 * 
	 * @return Void.
	 * 
	 * @since 1.0
	 */
	public static void logError(String format, Object... args) {
		if (logLevel != AMLogLevel.AMLogError) {
			logLevel = AMLogLevel.AMLogError;
		}
	
		String str = AMLogger.inflateLogMessage(format, args);
		Log.e(LOG_TAG, "Error_Log: " + str);		
	}

	/**
	 * 
	 * Create a formatted log message with Info level
	 * 
	 * @param format A string which contains placeholders which will be replaced by the variables listed in the second parameter args.
	 * 						  For example: "Got  %d records with type %s "
	 * @param args A list of variables.
	 * 
	 * @return Void.
	 * 
	 * @since 1.0
	 */
	public static void logInfo(String format, Object... args) {
		if (logLevel != AMLogLevel.AMLogInfo) {
			logLevel = AMLogLevel.AMLogInfo;
		}
		String str = AMLogger.inflateLogMessage(format, args);
		Log.i(LOG_TAG, "Info_Log: " + str);
	}

	/**
	 * 
	 * Create a formatted log message with Verbose level
	 * 
	 * @param format A string which contains placeholders which will be replaced by the variables listed in the second parameter args.
	 * 						   For example: "Got  %d records with type %s "
	 * @param args A list of variables.
	 * 
	 * @return Void.
	 * 
	 * @since 1.0
	 */
	public static void logVerbose(String format, Object... args) {
		if (logLevel != AMLogLevel.AMLogVerbose) {
			logLevel = AMLogLevel.AMLogVerbose;
		}
		String str = AMLogger.inflateLogMessage(format, args);
		Log.v(LOG_TAG, "Verbose_Log: " + str);
	}

	/**
	 * 
	 * Create a formatted log message with Warn level
	 * 
	 * @param format A string which contains placeholders which will be replaced by the variables listed in the second parameter args.
	 * 						   For example: "Got  %d records with type %s "
	 * @param args A list of variables.
	 * 
	 * @return Void.
	 * 
	 * @since 1.0
	 */
	public static void logWarn(String format, Object... args) {
		if (logLevel != AMLogLevel.AMLogWarn) {
			logLevel = AMLogLevel.AMLogWarn;
		}
		String str = AMLogger.inflateLogMessage(format, args);
		Log.w(LOG_TAG, "Warn_Log: " + str);	
	}	
	
	
	/**
	 * 
	 * Set level of the logger
	 * 
	 * @param level  One of enumeration values defined in AMEnumUtil.LogLevel .
	 * 
	 * @return Void.
	 * 
	 * @since 1.0
	 */
	public static void setLogLevelr(AMLogLevel level) {		
		logLevel = level;		
	}
	
}


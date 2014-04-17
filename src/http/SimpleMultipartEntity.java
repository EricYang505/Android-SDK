
package com.accela.mobile.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ResourceBundle;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicHeader;

import com.accela.mobile.AMLogger;
import com.accela.mobile.AMSetting;

/**
 * <pre>
 * 
 *  Accela Amobile
 *  File: SimpleMultipartEntity.java
 * 
 *  Accela, Inc.
 *  Copyright (C): 2012
 * 
 *  Description:
 *  A multipart HttpEntity wrapper object, used to handle file uploading .
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

class SimpleMultipartEntity implements HttpEntity {
	static final String MULTIPART_SEPARATOR_LINE = "------------MULTIPART_SEPARATOR_LINE";
	static final String MULTIPART_File_KEY = "uploadedFile";
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    boolean isSetLast = false;
    boolean isSetFirst = false;
    
	private ResourceBundle stringLoader = AMSetting.getStringResourceBundle();

    /**
	 * 
	 * Constructor.
	 * 
	 * @return An initialized SimpleMultipartEntity instance.
	 * 
	 * @since 1.0
	 */
    SimpleMultipartEntity() {}

    /**
	 * 
	 * Write boundary line to output byte array if it is the first part to be added.
	 * 
	 * @return Void.
	 * 
	 * @since 1.0
	 */
    void writeFirstBoundaryIfNeeds() {
        if (!isSetFirst){        	 
            try {
                byteArrayOutputStream.write(("--" + MULTIPART_SEPARATOR_LINE + "\r\n").getBytes());
            } catch (IOException e) {
            	AMLogger.logError("In SimpleMultipartEntity.writeFirstBoundaryIfNeeds(): IOException " + stringLoader.getString("Log_Exception_Occured"), e.getMessage());
    			if (AMSetting.DebugMode) {
    				e.printStackTrace();
    			}
            }
            isSetFirst = true;
        }
    }

    /**
	 * 
	 * Write boundary line to output byte array if it is the last part to be added.
	 * 
	 * @return Void.
	 * 
	 * @since 1.0
	 */
    void writeLastBoundaryIfNeeds(boolean isLast) {
    	String separatorLine = null;
    	if (isLast) {
    		separatorLine = "\r\n--" + MULTIPART_SEPARATOR_LINE + "--\r\n";
    		isSetLast = true;
       	} else {
       		separatorLine = "\r\n--" + MULTIPART_SEPARATOR_LINE + "\r\n";
       	}
       
        try {
            byteArrayOutputStream.write(separatorLine.getBytes());
        } catch (IOException e) {
        	AMLogger.logError("In SimpleMultipartEntity.writeLastBoundaryIfNeeds(): IOException " + stringLoader.getString("Log_Exception_Occured"), e.getMessage());
			if (AMSetting.DebugMode) {
				e.printStackTrace();
			}
        }                
    }

    /**
	 * 
	 * Add a text part which value is string.
	 * 
	 * @param key The key name.
	 * @param value The string value of the key.
	 * 
	 * @return Void.
	 * 
	 * @since 1.0
	 */
    void addPart(String key, String value) {
        writeFirstBoundaryIfNeeds();
        try {
        	String headerString = "Content-Disposition: form-data; name=\"" +key+"\"\r\n\r\n";
            byteArrayOutputStream.write(headerString.getBytes());
            byteArrayOutputStream.write(value.getBytes());
            byteArrayOutputStream.write(("\r\n--" + MULTIPART_SEPARATOR_LINE + "\r\n").getBytes());           
        } catch (IOException e) {
        	AMLogger.logError("In SimpleMultipartEnti(): Exception " + stringLoader.getString("Log_Exception_Occured"), e.getMessage());
			if (AMSetting.DebugMode) {
				e.printStackTrace();
			} 
        }
    }

    /**
	 * 
	 * Add a file part which with the given parameters.
	 * 
	 * @param key The key name.
	 * @param fileName The file's name.
	 * @param fin The file's input stream object.
	 * @param isLast Indicates whether it is the last part to be added.
	 * 
	 * @return Void.
	 * 
	 * @since 1.0
	 */
    void addPart(String key, String fileName, InputStream fin, boolean isLast) {
        addPart(key, fileName, fin, "application/octet-stream", isLast);
    }

    /**
	 * 
	 * Add a file part which with the given parameters.
	 * 
	 * @param key The key name.
	 * @param fileName The file's name.
	 * @param fin The file's input stream object.
	 * @param type The content of the the input stream.
	 * @param isLast Indicates whether it is the last part to be added.
	 * 
	 * @return Void.
	 * 
	 * @since 1.0
	 */
    void addPart(String key, String fileName, InputStream fin, String type, boolean isLast) {
        try {
        	writeFirstBoundaryIfNeeds();                
	       	type = "Content-Type: "+type+"\r\n\r\n";
	       	String headerString = "Content-Disposition: form-data; name=\""+ key+"\"; filename=\"" + fileName + "\"\r\n";           
	       	byteArrayOutputStream.write(headerString.getBytes());
	       	byteArrayOutputStream.write(type.getBytes());
	       	byte[] tmp = new byte[10240]; //10MB
	       	int l = 0;
	       	while ((l = fin.read(tmp)) != -1) {
	       		byteArrayOutputStream.write(tmp, 0, l);
	       	}   
	       	writeLastBoundaryIfNeeds(isLast);
	       	byteArrayOutputStream.flush();
        } catch (IOException e) {
        	AMLogger.logError("In SimpleMultipartEntity.addPart(): IOException " + stringLoader.getString("Log_Exception_Occured"), e.getMessage());
			if (AMSetting.DebugMode) {
				e.printStackTrace();
			}
        } finally {
            try {
                fin.close();
            } catch (IOException e) {
            	AMLogger.logError("In SimpleMultipartEntity.addPart(): IOException " + stringLoader.getString("Log_Exception_Occured"), e.getMessage());
    			if (AMSetting.DebugMode) {
    				e.printStackTrace();
    			}
            }
        }
    }

    /**
	 * 
	 * Add a file part which with the given parameters.
	 * 
	 * @param key The key name.
	 * @param value The file object to be added.
	 * @param isLast Indicates whether it is the last part to be added.
	 * 
	 * @return Void.
	 * 
	 * @since 1.0
	 */
    void addPart(String key, File value, boolean isLast) {
        try {
            addPart(key, value.getName(), new FileInputStream(value), isLast);
        } catch (FileNotFoundException e) {
        	AMLogger.logError("In SimpleMultipartEntity.addPart(): IOException " + stringLoader.getString("Log_Exception_Occured"), e.getMessage());
			if (AMSetting.DebugMode) {
				e.printStackTrace();
			}
        }
    }    
    

    /**
	 * 
	 * Get content length of the current http entity's content.
	 * 
	 * @return A number.
	 * 
	 * @since 1.0
	 */
    public long getContentLength() {
        writeLastBoundaryIfNeeds(true);
        return byteArrayOutputStream.toByteArray().length;
    }

    /**
	 * 
	 * Get content type of the current http entity.
	 * 
	 * @return A Header object.
	 * 
	 * @since 1.0
	 */
    public Header getContentType() {
        return new BasicHeader("Content-Type", "multipart/form-data; boundary=" + MULTIPART_SEPARATOR_LINE);
    }

    /**
	 * 
	 * Get whether the current http entity's content is chunked or not.
	 * 
	 * @return true or false.
	 * 
	 * @since 1.0
	 */
    public boolean isChunked() {
        return false;
    }

    /**
	 * 
	 * Get whether the current http entity's content is repeatable or not.
	 * 
	 * @return true or false.
	 * 
	 * @since 1.0
	 */
    public boolean isRepeatable() {
        return false;
    }

    /**
	 * 
	 * Get whether the current http entity's content is streaming or not.
	 * 
	 * @return true or false.
	 * 
	 * @since 1.0
	 */
    public boolean isStreaming() {
        return false;
    }

    /**
	 * 
	 * Write the current http entity's content to the given output stream.
	 * 
	 * @param outstream The target ouptput stream object.
	 * 
	 * @return Void.
	 * 
	 * @since 1.0
	 */
    public void writeTo(OutputStream outstream) throws IOException {
        outstream.write(byteArrayOutputStream.toByteArray());
    }

    /**
	 * 
	 * Get content encoding of the current http entity.
	 * 
	 * @return A Header object.
	 * 
	 * @since 1.0
	 */
    public Header getContentEncoding() {
        return null;
    }

    /**
	 * 
	 * Check whether the http entity is being consumed.
	 * 
	 * @return Void.
	 * 
	 * @throws IOException, UnsupportedOperationException
	 * 
	 * @since 1.0
	 */
    public void consumeContent() throws IOException, UnsupportedOperationException {
        if (isStreaming()) {
            throw new UnsupportedOperationException(
            "Streaming entity does not implement #consumeContent()");
        }
    }

    /**
	 * 
	 * Get content of the current http entity.
	 * 
	 * @return An InputStream object.
	 * 
	 * @throws IOException, UnsupportedOperationException
	 * 
	 * @since 1.0
	 */
    public InputStream getContent() throws IOException, UnsupportedOperationException { 	    	
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }
   
    
}
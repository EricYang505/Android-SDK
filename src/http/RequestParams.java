
package com.accela.mobile.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.accela.mobile.AMLogger;
import com.accela.mobile.AMSetting;


/**
 * <pre>
 * 
 *  Accela Amobile
 *  File: RequestParams.java
 * 
 *  Accela, Inc.
 *  Copyright (C): 2013
 * 
 *  Description:
 *  Request parameters wrapper object, used to wrap parameters which will be sent together with HTTP GET/POST/PUT requests.
 * 
 *  Notes:
 * A collection of string request parameters or files to send along with
 * requests made from an {@link AsyncHttpClient} instance.
 * <p>
 * For example:
 * <p>
 * <pre>
 * RequestParams params = new RequestParams();
 * params.put("username", "james");
 * params.put("password", "123456");
 * params.put("email", "my&#064;email.com");
 * params.put("profile_picture", new File("pic.jpg")); // Upload a File
 * params.put("profile_picture2", someInputStream); // Upload an InputStream
 * params.put("profile_picture3", new ByteArrayInputStream(someBytes)); // Upload some bytes
 *
 * AsyncHttpClient client = new AsyncHttpClient();
 * client.post("http://myendpoint.com", params, responseHandler);
 * 
 *  Revision History  
 * 
 * 	@since 1.0
 * 
 * </pre>
 */


public class RequestParams {        
    private static final String ENCODING = "UTF-8";
    private static final String KEY_NAME_FOR_JSON = "json";
    protected ConcurrentHashMap<String, String> urlParams;
   
	protected ConcurrentHashMap<String, FileWrapper> fileParams;

    private ResourceBundle stringLoader = AMSetting.getStringResourceBundle();

    /**
	 * Constructor without parameters.
	 * 
	 * @return An initialized RequestParams instance.
	 * 
	 * @since 1.0
	 */
    public RequestParams() {
        init();
    }

    /**
     * Constructs a new RequestParams instance containing the key/value
     * string parameters from the specified map.
     * 
     * @param source The source key/value string map to add.
     * 
     * @return An initialized RequestParams instance.
	 * 
	 * @since 1.0
	 */
    public RequestParams(Map<String, String> source) {
        init();

        for(Map.Entry<String, String> entry : source.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * Constructs a new RequestParams instance containing a JSON object.
     * Typically this constructor is only used in POSTrequest when the post data is a JSON object
     * which contains child JSON objects.
     * 
     * @param rootKey The name of the root key.
     * @param jsonContent The JSON object which maps to the root key.
     * 
     * @return An initialized RequestParams instance.
	 * 
	 * @since 1.0
	 */
    public RequestParams (String rootKey, JSONObject jsonContent) {
        init(); 
        JSONObject jsonToPost = new JSONObject();   
        if (rootKey != null) {	           
	        try {
	        	jsonToPost.put(rootKey, jsonContent);
			} catch (JSONException e) {
				e.printStackTrace();
			}    
        } else {
        	jsonToPost = jsonContent;
        }
        put(KEY_NAME_FOR_JSON, jsonToPost.toString());    
    }  

    /**
     * Constructs a new RequestParams instance and populate it with a single
     * initial key/value string parameter.
     * 
     * @param key The key name for the initial parameter.
     * @param value The value string for the initial parameter.
     * 
     * @return An initialized RequestParams instance.
	 * 
	 * @since 1.0
	 */
    public RequestParams(String key, String value) {
        init();

        put(key, value);
    }

	/**
     * Constructs a new RequestParams instance containing a JSON object.
     * Typically this constructor is only used in POSTrequest when the post data is a JSON object
     * which contains child JSON objects.
     * 
     * @param source The JSON object which contains key/value string map to add.
     * 
     * @return An initialized RequestParams instance.
	 * 
	 * @since 1.0
	 */
    public RequestParams (JSONObject source) {
        init();
       
		put(KEY_NAME_FOR_JSON, source.toString());
     
    }    
    
    public Map<String, String> getUrlParams(){
    	return this.urlParams;
    }
    
    public Map<String, FileWrapper> getFileParams(){
    	return this.fileParams;
    }
    
    /**
     * Adds a key/value string pair to the request.
     * 
     * @param key The key name for the new parameter.
     * @param value The value string for the new parameter.
     * 
     * @return Void.
	 * 
	 * @since 1.0
	 */
    public void put(String key, String value) {
        if(key != null && value != null) {
            urlParams.put(key, value);
        }
    }   
    

    /**
     * Adds a file to the request.
     * 
     * @param key The key name for the new parameter.
     * @param file The file to add.
     * 
     * @return Void.
	 * 
	 * @since 1.0
	 */
    public void put(String key, File file) throws FileNotFoundException {
        put(key, new FileInputStream(file), file.getName());
    }
    
   /**
 	* Adds an input stream to the request.
 	* 
 	* @param key The key name for the new parameter.
 	* @param stream The input stream to add.
 	* 
 	* @return Void.
 	* 
 	* @since 1.0
 	*/
	public void put(String key, InputStream stream) {
    	put(key, stream, null);
	}    
    
    /**
     * Adds an input stream to the request.
     * 
     * @param key The key name for the new parameter.
     * @param stream The input stream to add.
     * @param fileName The name of the file.
     * 
     * @return Void.
	 * 
	 * @since 1.0
	 */
    public void put(String key, InputStream stream, String fileName) {
        put(key, stream, fileName, null);
    }

    /**
     * Adds an input stream to the request.
     * 
     * @param key The key name for the new parameter.
     * @param stream The input stream to add.
     * @param fileName The name of the file.
     * @param contentType The content type of the file, eg. application/json.
     * 
     * @return Void.
	 * 
	 * @since 1.0
	 */
    public void put(String key, InputStream stream, String fileName, String contentType) {
        if(key != null && stream != null) {
        	  fileParams.put(key, new FileWrapper(stream, fileName, contentType));
        }
    }

    /**
     * Removes a parameter from the request.
     * 
     * @param key The key name for the parameter to remove.
     * 
     * @return Void.
	 * 
	 * @since 1.0
	 */
    public void remove(String key) {
        urlParams.remove(key);
        fileParams.remove(key);
    }

    /**
     * Assemble the current object's content into a string.
     * 
     * @return A string.
	 * 
	 * @since 1.0
	 */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for(ConcurrentHashMap.Entry<String, String> entry : urlParams.entrySet()) {
            if(result.length() > 0) {
                result.append("&");
            }
            result.append(entry.getKey());
            result.append("=");
            result.append(entry.getValue());
        }

        for(ConcurrentHashMap.Entry<String, FileWrapper> entry : fileParams.entrySet()) {
            if(result.length() > 0) {
                result.append("&");
            }
            result.append(entry.getKey());
            result.append("=");
            result.append("FILE");
        }

        return result.toString();
    }
 
    
    /**
     * Get the HttpEntity which contains all request parameters
     * 
     * @return A HttpEntity object.
	 * 
	 * @since 1.0
	 */   
    public HttpEntity getEntity() {
    	HttpEntity entity = null;

        if(!fileParams.isEmpty()) {
            SimpleMultipartEntity multipartEntity = new SimpleMultipartEntity();
            // Add string parameters
             for(ConcurrentHashMap.Entry<String, String> entry : urlParams.entrySet()) {
                multipartEntity.addPart(entry.getKey(), entry.getValue());
            }

            // Add file parameters
            int currentFileIndex = 0;
            int lastFileIndex = fileParams.entrySet().size() - 1;
            for(ConcurrentHashMap.Entry<String, FileWrapper> entry : fileParams.entrySet()) {
                FileWrapper file = entry.getValue();
                if(file.inputStream != null) {
                	boolean isLastFile = currentFileIndex == lastFileIndex;                	
                    if(file.contentType != null) {
                        multipartEntity.addPart(SimpleMultipartEntity.MULTIPART_File_KEY, file.getFileName(), file.inputStream, file.contentType, isLastFile);
                    } else {
                        multipartEntity.addPart(SimpleMultipartEntity.MULTIPART_File_KEY, file.getFileName(), file.inputStream, isLastFile);
                    }
                }
                currentFileIndex++;
            } 

            //For testing only
            //multipartEntity.printStreamToFile();
            
            entity = multipartEntity;
        } else {
            try {            	
                entity = new UrlEncodedFormEntity(getParamsList(), ENCODING);   
            } catch (UnsupportedEncodingException e) {
            	AMLogger.logError("In RequestParams.getEntity(): UnsupportedEncodingException " + stringLoader.getString("Log_Exception_Occured"), e.getMessage());
				if (AMSetting.DebugMode) {
					e.printStackTrace();
				}              
            }
        }        
      
        return entity;
    }    
   

    /**
     * Get the StringEntity which contains all request parameters
     * 
     * @param isJson Indicate whether it is JSON structure.
     * 
     * @return The StringEntity object got from the current RequestParams object.
	 * 
	 * @since 1.0
	 */     
    public StringEntity getStringEntity(Boolean isJson) {
    	StringEntity stringEntity = null;    	
    	if (isJson) {
	        if(!urlParams.isEmpty()) {
	        	if  (this.hasKey(KEY_NAME_FOR_JSON)) {        		
					try {
						stringEntity = new StringEntity(this.getParaValue(KEY_NAME_FOR_JSON));
					} catch (UnsupportedEncodingException e) {
						AMLogger.logError("In RequestParams.getStringEntity(): UnsupportedEncodingException " + stringLoader.getString("Log_Exception_Occured"), e.getMessage());
						if (AMSetting.DebugMode) {
							e.printStackTrace();
						}   						
					}						
	        	} else {
	        	JSONObject jsonObject = new JSONObject();
	        	  for(ConcurrentHashMap.Entry<String, String> entry : urlParams.entrySet()) {
	        		  try {
						jsonObject.put(entry.getKey(), entry.getValue());
						stringEntity = new StringEntity(jsonObject.toString());					
					} catch (JSONException e) {		
						AMLogger.logError("In RequestParams.getStringEntity(): JSONException " + stringLoader.getString("Log_Exception_Occured"), e.getMessage());
						if (AMSetting.DebugMode) {
							e.printStackTrace();
						}
					} catch (UnsupportedEncodingException e) {
						AMLogger.logError("In RequestParams.getStringEntity(): UnsupportedEncodingException " + stringLoader.getString("Log_Exception_Occured"), e.getMessage());
						if (AMSetting.DebugMode) {
							e.printStackTrace();
						}
					}
	              }
	        	}
	        }
    	} else {    		 
    		 if(!urlParams.isEmpty())
    		 {    			 
    			 String paramsKeyValStr = "";
    			 for(ConcurrentHashMap.Entry<String, String> entry : urlParams.entrySet()) {
    				 paramsKeyValStr += "&" +entry.getKey()+"="+entry.getValue();
    			 }    			 
    			 paramsKeyValStr = paramsKeyValStr.substring(1); // Skip the 1st space char	
    			 try {
					stringEntity = new StringEntity(paramsKeyValStr);
				} catch (UnsupportedEncodingException e) {
					AMLogger.logError("In RequestParams.getStringEntity(): UnsupportedEncodingException " + stringLoader.getString("Log_Exception_Occured"), e.getMessage());
					if (AMSetting.DebugMode) {
						e.printStackTrace();
					}   
				}			    				
    		 }
    	}
    	
        return stringEntity;
    }      
    
    /**
     * Get a parameter's value by its name
     * 
     * @param paraName The parameter name.
     *      
     * @return The parameter's value.
	 * 
	 * @since 1.0
	 */
    public String getParaValue(String paraName) {
    
    	return urlParams.get(paraName);
    }

	 /**
	 * Check whether a parameter exists or not.
	 * 
	 * @param key A key name.
	 * 
	 * @return Return true if the parameter exists; Otherwise, return false.
	 * 
	 * @since 1.0
	 */
	 public Boolean hasKey(String key) {	    
	    return urlParams.containsKey(key);
	 }  
    
	/**
	 * Convert the parameters to JSON.
	 *
     * @return A JSON string which presents the RequestParams objectt.
	 * 
	 * @since 4.0
	 */
    public String getJsonString() { 
    	String returnedJson = null;    	  
    	int paramSize = urlParams.size();        
        if ((paramSize == 1) && (urlParams.containsKey(KEY_NAME_FOR_JSON))) {
        	returnedJson = urlParams.get(KEY_NAME_FOR_JSON);        	
        } else if (paramSize > 1) {
        	String jsonValueString = ""; 
	        for(ConcurrentHashMap.Entry<String, String> entry : urlParams.entrySet()) {
	        	jsonValueString += "\"" + entry.getKey() + "\":\"" +  entry.getValue() + "\",";
	        }        
	        // Remove the last comma
	        if (!"".equalsIgnoreCase(jsonValueString)) {
	        	jsonValueString = jsonValueString.substring(0, jsonValueString.length()-1);
	        }
	        returnedJson = "{" + jsonValueString + "}";
        } else {
        	returnedJson = "{}";
        }
       
        return returnedJson;
    }
    
    /**
	 * Convert the parameter list to string in format key1=value1&key2=value2.
	 *
	 * @return A text string which joins the parameter name and value with & symbol.
	 * 
	 * @since 4.0
	 */
    public String getParamString() {
        return URLEncodedUtils.format(getParamsList(), ENCODING);
   
    }
    
    /**
	 * Protected method, used to get the list of the current URL parameters.
	 */	
    protected List<BasicNameValuePair> getParamsList() {
        List<BasicNameValuePair> lparams = new LinkedList<BasicNameValuePair>();
        
        for(ConcurrentHashMap.Entry<String, String> entry : urlParams.entrySet()) {
            lparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        return lparams;
    }

    /**
	 * Private method, used to initialize parameter collection objects.
	 */	
    private void init() {
        urlParams = new ConcurrentHashMap<String, String>();
        fileParams = new ConcurrentHashMap<String, FileWrapper>();
    }
    
    /**
	 * Private inner static class, used to wrap parameters related for file uploading.
	 */	
    private static class FileWrapper {
        public InputStream inputStream;
        public String fileName;
        public String contentType;

        public FileWrapper(InputStream inputStream, String fileName, String contentType) {
            this.inputStream = inputStream;
            this.fileName = fileName;
            this.contentType = contentType;
        }

        public String getFileName() {
            if(fileName != null) {
                return fileName;
            } else {
                return "nofilename";
            }
        }
    }
  
}
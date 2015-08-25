
/**
 * Created by eyang on 8/20/15.
 */
package com.accela.mobile.http;

        import java.io.File;
        import java.io.FileNotFoundException;
        import java.io.UnsupportedEncodingException;
        import java.util.HashMap;
        import java.util.LinkedList;
        import java.util.List;
        import java.util.Map;
        import java.util.ResourceBundle;
        import java.util.concurrent.ConcurrentHashMap;

        import org.json.JSONException;
        import org.json.JSONObject;

        import com.accela.mobile.AMLogger;
        import com.accela.mobile.AMSetting;
        import com.accela.mobile.http.volley.Legacy.BasicNameValuePair;
        import com.accela.mobile.http.volley.Legacy.URLEncodedUtils;


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

    protected Map<String, String> urlParams;

    protected Map<String, String> authBody;

    protected ConcurrentHashMap<String, String> stringBody;


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

    public RequestParams (JSONObject jsonObject) {
        init();
        throw new RuntimeException("not implemented yet!");
    }

    public RequestParams (String stringBody) {
        init();
        this.stringBody.put(KEY_NAME_FOR_JSON, stringBody);
    }

    public void setUrlParams(Map<String, String> urlParams){
        this.urlParams = urlParams;
    }

    public void setAuthBody(Map<String, String> authBody){
        this.authBody = authBody;
    }

    public Map<String, String> getUrlParams(){
        return this.urlParams;
    }

    public Map<String, FileWrapper> getFileParams(){
        return this.fileParams;
    }


    public String getStringBody()  {
        String stringBody = null;
        if (!this.stringBody.isEmpty()) {
            if (this.stringBody.containsKey(KEY_NAME_FOR_JSON)) {
                 stringBody = this.stringBody.get(KEY_NAME_FOR_JSON);
            } else {
                JSONObject jsonObject = new JSONObject();
                for (ConcurrentHashMap.Entry<String, String> entry : this.stringBody.entrySet()) {
                    try {
                        jsonObject.put(entry.getKey(), entry.getValue());
                        stringBody = jsonObject.toString();
                    } catch (JSONException e) {
                        AMLogger.logError("In RequestParams.getStringEntity(): JSONException " + stringLoader.getString("Log_Exception_Occured"), e.getMessage());
                        if (AMSetting.DebugMode) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return stringBody;
    }


    public void put(String key, String value) {
        // put(key, new FileInputStream(file), file.getName());
        if (key != null && value != null) {
            stringBody.put(key, value);
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
        // put(key, new FileInputStream(file), file.getName());
        if (key != null && file != null) {
            fileParams.put(key, new FileWrapper(file, null));
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
        authBody.remove(key);
        stringBody.remove(key);
    }


    public String toString() {
        StringBuilder result = new StringBuilder();
        for(ConcurrentHashMap.Entry<String, String> entry : urlParams.entrySet()) {
            if(result.length() > 0) {
                result.append("&");
            }
            result.append(entry.getKey());
            result.append("=");
            result.append(entry.getValue().toString());
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

    public String getAuthBody() throws JSONException {
        StringBuilder sb = new StringBuilder();

        if(!authBody.isEmpty()){
            for(ConcurrentHashMap.Entry<String, String> entry : authBody.entrySet()) {
                    sb.append("&");
                    sb.append(entry.getKey());
                    sb.append("=");
                    sb.append(entry.getValue());
            }
        }
        return sb.toString().substring(1, sb.length());
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
        stringBody = new ConcurrentHashMap<String, String>();
        urlParams = new HashMap<String, String>();
        authBody = new HashMap<String, String>();
        fileParams = new ConcurrentHashMap<String, FileWrapper>();
    }

    /**
     * Private inner static class, used to wrap parameters related for file uploading.
     */
    private static class FileWrapper {
        public File mfile;
        public String contentType;

        public FileWrapper(File file, String contentType) {
            this.mfile = file;
            this.contentType = contentType;
        }
    }

}
package com.accela.mobile.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.accela.mobile.AMLogger;
import com.accela.mobile.AMSetting;

/**
 * <pre>
 * 
 *  Accela Amobile
 *  File: PersistentCookieStore.java
 * 
 *  Accela, Inc.
 *  Copyright (C): 2012
 * 
 *  Description:
 *  Cookie store object, used to keep cookie for user sessions.
 * 
 *  Notes:
 * A persistent cookie store which implements the Apache HttpClient
 * {@link CookieStore} interface. Cookies are stored and will persist on the
 * user's device between application sessions since they are serialized and
 * stored in {@link SharedPreferences}.
 * <p>
 * Instances of this class are designed to be used with
 * {@link AsyncHttpClient#setCookieStore}, but can also be used with a 
 * regular old apache HttpClient/HttpContext if you prefer.
 * 
 *  Revision History
 *  
 * 
 * 	@since 1.0
 * 
 * </pre>
 */

class PersistentCookieStore implements CookieStore {
    private String cookiePrefFileName;
    private static final String COOKIE_NAME_STORE = "names";
    private static final String COOKIE_NAME_PREFIX = "cookie_";

    private final ConcurrentHashMap<String, Cookie> cookies;
    private final SharedPreferences cookiePrefs;
    
	private ResourceBundle stringLoader = AMSetting.getStringResourceBundle();

    /**
	 * Constructor.
	 * 
	 * @param context The context which will hold the cookie store. 
	 * 
	 * @return An initialized PersistentCookieStore instance
	 * 
	 * @since 1.0
	 */
    public PersistentCookieStore(Context context) {    	
    	cookiePrefFileName = "CookiePrefsFile_" + context.getApplicationContext().getPackageName();    	
        cookiePrefs = context.getSharedPreferences(cookiePrefFileName, 0);
        cookies = new ConcurrentHashMap<String, Cookie>();

        
        // Load any previously stored cookies into the store
        String storedCookieNames = cookiePrefs.getString(COOKIE_NAME_STORE, null);
        if(storedCookieNames != null) {
            String[] cookieNames = TextUtils.split(storedCookieNames, ",");
            for(String name : cookieNames) {
                String encodedCookie = cookiePrefs.getString(COOKIE_NAME_PREFIX + name, null);
                if(encodedCookie != null) {
                    Cookie decodedCookie = decodeCookie(encodedCookie);
                    if(decodedCookie != null) {
                        cookies.put(name, decodedCookie);
                    }
                }
            }

            // Clear out expired cookies
            clearExpired(new Date());
        }
    }

    /**
	 * Add a cookie to the current cookie store.
	 * 
	 * @param cookie A Cookie object. 
	 * 
	 * @return Void.
	 * 
	 * @since 1.0
	 */
    public void addCookie(Cookie cookie) {
        String name = cookie.getName();

        // Save cookie into local store, or remove if expired
        if(!cookie.isExpired(new Date())) {
            cookies.put(name, cookie);
        } else {
            cookies.remove(name);
        }

        // Save cookie into persistent store
        SharedPreferences.Editor prefsWriter = cookiePrefs.edit();
        prefsWriter.putString(COOKIE_NAME_STORE, TextUtils.join(",", cookies.keySet()));
        prefsWriter.putString(COOKIE_NAME_PREFIX + name, encodeCookie(new SerializableCookie(cookie)));
        prefsWriter.commit();
    }

    /**
	 * Clear the current cookie store.
	 * 
	 * @return Void.
	 * 
	 * @since 1.0
	 */
    public void clear() {
        // Clear cookies from local store
        cookies.clear();

        // Clear cookies from persistent store
        SharedPreferences.Editor prefsWriter = cookiePrefs.edit();
        for(String name : cookies.keySet()) {
            prefsWriter.remove(COOKIE_NAME_PREFIX + name);
        }
        prefsWriter.remove(COOKIE_NAME_STORE);
        prefsWriter.commit();
    }

    /**
	 * Add cookies which expires after the given date.
	 * 
	 * @param date A date. 
	 * 
	 * @return Return true if one or more cookies have been cleared; Otherwise, return false.
	 * 
	 * @since 1.0
	 */
    public boolean clearExpired(Date date) {
        boolean clearedAny = false;
        SharedPreferences.Editor prefsWriter = cookiePrefs.edit();

        for(ConcurrentHashMap.Entry<String, Cookie> entry : cookies.entrySet()) {
            String name = entry.getKey();
            Cookie cookie = entry.getValue();
            if(cookie.isExpired(date)) {
                // Clear cookies from local store
                cookies.remove(name);

                // Clear cookies from persistent store
                prefsWriter.remove(COOKIE_NAME_PREFIX + name);

                // We've cleared at least one
                clearedAny = true;
            }
        }

        // Update names in persistent store
        if(clearedAny) {
            prefsWriter.putString(COOKIE_NAME_STORE, TextUtils.join(",", cookies.keySet()));
        }
        prefsWriter.commit();

        return clearedAny;
    }

    /**
	 * Add the list of all cookies in the current cookie store.
	 * 
	 * @return A list of cookie objects.
	 * 
	 * @since 1.0
	 */
    public List<Cookie> getCookies() {
        return new ArrayList<Cookie>(cookies.values());
    }

    /**
	 * Protected method, used to encode cookie.
	*/
    protected String encodeCookie(SerializableCookie cookie) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(os);
            outputStream.writeObject(cookie);
        } catch (Exception e) {
            return null;
        }

        return byteArrayToHexString(os.toByteArray());
    }

    /**
	 * Protected method, used to decode cookie.
	*/
    protected Cookie decodeCookie(String cookieStr) {
        byte[] bytes = hexStringToByteArray(cookieStr);
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        Cookie cookie = null;
        try {
           ObjectInputStream ois = new ObjectInputStream(is);
           cookie = ((SerializableCookie)ois.readObject()).getCookie();
        } catch (Exception e) {
        	AMLogger.logError("In PersistentCookieStore.decodeCookie(): Exception " + stringLoader.getString("Log_Exception_Occured"), e.getMessage());
			if (AMSetting.DebugMode) {
				e.printStackTrace();
			}
        }

        return cookie;
    }

    /**
	 * Protected method, used to convert byte array to HEX string.
	*/        
	protected String byteArrayToHexString(byte[] b) {
        StringBuffer sb = new StringBuffer(b.length * 2);
        for (byte element : b) {
            int v = element & 0xff;
            if(v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString().toUpperCase(Locale.US);
    }

    /**
	 * Protected method, used to convert HEX string to byte array.
	*/    
    protected byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for(int i=0; i<len; i+=2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}

package com.accela.mobile.http;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;


/**
 * <pre>
 * 
 *  Accela Amobile
 *  File: SerializableCookie.java
 * 
 *  Accela, Inc.
 *  Copyright (C): 2012
 * 
 *  Description:
 * 	A wrapper class around {@link Cookie} and/or {@link BasicClientCookie}
 * designed for use in {@link PersistentCookieStore}.
 * 
 *  Notes:
 * 
 *  Revision History  
 * 
 * 	@since 1.0
 * 
 * </pre>
 */
class SerializableCookie implements Serializable {
    private static final long serialVersionUID = 6374381828722046732L;
    private transient final Cookie cookie;
    private transient BasicClientCookie clientCookie;

    /**
	 * Constructor
	 * 
     * @param cookie The cookie object to be serialized.
     * 
	 * @return An initialized SerializableCookie instance.
	 * 
	 * @since 1.0
	 */
    public SerializableCookie(Cookie cookie) {
        this.cookie = cookie;
    }

    /**
	 * Get the current client cookie
	 * 
	 * @return A cookie object.
	 * 
	 * @since 1.0
	 */
    public Cookie getCookie() {
        Cookie bestCookie = cookie;
        if(clientCookie != null) {
            bestCookie = clientCookie;
        }
        return bestCookie;
    }

    /**
	 * Private method, used write cookie's content into the given ObjectOutputStream object.
	 */	
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(cookie.getName());
        out.writeObject(cookie.getValue());
        out.writeObject(cookie.getComment());
        out.writeObject(cookie.getDomain());
        out.writeObject(cookie.getExpiryDate());
        out.writeObject(cookie.getPath());
        out.writeInt(cookie.getVersion());
        out.writeBoolean(cookie.isSecure());
    }

    /**
	 * Private method, used read cookie's content from the given ObjectInputStream object.
	 */	
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        String name = (String)in.readObject();
        String value = (String)in.readObject();
        clientCookie = new BasicClientCookie(name, value);
        clientCookie.setComment((String)in.readObject());
        clientCookie.setDomain((String)in.readObject());
        clientCookie.setExpiryDate((Date)in.readObject());
        clientCookie.setPath((String)in.readObject());
        clientCookie.setVersion(in.readInt());
        clientCookie.setSecure(in.readBoolean());
    }
}
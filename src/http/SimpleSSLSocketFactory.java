package com.accela.mobile.http;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.SSLSocketFactory;

/**
 * <pre>
 * 
 *  Accela Amobile
 *  File: SimpleSSLSocketFactory.java
 * 
 *  Accela, Inc.
 *  Copyright (C): 2012
 * 
 *  Description:
 *  A simple SSL socket factory object, used by HTTP request when it makes SSL connection.
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

class SimpleSSLSocketFactory extends SSLSocketFactory {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        /**
    	 * Constructor.
    	 * 
    	 * @param truststore The key store which will be used.	
    	 * 
    	 * @return An initialized SimpleSSLSocketFactory instance
    	 * 
    	 * @since 1.0
    	 */
        public SimpleSSLSocketFactory(KeyStore truststore)
                        throws NoSuchAlgorithmException, KeyManagementException,
                        KeyStoreException, UnrecoverableKeyException {
                super(truststore);

                TrustManager tm = new X509TrustManager() {
                        public void checkClientTrusted(X509Certificate[] chain,
                                        String authType) throws CertificateException {
                        }

                        public void checkServerTrusted(X509Certificate[] chain,
                                        String authType) throws CertificateException {
                        }

                        public X509Certificate[] getAcceptedIssuers() {
                                return null;
                        }
                };

                sslContext.init(null, new TrustManager[] { tm }, null);
        }

        /**
    	 * Override the createSocket(Socket socket, String host, int port, boolean autoClose) method inherited from its parent class.
    	*/
        @Override
        public Socket createSocket(Socket socket, String host, int port,
                        boolean autoClose) throws IOException, UnknownHostException {
                return sslContext.getSocketFactory().createSocket(socket, host, port,
                                autoClose);
        }

        /**
    	 * Override the createSocket() method inherited from its parent class.
    	*/
        @Override
        public Socket createSocket() throws IOException {
                return sslContext.getSocketFactory().createSocket();
        }

}

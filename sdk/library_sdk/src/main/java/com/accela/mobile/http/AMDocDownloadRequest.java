package com.accela.mobile.http;

import android.os.SystemClock;
import android.util.Log;

import com.accela.mobile.AMDocumentManager;
import com.accela.mobile.AMError;
import com.accela.mobile.AMLogger;
import com.accela.mobile.BuildConfig;
import com.accela.mobile.http.volley.DefaultRetryPolicy;
import com.accela.mobile.http.volley.Legacy.BasicHeader;
import com.accela.mobile.http.volley.Legacy.BasicHttpResponse;
import com.accela.mobile.http.volley.Legacy.BasicStatusLine;
import com.accela.mobile.http.volley.Legacy.Header;
import com.accela.mobile.http.volley.Legacy.HttpStatus;
import com.accela.mobile.http.volley.Legacy.ProtocolVersion;
import com.accela.mobile.http.volley.Legacy.StatusLine;
import com.accela.mobile.http.volley.NetworkResponse;
import com.accela.mobile.http.volley.ServerError;
import com.accela.mobile.http.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/**
 * Created by eyang on 8/28/15.
 */
public class AMDocDownloadRequest implements DocumentRequest {

    protected static final String PROTOCOL_CHARSET = "utf-8";
    private final URL mUrl;
    private final HashMap<String, String> mHttpHeader;
    private final AMDownloadDelegate mDownloadDelegate;
    private final String mStringBody;
    private final static int MAX_BUFFER_SIZE = 1024*4;
    private final String mLocalFilePath;

    public  AMDocDownloadRequest(String url, HashMap<String, String> customHttpHeader, String stringBody, String localFilePath, final AMDownloadDelegate downloadDelegate) throws MalformedURLException {
        mUrl = new URL(url);
        mHttpHeader = customHttpHeader;
        mStringBody = stringBody;
        mLocalFilePath = localFilePath;
        mDownloadDelegate = downloadDelegate;
    }

    @Override
    public NetworkResponse request() throws IOException, ServerError {
        long requestStart = SystemClock.elapsedRealtime();
        NetworkResponse networkResponse = null;
        HttpsURLConnection httpsConn = openConnection();
        HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                Log.i("AMDocumentLoader", "Approving certificate for " + hostname);
                return true;
            }
        };
        httpsConn.setHostnameVerifier(hostnameVerifier);
        InputStream isr = null;
        try {
            // Initialize HttpResponse with data from the HttpURLConnection.
            ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1);
            int responseCode = httpsConn.getResponseCode();
            if (responseCode == -1) {
                // -1 is returned by getResponseCode() if the response code could not be retrieved.
                // Signal to the caller that something was wrong with the connection.
                throw new IOException("Could not retrieve response code from HttpUrlConnection.");
            }
            StatusLine responseStatus = new BasicStatusLine(protocolVersion,
                    httpsConn.getResponseCode(), httpsConn.getResponseMessage());
            BasicHttpResponse response = new BasicHttpResponse(responseStatus);
            for (Map.Entry<String, List<String>> header : httpsConn.getHeaderFields().entrySet()) {
                if (header.getKey() != null) {
                    Header h = new BasicHeader(header.getKey(), header.getValue().get(0));
                    response.addHeader(h);
                }
            }
            if (responseCode >= 400) {
                String encoding = httpsConn.getContentEncoding();
                isr = httpsConn.getErrorStream();
                networkResponse = new NetworkResponse(responseCode, null, convertHeaders(response.getAllHeaders()), false,
                        SystemClock.elapsedRealtime() - requestStart);
                return networkResponse;
            }
            isr = httpsConn.getInputStream();
            saveFile2Storage(mLocalFilePath, isr);
        } catch (IOException e) {
            throw e;
        } finally {
            closeStreams(isr);
        }
//      handler.sendSuccessMessage(new File(localFile));
        return networkResponse;
    }

    @Override
    public void handleResponse(NetworkResponse networkResponse) {
        if (networkResponse==null){
            mDownloadDelegate.onFailure(new AMError(0, null, null, null, "handleResponse: response is empty!"));
            return;
        }
        int statusCode = networkResponse.statusCode;
        if (statusCode == AMDocumentManager.IOEXCEPTION_ERROR){
            mDownloadDelegate.onFailure(new AMError(AMDocumentManager.IOEXCEPTION_ERROR, null, null, networkResponse.headers.toString(), "IO EXCEPTION ERROR!"));
            return;
        }else if(statusCode == AMDocumentManager.SERVEREXCEPTION_ERROR){
            mDownloadDelegate.onFailure(new AMError(AMDocumentManager.SERVEREXCEPTION_ERROR, null, null, networkResponse.headers.toString(), "SERVER EXCEPTION ERROR!"));
            return;
        }
        String jsonString = null;
        try {
            jsonString = new String(networkResponse.data, HttpHeaderParser.parseCharset(networkResponse.headers, PROTOCOL_CHARSET));

            if (statusCode == HttpStatus.SC_OK)
                this.mDownloadDelegate.onSuccess(new File(mLocalFilePath));
            else
                mDownloadDelegate.onFailure(new AMError(statusCode, null, null, networkResponse.headers.toString(), null));
        } catch (UnsupportedEncodingException e) {
            mDownloadDelegate.onFailure(new AMError(statusCode, null, null, networkResponse.headers.toString(), e.toString()));
        }

    }

    private Map<String, String> convertHeaders(Header[] headers) {
        Map<String, String> result = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
        for (int i = 0; i < headers.length; i++) {
            result.put(headers[i].getName(), headers[i].getValue());
        }
        return result;
    }

     private HttpsURLConnection openConnection() throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection)mUrl.openConnection();

        int timeoutMs = DefaultRetryPolicy.DEFAULT_TIMEOUT_MS;
        connection.setConnectTimeout(timeoutMs);
        connection.setReadTimeout(timeoutMs);
        connection.setUseCaches(false);
        connection.setDoInput(true);
        return connection;
    }

    private synchronized void saveFile2Storage(final String localFile,
                                                      InputStream isr) throws IOException {
        FileOutputStream fos = null;
        try {
            crateFile(localFile);

            fos = new FileOutputStream(localFile);
            int bytesRead = -1;
            byte[] buffer = new byte[MAX_BUFFER_SIZE];
            while ((bytesRead = isr.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            fos.flush();
        } catch (IOException e) {
            throw e;
        } finally {
            closeStreams(fos);
            File file = new File(localFile);
            if (BuildConfig.DEBUG) {
                AMLogger.logWarn(localFile + " size=" + file.length());
            }
        }
    }

    private void crateFile(String localFile) throws IOException {
        File file = new File(localFile);
        createFolder(file.getParentFile());
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
    }

    public void createFolder(File folder)  {
        if (folder != null && !folder.exists()) {
            createFolder(folder.getParentFile());
            try {
                folder.mkdir();
            } catch (SecurityException e) {
                throw new SecurityException(
                        "Please grant the user creating folder permit:"
                                + e.getMessage());
            } catch (Throwable e) {
            }
        }
    }

    private void closeStreams(Closeable... streams) throws IOException {
        if (streams != null && streams.length > 0) {
            for (Closeable stream : streams) {
                if (stream == null)
                    continue;
                stream.close();
            }
        }
    }

    public static interface AMDownloadDelegate {

        public void onStart();

        public void onSuccess(File file);

        public void onFailure(AMError error);
    }

}

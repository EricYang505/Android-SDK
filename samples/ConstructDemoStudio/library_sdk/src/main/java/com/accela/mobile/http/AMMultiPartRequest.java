package com.accela.mobile.http;

import android.os.AsyncTask;

import com.accela.mobile.AMLogger;
import com.accela.mobile.AMRequestDelegate;
import com.accela.mobile.http.volley.AuthFailureError;
import com.accela.mobile.http.volley.DefaultRetryPolicy;
import com.accela.mobile.http.volley.Legacy.BasicHeader;
import com.accela.mobile.http.volley.Legacy.BasicHttpEntity;
import com.accela.mobile.http.volley.Legacy.BasicHttpResponse;
import com.accela.mobile.http.volley.Legacy.BasicStatusLine;
import com.accela.mobile.http.volley.Legacy.Header;
import com.accela.mobile.http.volley.Legacy.HttpEntity;
import com.accela.mobile.http.volley.Legacy.HttpResponse;
import com.accela.mobile.http.volley.Legacy.HttpStatus;
import com.accela.mobile.http.volley.Legacy.ProtocolVersion;
import com.accela.mobile.http.volley.Legacy.StatusLine;
import com.accela.mobile.http.volley.Request;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by eyang on 8/26/15.
 */
public class AMMultiPartRequest extends AsyncTask<Runnable, Object, HttpResponse> {

    public final static int MAX_BUFFER_SIZE = 1024*2;
    private final String MULTIPART_SEPARATOR_LINE = "---------------------------7de1a0c22082";
    private final String lineEnd = "\r\n";
    private final String twoHyphens = "--";
    private final String boundary = "*****";

    private URL mUrl;
    private Map<String, String> mHttpHeader = new HashMap<String, String>();
    private String mFilePath;
    private AMRequestDelegate mRequestDelegate;


    public AMMultiPartRequest(String url, HashMap<String, String> customHttpHeader, String uploadFilepath, final AMRequestDelegate requestDelegate) throws MalformedURLException {
        mUrl = new URL(url);
        mHttpHeader = customHttpHeader;
        mFilePath = uploadFilepath;
        mRequestDelegate = requestDelegate;
    }


    public HttpResponse performRequest() {
        try {
            return request();
        } catch (IOException e) {
            AMLogger.logError(e.toString());
        } catch (AuthFailureError authFailureError) {
            AMLogger.logError(authFailureError.toString());
        }
        return null;
    }

    public HttpResponse request()
            throws IOException, AuthFailureError {
        HttpURLConnection connection = openConnection();
        for (String headerName : mHttpHeader.keySet()) {
            connection.addRequestProperty(headerName, mHttpHeader.get(headerName));
        }
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Connection", "Keep-Alive");
//        connection.setRequestProperty("Accept", "text/*");
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + MULTIPART_SEPARATOR_LINE);

        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
        FileInputStream fileInputStream = new FileInputStream(new File(mFilePath));
        buildMultipartBody(connection, fileInputStream, outputStream);

        // Initialize HttpResponse with data from the HttpURLConnection.
        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1);
        int responseCode = connection.getResponseCode();
        if (responseCode == -1) {
            // -1 is returned by getResponseCode() if the response code could not be retrieved.
            // Signal to the caller that something was wrong with the connection.
            throw new IOException("Could not retrieve response code from HttpUrlConnection.");
        }
        StatusLine responseStatus = new BasicStatusLine(protocolVersion,
                connection.getResponseCode(), connection.getResponseMessage());
        BasicHttpResponse response = new BasicHttpResponse(responseStatus);
        if (hasResponseBody(Request.Method.POST, responseStatus.getStatusCode())) {
            response.setEntity(entityFromConnection(connection));
        }
        for (Map.Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
            if (header.getKey() != null) {
                Header h = new BasicHeader(header.getKey(), header.getValue().get(0));
                response.addHeader(h);
            }
        }
        fileInputStream.close();
        outputStream.flush();
        outputStream.close();
        outputStream = null;
        return response;
    }

    private void buildMultipartBody(HttpURLConnection connection, FileInputStream fileInputStream, DataOutputStream outputStream) throws IOException {
        outputStream.writeBytes(twoHyphens + boundary + lineEnd);
        outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + mFilePath + "\"" + lineEnd);
        outputStream.writeBytes(lineEnd);

        int bytesRead, bytesAvailable, bufferSize;
        bytesAvailable = fileInputStream.available();
        bufferSize = Math.min(bytesAvailable, MAX_BUFFER_SIZE);
        byte[] buffer = new byte[bufferSize];

        // Read file
        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        AMLogger.logInfo("Image length: " + bytesAvailable);
        try {
            while (bytesRead > 0) {
                try {
                    outputStream.write(buffer, 0, bufferSize);
                } catch (OutOfMemoryError e) {
                    AMLogger.logError(e.toString());
                    return;
                }
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, MAX_BUFFER_SIZE);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }
        } catch (Exception e) {
            AMLogger.logError(e.toString());
        }
        outputStream.writeBytes(lineEnd);
        outputStream.writeBytes(twoHyphens + boundary + twoHyphens
                + lineEnd);
    }


    private HttpURLConnection openConnection() throws IOException {
        HttpURLConnection connection = (HttpURLConnection)mUrl.openConnection();

        int timeoutMs = DefaultRetryPolicy.DEFAULT_TIMEOUT_MS;
        connection.setConnectTimeout(timeoutMs);
        connection.setReadTimeout(timeoutMs);
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setChunkedStreamingMode(MAX_BUFFER_SIZE); //chunk data into specific size, need not keep entire file in the memory.
        return connection;
    }

    private static boolean hasResponseBody(int requestMethod, int responseCode) {
        return requestMethod != Request.Method.HEAD
                && !(HttpStatus.SC_CONTINUE <= responseCode && responseCode < HttpStatus.SC_OK)
                && responseCode != HttpStatus.SC_NO_CONTENT
                && responseCode != HttpStatus.SC_NOT_MODIFIED;
    }

    private static HttpEntity entityFromConnection(HttpURLConnection connection) {
        BasicHttpEntity entity = new BasicHttpEntity();
        InputStream inputStream;
        try {
            inputStream = connection.getInputStream();
        } catch (IOException ioe) {
            inputStream = connection.getErrorStream();
        }
        entity.setContent(inputStream);
        entity.setContentLength(connection.getContentLength());
        entity.setContentEncoding(connection.getContentEncoding());
        entity.setContentType(connection.getContentType());
        return entity;
    }

    @Override
    protected HttpResponse doInBackground(Runnable... params) {
        return performRequest();
    }

    @Override
    protected void onPostExecute(HttpResponse result) {
        if (result.getStatusLine()==StatusLine.)
    }



}

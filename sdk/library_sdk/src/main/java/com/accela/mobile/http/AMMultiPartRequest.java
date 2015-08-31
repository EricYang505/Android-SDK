package com.accela.mobile.http;

import android.os.AsyncTask;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import com.accela.mobile.AMDocumentManager;
import com.accela.mobile.AMError;
import com.accela.mobile.AMLogger;
import com.accela.mobile.AMRequestDelegate;
import com.accela.mobile.AccelaMobile;
import com.accela.mobile.http.volley.AuthFailureError;
import com.accela.mobile.http.volley.Cache;
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
import com.accela.mobile.http.volley.NetworkResponse;
import com.accela.mobile.http.volley.Request;
import com.accela.mobile.http.volley.Response;
import com.accela.mobile.http.volley.ServerError;
import com.accela.mobile.http.volley.VolleyLog;
import com.accela.mobile.http.volley.toolbox.ByteArrayPool;
import com.accela.mobile.http.volley.toolbox.HttpHeaderParser;
import com.accela.mobile.http.volley.toolbox.PoolingByteArrayOutputStream;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by eyang on 8/26/15.
 */
public class AMMultiPartRequest implements DocumentRequest{
    protected static final String PROTOCOL_CHARSET = "utf-8";
    public final static int MAX_BUFFER_SIZE = 1024*2;
    private final String MULTIPART_SEPARATOR_LINE = "---------------------------7de1a0c22082";
    private final String lineEnd = "\r\n";
    String twoHyphens = "--";
    private static int DEFAULT_POOL_SIZE = 4096;

    private ByteArrayPool mPool;
    private URL mUrl;
    private Map<String, String> mHttpHeader = new HashMap<String, String>();
    private HttpEntity mHttpEntity;
    private AMRequestDelegate mRequestDelegate;

    public AMMultiPartRequest(String url, HashMap<String, String> customHttpHeader, HttpEntity httpEntity, final AMRequestDelegate requestDelegate) throws MalformedURLException {
        mUrl = new URL(url);
        mHttpHeader = customHttpHeader;
        mHttpEntity = httpEntity;
        mRequestDelegate = requestDelegate;
        mPool = new ByteArrayPool(DEFAULT_POOL_SIZE);
    }


    public NetworkResponse request() throws IOException, ServerError {
        long requestStart = SystemClock.elapsedRealtime();
        NetworkResponse networkResponse = null;

            HttpURLConnection connection = openConnection();
            for (String headerName : mHttpHeader.keySet()) {
                connection.addRequestProperty(headerName, mHttpHeader.get(headerName));
            }
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "Accela-Mobile-SDK");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + MULTIPART_SEPARATOR_LINE);

            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
//            FileInputStream fileInputStream = new FileInputStream(mFileWrapper.mfile);
            mHttpEntity.writeTo(outputStream);
//            copy(mHttpEntity.getContent(), outputStream);

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
//            fileInputStream.close();
            outputStream.flush();
            outputStream.close();
            outputStream = null;

            networkResponse = new NetworkResponse(responseCode, entityToBytes(response.getEntity()), convertHeaders(response.getAllHeaders()), false,
                    SystemClock.elapsedRealtime() - requestStart);
            return networkResponse;
    }

    public void handleResponse(NetworkResponse networkResponse){
        if (networkResponse==null){
            mRequestDelegate.onFailure(new AMError(0, null, null, null, "handleResponse: response is empty!"));
            return;
        }
        int statusCode = networkResponse.statusCode;
        if (statusCode == AMDocumentManager.IOEXCEPTION_ERROR){
            mRequestDelegate.onFailure(new AMError(AMDocumentManager.IOEXCEPTION_ERROR, null, null, networkResponse.headers.toString(), "IO EXCEPTION ERROR!"));
            return;
        }else if(statusCode == AMDocumentManager.SERVEREXCEPTION_ERROR){
            mRequestDelegate.onFailure(new AMError(AMDocumentManager.SERVEREXCEPTION_ERROR, null, null, networkResponse.headers.toString(), "SERVER EXCEPTION ERROR!"));
            return;
        }
        String jsonString = null;
        try {
            jsonString = new String(networkResponse.data, HttpHeaderParser.parseCharset(networkResponse.headers, PROTOCOL_CHARSET));

            if (statusCode == HttpStatus.SC_OK)
                this.mRequestDelegate.onSuccess(new JSONObject(jsonString));
            else
                mRequestDelegate.onFailure(new AMError(statusCode, null, null, networkResponse.headers.toString(), null));
        } catch (UnsupportedEncodingException e) {
            mRequestDelegate.onFailure(new AMError(statusCode, null, null, networkResponse.headers.toString(), e.toString()));
        } catch (JSONException e) {
            mRequestDelegate.onFailure(new AMError(statusCode, null, null, networkResponse.headers.toString(), e.toString()));
        }

//    } catch (ServerError serverError) {
//        mRequestDelegate.onFailure(new AMError(statusCode, null, null, networkResponse.headers.toString(), serverError.toString()));
//    } catch (IOException e) {
//        mRequestDelegate.onFailure(new AMError(statusCode, null, null, networkResponse.headers.toString(), e.toString()));
//    }
    }

    public  int  copy(InputStream input, DataOutputStream output) throws IOException {
                 long count = copyLarge(input, output);
                if (count > Integer.MAX_VALUE) {
                        return -1;
                    }
                return (int) count;
            }

    public  long copyLarge(InputStream input, DataOutputStream output)
                throws IOException {
                byte[] buffer = new byte[DEFAULT_POOL_SIZE];
                long count = 0;
                int n = 0;
                while (-1 != (n = input.read(buffer))) {
                        output.write(buffer, 0, n);
                        count += n;
                    }
                return count;
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

    private HttpEntity entityFromConnection(HttpURLConnection connection) {
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

    private Map<String, String> convertHeaders(Header[] headers) {
        Map<String, String> result = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
        for (int i = 0; i < headers.length; i++) {
            result.put(headers[i].getName(), headers[i].getValue());
        }
        return result;
    }

    /** Reads the contents of HttpEntity into a byte[]. */
    private byte[] entityToBytes(HttpEntity entity) throws IOException, ServerError {
        PoolingByteArrayOutputStream bytes =
                new PoolingByteArrayOutputStream(mPool, (int) entity.getContentLength());
        byte[] buffer = null;
        try {
            InputStream in = entity.getContent();
            if (in == null) {
                throw new ServerError();
            }
            buffer = mPool.getBuf(1024);
            int count;
            while ((count = in.read(buffer)) != -1) {
                bytes.write(buffer, 0, count);
            }
            return bytes.toByteArray();
        } finally {
            try {
                // Close the InputStream and release the resources by "consuming the content".
                entity.consumeContent();
            } catch (IOException e) {
                // This can happen if there was an exception above that left the entity in
                // an invalid state.
                VolleyLog.v("Error occured when calling consumingContent");
            }
            mPool.returnBuf(buffer);
            bytes.close();
        }
    }

}



//    private void buildMultipartBody(HttpURLConnection connection, DataOutputStream outputStream) throws IOException {
//
//    }
//        String agency = "ISLANDTON";
//        outputStream.writeBytes(MULTIPART_SEPARATOR_LINE + lineEnd);
//        outputStream.writeBytes("Content-Disposition: form-data; name=\"fileInfo\"" + lineEnd);
//        outputStream.writeBytes(lineEnd);
//        outputStream.writeBytes("[{\"serviceProviderCode\":\""+ agency +"\",\"fileName\":\"AccelaAnalytics.png\",\"type\":\"png\",\"description\":\"Upload document for testing.\"}]");
//        outputStream.writeBytes(lineEnd);
//        outputStream.writeBytes(MULTIPART_SEPARATOR_LINE + lineEnd);
//        outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedFile\";filename=\"" + mFileWrapper.mfile.getName() + "\"" + lineEnd);
//        outputStream.writeBytes("Content-Type: application/octet-stream" + lineEnd);
//        outputStream.writeBytes(lineEnd);
//
//        int bytesRead, bytesAvailable, bufferSize;
//        bytesAvailable = fileInputStream.available();
//        bufferSize = Math.min(bytesAvailable, MAX_BUFFER_SIZE);
//        byte[] buffer = new byte[bufferSize];
//
//        // Read file
//        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//        AMLogger.logInfo("Document length: " + bytesAvailable);
//        try {
//            while (bytesRead > 0) {
//                try {
//                    outputStream.write(buffer, 0, bufferSize);
//                } catch (OutOfMemoryError e) {
//                    AMLogger.logError(e.toString());
//                    return;
//                }
//                bytesAvailable = fileInputStream.available();
//                bufferSize = Math.min(bytesAvailable, MAX_BUFFER_SIZE);
//                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//            }
//        } catch (Exception e) {
//            AMLogger.logError(e.toString());
//        }
//        outputStream.writeBytes(lineEnd);
//        outputStream.writeBytes(MULTIPART_SEPARATOR_LINE + twoHyphens + lineEnd);


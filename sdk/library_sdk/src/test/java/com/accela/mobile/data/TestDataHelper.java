package com.accela.mobile.data;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import java.io.UnsupportedEncodingException;

/**
 * Created by jzhong on 6/19/15.
 */
public class TestDataHelper {

    public final static String URL_GET_APP_SETTINGS = "/v4/appsettings";


    public static HttpResponse makeReponseByUrl(
            String url, int expectedResponseStatus) {
        HttpResponse response = new BasicHttpResponse(new BasicStatusLine(
                new ProtocolVersion("HTTP", 1, 1), expectedResponseStatus, ""));
        response.setStatusCode(expectedResponseStatus);
        try {
            response.setEntity(new StringEntity(makeHttpBodyString(url)));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
        return response;
    }

    public static HttpResponse makeResponse(
            int expectedResponseStatus, String expectedResponseBody) {
        HttpResponse response = new BasicHttpResponse(new BasicStatusLine(
                new ProtocolVersion("HTTP", 1, 1), expectedResponseStatus, ""));
        response.setStatusCode(expectedResponseStatus);
        try {
            response.setEntity(new StringEntity(expectedResponseBody));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
        return response;
    }


    private static String makeHttpBodyString( String url) {
        if(url.contains (URL_GET_APP_SETTINGS)) {
            return "{\"result\":[{\"key\":\"Agency Phone Number\",\"value\":\"9255739045\"},{\"key\":\"Active Record Statuses\",\"value\":\"INCOMPLETE, OPEN, PENDING, UNASSIGNED, APPROVED\"}]}";
        }
        return "";
    }


}

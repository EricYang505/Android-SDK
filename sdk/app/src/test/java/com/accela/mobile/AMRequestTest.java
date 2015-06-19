package com.accela.mobile;

import com.accela.mobile.data.TestDataHelper;
import com.accela.mobile.http.AsyncHttpClient;
import com.accela.mobile.http.RequestParams;
import com.accela.mobile.http.SyncHttpClient;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.robolectric.Robolectric;

import java.io.UnsupportedEncodingException;


/**
 * Created by jzhong on 6/17/15.
 */

@PrepareForTest( {AMRequest.class, DefaultHttpClient.class, SyncHttpClient.class, AsyncHttpClient.class})
public class AMRequestTest extends PowerMockTestCaseBase{

    AccelaMobile accelaMobile;
    String appId = "appId12345";
    String appSecret = "appSecret12345";

    JSONObject expectedJsonReturn;

    @Before
    public void before() throws Exception {
        accelaMobile = new AccelaMobile(Robolectric.application, appId, appSecret);
        //Robolectric.getFakeHttpLayer().addHttpResponseRule(new CountingResponseRule());

        //create expected json return;
        expectedJsonReturn = new JSONObject();

        String status = "0";
        JSONArray array = new JSONArray();
        for(int i= 0; i< 5; i++) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(String.format("%d", i), "json-object3");
            array.put(i, jsonObject);
        }
        expectedJsonReturn.put("status", status);
        expectedJsonReturn.put("result", array);

    }

    private HttpResponse prepareResponse(int expectedResponseStatus,
                                         String expectedResponseBody) {
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

    @Test
    public void testFetch() throws Exception{
        RequestParams params = new RequestParams();
        RequestParams postData = new RequestParams();
        AMRequest.HTTPMethod httpMethod = AMRequest.HTTPMethod.GET;
        String serviceURL = "https://apis.accela.com";


        //power mock DefaultHttpClient
        PowerMockito.mockStatic(DefaultHttpClient.class);

        //create mockHttpClient
        DefaultHttpClient mockHttpClient = PowerMockito.mock(DefaultHttpClient.class);;
        PowerMockito.whenNew(DefaultHttpClient.class).withAnyArguments().thenReturn(mockHttpClient);

        //mock httpclient.execute
        PowerMockito.when(mockHttpClient.execute(Matchers.any(HttpUriRequest.class)) ).thenAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                Object mock = invocation.getMock();
                if(args.length == 1 && (args[0] instanceof HttpUriRequest)) {
                    HttpResponse response = TestDataHelper.makeResponse(200, expectedJsonReturn.toString());
                    return response;
                }
                return null;
            }
        });



        AMRequest request = new AMRequest(accelaMobile, serviceURL, params, httpMethod);
        JSONObject reponse  = request.fetch(postData);

        TestUtils.assertEquals(expectedJsonReturn, reponse);
    }







}

package com.accela.mobile;

import com.accela.mobile.http.AsyncHttpClient;
import com.accela.mobile.http.RequestParams;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.robolectric.Robolectric;
import org.robolectric.tester.org.apache.http.HttpEntityStub;

import java.io.IOException;
import java.io.UnsupportedEncodingException;


/**
 * Created by jzhong on 6/17/15.
 */

@PrepareForTest( {AMRequest.class, AsyncHttpClient.class})
public class AMRequestTest extends TestCaseBase{

    AccelaMobile accelaMobile;
    String appId = "appId12345";
    String appSecret = "appSecret12345";


    class CountingResponseRule implements HttpEntityStub.ResponseRule {

        private int mCounter = 0;

        @Override
        public HttpResponse getResponse() throws HttpException, IOException {
            HttpResponse response= new BasicHttpResponse(
                    new ProtocolVersion("HTTP", 1, 1), 200, "OK");
            response.setEntity(new StringEntity(Integer.toString(mCounter)));
            mCounter++;

            return response;
        }

        @Override
        public boolean matches(HttpRequest request) {
            String uri = request.getRequestLine().getUri();
            return true;
           /* if (uri.equals("https://apis.accela.com")) {
                return true;
            }
            return false; */
        }
    }


    @Before
    public void before() throws Exception {
        accelaMobile = new AccelaMobile(Robolectric.application, appId, appSecret);
        Robolectric.getFakeHttpLayer().addHttpResponseRule(
                new CountingResponseRule());
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
       // DefaultHttpClient mockHttpClient = new MockHttpClient();
       // PowerMockito.whenNew(DefaultHttpClient.class).withAnyArguments().thenReturn(mockHttpClient);

        //MockSyncHttpClient syncHttpClient = new MockSyncHttpClient();
        //PowerMockito.whenNew(AsyncHttpClient.class).withAnyArguments().thenReturn(new AsyncHttpClient());


        /*
        PowerMockito.whenNew(DefaultHttpClient.class).withAnyArguments().then(new Answer<Object>() {
            @Override
            public DefaultHttpClient answer(InvocationOnMock invocation) throws Throwable {
                DefaultHttpClient httpClient = new MockHttpClient();
                HttpResponse response = prepareResponse(200, "{'status': 0}");
                HttpClient mockHttpClient = Mockito.mock(HttpClient.class);
                Mockito.when(mockHttpClient.execute(Mockito.any(HttpUriRequest.class)))
                        .thenReturn(response);
                return httpClient;
            }
        });*/

        HttpResponse response = prepareResponse(200, "{'status': 0}");

       // Mockito.when(mockHttpClient.execute(Mockito.any(HttpUriRequest.class))).thenReturn(response);

        AMRequest request = new AMRequest(accelaMobile, serviceURL, params, httpMethod);

        JSONObject reponse  = request.fetch(postData);

        /*
       request.sendRequest(postData, new AMRequestDelegate() {
            @Override
            public void onSuccess(JSONObject response) {
                assertTrue(true);
            }

            @Override
            public void onFailure(AMError error) {
                assertTrue(true);
            }

            @Override
            public void onStart() {

            }
        });
        */
    }



}

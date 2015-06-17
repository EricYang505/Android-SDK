package com.accela.mobile;

import com.accela.mobile.http.AsyncHttpClient;
import com.accela.mobile.http.MockAsyncHttpClient;
import com.accela.mobile.http.RequestParams;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.spy;


/**
 * Created by jzhong on 6/17/15.
 */

@RunWith(RobolectricTestRunner.class)
@PrepareForTest( {AMRequest.class})
public class AMRequestTest {


    AccelaMobile accelaMobile;
    String appId = "appId12345";
    String appSecret = "appSecret12345";
    @Before
    public void before() throws Exception{
        accelaMobile = new AccelaMobile(Robolectric.application, appId, appSecret);
        //mockStatic(AsyncHttpClient.class);
        }

    @Test
    public void testFetch() throws Exception{
        RequestParams params = new RequestParams();
        RequestParams postData = new RequestParams();
        AMRequest.HTTPMethod httpMethod = AMRequest.HTTPMethod.GET;
        String serviceURL = "http://test.accela.com";
        AsyncHttpClient httpClient = new MockAsyncHttpClient();

        PowerMockito.whenNew(AsyncHttpClient.class).withAnyArguments().thenReturn(httpClient);

        AMRequest request = spy(new AMRequest(accelaMobile, serviceURL, params, httpMethod));

        //request.fetch(postData);
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
    }



}

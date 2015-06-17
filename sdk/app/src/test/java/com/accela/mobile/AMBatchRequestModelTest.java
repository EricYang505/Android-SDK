package com.accela.mobile;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by jzhong on 6/17/15.
 */

@RunWith(RobolectricTestRunner.class)
@PrepareForTest( {})
public class AMBatchRequestModelTest {

    String url;
    String method;
    String body;
    AMBatchRequestModel requestModel;

    @Before
    public void before() throws Exception {
        url = "https://apis.accela.com";
        method = "GET";
        body = "Request body";
        requestModel = new AMBatchRequestModel();
        requestModel.setUrl(url);
        requestModel.setBody(body);
        requestModel.setMethod(method);
    }

    @Test
    public void testSetAndGet() {

        assertEquals(url, requestModel.getUrl());
        assertEquals(body, requestModel.getBody());
        assertEquals(method, requestModel.getMethod());
    }

    @Test
    public void testToJsonObject() {
        JSONObject jsonObject = requestModel.toJsonObject();
        assertTrue(jsonObject!=null);
        try {
            assertEquals(jsonObject.get("url"), url);
            assertEquals(jsonObject.get("method"), method);
            assertEquals(jsonObject.get("body"), body);

        } catch (JSONException e) {
            assertTrue(false);
        }

    }

}

package com.accela.mobile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by jzhong on 6/17/15.
 */

@RunWith(RobolectricTestRunner.class)
@PrepareForTest( {})
public class AMBatchResponseTest {

    @Before
    public void before() {

    }

    @Test
    public void testConstrcutor() throws JSONException{
        String status = "200";
        JSONObject jsonResponse = new JSONObject();
        JSONArray  array = new JSONArray();
        for(int i= 0; i< 5; i++) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(String.format("%d", i), "json-object3");
            array.put(i, jsonObject);
        }
        jsonResponse.put("status", status);
        jsonResponse.put("result", array);
        AMBatchResponse reponse = new AMBatchResponse(jsonResponse);


        //assert getStatus
        assertEquals(status, reponse.getStatus());

        //assert Get result
        List<JSONObject> result = reponse.getResult();

        assertTrue(array.length() == result.size());

        for(int i=0;i < array.length(); i++) {
            JSONObject objectOriginal = array.getJSONObject(i);
            //check if the object is in results
            boolean existInResult = false;
            for(JSONObject objectResult: result) {
                if(TestUtils.areEqual(objectOriginal, objectResult)) {
                    existInResult = true;
                    break;
                }
            }

            assertTrue(existInResult);
        }

    }




}

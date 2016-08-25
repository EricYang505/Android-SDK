package com.accela.mobile;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.AndroidTestCase;
import android.util.Log;

import com.accela.mobile.http.RequestParams;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by jzhong on 6/22/15.
 */
@RunWith(AndroidJUnit4.class)
public class AccelaMobileAndroidTest extends AndroidTestCase{
    private final static String TAG = "AccelaMobileAndroidTest";
    //civic app.
    public final static String appId = "635575464179170502";
    public final static String appSecret = "4dd235bf3c464516a071a980a7029e86";
    public final static String apiHost = "https://apis.dev.accela.com";
    public final static String authHost = "https://auth.dev.accela.com";
    public final static String[] appScopesCitizen = {"search_records", "get_records", "get_my_records",
            "get_record", "get_record_inspections", "schedule_inspection",
            "get_inspections", "search_inspections", "get_inspection", "get_record_documents",
            "get_document", "create_record", "get_record_fees", "batch_request", "get_document_thumbnail", "download_document", "get_record_inspection_types", "cancel_inspection",
            "get_inspections_checkavailability", "get_inspection_documents" , "get_linked_accounts", "create_civicid_accounts", "reschedule_inspection",
            "delete_civicid_accounts", "get_app_settings"//, "get_inspector"//, "create_inspection_documents"
    };
    public final static String environment = "PROD";

    private final static String userName = "ca@accela.com";
    private final static String password  = "test1234";

    AccelaMobile accelaMobile;
    Context context;
    CountDownLatch signal;

    AMSessionDelegate sessionDelegate = new AMSessionDelegate() {
        @Override
        public void amDidLogin() {
            Log.d(TAG, "login successfully");
            signal.countDown();
        }

        @Override
        public void amDidLoginFailure(AMError error) {
            Log.d(TAG, "login failed" + error.getMessage());
            signal.countDown();
        }

        @Override
        public void amDidCancelLogin() {
            Log.d(TAG, "login canceled");
            signal.countDown();
        }

        @Override
        public void amDidSessionInvalid(AMError error) {

        }

        @Override
        public void amDidLogout() {
            signal.countDown();
            Log.d(TAG, "logout");
        }
    };


    @Before
    public void setUp() throws Exception {
        super.setUp();
        context = InstrumentationRegistry.getContext();
        Log.d(TAG, "setUp");
        accelaMobile = AccelaMobile.getInstance();
        accelaMobile.initialize(InstrumentationRegistry.getContext(), appId, appSecret, AccelaMobile.Environment.PROD, sessionDelegate, authHost, apiHost);
        login();
    }

    @After
    public void tearDown() throws Exception {

    }


    private void login() {
        signal = new CountDownLatch(1);
        accelaMobile.getAuthorizationManager().authenticate("", userName, password, AccelaMobile.Environment.PROD, appScopesCitizen);

        try {
            signal.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {

        }
        Log.d(TAG, "login completed");
        assertTrue(accelaMobile.isSessionValid());
    }

    @Test
    public void testLogin() {
        Log.d(TAG, "testLogin start");
        assertTrue(accelaMobile.isSessionValid());
        Log.d(TAG, "testLogin end");
    }

    @Test
    public void testGetRecord() {
        Log.d(TAG, "testGetRecord start");
        RequestParams requestParams = new RequestParams();
        requestParams.put("offset", "0");
        requestParams.put("limit", "20");
        requestParams.put("lang", "en_US");

        Map<String, String> headerParams = new HashMap<String, String>();
        headerParams.put("x-accela-agencies", "All");
        headerParams.put("x-accela-environment", "PROD");
        signal = new CountDownLatch(1);
        accelaMobile.getRequestSender().sendRequest("/v4/records/mine", requestParams, headerParams, new AMRequestDelegate() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d(TAG, "testGetRecord successfully" + response.toString());
                signal.countDown();
                assertTrue(true);
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onFailure(AMError error) {
                Log.d(TAG, "testGetRecord failed");
                signal.countDown();
                assertTrue(false);
            }
        });
        try {
            signal.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {

        }
        Log.d(TAG, "testGetRecord end");
    }


    @Test
    public void testGetInspection() {
        Log.d(TAG, "testGetInspection start");
        RequestParams requestParams = new RequestParams();
        requestParams.put("offset", "0");
        requestParams.put("limit", "20");
        requestParams.put("lang", "en_US");

        Map<String, String> headerParams = new HashMap<String, String>();
        headerParams.put(AMRequest.HEADER_X_ACCELA_AGENCY, "ISLANDTON");
        headerParams.put(AMRequest.HEADER_X_ACCELA_ENVIRONMENT, "PROD");
        signal = new CountDownLatch(1);
        accelaMobile.getRequestSender().sendRequest("/v4/records/ISLANDTON-15CAP-00000-005Z4/inspections", requestParams, headerParams, new AMRequestDelegate() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d(TAG, "testGetInspection successfully" + response.toString());
                signal.countDown();
                assertTrue(true);
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onFailure(AMError error) {
                Log.d(TAG, "testGetInspection failed: " + error.getMessage());
                signal.countDown();
                assertTrue(false);
            }
        });
        try {
            signal.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {

        }
        Log.d(TAG, "testGetInspection end");
    }


    @Test
    public void testLogout() {
        Log.d(TAG, "testLogout start");
        signal = new CountDownLatch(1);
        accelaMobile.getAuthorizationManager().logout();
        try {
            signal.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {

        }
        Log.d(TAG, "logout completed");
        assertFalse(accelaMobile.isSessionValid());
        Log.d(TAG, "testLogout end");
    }

}

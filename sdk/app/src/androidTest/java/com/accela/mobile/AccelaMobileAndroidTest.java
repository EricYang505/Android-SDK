package com.accela.mobile;

import android.app.Instrumentation;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.AndroidTestCase;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

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

    AccelaMobileInternal accelaMobile;
    Context context;
    final CountDownLatch signalLogin = new CountDownLatch(1);
    final CountDownLatch signalLogout = new CountDownLatch(1);

    AMSessionDelegate sessionDelegate = new AMSessionDelegate() {
        @Override
        public void amDidLogin() {
            Log.d(TAG, "login successfully");
            signalLogin.countDown();
        }

        @Override
        public void amDidLoginFailure(AMError error) {
            Log.d(TAG, "login failed" + error.getMessage());
            signalLogin.countDown();
        }

        @Override
        public void amDidCancelLogin() {
            Log.d(TAG, "login canceled");
            signalLogin.countDown();
        }

        @Override
        public void amDidSessionInvalid(AMError error) {

        }

        @Override
        public void amDidLogout() {
            signalLogout.countDown();
            Log.d(TAG, "logout");
        }
    };


    @Before
    public void setUp() throws Exception {
        super.setUp();
        context = InstrumentationRegistry.getContext();
        Log.d(TAG, "setUp");
        accelaMobile = new AccelaMobileInternal(InstrumentationRegistry.getContext(), appId, appSecret, sessionDelegate, authHost, apiHost);
        login();
    }

    @After
    public void tearDown() throws Exception {

    }


    private void login() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                accelaMobile.authenticate("", userName, password, appScopesCitizen);
            }
        });
        try {
            signalLogin.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {

        }
        Log.d(TAG, "login completed");
        assertTrue(accelaMobile.isSessionValid());
    }

    @Test
    public void testLogin() {
        assertTrue(accelaMobile.isSessionValid());
    }

    @Test
    public void testSyncRequest() {
        
    }

    @Test
    public void testLogout() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                accelaMobile.logout();
            }
        });
        try {
            signalLogout.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {

        }
        Log.d(TAG, "logout completed");
        assertFalse(accelaMobile.isSessionValid());
    }

}

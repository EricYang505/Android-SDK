/**
 * Copyright 2015 Accela, Inc.
 *
 * You are hereby granted a non-exclusive, worldwide, royalty-free license to
 * use, copy, modify, and distribute this software in source code or binary
 * form for use in connection with the web services and APIs provided by
 * Accela.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 */

package testcase.accela.com.constructdemo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.accela.mobile.AMError;
import com.accela.mobile.AMLogger;
import com.accela.mobile.AMRequest;
import com.accela.mobile.AMRequest.HTTPMethod;
import com.accela.mobile.AMRequestDelegate;
import com.accela.mobile.AMSessionDelegate;
import com.accela.mobile.AMSetting;
import com.accela.mobile.AccelaMobile;
import com.accela.mobile.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AgencyTestActivity extends AppCompatActivity {
    private final String SERVICE_URI_RECORD_LIST = "/v4/records/";
    private final String SERVICE_URI_RECORD_CREATE = "/v4/records/";
    private final String SERVICE_URI_RECORD_SEARCH = "/v4/search/records/";
    private final String AppID = "635442545965802935";
    private final String AppSecret = "e7b22310882f4e5185c9ca339aa1a67c";
    private final String[] Permissions4Authorization = new String[]{"search_records",
            "get_records", "get_record", "get_record_inspections",
            "get_inspections", "get_inspection", "get_record_documents",
            "get_document", "create_record", "create_record_document",
            "a311citizen_create_record"};
    private final String URL_SCHEMA = "amtest";
    private final String AGENCY = "ISLANDTON";

    private ProgressDialog accessTokenProgressDialog = null;
    private Button btnCivicWebLogin, btnCivicEmbeddedWebLogin, btnGetRecords,
            btnCreateRecord, btnSearchRecord,
            btnCivicLogout, btnBack;
    private ViewGroup mainLayout = null;
    private AMRequest currentRequest = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize content view.
        setContentView(R.layout.activity_agency_test);
    }
}


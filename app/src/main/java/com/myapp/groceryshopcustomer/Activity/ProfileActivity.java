package com.myapp.groceryshopcustomer.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.myapp.groceryshopcustomer.DBHandler.Customer;
import com.myapp.groceryshopcustomer.DBHandler.DatabaseHandler;
import com.myapp.groceryshopcustomer.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.myapp.groceryshopcustomer.Constant.Constants.appendLog;
import static com.myapp.groceryshopcustomer.Constant.Constants.baseUrl1;
import static com.myapp.groceryshopcustomer.Constant.Constants.isNetworkAvailable;
import static com.myapp.groceryshopcustomer.Constant.Constants.progressDialog;

public class ProfileActivity extends AppCompatActivity {
    TextView tvUpdateProfile;
    EditText edtName, edtLastName, edtMobileNumber;
    Button btnsave;
    String name, lastname, mobileNumber, email;
    DatabaseHandler db = new DatabaseHandler(this);

    int status, custId;
    String Status;
    ProgressDialog progress;

    int tempstatus;
    InputStream inputStream = null;
    String result = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        progress = new ProgressDialog(ProfileActivity.this);
        //get customer details from service using cust_id and set to edit text before edit
        tvUpdateProfile = (TextView) findViewById(R.id.tv_updateprofile);
        edtName = (EditText) findViewById(R.id.edt_name);
        edtLastName = (EditText) findViewById(R.id.edt_last_name);
        edtMobileNumber = (EditText) findViewById(R.id.edt_mobile_number);
        btnsave = (Button) findViewById(R.id.btn_create_account);

        Customer user1 = db.getCustomerDetails();
        custId = user1.getCust_id();
        //  name = user1.getCust_name();
        //   email = user1.getEmaiid();
        //edtName.setText(String.valueOf(custId));

        btnsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = edtName.getText().toString();
                lastname = edtLastName.getText().toString();
                mobileNumber = edtMobileNumber.getText().toString();
                if (name.equals("") || name.equals(null) && lastname.equals("") || lastname.equals(null) && mobileNumber.equals("") || mobileNumber.equals(null)) {
                    Toast.makeText(ProfileActivity.this, "Enter all fields", Toast.LENGTH_SHORT).show();
                } else if (name.trim().equals("")) {
                    edtName.setError("Enter Name");
                    edtName.requestFocus();
                } else if (lastname.trim().equals("")) {
                    edtLastName.setError("Enter Last Name");
                    edtLastName.requestFocus();
                } else if (mobileNumber.equals("") || mobileNumber == null || mobileNumber.trim().length() < 10) {
                    edtMobileNumber.setError("Enter Mobile Number");
                    edtMobileNumber.requestFocus();
                } else {

                    if (isNetworkAvailable(ProfileActivity.this)) {
                        new SendEditProfileDetails().execute();
                        progressDialog(progress, "Loading", "Please wait...");
                    } else {
                        Toast.makeText(ProfileActivity.this, "Check Your Network Connection", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

        if (isNetworkAvailable(ProfileActivity.this)) {
            new GetCustomerDetails().execute();
            progressDialog(progress, "Loading", "Please wait...");
        } else {
            Toast.makeText(ProfileActivity.this, "Check Your Network Connection", Toast.LENGTH_SHORT).show();
        }

    }

    public class SendEditProfileDetails extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {

                String url = baseUrl1 + "EditProfileCustomer";
                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost(url);
                String json = "";
                // 3. build jsonObject
                JSONObject jsonObject = new JSONObject();

                jsonObject.accumulate("CustomerId", custId);
                jsonObject.accumulate("FirstName", name);
                jsonObject.accumulate("LastName", lastname);
                jsonObject.accumulate("MobileNo", mobileNumber);
                // 4. convert JSONObject to JSON to String
                json = jsonObject.toString();
                // 5. set json to StringEntity
                StringEntity se = new StringEntity(json);
                se.setContentType("application/json");
                // 6. set httpPost Entity
                httpPost.setEntity(se);
                // 7. Set some headers to inform server about the type of the content
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");
                // 8. Execute POST request to the given URL
                HttpResponse response = httpclient.execute(httpPost);

                // StatusLine stat = response.getStatusLine();
                status = response.getStatusLine().getStatusCode();

                if (status == 200) {
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);
                    JSONArray mainObject = new JSONArray(data);
                    //[{"Status":"success","UserId":"6"}]
                    JSONObject othposjsonobj = mainObject.getJSONObject(0);
                    Status = othposjsonobj.getString("Status");
                }

            } catch (Exception e) {
                DateFormat dff = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                String date = dff.format(Calendar.getInstance().getTime());
                Log.d("InputStream", e.getLocalizedMessage());
                e.printStackTrace();
                appendLog(ProfileActivity.this, "1 RegisterActivity " + e.toString() + date);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (progress.isShowing()) {
                progress.dismiss();
            }
            if (status == 200) {
                if (Status.equals("success")) {
                    //  db.updateProfile(new Customer(custId, name, mobileNumber));
                    Intent selectpickupAddressIntent = new Intent(ProfileActivity.this, MainActivity.class);
                    startActivity(selectpickupAddressIntent);
                    Toast.makeText(getApplicationContext(), "Changes saved successfully", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), " Error,Please try letter", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public class GetCustomerDetails extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                String url = baseUrl1 + "GetDetailsCustomer";
                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost(url);
                String json = "";
                // 3. build jsonObject
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("CustomerId", custId);//

//http://202.88.154.118/GroceryWebAPINew/api/Home/GetDetailsCustomer
                //{"CustomerId":123}
                // 4. convert JSONObject to JSON to String
                json = jsonObject.toString();
                // 5. set json to StringEntity
                StringEntity se = new StringEntity(json);
                se.setContentType("application/json");
                // 6. set httpPost Entity
                httpPost.setEntity(se);
                // 7. Set some headers to inform server about the type of the content
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");
                // 8. Execute POST request to the given URL
                HttpResponse httpResponse = httpclient.execute(httpPost);
                // 9. receive response as inputStream
                tempstatus = httpResponse.getStatusLine().getStatusCode();
                inputStream = httpResponse.getEntity().getContent();
                HttpResponse response = httpclient.execute(httpPost);

                // StatusLine stat = response.getStatusLine();
                status = response.getStatusLine().getStatusCode();
//
                if (status == 200) {
//
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);
                    JSONArray mainObject = new JSONArray(data);
                    JSONObject othposjsonobj = mainObject.getJSONObject(0);
                    JSONArray cityArray = othposjsonobj.getJSONArray("data");
                    for (int n = 0; n < cityArray.length(); n++) {
                        JSONObject object = cityArray.getJSONObject(n);
                        name = object.getString("FirstName");
                        lastname = object.getString("LastName");
                        mobileNumber = object.getString("MobileNo");
                    }
                } else
                    result = "Did not work!";
            } catch (Exception e) {
                DateFormat dff = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                String date = dff.format(Calendar.getInstance().getTime());
                Log.d("InputStream", e.getLocalizedMessage());
                e.printStackTrace();
                appendLog(ProfileActivity.this, "11 ProfileActivity " + e.toString() + date);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (progress.isShowing()) {
                progress.dismiss();
            }
            if (tempstatus == 200) {
                edtName.setText(name);
                edtLastName.setText(lastname);
                edtMobileNumber.setText(mobileNumber);
                tvUpdateProfile.setVisibility(View.GONE);

            } else {
                tvUpdateProfile.setVisibility(View.VISIBLE);
                // Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_LONG).show();
            }
        }
    }
}

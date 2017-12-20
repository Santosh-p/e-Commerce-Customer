package com.myapp.groceryshopcustomer.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.myapp.groceryshopcustomer.Constant.Constants.appendLog;
import static com.myapp.groceryshopcustomer.Constant.Constants.baseUrl1;
import static com.myapp.groceryshopcustomer.Constant.Constants.isNetworkAvailable;
import static com.myapp.groceryshopcustomer.Constant.Constants.progressDialog;

public class GetDeliveryAddressActivity extends AppCompatActivity {
    EditText edtCity, edtArea, edtBuilding, edtPincode, edtState, edtLandmark, edtName, edtPhoneNumber, edtAlternetPnNo;
    String city, area, building, pincode, state, landmark, name, phonenumber, alernatenumber, addressType = "", Status;
    RadioGroup rgAddressType;
    RadioButton rbHome, rbOffice;
    Button btnDeliverHere;
    DatabaseHandler db = new DatabaseHandler(this);
    int custId, status, addressId;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_delivery_address);
        getSupportActionBar().setHomeButtonEnabled(true);
        progress = new ProgressDialog(GetDeliveryAddressActivity.this);
        edtCity = (EditText) findViewById(R.id.edt_city);
        edtArea = (EditText) findViewById(R.id.edt_area);
        edtBuilding = (EditText) findViewById(R.id.edt_building);
        edtPincode = (EditText) findViewById(R.id.edt_pincode);
        edtState = (EditText) findViewById(R.id.edt_state);
        edtLandmark = (EditText) findViewById(R.id.edt_landmark);
        edtName = (EditText) findViewById(R.id.edt_address_name);
        edtPhoneNumber = (EditText) findViewById(R.id.edt_address_phone_number);
        edtAlternetPnNo = (EditText) findViewById(R.id.edt_address_alternet_phone_number);
        rgAddressType = (RadioGroup) findViewById(R.id.rgAddressType);
        rbHome = (RadioButton) findViewById(R.id.rbtn_home);
        rbOffice = (RadioButton) findViewById(R.id.rbtn_office);
        btnDeliverHere = (Button) findViewById(R.id.btn_deliver_here);

        Customer user1 = db.getCustomerDetails();
        custId = user1.getCust_id();

        btnDeliverHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                city = edtCity.getText().toString();
                area = edtArea.getText().toString();
                building = edtBuilding.getText().toString();
                pincode = edtPincode.getText().toString();
                state = edtState.getText().toString();
                landmark = edtLandmark.getText().toString();
                name = edtName.getText().toString();
                phonenumber = edtPhoneNumber.getText().toString();
                alernatenumber = edtAlternetPnNo.getText().toString();
                if (rgAddressType.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(GetDeliveryAddressActivity.this, "Select Delivery option", Toast.LENGTH_SHORT).show();
                } else {
                    if (rbHome.isChecked()) {
                        addressType = "Home";
                    }
                    if (rbOffice.isChecked()) {
                        addressType = "Office";
                    }
                }
                if (city.equals("") || city.equals(null) || city.isEmpty()) {
                    Toast.makeText(GetDeliveryAddressActivity.this, "Enter City", Toast.LENGTH_SHORT).show();
                } else if (area.equals("") || area.equals(null) || area.isEmpty()) {
                    Toast.makeText(GetDeliveryAddressActivity.this, "Enter Area", Toast.LENGTH_SHORT).show();
                } else if (building.equals("") || building.equals(null) || building.isEmpty()) {
                    Toast.makeText(GetDeliveryAddressActivity.this, "Enter Building Name", Toast.LENGTH_SHORT).show();
                } else if (pincode.equals("") || pincode.equals(null) || pincode.isEmpty()) {
                    Toast.makeText(GetDeliveryAddressActivity.this, "Enter Pincode", Toast.LENGTH_SHORT).show();
                } else if (state.equals("") || state.equals(null) || state.isEmpty()) {
                    Toast.makeText(GetDeliveryAddressActivity.this, "Enter State", Toast.LENGTH_SHORT).show();
                } else if (name.equals("") || name.equals(null) || name.isEmpty()) {
                    Toast.makeText(GetDeliveryAddressActivity.this, "Enter Name", Toast.LENGTH_SHORT).show();
                } else if (phonenumber.equals("") || phonenumber.equals(null) || phonenumber.isEmpty()) {
                    Toast.makeText(GetDeliveryAddressActivity.this, "Enter Phone Number", Toast.LENGTH_SHORT).show();
                } else if (phonenumber.length() < 10) {
                    Toast.makeText(GetDeliveryAddressActivity.this, "Enter valid Phone Number", Toast.LENGTH_SHORT).show();
                } else if (addressType.equals("") || addressType.equals(null) || addressType.isEmpty()) {
                    Toast.makeText(GetDeliveryAddressActivity.this, "Select Address Type", Toast.LENGTH_SHORT).show();
                } else {
                    if (alernatenumber.equals(null) || alernatenumber.equals("")) {
                        alernatenumber = "null";
                    }
                    if (landmark.equals(null) || landmark.equals("")) {
                        landmark = "null";
                    }
                    if (isNetworkAvailable(GetDeliveryAddressActivity.this)) {
                        new SendAddress().execute();
                        progressDialog(progress, "Loading", "Please wait...");
                    } else {
                        Toast.makeText(GetDeliveryAddressActivity.this, "Check Your Network Connection", Toast.LENGTH_SHORT).show();
                    }
                }


            }
        });


    }

    public class SendAddress extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {

                String url = baseUrl1 + "AddDeliveryAddressOfCustomer";
                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost(url);
                String json = "";
                // 3. build jsonObject
                JSONObject jsonObject = new JSONObject();

                jsonObject.accumulate("CustomerId", custId);
                jsonObject.accumulate("AddressType", addressType);
                jsonObject.accumulate("Preference", "primary");
                jsonObject.accumulate("City", city);
                jsonObject.accumulate("Area", area);
                jsonObject.accumulate("BuildingName", building);
                jsonObject.accumulate("Pincode", pincode);
                jsonObject.accumulate("State", state);
                jsonObject.accumulate("Landmark", landmark);
                jsonObject.accumulate("Name", name);
                jsonObject.accumulate("PhoneNo", phonenumber);
                jsonObject.accumulate("AlternatePhoneNo", alernatenumber);
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
                    //{"Status":"success","AddressId":2}
                    JSONObject othposjsonobj = mainObject.getJSONObject(0);
                    Status = othposjsonobj.getString("Status");
                    addressId = othposjsonobj.getInt("AddressId");
                }

            } catch (Exception e) {
                DateFormat dff = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                String date = dff.format(Calendar.getInstance().getTime());
                Log.d("InputStream", e.getLocalizedMessage());
                e.printStackTrace();
                appendLog(GetDeliveryAddressActivity.this, "1 GetDeliveryAddressActivity " + e.toString() + date);
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
                    pref = GetDeliveryAddressActivity.this.getSharedPreferences("mypref", MODE_PRIVATE);
                    editor = pref.edit();
                    editor.putInt("addressId", addressId);
                    editor.apply();
                    Intent selectAddressIntent = new Intent(GetDeliveryAddressActivity.this, PlaceOrderActivity.class);
                    startActivity(selectAddressIntent);
                }
            }
        }
    }
}

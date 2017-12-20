package com.myapp.groceryshopcustomer.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

public class DeliveryOptionActivity extends AppCompatActivity {
    RadioGroup rgChooseDelivery;
    RadioButton rbDelivery, rbPickup;
    Button btnContinue;
    DatabaseHandler db = new DatabaseHandler(this);
    String totalAmount;
    TextView tvTotal;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    String name, lastname, mobileNumber;
    ProgressDialog progress;

    int status, custId, addressId;
    int tempstatus;
    InputStream inputStream = null;
    String result = null;
    boolean isAlreadyRegister = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_option);
        progress = new ProgressDialog(DeliveryOptionActivity.this);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        rbDelivery = (RadioButton) findViewById(R.id.rbtn_delivery);
        rbPickup = (RadioButton) findViewById(R.id.rbtn_pickup);
        btnContinue = (Button) findViewById(R.id.btn_continue_to_order);
        rgChooseDelivery = (RadioGroup) findViewById(R.id.rgChooseDelivery);
        tvTotal = (TextView) findViewById(R.id.tv_total_delivery_option);

        pref = getApplicationContext().getSharedPreferences("mypref", MODE_PRIVATE);
        editor = pref.edit();
        totalAmount = pref.getString("TotalAmt", null);
        tvTotal.setText("₹ " + totalAmount);
        rbDelivery.setText(Html.fromHtml("<b><big>" + "Delivery" + "</big></b>" + "<br />" +
                "<small>" + "subtitle" + "</small>" + "<br />"));

        rbPickup.setText(Html.fromHtml("<b><big>" + "Pick Up" + "</big></b>" + "<br />" +
                "<small>" + "subtitle" + "</small>" + "<br />"));
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (rgChooseDelivery.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(DeliveryOptionActivity.this, "Select Delivery option", Toast.LENGTH_SHORT).show();
                } else {

                    if (rbDelivery.isChecked()) {
                        editor.putInt("delOption", 1);
                        editor.apply();

                        if (isAlreadyRegister) {
                            Intent deliveryAddressIntent = new Intent(DeliveryOptionActivity.this, ChooseDeliveryAddActivity.class);
                            startActivity(deliveryAddressIntent);
                        } else {
                            Intent registerIntent = new Intent(DeliveryOptionActivity.this, RegisterActivity.class);
                            startActivity(registerIntent);

                        }
                    }

                    if (rbPickup.isChecked()) {
                        editor.putInt("delOption", 0);
                        editor.apply();

                        if (isAlreadyRegister) {
                            Intent placeOrderIntent = new Intent(DeliveryOptionActivity.this, PlaceOrderActivity.class);
                            startActivity(placeOrderIntent);
                        } else {
                            Intent registerIntent = new Intent(DeliveryOptionActivity.this, RegisterActivity.class);
                            startActivity(registerIntent);

                        }

                    }
                }

            }
        });

        Customer user1 = db.getCustomerDetails();
        custId = user1.getCust_id();
        if (isNetworkAvailable(DeliveryOptionActivity.this)) {
            new GetCustomerDetails().execute();
            progressDialog(progress, "Loading", "Please wait...");
        } else {
            Toast.makeText(DeliveryOptionActivity.this, "Check Your Network Connection", Toast.LENGTH_SHORT).show();
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
                appendLog(DeliveryOptionActivity.this, "11 DeliveryOptionActivity " + e.toString() + date);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (progress.isShowing()) {
                progress.dismiss();
            }
            if (tempstatus == 200) {
//
//                pref = getApplicationContext().getSharedPreferences("mypref", MODE_PRIVATE);
//                editor = pref.edit();
//                delOption = pref.getInt("delOption", 0);
//                if (delOption == 0) {
//
//                    Intent selectpickupAddressIntent = new Intent(ChooseDeliveryAddActivity.this, PlaceOrderActivity.class);
//                    startActivity(selectpickupAddressIntent);
//                } else {
//                    Intent selectAddressIntent = new Intent(ChooseDeliveryAddActivity.this, ChooseDeliveryAddActivity.class);
//                    startActivity(selectAddressIntent);
//                }

                isAlreadyRegister = true;
            } else {
                isAlreadyRegister = false;
                // Toast.makeText(getApplicationContext(), "Please update your profile", Toast.LENGTH_LONG).show();
            }
        }
    }

}

package com.myapp.groceryshopcustomer.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.myapp.groceryshopcustomer.DBHandler.Address;
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
import static com.myapp.groceryshopcustomer.Constant.Constants.mid;

public class PlaceOrderActivity extends AppCompatActivity {
    int custId, productId, status, delOption, addressId;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    DatabaseHandler db = new DatabaseHandler(this);
    String strStatus, orderType;
    Button btnConfirmOrder;
    TextView tvTotal;
    Address addressObj;
    TextView txtName, txtAddress, txtMobileNumber, txtAlternetMobileNumber, txtxTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);
        btnConfirmOrder = (Button) findViewById(R.id.btn_confirm_order);
        tvTotal = (TextView) findViewById(R.id.tv_total_place_order);
        txtName = (TextView) findViewById(R.id.tv_address_namePO);
        txtAddress = (TextView) findViewById(R.id.tv_addressPO);
        txtMobileNumber = (TextView) findViewById(R.id.tv_address_mobile_numberPO);
        txtAlternetMobileNumber = (TextView) findViewById(R.id.tv_address_alternate_mobile_numberPO);
        txtxTitle = (TextView) findViewById(R.id.tv_title);


        SharedPreferences pref;
        SharedPreferences.Editor editor;
        pref = getApplicationContext().getSharedPreferences("mypref", MODE_PRIVATE);
        editor = pref.edit();
        String totalAmount = pref.getString("TotalAmt", null);
        tvTotal.setText("₹ " + totalAmount);
        Customer user1 = db.getCustomerDetails();
        custId = user1.getCust_id();
        pref = getApplicationContext().getSharedPreferences("mypref", MODE_PRIVATE);
        editor = pref.edit();
        //   mid = pref.getInt("mid", 0);
        productId = pref.getInt("productID", 0);
        delOption = pref.getInt("delOption", 0);
        addressId = pref.getInt("addressId", 0);

        if (delOption == 1) {
            txtxTitle.setText("Delivery Address -");
            orderType = "Delivery";
            new JSONAsyncTaskToGetDeliveryAddress().execute();
        } else {
            txtxTitle.setText("Pick Up Address -");
            orderType = "Pick Up";
            new JSONAsyncTaskToGetPickupAddress().execute();
        }
        btnConfirmOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new JSONAsyncTaskToPlaceOrder().execute();
            }
        });
    }

    public class JSONAsyncTaskToPlaceOrder extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                // code to consume wcf service which sends the details to the server
                String url = baseUrl1 + "PlaceOrder";
                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost(url);
                String json = "";

                // 3. build jsonObject
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("CustomerId", custId);
                jsonObject.accumulate("MerchantId", mid);
                jsonObject.accumulate("OrderType", orderType);
                jsonObject.accumulate("AddressId", addressId);

                // jsonObject.accumulate("ProductId", productId);
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

                HttpResponse response = httpclient.execute(httpPost);
                // 9. receive response as inputStream
                // inputStream = httpResponse.getEntity().getContent();
                // 10. convert inputstream to string

                // StatusLine stat = response.getStatusLine();
                status = response.getStatusLine().getStatusCode();
                if (status == 200) {
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);
                    JSONArray mainObject = new JSONArray(data);
                    JSONObject othposjsonobj = mainObject.getJSONObject(0);
                    //"Status":"success","Count":6

                    strStatus = othposjsonobj.getString("Status");//merchantuninstalledapp

                } else {
                    // result = "Did not work!";
                }
            } catch (Exception e) {
                DateFormat dff = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                String date = dff.format(Calendar.getInstance().getTime());
                Log.d("InputStream", e.getLocalizedMessage());
                e.printStackTrace();
                appendLog(PlaceOrderActivity.this, "1 PlaceOrderActivity " + e.toString() + date);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (status == 200) {
                if (strStatus.equals("success")) {
                    Intent selectCategoryIntent = new Intent(PlaceOrderActivity.this, MainActivity.class);
                    startActivity(selectCategoryIntent);

                    Toast.makeText(PlaceOrderActivity.this, "Order Placed Successfully.", Toast.LENGTH_SHORT).show();
                }
                if (strStatus.equals("merchantuninstalledapp")) {
                    Toast.makeText(PlaceOrderActivity.this, "Merchant uninstalled app Order not send", Toast.LENGTH_SHORT).show();
                    Intent selectCategoryIntent = new Intent(PlaceOrderActivity.this, MainActivity.class);
                    startActivity(selectCategoryIntent);
                }
            }
        }
    }


    public class JSONAsyncTaskToGetDeliveryAddress extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                // code to consume wcf service which sends the details to the server
                String url = baseUrl1 + "getDeliveryAddressById";
                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost(url);
                String json = "";

                // 3. build jsonObject
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("AddressId", addressId);
                // jsonObject.accumulate("ProductId", productId);
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

                HttpResponse response = httpclient.execute(httpPost);
                // 9. receive response as inputStream
                // inputStream = httpResponse.getEntity().getContent();
                // 10. convert inputstream to string

                // StatusLine stat = response.getStatusLine();
                status = response.getStatusLine().getStatusCode();
                if (status == 200) {
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);
                    JSONArray mainObject = new JSONArray(data);
                    JSONObject othposjsonobj = mainObject.getJSONObject(0);
                    //{"Status":"success","data":[{"AddressId":2,"AddressType":"default","Preference":"primary","City":"pune1","Area":"bhusari1","BuildingName":"deodar1","Pincode":"8999","State":"maharashtra","Landmark":"null","Name":"rajashri","PhoneNo":"89743687654","AlternatePhoneNo":"null"}]}
                    strStatus = othposjsonobj.getString("Status");
                    JSONArray productArray = othposjsonobj.getJSONArray("data");

                    JSONObject object = productArray.getJSONObject(0);
                    addressObj = new Address();
                    addressObj.setAddressId(object.getInt("AddressId"));
                    addressObj.setAddressType(object.getString("AddressType"));
                    addressObj.setCity(object.getString("City"));
                    addressObj.setArea(object.getString("Area"));
                    addressObj.setBuilding(object.getString("BuildingName"));
                    addressObj.setPincode(object.getString("Pincode"));
                    addressObj.setState(object.getString("State"));
                    addressObj.setLandmark(object.getString("Landmark"));
                    addressObj.setName(object.getString("Name"));
                    addressObj.setPhonenumber(object.getString("PhoneNo"));
                    addressObj.setAlernatenumber(object.getString("AlternatePhoneNo"));
                    addressObj.setStatus("no");


                } else {
                    // result = "Did not work!";
                }
            } catch (Exception e) {
                DateFormat dff = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                String date = dff.format(Calendar.getInstance().getTime());
                Log.d("InputStream", e.getLocalizedMessage());
                e.printStackTrace();
                appendLog(PlaceOrderActivity.this, "2 PlaceOrderActivity " + e.toString() + date);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (status == 200) {
                if (strStatus.equals("success")) {
                    if (!addressObj.getName().equals("null")) {
                        txtName.setText(addressObj.getName());
                    }
                    if (!addressObj.getLandmark().equals("null")) {
                        txtAddress.setText(addressObj.getBuilding() + ", " + addressObj.getArea() + ", " + addressObj.getLandmark() + ", " + addressObj.getCity() + ", " + addressObj.getState() + ", " + addressObj.getPincode());

                    } else {
                        txtAddress.setText(addressObj.getBuilding() + ", " + addressObj.getArea() + ", " + addressObj.getCity() + ", " + addressObj.getState() + ", " + addressObj.getPincode());

                    }
                    txtMobileNumber.setText(addressObj.getPhonenumber());
                    if (!addressObj.getAlernatenumber().equals("null")) {
                        txtAlternetMobileNumber.setText(addressObj.getAlernatenumber());
                    }
                }
            }
        }
    }


    public class JSONAsyncTaskToGetPickupAddress extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                // code to consume wcf service which sends the details to the server
                String url = baseUrl1 + "getMerchantAddressById";
                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost(url);
                String json = "";

                // 3. build jsonObject
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("MerchantId", mid);
                // jsonObject.accumulate("ProductId", productId);
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

                HttpResponse response = httpclient.execute(httpPost);
                // 9. receive response as inputStream
                // inputStream = httpResponse.getEntity().getContent();
                // 10. convert inputstream to string

                // StatusLine stat = response.getStatusLine();
                status = response.getStatusLine().getStatusCode();
                if (status == 200) {
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);
                    JSONArray mainObject = new JSONArray(data);
                    JSONObject othposjsonobj = mainObject.getJSONObject(0);
                    //{"Status":"success","data":[{"ShopName":"Raj Grocery Shop","ShopAddress":"Right Bhusari Colony","MobileNo":"9096326024","City":"Pune","Area":"Kothrud"}]}

                    strStatus = othposjsonobj.getString("Status");
                    JSONArray productArray = othposjsonobj.getJSONArray("data");

                    JSONObject object = productArray.getJSONObject(0);
                    addressObj = new Address();
                    addressObj.setCity(object.getString("City"));
                    addressObj.setArea(object.getString("Area"));
                    addressObj.setBuilding(object.getString("ShopAddress"));
                    addressObj.setName(object.getString("ShopName"));
                    addressObj.setPhonenumber(object.getString("MobileNo"));

                } else {
                    // result = "Did not work!";
                }
            } catch (Exception e) {
                DateFormat dff = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                String date = dff.format(Calendar.getInstance().getTime());
                Log.d("InputStream", e.getLocalizedMessage());
                e.printStackTrace();
                appendLog(PlaceOrderActivity.this, "3 PlaceOrderActivity " + e.toString() + date);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (status == 200) {
                if (strStatus.equals("success")) {
                    txtName.setText(addressObj.getName());
                    txtAddress.setText(addressObj.getBuilding() + ", " + addressObj.getArea() + "," + addressObj.getCity());
                    txtMobileNumber.setText(addressObj.getPhonenumber());

                    txtAlternetMobileNumber.setText("");
                }
            }
        }
    }
}

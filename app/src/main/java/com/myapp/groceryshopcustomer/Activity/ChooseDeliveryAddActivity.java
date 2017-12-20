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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.myapp.groceryshopcustomer.Adapters.AddressListAdapter;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static com.myapp.groceryshopcustomer.Constant.Constants.appendLog;
import static com.myapp.groceryshopcustomer.Constant.Constants.baseUrl1;
import static com.myapp.groceryshopcustomer.Constant.Constants.isNetworkAvailable;
import static com.myapp.groceryshopcustomer.Constant.Constants.progressDialog;

public class ChooseDeliveryAddActivity extends AppCompatActivity {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Button btnAddAddress;
    public AddressListAdapter addressListAdapter;
    ListView lstAddList;
    DatabaseHandler db = new DatabaseHandler(this);
    int status, custId, addressId;
    Address addressObj;
    ArrayList<Address> addressList = new ArrayList<Address>();
    TextView tvTotal;
    String responceStatus;
    int tempstatus;
    InputStream inputStream = null;
    String result = null;
    ProgressDialog progress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_delivery_add);
        progress = new ProgressDialog(ChooseDeliveryAddActivity.this);
        getSupportActionBar().setHomeButtonEnabled(true);
        btnAddAddress = (Button) findViewById(R.id.btn_add_address);
        lstAddList = (ListView) findViewById(R.id.lst_address_list);
        tvTotal = (TextView) findViewById(R.id.tv_total_choose_delivery);

        pref = getApplicationContext().getSharedPreferences("mypref", MODE_PRIVATE);
        editor = pref.edit();
        String totalAmount = pref.getString("TotalAmt", null);
        tvTotal.setText("₹ " + totalAmount);
        Customer user1 = db.getCustomerDetails();
        custId = user1.getCust_id();
        if (isNetworkAvailable(ChooseDeliveryAddActivity.this)) {
            new JSONAsyncTaskToGetAddressList().execute();
            progressDialog(progress, "Getting address list", "Please wait...");
        } else {
            Toast.makeText(ChooseDeliveryAddActivity.this, "Check Your Network Connection", Toast.LENGTH_SHORT).show();
        }

        btnAddAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getAddressIntent = new Intent(ChooseDeliveryAddActivity.this, GetDeliveryAddressActivity.class);
                startActivity(getAddressIntent);
            }
        });

    }

    public class JSONAsyncTaskToGetAddressList extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                // code to consume wcf service which sends the details to the server
                String url = baseUrl1 + "getDeliveryAddressDetailsCustomer";
                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost(url);
                String json = "";

                // 3. build jsonObject
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("CustomerId", custId);

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
                    addressList.clear();
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);
                    JSONArray mainObject = new JSONArray(data);
                    JSONObject othposjsonobj = mainObject.getJSONObject(0);
                    //{"Status":"success","data":[{"AddressType":"Home","Preference":"primary","City":"pune","Area":"shivaji nagar","BuildingName":"14 b rajwada","Pincode":"411036","State":"maharashtra","Landmark":"near pantaloons","Name":"santosh","PhoneNo":"555544456","AlternatePhoneNo":"null"}]}
                    JSONArray productArray = othposjsonobj.getJSONArray("data");
                    for (int n = 0; n < productArray.length(); n++) {
                        JSONObject object = productArray.getJSONObject(n);
                        Address proObj = new Address();
                        proObj.setAddressId(object.getInt("AddressId"));
                        proObj.setAddressType(object.getString("AddressType"));
                        proObj.setCity(object.getString("City"));
                        proObj.setArea(object.getString("Area"));
                        proObj.setBuilding(object.getString("BuildingName"));
                        proObj.setPincode(object.getString("Pincode"));
                        proObj.setState(object.getString("State"));
                        proObj.setLandmark(object.getString("Landmark"));
                        proObj.setName(object.getString("Name"));
                        proObj.setPhonenumber(object.getString("PhoneNo"));
                        proObj.setAlernatenumber(object.getString("AlternatePhoneNo"));
                        proObj.setStatus("no");
                        addressList.add(proObj);
                        proObj = null;
                    }
                } else {
                    // result = "Did not work!";
                }
            } catch (Exception e) {
                DateFormat dff = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                String date = dff.format(Calendar.getInstance().getTime());
                Log.d("InputStream", e.getLocalizedMessage());
                e.printStackTrace();
                appendLog(ChooseDeliveryAddActivity.this, "1 ChooseDeliveryAddActivity " + e.toString() + date);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (progress.isShowing()) {
                progress.dismiss();
            }
            if (status == 200) {

                addressListAdapter = new AddressListAdapter(ChooseDeliveryAddActivity.this,
                        R.layout.cart_list_item, addressList);
                lstAddList.setAdapter(addressListAdapter);
                addressListAdapter.notifyDataSetChanged();
            }
        }
    }

    public void RemoveAddress(View v) {

        addressObj = (Address) v.getTag();
        addressId = addressObj.getAddressId();
        // Toast.makeText(ChooseDeliveryAddActivity.this, "Delete pending", Toast.LENGTH_SHORT).show();
        new RemoveDeliveryAddressCustomer().execute();
    }

    public void editAddress(View v) {

        addressObj = (Address) v.getTag();
        editor.putInt("addressId", addressObj.getAddressId());
        editor.putString("addressCity", addressObj.getCity());
        editor.putString("addressArea", addressObj.getArea());
        editor.putString("addressBuilding", addressObj.getBuilding());
        editor.putString("addressPincode", addressObj.getPincode());
        editor.putString("addressState", addressObj.getState());
        editor.putString("addressLandmark", addressObj.getLandmark());
        editor.putString("addressName", addressObj.getName());
        editor.putString("addressPhoneNumber", addressObj.getPhonenumber());
        editor.putString("addressAlternateNumber", addressObj.getAlernatenumber());
        editor.putString("addressAddressType", addressObj.getAddressType());


        editor.apply();
        Intent selectAddressIntent = new Intent(ChooseDeliveryAddActivity.this, EditAddressActivity.class);
        startActivity(selectAddressIntent);
        // Toast.makeText(ChooseDeliveryAddActivity.this, "pending", Toast.LENGTH_SHORT).show();
    }

    public void deliverHere(View v) {
        addressObj = (Address) v.getTag();
        editor.putInt("addressId", addressObj.getAddressId());
        editor.apply();
        Intent selectAddressIntent = new Intent(ChooseDeliveryAddActivity.this, PlaceOrderActivity.class);
        startActivity(selectAddressIntent);
        //   Toast.makeText(ChooseDeliveryAddActivity.this, "Edit", Toast.LENGTH_SHORT).show();
    }

    public class RemoveDeliveryAddressCustomer extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                String url = baseUrl1 + "RemoveDeliveryAddressCustomer";
                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost(url);
                String json = "";
                // 3. build jsonObject
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("CustomerId", custId);
                jsonObject.accumulate("AddressId", addressId);


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
                // 10. convert inputstream to string
                if (inputStream != null) {
                    result = convertInputStreamToString(inputStream);
                    result = result.replace("[", "");
                    result = result.replace("]", "");
                    JSONObject mainObject = new JSONObject(result);
                    /// String areaID = mainObject.getString("$id");
                    responceStatus = mainObject.getString("Status");
                } else
                    result = "Did not work!";
            } catch (Exception e) {
                DateFormat dff = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                String date = dff.format(Calendar.getInstance().getTime());
                Log.d("InputStream", e.getLocalizedMessage());
                e.printStackTrace();
                appendLog(ChooseDeliveryAddActivity.this, "2 ChooseDeliveryAddActivity " + e.toString() + date);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (responceStatus.equals("success")) {
                Toast.makeText(getApplicationContext(), "Address Deleted successfully...", Toast.LENGTH_LONG).show();

                new JSONAsyncTaskToGetAddressList().execute();
            } else {
                Toast.makeText(getApplicationContext(), "Error,Please try letter", Toast.LENGTH_LONG).show();
            }
        }
    }

    public String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }

}

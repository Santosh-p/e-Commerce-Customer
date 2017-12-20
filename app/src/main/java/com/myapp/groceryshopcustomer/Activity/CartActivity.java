package com.myapp.groceryshopcustomer.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.myapp.groceryshopcustomer.Adapters.CartDetailsListAdapter;
import com.myapp.groceryshopcustomer.DBHandler.Cart;
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
import java.util.ArrayList;
import java.util.Calendar;

import static com.myapp.groceryshopcustomer.Constant.Constants.appendLog;
import static com.myapp.groceryshopcustomer.Constant.Constants.baseUrl1;
import static com.myapp.groceryshopcustomer.Constant.Constants.isNetworkAvailable;
import static com.myapp.groceryshopcustomer.Constant.Constants.mid;
import static com.myapp.groceryshopcustomer.Constant.Constants.progressDialog;

public class CartActivity extends AppCompatActivity {
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    int status, custId, itemsCount, productId, unitQty;
    ListView lstProducts;
    Button btnplaceOrder, btnAddItems;
    public static CartDetailsListAdapter cartItemAdapter;
    ArrayList<Cart> productList = new ArrayList<Cart>();
    Cart cartObj;
    TextView txtItems, txtTotalAmt;
    String strStatus, strUnit, result;
    Double totalPrice = 0.00;
    ProgressDialog progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progress = new ProgressDialog(CartActivity.this);

        lstProducts = (ListView) findViewById(R.id.lst_cart_items);
        btnplaceOrder = (Button) findViewById(R.id.btn_place_order);
        btnAddItems = (Button) findViewById(R.id.btn_Add_items);
        txtItems = (TextView) findViewById(R.id.tv_items);
        txtTotalAmt = (TextView) findViewById(R.id.tv_total_amt);
        DatabaseHandler db = new DatabaseHandler(CartActivity.this);
        Customer user1 = db.getCustomerDetails();
        custId = user1.getCust_id();
        pref = getApplicationContext().getSharedPreferences("mypref", MODE_PRIVATE);
        editor = pref.edit();
        //   mid = pref.getInt("mid", 0);
        if (isNetworkAvailable(CartActivity.this)) {
            new JSONAsyncTaskToGetCartDetails().execute();
            progressDialog(progress, "Loading", "Please wait...");
        } else {
            Toast.makeText(CartActivity.this, "Check Your Network Connection", Toast.LENGTH_SHORT).show();
        }

        btnplaceOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (productList.size() <= 0) {
                    Toast.makeText(CartActivity.this, "Need to add items", Toast.LENGTH_SHORT).show();

                } else {
                    editor.putString("TotalAmt", String.valueOf(totalPrice));
                    editor.apply();
                    Intent CDIntent = new Intent(CartActivity.this, DeliveryOptionActivity.class);
                    startActivity(CDIntent);
                }
                // new JSONAsyncTaskToPlaceOrder().execute();
            }
        });
        btnAddItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent ProductsIntent = new Intent(CartActivity.this, MainActivity.class);
                startActivity(ProductsIntent);
                // new JSONAsyncTaskToPlaceOrder().execute();
            }
        });
    }

    public class JSONAsyncTaskToGetCartDetails extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                // code to consume wcf service which sends the details to the server
                String url = baseUrl1 + "getAllProductsInCart";
                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost(url);
                String json = "";

                // 3. build jsonObject
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("CustomerId", custId);
                jsonObject.accumulate("MerchantId", mid);

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
                    //{"Status":"success","data":[{"ProductId":16,"ProductName":"MTR Ready To Eat Mutter Paneer","ProductImage":"sdfghn","Manufacturer":"MTR","CategoryName":"Packaged Food","Unit":"11Kg","Qty":2,"Price":0},{"ProductId":16,"ProductName":"MTR Ready To Eat Mutter Paneer","ProductImage":"sdfghn","Manufacturer":"MTR","CategoryName":"Packaged Food","Unit":"500gm","Qty":1,"Price":125},{"ProductId":17,"ProductName":"Pampers Baby Dry","ProductImage":"sdfghn","Manufacturer":"Pampers","CategoryName":"Baby & Kids","Unit":"48piece","Qty":1,"Price":484},{"ProductId":17,"ProductName":"Pampers Baby Dry","ProductImage":"sdfghn","Manufacturer":"Pampers","CategoryName":"Baby & Kids","Unit":"48piece","Qty":2,"Price":484},{"ProductId":17,"ProductName":"Pampers Baby Dry","ProductImage":"sdfghn","Manufacturer":"Pampers","CategoryName":"Baby & Kids","Unit":"48piece","Qty":2,"Price":484},{"ProductId":17,"ProductName":"Pampers Baby Dry","ProductImage":"sdfghn","Manufacturer":"Pampers","CategoryName":"Baby & Kids","Unit":"48piece","Qty":2,"Price":484}]}
                    JSONArray productArray = othposjsonobj.getJSONArray("data");

                    productList.clear();
                    itemsCount = 0;
                    totalPrice = 0.0;

                    for (int n = 0; n < productArray.length(); n++) {
                        JSONObject object = productArray.getJSONObject(n);
                        Cart proObj = new Cart();
                        proObj.setProductId(object.getInt("ProductId"));
                        proObj.setProductName(object.getString("ProductName"));
                        proObj.setManufacturer(object.getString("Manufacturer"));
                        proObj.setProductImage(object.getString("ProductImage"));
                        proObj.setCategoryName(object.getString("CategoryName"));
                        proObj.setUnit(object.getString("Unit"));
                        proObj.setQty(object.getInt("Qty"));
                        proObj.setPrice(object.getDouble("Price"));
                        proObj.setMrp(object.getDouble("MRP"));
                        proObj.setProductTotalPrice(object.getDouble("Price") * object.getInt("Qty"));

                        itemsCount++;
                        // Double tempprice=object.getDouble("Price");
                        totalPrice = totalPrice + object.getDouble("Price") * object.getInt("Qty");
                        productList.add(proObj);

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
                appendLog(CartActivity.this, "1 CartActivity " + e.toString() + date);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (progress.isShowing()) {
                progress.dismiss();
            }
            if (status == 200) {

                cartItemAdapter = new CartDetailsListAdapter(CartActivity.this,
                        R.layout.main_view_list_row, productList);
                lstProducts.setAdapter(cartItemAdapter);
                cartItemAdapter.notifyDataSetChanged();
                txtItems.setText(String.valueOf("Items :" + itemsCount));
                txtTotalAmt.setText(String.valueOf("Total Price : ₹ " + String.format("%.02f", totalPrice)));
                // holder.txtsave.setText("Save : ₹" + String.format("%.02f", save));
            }
        }
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

                    strStatus = othposjsonobj.getString("Status");

                } else {
                    // result = "Did not work!";
                }
            } catch (Exception e) {
                DateFormat dff = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                String date = dff.format(Calendar.getInstance().getTime());
                Log.d("InputStream", e.getLocalizedMessage());
                e.printStackTrace();
                appendLog(CartActivity.this, "2 CartActivity " + e.toString() + date);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (status == 200) {
                if (strStatus.equals("success")) {
                    onBackPressed();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    public void increaseItem(View v) {
        cartObj = (Cart) v.getTag();
        productId = cartObj.getProductId();
        strUnit = cartObj.getUnit();
        unitQty = cartObj.getQty() + 1;

        new JSONAsyncTaskToIncProQty().execute();
        //    Toast.makeText(CartActivity.this, "Increased", Toast.LENGTH_SHORT).show();
    }

    public void decreaseItem(View v) {
        cartObj = (Cart) v.getTag();
        productId = cartObj.getProductId();
        strUnit = cartObj.getUnit();
        unitQty = cartObj.getQty() - 1;

        new JSONAsyncTaskToIncProQty().execute();
        // Toast.makeText(CartActivity.this, "Decreased", Toast.LENGTH_SHORT).show();
    }

    public class JSONAsyncTaskToIncProQty extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                // code to consume wcf service which sends the details to the server
                String url = baseUrl1 + "IncOrDecCartProduct";
                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost(url);
                String json = "";

                // 3. build jsonObject
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("MerchantId", mid);
                jsonObject.accumulate("CustomerId", custId);
                jsonObject.accumulate("ProductId", productId);
                jsonObject.accumulate("Unit", strUnit);
                jsonObject.accumulate("Qty", unitQty);

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
                    JSONObject firstobj = mainObject.getJSONObject(0);
                    result = firstobj.getString("Status");

                } else {
                    // result = "Did not work!";
                }
            } catch (Exception e) {
                DateFormat dff = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                String date = dff.format(Calendar.getInstance().getTime());
                Log.d("InputStream", e.getLocalizedMessage());
                e.printStackTrace();
                appendLog(CartActivity.this, "3 CartActivity " + e.toString() + date);
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (status == 200) {
                if (result.equals("success")) {
                    if (isNetworkAvailable(CartActivity.this)) {
                        new JSONAsyncTaskToGetCartDetails().execute();
                        progressDialog(progress, "Loading", "Please wait...");
                    } else {
                        Toast.makeText(CartActivity.this, "Check Your Network Connection", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cart_items_comp, menu);
        // setBadgeCount(this, mCartMenuIcon, String.valueOf(2));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        int id = item.getItemId();
//        if (id == R.id.action_compare) {
//
//            Intent cartIntent = new Intent(CartActivity.this, ShopListToCompActivity.class);
//            startActivity(cartIntent);
//
//        }
        return super.onOptionsItemSelected(item);
    }
}

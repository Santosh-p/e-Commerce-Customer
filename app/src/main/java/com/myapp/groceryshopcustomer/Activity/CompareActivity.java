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

import com.myapp.groceryshopcustomer.Adapters.CompareProductsAdapter;
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

public class CompareActivity extends AppCompatActivity {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    int status, custId, itemsCount, comItemsCount, productId, unitQty, midToComp;
    ListView lstProducts;
    Button btnSwitchOrder, btnSkip;
    public static CompareProductsAdapter cartItemAdapter;
    ArrayList<Cart> productList = new ArrayList<Cart>();
    Cart cartObj;
    TextView txtItems, txtTotalAmt, txtCompItems, txtCompTotalAmt, txtShopName, txtShopNameToComp;
    String strStatus, strUnit, result, shopName, shopNameToComp;
    Double totalPrice = 0.00, comTotalPrice = 0.00;
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progress = new ProgressDialog(CompareActivity.this);
        lstProducts = (ListView) findViewById(R.id.lst_cart_items);
        btnSwitchOrder = (Button) findViewById(R.id.btn_switch_order);
        btnSkip = (Button) findViewById(R.id.btn_skip);

        txtItems = (TextView) findViewById(R.id.tv_items);
        txtTotalAmt = (TextView) findViewById(R.id.tv_total_amt);
        txtShopName = (TextView) findViewById(R.id.tv_shop_name);

        txtCompItems = (TextView) findViewById(R.id.tv_items1);
        txtCompTotalAmt = (TextView) findViewById(R.id.tv_total_amt1);
        txtShopNameToComp = (TextView) findViewById(R.id.tv_shop_name1);

        DatabaseHandler db = new DatabaseHandler(CompareActivity.this);
        Customer user1 = db.getCustomerDetails();
        custId = user1.getCust_id();
        pref = getApplicationContext().getSharedPreferences("mypref", MODE_PRIVATE);
        editor = pref.edit();
    //    mid = pref.getInt("mid", 0);
        midToComp = pref.getInt("midToComp", 0);
        shopName = pref.getString("shopName", null);
        shopNameToComp = pref.getString("shopNameToComp", null);

        txtShopName.setText(shopName);
        txtShopNameToComp.setText(shopNameToComp);

        if (isNetworkAvailable(CompareActivity.this)) {
            new JSONAsyncTaskToGetCartDetails().execute();
            progressDialog(progress, "Loading", "Please wait...");
        } else {
            Toast.makeText(CompareActivity.this, "Check Your Network Connection", Toast.LENGTH_SHORT).show();
        }


        btnSwitchOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (isNetworkAvailable(CompareActivity.this)) {
                    new JSONAsyncTaskToSwitchOrder().execute();
                    progressDialog(progress, "Loading", "Please wait...");
                } else {
                    Toast.makeText(CompareActivity.this, "Check Your Network Connection", Toast.LENGTH_SHORT).show();
                }


            }
        });
        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent ProductsIntent = new Intent(CompareActivity.this, ProductListActivity.class);
                startActivity(ProductsIntent);

            }
        });
    }

    public class JSONAsyncTaskToGetCartDetails extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                // code to consume wcf service which sends the details to the server
                String url = baseUrl1 + "getPriceCompare";
                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost(url);
                String json = "";

                // 3. build jsonObject
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("CustomerId", custId);
                jsonObject.accumulate("MerchantId", mid);
                jsonObject.accumulate("PriceComapareMerchantId", midToComp);

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
                    //{"Status":"success","data":[{"ProductId":4,"ProductName":"Banana","ProductImage":"banana.jpg","Manufacturer":"Rajendra Kumar","CategoryName":"Fruits","Unit":"1Dazen","Qty":1,"Price":40,"Availability":"no","AnotherMerchantPrice":0},{"ProductId":11,"ProductName":"Pressuer cooker","ProductImage":"cooker.jpg","Manufacturer":"Vaidarbhi Engineering Company","CategoryName":"Household Needs","Unit":"1Piece","Qty":1,"Price":2000,"Availability":"no","AnotherMerchantPrice":0},{"ProductId":18,"ProductName":"Pediasure Premium Chocolates","ProductImage":"PediasurePremiumChocolates.jpg","Manufacturer":"Pedia","CategoryName":"Bab","Unit":"1Piece","Qty":1,"Price":300,"Availability":"no","AnotherMerchantPrice":0},{"ProductId":21,"ProductName":"Chheda's Golden Potato Chips-Lightly Salted","ProductImage":"ChhedaGoldenPotatoChipsLightlySalted.jpg","Manufacturer":"Chheda's","CategoryName":"Packaged Food","Unit":"170gm","Qty":1,"Price":70,"Availability":"no","AnotherMerchantPrice":0}]}
                    JSONArray productArray = othposjsonobj.getJSONArray("data");
                    //"Availability":"no","AnotherMerchantPrice":0
                    productList.clear();
                    itemsCount = 0;
                    comItemsCount = 0;
                    totalPrice = 0.0;
                    comTotalPrice = 0.00;

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
                        proObj.setAvailablityStatus(object.getString("Availability"));
                        proObj.setComPrice(object.getDouble("AnotherMerchantPrice"));
                        proObj.setProductTotalPrice(object.getDouble("Price") * object.getInt("Qty"));
                        proObj.setCompProductTotalPrice(object.getDouble("AnotherMerchantPrice") * object.getInt("Qty"));

                        if (!object.getString("Availability").equals("no")) {
                            comItemsCount++;
                            comTotalPrice = comTotalPrice + object.getDouble("AnotherMerchantPrice") * object.getInt("Qty");
                        } else {
                            proObj.setComPrice(0.00);
                            proObj.setCompProductTotalPrice(0.00);
                        }
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
                appendLog(CompareActivity.this, "1 CompareActivity " + e.toString() + date);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (progress.isShowing()) {
                progress.dismiss();
            }
            if (status == 200) {

                cartItemAdapter = new CompareProductsAdapter(CompareActivity.this,
                        R.layout.main_view_list_row, productList);
                lstProducts.setAdapter(cartItemAdapter);
                cartItemAdapter.notifyDataSetChanged();
                txtItems.setText(String.valueOf("Items :" + itemsCount));
                txtTotalAmt.setText(String.valueOf("Total Price : ₹ " + totalPrice));
                txtCompItems.setText(String.valueOf("Items :" + comItemsCount));
                txtCompTotalAmt.setText(String.valueOf("Total Price : ₹ " + comTotalPrice));

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
        if (isNetworkAvailable(CompareActivity.this)) {
            new JSONAsyncTaskToIncProQty().execute();
            progressDialog(progress, "Loading", "Please wait...");
        } else {
            Toast.makeText(CompareActivity.this, "Check Your Network Connection", Toast.LENGTH_SHORT).show();
        }

        Toast.makeText(CompareActivity.this, "Increased", Toast.LENGTH_SHORT).show();
    }

    public void decreaseItem(View v) {
        cartObj = (Cart) v.getTag();
        productId = cartObj.getProductId();
        strUnit = cartObj.getUnit();
        unitQty = cartObj.getQty() - 1;
        if (isNetworkAvailable(CompareActivity.this)) {
            new JSONAsyncTaskToIncProQty().execute();
            progressDialog(progress, "Loading", "Please wait...");
        } else {
            Toast.makeText(CompareActivity.this, "Check Your Network Connection", Toast.LENGTH_SHORT).show();
        }

        Toast.makeText(CompareActivity.this, "Decreased", Toast.LENGTH_SHORT).show();
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
                appendLog(CompareActivity.this, "2 CompareActivity " + e.toString() + date);
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (progress.isShowing()) {
                progress.dismiss();
            }
            if (status == 200) {
                if (result.equals("success")) {
                    if (isNetworkAvailable(CompareActivity.this)) {
                        new JSONAsyncTaskToGetCartDetails().execute();
                        progressDialog(progress, "Loading", "Please wait...");
                    } else {
                        Toast.makeText(CompareActivity.this, "Check Your Network Connection", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        }
    }

    public class JSONAsyncTaskToSwitchOrder extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                // code to consume wcf service which sends the details to the server
                String url = baseUrl1 + "ChangeMerchantOfProductInCart";
                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost(url);
                String json = "";

                // 3. build jsonObject
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("MerchantId", mid);
                jsonObject.accumulate("CustomerId", custId);
                jsonObject.accumulate("PriceComapareMerchantId", midToComp);

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
                appendLog(CompareActivity.this, "3 CompareActivity " + e.toString() + date);
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (progress.isShowing()) {
                progress.dismiss();
            }
            if (status == 200) {
                if (result.equals("success")) {
                    editor.putInt("mid", midToComp);
                    editor.putString("shopName", shopNameToComp);
                    editor.apply();
                    Intent ProductsIntent = new Intent(CompareActivity.this, ProductListActivity.class);
                    startActivity(ProductsIntent);
                }
            }
        }
    }
}

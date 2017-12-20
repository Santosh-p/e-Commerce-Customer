package com.myapp.groceryshopcustomer.Activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.myapp.groceryshopcustomer.Adapters.ProductUnitsAdapter;
import com.myapp.groceryshopcustomer.App.Config;
import com.myapp.groceryshopcustomer.DBHandler.Customer;
import com.myapp.groceryshopcustomer.DBHandler.DatabaseHandler;
import com.myapp.groceryshopcustomer.DBHandler.ProductUnit;
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

import java.util.ArrayList;

import static com.myapp.groceryshopcustomer.Constant.Constants.baseUrl1;
import static com.myapp.groceryshopcustomer.Constant.Constants.isNetworkAvailable;
import static com.myapp.groceryshopcustomer.Constant.Constants.mid;
import static com.myapp.groceryshopcustomer.Constant.Constants.progressDialog;
import static com.myapp.groceryshopcustomer.Constant.Constants.setBadgeCount;

public class ProductDetailsActivity extends AppCompatActivity {
    ProgressDialog progress;
    ImageView imgProduct;
    ListView listListOfWeight;
    TextView tvProductName, tvProductManufactutrar;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    int catId, productId, status, custId, itemsCount, unitQty;
    ;
    String productName, productmanufacturar, strStatus, strUnit, result, productImageName;
    DatabaseHandler db = new DatabaseHandler(this);
    // temp
    public ProductUnitsAdapter imageItemAdapter;
    ArrayList<ProductUnit> unitsList = new ArrayList<ProductUnit>();
    private LayerDrawable mCartMenuIcon;
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    ProductUnit productUnit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);
        progress = new ProgressDialog(ProductDetailsActivity.this);
        getSupportActionBar().setHomeButtonEnabled(true);
        imgProduct = (ImageView) findViewById(R.id.img_productimage);
        listListOfWeight = (ListView) findViewById(R.id.list_product_weight);
        tvProductName = (TextView) findViewById(R.id.tv_product_name);
        tvProductManufactutrar = (TextView) findViewById(R.id.tv_product_manufacturar);
        pref = getApplicationContext().getSharedPreferences("mypref", MODE_PRIVATE);
        editor = pref.edit();

        //  mid = pref.getInt("mid", 0);
        //catId = pref.getInt("categoryId", 0);
        productId = pref.getInt("productId", 0);
        getSupportActionBar().setTitle(pref.getString("shopName", null));
        DatabaseHandler db = new DatabaseHandler(ProductDetailsActivity.this);
        Customer user1 = db.getCustomerDetails();
        custId = user1.getCust_id();
        if (isNetworkAvailable(ProductDetailsActivity.this)) {
            new JSONAsyncTaskToGetProductDetails().execute();
            progressDialog(progress, "Loading", "Please wait...");
        } else {
            Toast.makeText(ProductDetailsActivity.this, "Check Your Network Connection", Toast.LENGTH_SHORT).show();
        }


        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received
                    String message = intent.getStringExtra("message");
                    Toast.makeText(getApplicationContext(), "Push notification: " + message, Toast.LENGTH_LONG).show();

                    if (message.equals("refresh")) {
                        new JSONAsyncTaskToGetProductDetails().execute();
                    }
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        new JSONAsyncTaskToGetCartCount().execute();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        Intent backInent=new Intent(ProductDetailsActivity.this,ProductListActivity.class);
//        backInent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//        startActivity(backInent);
    }


    public void increaseItem(View v) {
        productUnit = (ProductUnit) v.getTag();

        strUnit = productUnit.getUnit();
        unitQty = productUnit.getQuantity() + 1;

        new JSONAsyncTaskToIncDecProQty().execute();
        //Toast.makeText(ProductDetailsActivity.this, "Increased", Toast.LENGTH_SHORT).show();
    }

    public void decreaseItem(View v) {
        productUnit = (ProductUnit) v.getTag();
        strUnit = productUnit.getUnit();
        unitQty = productUnit.getQuantity() - 1;

        new JSONAsyncTaskToIncDecProQty().execute();
        //   Toast.makeText(ProductDetailsActivity.this, "Decreased", Toast.LENGTH_SHORT).show();
    }

    public void addItemInCart(View v) {
        productUnit = (ProductUnit) v.getTag();
        strUnit = productUnit.getUnit();
        unitQty = 1;

        new JSONAsyncTaskToAddProductInCart().execute();
        //Toast.makeText(ProductDetailsActivity.this, "Added", Toast.LENGTH_SHORT).show();
    }

    public class JSONAsyncTaskToGetProductDetails extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                // code to consume wcf service which sends the details to the server
                String url = baseUrl1 + "getProductDetailOne";
                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost(url);
                String json = "";

                // 3. build jsonObject
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("CustomerId", custId);
                jsonObject.accumulate("MerchantId", mid);
                jsonObject.accumulate("ProductId", productId);

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
                    //[{"Status":"success","data":[{"ProductId":11,"ProductName":"Pressuer cooker","ProductImage":"asddfsd","Manufacturer":"testManufacturer","UnitArr":[]}]}]
                    JSONArray productArr = othposjsonobj.getJSONArray("data");
                    for (int n = 0; n < productArr.length(); n++) {
                        JSONObject object = productArr.getJSONObject(n);
                        productName = object.getString("ProductName");
                        productmanufacturar = object.getString("Manufacturer");
                        productImageName = object.getString("ProductImage");

                        JSONArray unitArray = object.getJSONArray("QtyArr");
                        for (int unit = 0; unit < unitArray.length(); unit++) {
                            JSONObject unitObject = unitArray.getJSONObject(unit);
                            ProductUnit projectUnitObj = new ProductUnit();
                            projectUnitObj.setUnit(unitObject.getString("Unit"));
                            projectUnitObj.setPrice(unitObject.getDouble("Price"));
                            projectUnitObj.setMrp(unitObject.getDouble("MRP"));
                            projectUnitObj.setQuantity(unitObject.getInt("Qty"));

                            double Save = (unitObject.getDouble("MRP")) - (unitObject.getDouble("Price"));
                            projectUnitObj.setSave_rs(Save);
                            unitsList.add(projectUnitObj);
                            projectUnitObj = null;
                        }
                    }
                } else {
                    // result = "Did not work!";
                }
            } catch (Exception e) {
//                DateFormat dff = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
//                String date = dff.format(Calendar.getInstance().getTime());
//                Log.d("InputStream", e.getLocalizedMessage());
//                e.printStackTrace();
//                appendLog(ProductDetailsActivity.this, "1 ProductDetailsActivity " + e.toString() + date);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (progress.isShowing()) {
                progress.dismiss();
            }
            if (status == 200) {
                String url = baseUrl1 + "GetProductImage?filename=" + productImageName;
                Picasso.with(ProductDetailsActivity.this).load(url).fit().into(imgProduct);
                tvProductName.setText(productName);
                tvProductManufactutrar.setText(productmanufacturar);
                imageItemAdapter = new ProductUnitsAdapter(ProductDetailsActivity.this,
                        R.layout.main_view_list_row, unitsList);
                listListOfWeight.setAdapter(imageItemAdapter);
                imageItemAdapter.notifyDataSetChanged();
            }
        }
    }

    public class JSONAsyncTaskToGetProductDetails1 extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                // code to consume wcf service which sends the details to the server
                String url = baseUrl1 + "getProductDetailOne";
                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost(url);
                String json = "";

                // 3. build jsonObject
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("CustomerId", custId);
                jsonObject.accumulate("MerchantId", mid);
                jsonObject.accumulate("ProductId", productId);


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

                    unitsList.clear();
                    JSONArray productArr = othposjsonobj.getJSONArray("data");
                    for (int n = 0; n < productArr.length(); n++) {
                        JSONObject object = productArr.getJSONObject(n);
                        productName = object.getString("ProductName");
                        productmanufacturar = object.getString("Manufacturer");

                        JSONArray unitArray = object.getJSONArray("QtyArr");
                        for (int unit = 0; unit < unitArray.length(); unit++) {
                            JSONObject unitObject = unitArray.getJSONObject(unit);
                            ProductUnit projectUnitObj = new ProductUnit();
                            projectUnitObj.setUnit(unitObject.getString("Unit"));
                            projectUnitObj.setPrice(unitObject.getDouble("Price"));
                            projectUnitObj.setMrp(unitObject.getDouble("MRP"));
                            projectUnitObj.setQuantity(unitObject.getInt("Qty"));

                            double Save = (unitObject.getDouble("MRP")) - (unitObject.getDouble("Price"));
                            projectUnitObj.setSave_rs(Save);

                            unitsList.add(projectUnitObj);
                            projectUnitObj = null;
                        }
                    }
                } else {
                    // result = "Did not work!";
                }
            } catch (Exception e) {
//                DateFormat dff = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
//                String date = dff.format(Calendar.getInstance().getTime());
//                Log.d("InputStream", e.getLocalizedMessage());
//                e.printStackTrace();
//                appendLog(ProductDetailsActivity.this, "2 ProductDetailsActivity " + e.toString() + date);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            if (status == 200) {

                new JSONAsyncTaskToGetCartCount().execute();
                imageItemAdapter = new ProductUnitsAdapter(ProductDetailsActivity.this,
                        R.layout.main_view_list_row, unitsList);
                listListOfWeight.setAdapter(imageItemAdapter);
                imageItemAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cart_items, menu);
        mCartMenuIcon = (LayerDrawable) menu.findItem(R.id.action_cart).getIcon();
        // setBadgeCount(this, mCartMenuIcon, String.valueOf(2));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        int id = item.getItemId();
        if (id == R.id.action_cart) {
            if (itemsCount > 0) {
                Intent cartIntent = new Intent(ProductDetailsActivity.this, CartActivity.class);
                startActivity(cartIntent);
            } else {
                Toast.makeText(ProductDetailsActivity.this, "No Items", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public class JSONAsyncTaskToGetCartCount extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                Customer user1 = db.getCustomerDetails();
                int id = user1.getCust_id();
                // code to consume wcf service which sends the details to the server
                String url = baseUrl1 + "GetCountOfCartProduct";
                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost(url);
                String json = "";

                // 3. build jsonObject
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("MerchantId", mid);
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
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);
                    JSONArray mainObject = new JSONArray(data);
                    JSONObject othposjsonobj = mainObject.getJSONObject(0);
                    //"Status":"success","Count":6

                    strStatus = othposjsonobj.getString("Status");
                    if (strStatus.equals("success")) {
                        itemsCount = othposjsonobj.getInt("Count");
                        setBadgeCount(ProductDetailsActivity.this, mCartMenuIcon, String.valueOf(itemsCount));
                    }
                } else {
                    // result = "Did not work!";
                }


            } catch (Exception e) {
//                DateFormat dff = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
//                String date = dff.format(Calendar.getInstance().getTime());
//                Log.d("InputStream", e.getLocalizedMessage());
//                e.printStackTrace();
//                appendLog(ProductDetailsActivity.this, "3 ProductDetailsActivity " + e.toString() + date);

            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (progress.isShowing()) {
                progress.dismiss();
            }

            if (status == 200) {

                if (strStatus.equals("success")) {
                    setBadgeCount(ProductDetailsActivity.this, mCartMenuIcon, String.valueOf(itemsCount));
                }
            }
        }
    }


    public class JSONAsyncTaskToIncDecProQty extends AsyncTask<String, Void, String> {
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
//                DateFormat dff = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
//                String date = dff.format(Calendar.getInstance().getTime());
//                Log.d("InputStream", e.getLocalizedMessage());
//                e.printStackTrace();
//                appendLog(ProductDetailsActivity.this, "4 ProductDetailsActivity " + e.toString() + date);
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (status == 200) {
                if (result.equals("success")) {
                    new JSONAsyncTaskToGetProductDetails1().execute();
                }
            }
        }
    }

    public class JSONAsyncTaskToAddProductInCart extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                // code to consume wcf service which sends the details to the server
                String url = baseUrl1 + "AddProductToCart";
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
//                DateFormat dff = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
//                String date = dff.format(Calendar.getInstance().getTime());
//                Log.d("InputStream", e.getLocalizedMessage());
//                e.printStackTrace();
//                appendLog(ProductDetailsActivity.this, "5 ProductDetailsActivity " + e.toString() + date);
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (status == 200) {
                if (result.equals("success")) {
                    new JSONAsyncTaskToGetProductDetails1().execute();
                    new JSONAsyncTaskToGetCartCount().execute();
                }
            }
        }
    }
}
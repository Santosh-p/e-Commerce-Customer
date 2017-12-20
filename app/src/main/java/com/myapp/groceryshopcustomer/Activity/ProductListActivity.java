package com.myapp.groceryshopcustomer.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.myapp.groceryshopcustomer.Adapters.ProductDetailsListAdapter;
import com.myapp.groceryshopcustomer.DBHandler.Customer;
import com.myapp.groceryshopcustomer.DBHandler.DatabaseHandler;
import com.myapp.groceryshopcustomer.DBHandler.Product;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.myapp.groceryshopcustomer.Constant.Constants.appendLog;
import static com.myapp.groceryshopcustomer.Constant.Constants.baseUrl1;
import static com.myapp.groceryshopcustomer.Constant.Constants.isNetworkAvailable;
import static com.myapp.groceryshopcustomer.Constant.Constants.mid;
import static com.myapp.groceryshopcustomer.Constant.Constants.progressDialog;
import static com.myapp.groceryshopcustomer.Constant.Constants.setBadgeCount;

public class ProductListActivity extends AppCompatActivity {
    ProgressDialog progress;
    ListView productlist;
    int catId, status, custId, itemsCount;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    ArrayList<Product> productList = new ArrayList<Product>();
    DatabaseHandler db = new DatabaseHandler(this);
    public ProductDetailsListAdapter imageItemAdapter;
    private LayerDrawable mCartMenuIcon;
    String catName, strStatus;
    boolean doubleBackToExitPressedOnce = true;
    SearchView searchView;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        progress = new ProgressDialog(ProductListActivity.this);

      //  getSupportActionBar().setHomeButtonEnabled(true);
        productlist = (ListView) findViewById(R.id.list_product);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                productList.clear();
                swipeRefreshLayout.setRefreshing(true);
                searchView.onActionViewCollapsed();
                searchView.setQuery("", false);
                searchView.clearFocus();

                if (isNetworkAvailable(ProductListActivity.this)) {
                    new JSONAsyncTaskToGetProductsList().execute();
                    progressDialog(progress, "Loading", "Please wait...");
                } else {
                    Toast.makeText(ProductListActivity.this, "Check Your Network Connection", Toast.LENGTH_SHORT).show();
                }

            }
        });
        pref = getApplicationContext().getSharedPreferences("mypref", MODE_PRIVATE);
        editor = pref.edit();
    //    mid = pref.getInt("mid", 0);
        //Intent i = getIntent();
        catId = pref.getInt("categoryId", 0);//i.getIntExtra("categoryId",0);
        DatabaseHandler db = new DatabaseHandler(ProductListActivity.this);
        Customer user1 = db.getCustomerDetails();
        custId = user1.getCust_id();
        getSupportActionBar().setTitle(pref.getString("shopName", null));

        productlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Product selectedFromList = (Product) productlist.getItemAtPosition(position);
                editor.putInt("productId", selectedFromList.getProductId());
                editor.apply();
                Intent pdetailsIntent = new Intent(ProductListActivity.this, ProductDetailsActivity.class);
                startActivity(pdetailsIntent);
                // String selectedFromList =(String) (productlist.getItemAtPosition(position));
                Toast.makeText(getApplicationContext(), " selected", Toast.LENGTH_LONG).show();
            }
        });

        if (isNetworkAvailable(ProductListActivity.this)) {
            new JSONAsyncTaskToGetProductsList().execute();
            progressDialog(progress, "Loading", "Please wait...");
        } else {
            Toast.makeText(ProductListActivity.this, "Check Your Network Connection", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        new JSONAsyncTaskToGetCartCount().execute();
    }

    public class JSONAsyncTaskToGetProductsList extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                Customer user1 = db.getCustomerDetails();
                int id = user1.getCust_id();
                // code to consume wcf service which sends the details to the server
                String url = baseUrl1 + "getAllProductsOfMerchant";
                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost(url);
                String json = "";

                // 3. build jsonObject
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("MerchantId", mid);
                // jsonObject.accumulate("CategoryId", catId);

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
                    productList.clear();
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);
                    JSONArray mainObject = new JSONArray(data);
                    JSONObject othposjsonobj = mainObject.getJSONObject(0);

                    JSONArray productArray = othposjsonobj.getJSONArray("data");

                    for (int n = 0; n < productArray.length(); n++) {
                        JSONObject object = productArray.getJSONObject(n);
                        //{"ProductId":15,"ProductName":"Bratiannia Good Day Cashew Cookies","ProductImage":"sdfghn","Manufacturer":"Bratiannia","CategoryName":"Packaged Food","UnitArr":[{"Unit":"200gm","Price":30},{"Unit":"100gm","Price":17},{"Unit":"600gm","Price":85},{"Unit":"1Kg","Price":11}]}
                        Product proObj = new Product();
                        proObj.setProductId(object.getInt("ProductId"));
                        proObj.setProductName(object.getString("ProductName"));
                        proObj.setProductManufacturar(object.getString("Manufacturer"));
                        proObj.setProductImage(object.getString("ProductImage"));
                        proObj.setProductCategory(object.getString("CategoryName"));

                        JSONArray productUnitArr = object.getJSONArray("UnitArr");
                        //[{"Unit":"200gm","Price":30},{"Unit":"100gm","Price":17},{"Unit":"600gm","Price":85},{"Unit":"1Kg","Price":11}]
                        List<ProductUnit> unitsLst = new ArrayList<ProductUnit>();
                        for (int u = 0; u < productUnitArr.length(); u++) {
                            JSONObject uObject = productUnitArr.getJSONObject(u);
                            ProductUnit proProductUnit = new ProductUnit();
                            proProductUnit.setUnit(uObject.getString("Unit"));
                            proProductUnit.setPrice(uObject.getDouble("Price"));
                            proProductUnit.setMrp(uObject.getDouble("MRP"));

                            double Save = (uObject.getDouble("MRP")) - (uObject.getDouble("Price"));
                            proProductUnit.setSave_rs(Save);
                            unitsLst.add(proProductUnit);

                            unitsLst.add(proProductUnit);

                            proProductUnit = null;
                        }

                        proObj.setProductUnitArr(unitsLst);

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
                appendLog(ProductListActivity.this, "1 ProductListActivity " + e.toString() + date);

            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (progress.isShowing()) {
                progress.dismiss();
            }
            if (status == 200) {

                swipeRefreshLayout.setRefreshing(false);
                //Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_LONG).show();
                imageItemAdapter = new ProductDetailsListAdapter(ProductListActivity.this,
                        R.layout.product_details_list, productList);
                productlist.setAdapter(imageItemAdapter);
                imageItemAdapter.notifyDataSetChanged();

            }
        }
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
                        setBadgeCount(ProductListActivity.this, mCartMenuIcon, String.valueOf(itemsCount));
                    }
                } else {
                    // result = "Did not work!";
                }

            } catch (Exception e) {
                DateFormat dff = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                String date = dff.format(Calendar.getInstance().getTime());
                Log.d("InputStream", e.getLocalizedMessage());
                e.printStackTrace();
                appendLog(ProductListActivity.this, "2 ProductListActivity " + e.toString() + date);

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
                    // itemsCount = othposjsonobj.getInt("Count");
                    setBadgeCount(ProductListActivity.this, mCartMenuIcon, String.valueOf(itemsCount));
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_serch_cart_more_items, menu);
        mCartMenuIcon = (LayerDrawable) menu.findItem(R.id.action_cart).getIcon();
        // setBadgeCount(this, mCartMenuIcon, String.valueOf(2));

        MenuItem searchViewItem = menu.findItem(R.id.action_serch);
        searchView = (SearchView) MenuItemCompat.getActionView(searchViewItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (!newText.equals("")) {
                    doubleBackToExitPressedOnce = false;
                    imageItemAdapter.filter(newText);
                }
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        int id = item.getItemId();
        if (id == R.id.action_cart) {
            if (itemsCount > 0) {
                Intent cartIntent = new Intent(ProductListActivity.this, CartActivity.class);
                startActivity(cartIntent);
            } else {
                Toast.makeText(ProductListActivity.this, "No Items", Toast.LENGTH_SHORT).show();
            }
        }
        if (isNetworkAvailable(ProductListActivity.this)) {
            progressDialog(progress, "Loading", "Please wait...");
            if (id == R.id.action_vegitables) {
                catId = 1;
                catName = "Vegitables";
                new JSONAsyncTaskToGetProductsListByCategory().execute();
            } else if (id == R.id.action_fruits) {
                catId = 2;
                catName = "Fruits";
                new JSONAsyncTaskToGetProductsListByCategory().execute();
            } else if (id == R.id.action_cloths) {
                catId = 3;
                catName = "Cloths";
                new JSONAsyncTaskToGetProductsListByCategory().execute();
            } else if (id == R.id.action_grocery) {
                catId = 4;
                catName = "Grocery";
                new JSONAsyncTaskToGetProductsListByCategory().execute();
            } else if (id == R.id.action_personal_care) {
                catId = 5;
                catName = "Personal Care";
                new JSONAsyncTaskToGetProductsListByCategory().execute();
            } else if (id == R.id.action_dairy_beverages) {
                catId = 6;
                catName = "Dairy & Beverages";
                new JSONAsyncTaskToGetProductsListByCategory().execute();
            } else if (id == R.id.action_household_needs) {
                catId = 7;
                catName = "Household Needs";
                new JSONAsyncTaskToGetProductsListByCategory().execute();
            } else if (id == R.id.action_baby_kids) {
                catId = 8;
                catName = "Baby & Kids";
                new JSONAsyncTaskToGetProductsListByCategory().execute();
            } else if (id == R.id.action_packaged_food) {
                catId = 10;
                catName = "Packaged Food";
                new JSONAsyncTaskToGetProductsListByCategory().execute();
            } else if (id == R.id.action_others) {
                catId = 11;
                catName = "Others";
                new JSONAsyncTaskToGetProductsListByCategory().execute();
            }
        } else {
            Toast.makeText(ProductListActivity.this, "Check Your Network Connection", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    public class JSONAsyncTaskToGetProductsListByCategory extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                Customer user1 = db.getCustomerDetails();
                int id = user1.getCust_id();
                // code to consume wcf service which sends the details to the server
                String url = baseUrl1 + "getProducts";
                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost(url);
                String json = "";

                // 3. build jsonObject
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("MerchantId", mid);
                jsonObject.accumulate("CategoryId", catId);

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
                    productList.clear();
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);
                    JSONArray mainObject = new JSONArray(data);
                    JSONObject othposjsonobj = mainObject.getJSONObject(0);

                    JSONArray productArray = othposjsonobj.getJSONArray("data");

                    for (int n = 0; n < productArray.length(); n++) {
                        JSONObject object = productArray.getJSONObject(n);
                        //{"ProductId":15,"ProductName":"Bratiannia Good Day Cashew Cookies","ProductImage":"sdfghn","Manufacturer":"Bratiannia","CategoryName":"Packaged Food","UnitArr":[{"Unit":"200gm","Price":30},{"Unit":"100gm","Price":17},{"Unit":"600gm","Price":85},{"Unit":"1Kg","Price":11}]}
                        Product proObj = new Product();
                        proObj.setProductId(object.getInt("ProductId"));
                        proObj.setProductName(object.getString("ProductName"));
                        proObj.setProductManufacturar(object.getString("Manufacturer"));
                        proObj.setProductImage(object.getString("ProductImage"));
                        proObj.setProductCategory(catName);

                        JSONArray productUnitArr = object.getJSONArray("UnitArr");
                        //[{"Unit":"200gm","Price":30},{"Unit":"100gm","Price":17},{"Unit":"600gm","Price":85},{"Unit":"1Kg","Price":11}]
                        List<ProductUnit> unitsLst = new ArrayList<ProductUnit>();
                        for (int u = 0; u < productUnitArr.length(); u++) {
                            JSONObject uObject = productUnitArr.getJSONObject(u);
                            ProductUnit proProductUnit = new ProductUnit();
                            proProductUnit.setUnit(uObject.getString("Unit"));
                            proProductUnit.setPrice(uObject.getDouble("Price"));
                            proProductUnit.setMrp(uObject.getDouble("MRP"));

                            double Save = (uObject.getDouble("MRP")) - (uObject.getDouble("Price"));
                            proProductUnit.setSave_rs(Save);
                            unitsLst.add(proProductUnit);

                            proProductUnit = null;
                        }

                        proObj.setProductUnitArr(unitsLst);

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
                appendLog(ProductListActivity.this, "3 ProductListActivity " + e.toString() + date);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (status == 200) {

                //Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_LONG).show();
                imageItemAdapter = new ProductDetailsListAdapter(ProductListActivity.this,
                        R.layout.product_details_list, productList);
                productlist.setAdapter(imageItemAdapter);
                imageItemAdapter.notifyDataSetChanged();
                // Double back press exit
                doubleBackToExitPressedOnce = false;
            }
            if (progress.isShowing()) {
                progress.dismiss();
            }
        }
    }


    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            Intent backIntent = new Intent(ProductListActivity.this, MainActivity.class);
            startActivity(backIntent);
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        searchView.setIconified(true);

        searchView.onActionViewCollapsed();
        searchView.setQuery("", false);
        searchView.clearFocus();
        //imageItemAdapter.filter("");
        // Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();
        new JSONAsyncTaskToGetProductsList().execute();

    }
}

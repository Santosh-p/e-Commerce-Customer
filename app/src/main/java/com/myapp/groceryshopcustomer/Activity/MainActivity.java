package com.myapp.groceryshopcustomer.Activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.myapp.groceryshopcustomer.Adapters.ProductDetailsListAdapter;
import com.myapp.groceryshopcustomer.Adapters.ProductUnitsAdapter;
import com.myapp.groceryshopcustomer.DBHandler.Customer;
import com.myapp.groceryshopcustomer.DBHandler.DatabaseHandler;
import com.myapp.groceryshopcustomer.DBHandler.Merchant;
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

import java.io.InputStream;
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

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    String stat;
    TextView tvCity, tvArea, tvSearch, tvFilter;
    List<Customer> custcount;
    int count, CustomerId;
    DatabaseHandler db = new DatabaseHandler(this);
    ArrayList<Merchant> merchantList = new ArrayList<Merchant>();
    ArrayList<Product> productList = new ArrayList<Product>();
    // ArrayList<Merchant> searchMerchantList = new ArrayList<Merchant>();
    //  boolean productAdded = false;
    public ProductDetailsListAdapter productlistItemAdapter;
    ListView listShops;
    SharedPreferences pref;
    TextView tv;
    SharedPreferences.Editor editor;
    SearchView searchView;
    private SwipeRefreshLayout swipeRefreshLayout;
    ProgressDialog progress;
    ProductUnit productUnit;
    Product product;
    String productName, productmanufacturar, strUnit, result, productImageName;
    int catId, productId, status, custId, itemsCount, unitQty;
    ArrayList<ProductUnit> unitsList = new ArrayList<ProductUnit>();
    int position = 0;
    public ProductUnitsAdapter imageItemAdapter;
    ListView listListOfWeight;
    private LayerDrawable mCartMenuIcon;
    String catName, strStatus, custName, custmailid, lastname, mobileNumber;
    String filter = "no";
    int tempstatus;
    InputStream inputStream = null;
    NavigationView navigationView;
    String MerchantShopName, MerchantArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        Typeface fontAwesomeFont = Typeface.createFromAsset(getAssets(), "fontawesome-webfont.ttf");
        progress = new ProgressDialog(MainActivity.this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        tvCity = (TextView) toolbar.findViewById(R.id.tvCity);
        tvArea = (TextView) toolbar.findViewById(R.id.tvArea);
        // tvEdit = (TextView) toolbar.findViewById(R.id.tvEdit);
        //   tvEdit.setTypeface(fontAwesomeFont);

        custcount = db.getAllCustomers();
        count = 0;
        for (Customer cn : custcount) {
            count++;
        }
        if (count > 0) {
            //  new updateLocationDetails().execute();
            DatabaseHandler db = new DatabaseHandler(MainActivity.this);
            Customer user1 = db.getCustomerDetails();
            custId = user1.getCust_id();
        }
        // custName = user1.getCust_name();
        //  custmailid = user1.getEmaiid();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        listShops = (ListView) findViewById(R.id.lst_shop_list);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                merchantList.clear();
                swipeRefreshLayout.setRefreshing(true);
                searchView.onActionViewCollapsed();
                searchView.setQuery("", false);
                searchView.clearFocus();

                if (isNetworkAvailable(MainActivity.this)) {
                    new JSONAsyncTaskToGetProductsList().execute();
                    progressDialog(progress, "Loading", "Please wait...");
                } else {
                    Toast.makeText(MainActivity.this, "Check Your Network Connection", Toast.LENGTH_SHORT).show();
                }
            }
        });
        pref = getApplicationContext().getSharedPreferences("mypref", MODE_PRIVATE);
        editor = pref.edit();
        Intent i = getIntent();

        String city = pref.getString("city", null);
        String area = pref.getString("area", null);
        toolbar.setTitle("");


        setSupportActionBar(toolbar);

        if (isNetworkAvailable(MainActivity.this)) {
            new GetMerchantDetails().execute();
            new GetCustomerDetails().execute();
            new JSONAsyncTaskToGetProductsList().execute();
            progressDialog(progress, "Loading", "Please wait...");
        } else {
            Toast.makeText(MainActivity.this, "Check Your Network Connection", Toast.LENGTH_SHORT).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        listShops.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Product selectedFromList = (Product) listShops.getItemAtPosition(position);
                editor.putInt("productId", selectedFromList.getProductId());
                editor.apply();
                Intent pdetailsIntent = new Intent(MainActivity.this, ProductDetailsActivity.class);
                startActivity(pdetailsIntent);
                // String selectedFromList =(String) (productlist.getItemAtPosition(position));
                Toast.makeText(getApplicationContext(), " selected", Toast.LENGTH_LONG).show();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        new JSONAsyncTaskToGetProductsList().execute();
        new JSONAsyncTaskToGetCartCount().execute();

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_my_orders) {
            Intent selectCategoryIntent = new Intent(MainActivity.this, MyOrdersActivity.class);
            startActivity(selectCategoryIntent);
        }
        if (id == R.id.nav_my_profile) {
            Intent selectCategoryIntent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(selectCategoryIntent);
        }
        if (id == R.id.nav_my_logout) {
            String LoginStatus = "fail";
            db.updateLoginStatus(new Customer(custId, LoginStatus));
            Intent selectCategoryIntent = new Intent(MainActivity.this, GetLocationActivity.class);
            startActivity(selectCategoryIntent);
        }
//        else if (id == R.id.nav_gallery) {
//            Intent selectCategoryIntent = new Intent(MainActivity.this, CompareActivity.class);
//            startActivity(selectCategoryIntent);
//        }
// else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        //getMenuInflater().inflate(R.menu.menu_cart_items, menu);
        mCartMenuIcon = (LayerDrawable) menu.findItem(R.id.action_cart).getIcon();
        // setBadgeCount(this, mCartMenuIcon, String.valueOf(2));
        //return super.onCreateOptionsMenu(menu);


        MenuItem searchViewItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchViewItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (!newText.equals("")) {
                    productlistItemAdapter.filter(newText);
                }
                return false;
            }

        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_cart) {

            if (itemsCount > 0) {
                Intent cartIntent = new Intent(MainActivity.this, CartActivity.class);
                startActivity(cartIntent);
            } else {
                Toast.makeText(MainActivity.this, "No items in cart", Toast.LENGTH_SHORT).show();

            }
        }
        if (isNetworkAvailable(MainActivity.this)) {

            if (id == R.id.action_vegitables) {
                catId = 1;
                catName = "Vegetables";
                new JSONAsyncTaskToGetProductsListByCategory().execute();
                filter = "yes";
            } else if (id == R.id.action_fruits) {
                catId = 2;
                catName = "Fruits";
                filter = "yes";
                new JSONAsyncTaskToGetProductsListByCategory().execute();
            } else if (id == R.id.action_cloths) {
                catId = 3;
                catName = "Cloths";
                filter = "yes";
                new JSONAsyncTaskToGetProductsListByCategory().execute();
            } else if (id == R.id.action_grocery) {
                catId = 4;
                catName = "Grocery";
                filter = "yes";
                new JSONAsyncTaskToGetProductsListByCategory().execute();
            } else if (id == R.id.action_personal_care) {
                catId = 5;
                catName = "Personal Care";
                filter = "yes";
                new JSONAsyncTaskToGetProductsListByCategory().execute();
            } else if (id == R.id.action_dairy_beverages) {
                catId = 6;
                catName = "Dairy & Beverages";
                filter = "yes";
                new JSONAsyncTaskToGetProductsListByCategory().execute();
            } else if (id == R.id.action_household_needs) {
                catId = 7;
                catName = "Household Needs";
                filter = "yes";
                new JSONAsyncTaskToGetProductsListByCategory().execute();
            } else if (id == R.id.action_baby_kids) {
                catId = 8;
                catName = "Baby & Kids";
                filter = "yes";
                new JSONAsyncTaskToGetProductsListByCategory().execute();
            } else if (id == R.id.action_packaged_food) {
                catId = 10;
                catName = "Packaged Food";
                filter = "yes";
                new JSONAsyncTaskToGetProductsListByCategory().execute();
            } else if (id == R.id.action_others) {
                catId = 11;
                catName = "Others";
                filter = "yes";
                new JSONAsyncTaskToGetProductsListByCategory().execute();
            }
        } else {
            Toast.makeText(MainActivity.this, "Check Your Network Connection", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    // Double back press exit
    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (doubleBackToExitPressedOnce) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();
        searchView.setIconified(true);

        searchView.onActionViewCollapsed();
        searchView.setQuery("", false);
        searchView.clearFocus();
        if (!productlistItemAdapter.equals(null))
            productlistItemAdapter.filter("");

        if (filter.equals("yes")) {
            if (isNetworkAvailable(MainActivity.this)) {
                new JSONAsyncTaskToGetProductsList().execute();
                progressDialog(progress, "Loading", "Please wait...");
                filter = "no";
            } else {
                Toast.makeText(MainActivity.this, "Check Your Network Connection", Toast.LENGTH_SHORT).show();
            }
        }

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    public void AddSingleProduct(View v) {
        // listShops.smoothScrollToPosition(7);


        product = (Product) v.getTag();
        productId = product.getProductId();
        position = product.getPosition();
        List<ProductUnit> unitsList = new ArrayList<ProductUnit>();
        unitsList = product.getProductUnitArr();

        productUnit = unitsList.get(0);
        strUnit = productUnit.getUnit();

        unitQty = 1;

        if (isNetworkAvailable(MainActivity.this)) {
            new JSONAsyncTaskToAddSingleProductInCart().execute();
            progressDialog(progress, "Adding product in cart", "Please wait...");
        } else {
            Toast.makeText(MainActivity.this, "Check Your Network Connection", Toast.LENGTH_SHORT).show();
        }


    }

    public void increaseSingleItem(View v) {
        product = (Product) v.getTag();
        productId = product.getProductId();

        List<ProductUnit> unitsList = new ArrayList<ProductUnit>();
        unitsList = product.getProductUnitArr();

        productUnit = unitsList.get(0);
        strUnit = productUnit.getUnit();
        unitQty = productUnit.getCart_qty() + 1;

        if (isNetworkAvailable(MainActivity.this)) {
            new JSONAsyncTaskToIncDecSingleProQty().execute();
            progressDialog(progress, "Loading", "Please wait...");
        } else {
            Toast.makeText(MainActivity.this, "Check Your Network Connection", Toast.LENGTH_SHORT).show();
        }
    }

    public void decreaseSingleItem(View v) {
        product = (Product) v.getTag();
        productId = product.getProductId();


        List<ProductUnit> unitsList = new ArrayList<ProductUnit>();
        unitsList = product.getProductUnitArr();

        productUnit = unitsList.get(0);
        strUnit = productUnit.getUnit();
        unitQty = productUnit.getCart_qty() - 1;

        if (isNetworkAvailable(MainActivity.this)) {
            new JSONAsyncTaskToIncDecSingleProQty().execute();
            progressDialog(progress, "Loading", "Please wait...");
        } else {
            Toast.makeText(MainActivity.this, "Check Your Network Connection", Toast.LENGTH_SHORT).show();
        }
    }

    public void AddMultipleProduct(View v) {
        product = (Product) v.getTag();
        List<ProductUnit> unitlist = new ArrayList<ProductUnit>();
        unitlist = product.getProductUnitArr();
        productId = product.getProductId();
        pref = getApplicationContext().getSharedPreferences("mypref", MODE_PRIVATE);
        editor = pref.edit();
        editor.putInt("productId", productId);
        editor.commit();
        if (unitlist.size() > 1) {
            progressDialog(progress, "Loading", "Please wait...");
            new JSONAsyncTaskToGetProductDetails().execute();
            final Context context = this;
            LayoutInflater li = LayoutInflater.from(context);
            View promptsView = li.inflate(R.layout.popup_addmultiple_product, null);

            listListOfWeight = (ListView) promptsView.findViewById(R.id.list_product_weight);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    context);

            // set prompts.xml to alertdialog builder
            alertDialogBuilder.setView(promptsView);
            alertDialogBuilder.setCancelable(true);

//        alertDialogBuilder
//                .setCancelable(false)
//                .setPositiveButton("OK",
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//
//
//                            }
//                        })
//                .setNegativeButton("Cancel",
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                dialog.cancel();
//                            }
//                        });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();


        } else {
            Toast.makeText(MainActivity.this, "Single Item", Toast.LENGTH_SHORT).show();

        }
    }

    public void increaseItem(View v) {

        //  productId = 2;
        productUnit = (ProductUnit) v.getTag();

        strUnit = productUnit.getUnit();
        unitQty = productUnit.getQuantity() + 1;
        progressDialog(progress, "Loading", "Please wait...");
        new JSONAsyncTaskToIncDecProQty().execute();
    }

    public void decreaseItem(View v) {
        productUnit = (ProductUnit) v.getTag();
        strUnit = productUnit.getUnit();
        unitQty = productUnit.getQuantity() - 1;
        progressDialog(progress, "Loading", "Please wait...");
        new JSONAsyncTaskToIncDecProQty().execute();
    }

    public void addItemInCart(View v) {

        productUnit = (ProductUnit) v.getTag();
        strUnit = productUnit.getUnit();
        unitQty = 1;
        progressDialog(progress, "Loading", "Please wait...");
        new JSONAsyncTaskToAddProductInCart().execute();
        // Toast.makeText(MainActivity.this, "Added", Toast.LENGTH_SHORT).show();
    }

    public class JSONAsyncTaskToGetProductsList extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {

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
                            proProductUnit.setCart_availability(uObject.getString("CartAvailability"));
                            proProductUnit.setCart_qty(uObject.getInt("Qty"));
                            proProductUnit.setUnit(uObject.getString("Unit"));
                            proProductUnit.setPrice(uObject.getDouble("Price"));
                            proProductUnit.setMrp(uObject.getDouble("MRP"));

                            double Save = (uObject.getDouble("MRP")) - (uObject.getDouble("Price"));
                            proProductUnit.setSave_rs(Save);
                            unitsLst.add(proProductUnit);

                            //   unitsLst.add(proProductUnit);

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
//                DateFormat dff = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
//                String date = dff.format(Calendar.getInstance().getTime());
//                Log.d("InputStream", e.getLocalizedMessage());
//                e.printStackTrace();
//                appendLog(MainActivity.this, "1 MainActivity " + e.toString() + date);

            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (progress.isShowing()) {
                progress.dismiss();
            }
            if (status == 200) {
                new JSONAsyncTaskToGetCartCount().execute();
                swipeRefreshLayout.setRefreshing(false);
                productlistItemAdapter = new ProductDetailsListAdapter(MainActivity.this,
                        R.layout.product_details_list, productList);
                listShops.setAdapter(productlistItemAdapter);
                productlistItemAdapter.notifyDataSetChanged();
                listShops.setSelection(position);

            } else {
                Toast.makeText(getApplicationContext(), "No products found", Toast.LENGTH_LONG).show();

            }
        }
    }

    public class JSONAsyncTaskToAddSingleProductInCart extends AsyncTask<String, Void, String> {
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
//                appendLog(MainActivity.this, "2 MainActivity " + e.toString() + date);
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (status == 200) {
                if (result.equals("success")) {

                    new JSONAsyncTaskToGetProductsList().execute();
                    new JSONAsyncTaskToGetCartCount().execute();

                }
            }
        }
    }

    public class JSONAsyncTaskToIncDecSingleProQty extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {

                if (unitQty < 0) {
                    unitQty = 0;
                }


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
//                appendLog(MainActivity.this, "3 MainActivity " + e.toString() + date);
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (status == 200) {
                if (result.equals("success")) {
                    new JSONAsyncTaskToGetSingleProductDetails1().execute();
                    new JSONAsyncTaskToGetProductsList().execute();

                }
            }
        }
    }

    public class JSONAsyncTaskToIncDecProQty extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {

                if (unitQty < 0) {
                    unitQty = 0;
                }


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
//                appendLog(MainActivity.this, "4 MainActivity " + e.toString() + date);
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (status == 200) {
                if (result.equals("success")) {
                    new JSONAsyncTaskToGetProductDetails1().execute();

                    new JSONAsyncTaskToGetProductsList().execute();
                    new JSONAsyncTaskToGetCartCount().execute();
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
//                appendLog(MainActivity.this, "5 MainActivity " + e.toString() + date);
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (status == 200) {
                if (result.equals("success")) {

                    new JSONAsyncTaskToGetProductDetails1().execute();

                    new JSONAsyncTaskToGetProductsList().execute();
                    new JSONAsyncTaskToGetCartCount().execute();
                }
            }
        }
    }

    public class JSONAsyncTaskToGetSingleProductDetails1 extends AsyncTask<String, Void, String> {
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
                            //   projectUnitObj.setCatr_availability(unitObject.getString("CartAvailability"));
                            //   projectUnitObj.setCart_qty(unitObject.getInt("Qty"));
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
//                appendLog(MainActivity.this, "6 MainActivity " + e.toString() + date);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (status == 200) {
                if (progress.isShowing()) {
                    progress.dismiss();
                }
                productlistItemAdapter = new ProductDetailsListAdapter(MainActivity.this,
                        R.layout.product_details_list, productList);
                listShops.setAdapter(productlistItemAdapter);
                productlistItemAdapter.notifyDataSetChanged();


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
                            //   projectUnitObj.setCatr_availability(unitObject.getString("CartAvailability"));
                            //   projectUnitObj.setCart_qty(unitObject.getInt("Qty"));
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
//                appendLog(MainActivity.this, "7 MainActivity " + e.toString() + date);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (progress.isShowing()) {
                progress.dismiss();
            }
            if (status == 200) {

                imageItemAdapter = new ProductUnitsAdapter(MainActivity.this,
                        R.layout.main_view_list_row, unitsList);
                listListOfWeight.setAdapter(imageItemAdapter);
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
                        setBadgeCount(MainActivity.this, mCartMenuIcon, String.valueOf(itemsCount));
                    }
                } else {
                    // result = "Did not work!";
                }
            } catch (Exception e) {
//                DateFormat dff = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
//                String date = dff.format(Calendar.getInstance().getTime());
//                Log.d("InputStream", e.getLocalizedMessage());
//                e.printStackTrace();
//                appendLog(MainActivity.this, "8 MainActivity " + e.toString() + date);

            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (status == 200) {
                if (progress.isShowing()) {
                    progress.dismiss();
                }
                if (strStatus.equals("success")) {
                    setBadgeCount(MainActivity.this, mCartMenuIcon, String.valueOf(itemsCount));
                }
            }
        }
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
                    unitsList.clear();
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
//                appendLog(MainActivity.this, "9 MainActivity " + e.toString() + date);
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

                imageItemAdapter = new ProductUnitsAdapter(MainActivity.this,
                        R.layout.main_view_list_row, unitsList);
                listListOfWeight.setAdapter(imageItemAdapter);
                imageItemAdapter.notifyDataSetChanged();

//                productlistItemAdapter = new ProductDetailsListAdapter(MainActivity.this,
//                        R.layout.product_details_list, productList);
//                listShops.setAdapter(productlistItemAdapter);
//                productlistItemAdapter.notifyDataSetChanged();
            }
        }
    }

    public class JSONAsyncTaskToGetProductsListByCategory extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            progressDialog(progress, "Loading", "Please wait...");
            super.onPreExecute();
        }

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
                    productList.clear();
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);
                    JSONArray mainObject = new JSONArray(data);
                    JSONObject othposjsonobj = mainObject.getJSONObject(0);
                    stat = othposjsonobj.getString("Status");
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
                            proProductUnit.setCart_availability(uObject.getString("CartAvailability"));
                            proProductUnit.setCart_qty(uObject.getInt("Qty"));
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
//                DateFormat dff = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
//                String date = dff.format(Calendar.getInstance().getTime());
//                Log.d("InputStream", e.getLocalizedMessage());
//                e.printStackTrace();
//                appendLog(MainActivity.this, "10 MainActivity " + e.toString() + date);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {


            if (status == 200) {

                if (stat.equals("fail")) {
                    Toast.makeText(getApplicationContext(), "No products found", Toast.LENGTH_LONG).show();

                } else {

                    productlistItemAdapter = new ProductDetailsListAdapter(MainActivity.this,
                            R.layout.product_details_list, productList);
                    listShops.setAdapter(productlistItemAdapter);
                    productlistItemAdapter.notifyDataSetChanged();
                    // Double back press exit
                    doubleBackToExitPressedOnce = false;
                }
            }
            if (progress.isShowing()) {
                progress.dismiss();
            }
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
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
                        custName = object.getString("FirstName");
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
                appendLog(MainActivity.this, "11 MainActivity " + e.toString() + date);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (progress.isShowing()) {
                progress.dismiss();
            }
            if (tempstatus == 200) {
//                edtName.setText(name);
//                edtLastName.setText(lastname);
//                edtMobileNumber.setText(mobileNumber);
//                tvUpdateProfile.setVisibility(View.GONE);


                View hView = navigationView.getHeaderView(0);
                TextView Tv_shop_name = (TextView) hView.findViewById(R.id.tv_drawer_title);
                if (!custName.equals("null")||!custName.equals(null)) {
                    Tv_shop_name.setText(custName + " " + lastname);
                }
                // ImageView ImgVw_shop_img = (ImageView) hView.findViewById(R.id.imageView);
                //   Picasso.with(getApplicationContext()).load("http://202.88.154.118/GroceryWebAPI/api/Home/GetShopImage?filename=45KYCShopImage.jpg").fit().into(ImgVw_shop_img);
                TextView Tv_shop_address = (TextView) hView.findViewById(R.id.tv_drawer_subtitle);
                if (!mobileNumber.equals("null")||!mobileNumber.equals(null)) {
                    Tv_shop_address.setText(mobileNumber);
                }

            } else {
                //   tvUpdateProfile.setVisibility(View.VISIBLE);
                // Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_LONG).show();
            }
        }
    }

    public class GetMerchantDetails extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                String url = baseUrl1 + "GetDetailsMerchant";
                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost(url);
                String json = "";
                // 3. build jsonObject
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("MerchantId", mid);//


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
//[{"ShopName":"Ganesh","ShopAddress":"testaddress","MobileNo":"1234567890","ProvideDelivery":"yes","City":"pune","Area":"kothrud","OpenTime":"10am","CloseTime":"11pm","DeliveryTime":"null","DeliveryCharges":"200"}]
                if (status == 200) {
// , , , , , , , , Charges;
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);
                    JSONArray mainObject = new JSONArray(data);
                    JSONObject othposjsonobj = mainObject.getJSONObject(0);
                    JSONArray cityArray = othposjsonobj.getJSONArray("data");
                    for (int n = 0; n < cityArray.length(); n++) {
                        JSONObject object = cityArray.getJSONObject(n);
                        MerchantShopName = object.getString("ShopName");
                        //   ShopCity = object.getString("City");
                        MerchantArea = object.getString("Area");
//                        ShopAddress = object.getString("ShopAddress");
//                        MobNumber = object.getString("MobileNo");
//                        ShopOpenTime = object.getString("OpenTime");
//                        ShopCloseTime = object.getString("CloseTime");
//                        DeliveryOption = object.getString("ProvideDelivery");
//                        Time = object.getString("DeliveryTime");
//                        Charges = object.getString("DeliveryCharges");
                    }
                } else
                    result = "Did not work!";
            } catch (Exception e) {
                DateFormat dff = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                String date = dff.format(Calendar.getInstance().getTime());
                Log.d("InputStream", e.getLocalizedMessage());
                e.printStackTrace();
                appendLog(MainActivity.this, "11 MainActivity " + e.toString() + date);
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
                tvCity.setText(MerchantShopName);
                tvArea.setText(MerchantArea);
            } else {
                //  Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
            }
        }
    }
}

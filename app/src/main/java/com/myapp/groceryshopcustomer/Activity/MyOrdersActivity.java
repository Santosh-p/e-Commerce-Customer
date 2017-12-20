package com.myapp.groceryshopcustomer.Activity;

import android.app.ProgressDialog;
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
import android.widget.ListView;
import android.widget.Toast;

import com.myapp.groceryshopcustomer.Adapters.MyOrdersListAdapter;
import com.myapp.groceryshopcustomer.DBHandler.Customer;
import com.myapp.groceryshopcustomer.DBHandler.DatabaseHandler;
import com.myapp.groceryshopcustomer.MyOrdersDetails;
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
import java.util.Collections;

import static com.myapp.groceryshopcustomer.Constant.Constants.appendLog;
import static com.myapp.groceryshopcustomer.Constant.Constants.baseUrl1;
import static com.myapp.groceryshopcustomer.Constant.Constants.isNetworkAvailable;
import static com.myapp.groceryshopcustomer.Constant.Constants.progressDialog;


public class MyOrdersActivity extends AppCompatActivity {
    MyOrdersDetails myOrdersdetails;
    private SwipeRefreshLayout swipeRefreshLayout;
    ListView ListViewMyOrders;
    ArrayList<MyOrdersDetails> MyOrdersList = new ArrayList<MyOrdersDetails>();
    public static MyOrdersListAdapter myorderslistadapter;
    DatabaseHandler db = new DatabaseHandler(this);
    ProgressDialog progress;
    int status, custId;
    int tempstatus;
    InputStream inputStream = null;
    String result = null;

    boolean doubleBackToExitPressedOnce = true;
    SearchView searchView;
    String responceStatus;
    String OrderId;
    String OrderStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);
        progress = new ProgressDialog(MyOrdersActivity.this);


        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.myorders_swipe_refresh_layout);
        ListViewMyOrders = (ListView) findViewById(R.id.list_my_orders);
        Customer user1 = db.getCustomerDetails();
        custId = user1.getCust_id();
        if (isNetworkAvailable(MyOrdersActivity.this)) {
            new getMyOrdersList().execute();
            progressDialog(progress, "Loading", "Please wait...");
        } else {
            Toast.makeText(MyOrdersActivity.this, "Check Your Network Connection", Toast.LENGTH_SHORT).show();
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                MyOrdersList.clear();
                swipeRefreshLayout.setRefreshing(true);
                progressDialog(progress, "Loading", "Please wait...");
                new getMyOrdersList().execute();
            }
        });


    }

    public class getMyOrdersList extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            //  http://192.168.10.128/GroceryWebAPI/api/Home/getOrderListInCustomer
            try {
                String url = baseUrl1 + "getOrderListInCustomer";
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
                    MyOrdersList.clear();
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);
                    JSONArray mainObject = new JSONArray(data);
                    JSONObject othposjsonobj = mainObject.getJSONObject(0);
                    JSONArray cityArray = othposjsonobj.getJSONArray("data");
                    for (int n = 0; n < cityArray.length(); n++) {
                        JSONObject object = cityArray.getJSONObject(n);

                        MyOrdersDetails myordersdetails = new MyOrdersDetails();
                        myordersdetails.set_date(object.getString("Date"));
                        myordersdetails.set_time(object.getString("Time"));
                        myordersdetails.set_shopName(object.getString("ShopName"));
                        myordersdetails.set_order_id(object.getString("OrderId"));
                        myordersdetails.set_total_price(object.getString("TotalPrice"));
                        myordersdetails.set_orderStatus(object.getString("OrderStatus"));


                        MyOrdersList.add(myordersdetails);

                    }
                } else
                    result = "Did not work!";
            } catch (Exception e) {
                DateFormat dff = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                String date = dff.format(Calendar.getInstance().getTime());
                Log.d("InputStream", e.getLocalizedMessage());
                e.printStackTrace();
                appendLog(MyOrdersActivity.this, "1 MyOrdersActivity " + e.toString() + date);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (progress.isShowing()) {
                progress.dismiss();
            }
            swipeRefreshLayout.setRefreshing(false);
            if (tempstatus == 200) {
                //  if (responceStatus.equals("success")) {
                // progress.dismiss();
                swipeRefreshLayout.setRefreshing(false);
                Collections.reverse(MyOrdersList);
                myorderslistadapter = new MyOrdersListAdapter(MyOrdersActivity.this,
                        R.layout.my_orders_list, MyOrdersList);
                ListViewMyOrders.setAdapter(myorderslistadapter);
                myorderslistadapter.notifyDataSetChanged();
            } else {
                ///   progress.dismiss();
                Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.myordersmenu, menu);

        MenuItem searchViewItem = menu.findItem(R.id.myorders_action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchViewItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

//                SearchedProductDetailsList.clear();
//                if (!newText.equals("")) {
//                    for (ProductDetails d : ProductDetailsList) {
//                        if (d.get_productName() != null && d.get_productName().toLowerCase().contains(newText.toLowerCase())) {
//
//                            ProductDetails productdetails = new ProductDetails();
//                            productdetails.set_productName(d.get_productName());
//                            productdetails.set_categoryName(d.get_categoryName());
//                            productdetails.set_price(d.get_price());
//                            productdetails.set_unit(d.get_unit());
//
//                            SearchedProductDetailsList.add(productdetails);
//                        }
//                    }
//                    imageItemAdapter = new ProductDetailsListAdapter(ProductListActivity.this,
//                            R.layout.product_details_list, SearchedProductDetailsList);
//                    productlist.setAdapter(imageItemAdapter);
//                    imageItemAdapter.notifyDataSetChanged();
//                }

                if (!newText.equals("")) {
                    doubleBackToExitPressedOnce = false;
                    myorderslistadapter.filter(newText);
                }
                return false;


            }
        });
        return true;
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        searchView.setIconified(true);

        searchView.onActionViewCollapsed();
        searchView.setQuery("", false);
        searchView.clearFocus();
        myorderslistadapter.filter("");

    }


    public void CancelOrder(View v) {
        myOrdersdetails = (MyOrdersDetails) v.getTag();
        // String   orderid = myOrdersdetails.get_order_id();

        //   new JSONAsyncTaskToAddProductInCart().execute();
        //   Toast.makeText(MyOrdersActivity.this, orderid, Toast.LENGTH_SHORT).show();
        OrderId = myOrdersdetails.get_order_id();
        OrderStatus = "Cancelled";
        new SendOrderStatus().execute();
    }

    public class SendOrderStatus extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                String url = baseUrl1 + "UpdateOrderStatusCustomer";
                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost(url);
                String json = "";
                // 3. build jsonObject
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("CustomerId", custId);
                jsonObject.accumulate("OrderId", OrderId);
                jsonObject.accumulate("OrderStatus", OrderStatus);

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
                appendLog(MyOrdersActivity.this, "2 MyOrdersActivity " + e.toString() + date);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (responceStatus.equals("success")) {
                Toast.makeText(getApplicationContext(), "Order cancel successfully...", Toast.LENGTH_LONG).show();

                //    Intent i = new Intent(getApplicationContext(), DeliveryAddressActivity.class);
                //  startActivity(i);,

                new getMyOrdersList().execute();
            } else {
                Toast.makeText(getApplicationContext(), responceStatus, Toast.LENGTH_LONG).show();
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

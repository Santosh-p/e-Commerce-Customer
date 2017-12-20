package com.myapp.groceryshopcustomer.Activity;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.myapp.groceryshopcustomer.Adapters.OrderDetailsListAdapter;
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
import java.util.ArrayList;
import java.util.Calendar;

import static com.myapp.groceryshopcustomer.Constant.Constants.appendLog;
import static com.myapp.groceryshopcustomer.Constant.Constants.baseUrl1;
import static com.myapp.groceryshopcustomer.Constant.Constants.isNetworkAvailable;
import static com.myapp.groceryshopcustomer.Constant.Constants.progressDialog;

public class MyOrdersDetailsActivity extends AppCompatActivity {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    ProgressDialog progress;
    String orderId;
    int catId, productId, status, custId, itemsCount, unitQty;
    int tempstatus;
    InputStream inputStream = null;
    String result = null;
    String responceStatus;
    ArrayList<CustomerOrderDetails> CustomerOrderList = new ArrayList<CustomerOrderDetails>();
    String OrderStatus;
    TextView TvTotalItems, TvTotalAmmount;
    ListView ListViewOrderDetails;
    public static OrderDetailsListAdapter orderdetailslistadapter;
    Double total = 0.00;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders_details);
        progress = new ProgressDialog(MyOrdersDetailsActivity.this);

        DatabaseHandler db = new DatabaseHandler(MyOrdersDetailsActivity.this);
        Customer user1 = db.getCustomerDetails();
        custId = user1.getCust_id();

        pref = getApplicationContext().getSharedPreferences("mypref", MODE_PRIVATE);
        editor = pref.edit();
        orderId = pref.getString("orderId", "orderId");

        TvTotalItems = (TextView) findViewById(R.id.tv_total_items);
        TvTotalAmmount = (TextView) findViewById(R.id.tv_total_ammount);
        ListViewOrderDetails = (ListView) findViewById(R.id.list_order_details);

        if (isNetworkAvailable(MyOrdersDetailsActivity.this)) {
            progressDialog(progress, "Loading", "Please wait...");
            new GetOrderDetailsInCustomer().execute();
        } else {
            Toast.makeText(MyOrdersDetailsActivity.this, "Check Your Network Connection", Toast.LENGTH_SHORT).show();
        }

    }

    public class GetOrderDetailsInCustomer extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            //   http://192.168.10.128/GroceryWebAPI/api/Home/getOrderListInMerchant
            try {//http://202.88.154.118/GroceryWebAPI/api/Home/getOrderDetailsInMerchant
                String url = baseUrl1 + "getOrderDetailsInCustomer";
                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost(url);
                String json = "";
                // 3. build jsonObject
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("CustomerId", custId);//
                jsonObject.accumulate("OrderId", orderId);//77  MerchantId = 40  OrderId = 77

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
                    CustomerOrderList.clear();
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);
                    JSONArray mainObject = new JSONArray(data);
                    JSONObject othposjsonobj = mainObject.getJSONObject(0);
                    JSONArray cityArray = othposjsonobj.getJSONArray("data");
                    for (int n = 0; n < cityArray.length(); n++) {
                        JSONObject object = cityArray.getJSONObject(n);
//[{"ProductId":1,"ProductName":"Potatos","ProductImage":"potatoes.jpg","Manufacturer":"Yeshraj Agro","CategoryName":"Vegitables","Unit":"1kg","Qty":1,"Price":10}]
                        CustomerOrderDetails customerorderDetails = new CustomerOrderDetails();
                        customerorderDetails.set_productId(object.getInt("ProductId"));
                        customerorderDetails.set_productName(object.getString("ProductName"));
                        customerorderDetails.set_productImage(object.getString("ProductImage"));
                        customerorderDetails.set_manufacturer(object.getString("Manufacturer"));
                        customerorderDetails.set_categoryName(object.getString("CategoryName"));
                        customerorderDetails.set_unit(object.getString("Unit"));
                        customerorderDetails.set_quantity(object.getInt("Qty"));
                        customerorderDetails.set_price(object.getLong("Price"));
                        // customerorderDetails.set_mrp(object.getLong("MRP"));
                        customerorderDetails.setPacked_flag("unpacked");
                        customerorderDetails.set_orderStatus(OrderStatus);

                        CustomerOrderList.add(customerorderDetails);

                        total = total + object.getLong("Price") * object.getLong("Qty");
                    }
                } else
                    result = "Did not work!";
            } catch (Exception e) {
                DateFormat dff = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                String date = dff.format(Calendar.getInstance().getTime());
                Log.d("InputStream", e.getLocalizedMessage());
                e.printStackTrace();
                appendLog(MyOrdersDetailsActivity.this, "1 MyOrdersDetailsActivity " + e.toString() + date);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            if (tempstatus == 200) {
                //  if (responceStatus.equals("success")) {
                progress.dismiss();
                TvTotalItems.setText("Total Items : " + String.valueOf(CustomerOrderList.size()));
                TvTotalAmmount.setText("Total Ammount : ₹ " + String.valueOf(total));
                orderdetailslistadapter = new OrderDetailsListAdapter(MyOrdersDetailsActivity.this,
                        R.layout.order_details__list, CustomerOrderList);
                ListViewOrderDetails.setAdapter(orderdetailslistadapter);
                orderdetailslistadapter.notifyDataSetChanged();
            } else {
                progress.dismiss();
                Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_LONG).show();
            }
        }
    }
}

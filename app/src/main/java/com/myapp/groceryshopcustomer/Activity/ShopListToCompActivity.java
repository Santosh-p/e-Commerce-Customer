package com.myapp.groceryshopcustomer.Activity;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.myapp.groceryshopcustomer.Adapters.ShopListAdapterForCompare;
import com.myapp.groceryshopcustomer.DBHandler.Customer;
import com.myapp.groceryshopcustomer.DBHandler.DatabaseHandler;
import com.myapp.groceryshopcustomer.DBHandler.Merchant;
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
import static com.myapp.groceryshopcustomer.Constant.Constants.mid;

public class ShopListToCompActivity extends AppCompatActivity {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    int status;
    DatabaseHandler db = new DatabaseHandler(this);
    ArrayList<Merchant> merchantList = new ArrayList<Merchant>();
    ListView listShops;
    public ShopListAdapterForCompare imageItemAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_list_to_comp);
        listShops = (ListView) findViewById(R.id.lst_shop_list_compshplist);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout1);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                merchantList.clear();
                swipeRefreshLayout.setRefreshing(true);
                searchView.onActionViewCollapsed();
                searchView.setQuery("", false);
                searchView.clearFocus();
                new JSONAsyncTaskToGetShopsList().execute();
            }
        });
        pref = getApplicationContext().getSharedPreferences("mypref", MODE_PRIVATE);
        editor = pref.edit();
        //     mid = pref.getInt("mid", 0);
        new JSONAsyncTaskToGetShopsList().execute();
    }

    public class JSONAsyncTaskToGetShopsList extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                Customer user1 = db.getCustomerDetails();
                int id = user1.getCust_id();
                // code to consume wcf service which sends the details to the server
                String url = baseUrl1 + "getShopDetails";
                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost(url);
                String json = "";

                // 3. build jsonObject
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("CustomerId", id);

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
                    //[{"data":[{"Distance":"4Km","MerchantLat":"18.5092","ShopImage":"cccc","MerchantId":6,"Area":"Nal stop","ProvideDelivery":"yes","ShopName":"Suyog shop","EmailId":"v@gmail.com","ShopAddress":"Nal stop","MerchantLong":"18.5092","City":"pune","MobileNo":null},{"Distance":"6Km","MerchantLat":"18.5245","ShopImage":"ddd","MerchantId":7,"Area":"Fc road","ProvideDelivery":"yes","ShopName":"Novel shop","EmailId":"bb@gmail.com","ShopAddress":"Fc road","MerchantLong":"18.5245","City":"pune","MobileNo":null},{"Distance":"2Km","MerchantLat":"18.5073","ShopImage":"null","MerchantId":5,"Area":"Vanaz corner","ProvideDelivery":"yes","ShopName":"ABC electronics","EmailId":"xyz@gmail.com","ShopAddress":"Vanaz corner","MerchantLong":"18.5073","City":"pune","MobileNo":null},{"Distance":"2Km","MerchantLat":"18.5073","ShopImage":"ddfdfhfgh","MerchantId":8,"Area":"Vanaz corner","ProvideDelivery":"yes","ShopName":"Pavan shop","EmailId":"uu@gmail.com","ShopAddress":"Vanaz corner","MerchantLong":"18.5073","City":"pune","MobileNo":null},{"Distance":"4Km","MerchantLat":"18.5092","ShopImage":"csdfsd","MerchantId":9,"Area":"Nal stop","ProvideDelivery":"yes","ShopName":"Dmart","EmailId":"hgff@gmail.com","ShopAddress":"Nal stop","MerchantLong":"18.5092","City":"pune","MobileNo":null}],"Status":"success"}]
                    JSONArray cityArray = othposjsonobj.getJSONArray("data");
                    for (int n = 0; n < cityArray.length(); n++) {
                        JSONObject object = cityArray.getJSONObject(n);

                        Merchant merchantObj = new Merchant();
                        merchantObj.setMid(object.getInt("MerchantId"));
                        merchantObj.setShopName(object.getString("ShopName"));
                        merchantObj.setShopAddress(object.getString("ShopAddress"));
                        merchantObj.setMobileNumber(object.getString("MobileNo"));
                        merchantObj.setEmailId(object.getString("EmailId"));
                        merchantObj.setShopImage(object.getString("ShopImage"));
                        merchantObj.setCity(object.getString("City"));
                        merchantObj.setArea(object.getString("Area"));
                        merchantObj.setProvidesDelivery(object.getString("ProvideDelivery"));
                        merchantObj.setDistance(object.getString("Distance"));
                        //= new Merchant(,,,,,,,,);

                        if (object.getInt("MerchantId") != mid)
                            merchantList.add(merchantObj);

                        merchantObj = null;
                    }
                } else {
                    // result = "Did not work!";
                }


            } catch (Exception e) {
                DateFormat dff = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                String date = dff.format(Calendar.getInstance().getTime());
                Log.d("InputStream", e.getLocalizedMessage());
                e.printStackTrace();
                appendLog(ShopListToCompActivity.this, "1 ShopListToCompActivity " + e.toString() + date);

            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (status == 200) {

                swipeRefreshLayout.setRefreshing(false);
                imageItemAdapter = new ShopListAdapterForCompare(ShopListToCompActivity.this,
                        R.layout.main_view_list_row, merchantList);
                listShops.setAdapter(imageItemAdapter);
                imageItemAdapter.notifyDataSetChanged();

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

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
                    imageItemAdapter.filter(newText);
                }
                return false;
            }

        });

        return true;
    }
}

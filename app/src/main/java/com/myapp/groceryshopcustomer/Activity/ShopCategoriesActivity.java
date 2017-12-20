package com.myapp.groceryshopcustomer.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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

public class ShopCategoriesActivity extends AppCompatActivity {
    List<String> categoryArrList = new ArrayList<String>();
    List<Integer> categoryIdArrList = new ArrayList<Integer>();
    int status;
    ArrayAdapter lstAdapter;
    ListView listView;
    String categoryName;
    String result;
    int categoryId;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    private Menu menu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_categories);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listView = (ListView) findViewById(R.id.listviewCagegory);
        pref = getApplicationContext().getSharedPreferences("mypref", MODE_PRIVATE);
        editor = pref.edit();

        Intent intent=getIntent();
        //  mid = intent.getIntExtra("mid",0);

     //   mid=pref.getInt("mid",0);
        getSupportActionBar().setTitle(pref.getString("shopName",null));

        if (isNetworkAvailable(ShopCategoriesActivity.this)) {
            new JSONAsyncTaskToGetCategoriesList().execute();
        }


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // ListView Clicked item value
                categoryName = (String) listView.getItemAtPosition(position);

                categoryId = categoryIdArrList.get(position);
                editor.putString("categoryName",categoryName);
                editor.putInt("categoryId",categoryId);
                editor.apply();


                Intent productListIntent=new Intent(ShopCategoriesActivity.this,ProductListActivity.class);
                productListIntent.putExtra("categoryName",categoryName);
                productListIntent.putExtra("categoryId",categoryId);
                startActivity(productListIntent);
            }

        });
    }

    class JSONAsyncTaskToGetCategoriesList extends AsyncTask<String, Void, Boolean> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (android.os.Build.VERSION.SDK_INT > 9) {
                StrictMode.ThreadPolicy policy =
                        new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }
        }


        @Override
        protected Boolean doInBackground(String... urls) {
            try {
                // code to consume wcf service which sends the details to the server
                String url = baseUrl1 + "getCategories";
                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost(url);
                String json = "";

                // 3. build jsonObject
                JSONObject jsonObject = new JSONObject();
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
                    JSONArray cityArray = othposjsonobj.getJSONArray("data");
                    //[{"CategoryName":"Cloths","CategoryId":3}]
                    for (int n = 0; n < cityArray.length(); n++) {
                        JSONObject object = cityArray.getJSONObject(n);
                        categoryArrList.add(object.getString("CategoryName"));
                        categoryIdArrList.add(object.getInt("CategoryId"));
//                        categoryArrList.add(object.getString("CategoryName"));
//                        categoryArrList.add(object.getString("CategoryName"));
//                        categoryArrList.add(object.getString("CategoryName"));
//                        categoryArrList.add(object.getString("CategoryName"));
//                        categoryArrList.add(object.getString("CategoryName"));
//                        categoryArrList.add(object.getString("CategoryName"));
                    }
                } else
                    result = "Did not work!";

            } catch (Exception e) {
                DateFormat dff = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                String date = dff.format(Calendar.getInstance().getTime());
                Log.d("InputStream", e.getLocalizedMessage());
                e.printStackTrace();
                appendLog(ShopCategoriesActivity.this, "1 ShopCategoriesActivity " + e.toString() + date);

            }
            return null;
        }

        protected void onPostExecute(Boolean result) {
            if (status == 200) {
                lstAdapter = new ArrayAdapter<String>(ShopCategoriesActivity.this,
                        android.R.layout.simple_list_item_1, categoryArrList);
                listView.setAdapter(lstAdapter);
                lstAdapter.notifyDataSetChanged();
            }
        }
    }
        @Override
    public void onBackPressed() {


            super.onBackPressed();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.shopmenu, menu);
        this.menu = menu;
        Typeface fontAwesomeFont = Typeface.createFromAsset(getAssets(), "fontawesome-webfont.ttf");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_menucart) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

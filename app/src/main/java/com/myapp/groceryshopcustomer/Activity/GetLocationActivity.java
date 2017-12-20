package com.myapp.groceryshopcustomer.Activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.messaging.FirebaseMessaging;
import com.myapp.groceryshopcustomer.App.Config;
import com.myapp.groceryshopcustomer.DBHandler.Customer;
import com.myapp.groceryshopcustomer.DBHandler.DatabaseHandler;
import com.myapp.groceryshopcustomer.R;
import com.myapp.groceryshopcustomer.Util.NotificationUtils;

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
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static com.myapp.groceryshopcustomer.Constant.Constants.appendLog;
import static com.myapp.groceryshopcustomer.Constant.Constants.baseUrl1;
import static com.myapp.groceryshopcustomer.Constant.Constants.checkGPSService;
import static com.myapp.groceryshopcustomer.Constant.Constants.isNetworkAvailable;
import static com.myapp.groceryshopcustomer.Constant.Constants.progressDialog;

public class GetLocationActivity extends AppCompatActivity implements LocationListener, View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLocation;
    static double ulatitude, ulongitude;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    int status;

    private static final String TAG = MainActivity.class.getSimpleName();
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    //   private TextView txtRegId, txtMessage;
    public static String cityName, areaName;
    String DeviceId, Status, fromintent;
    int count, CustomerId, userId;
    List<Customer> custcount;
    DatabaseHandler db = new DatabaseHandler(this);
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    LinearLayout ll_SignIn, ll_SignUP;
    Button BtnHideShow, BtnSignIn, BtnSignUp;
    boolean hideshow = true;
    String EmailId, Password;
    EditText EdtSignInEmailId, EdtSignUpEmailId, EdtSignInPassword, EdtSignUPPassword;
    ProgressDialog progress;
    boolean alreadyExist;
    String LoginStatus = "fail";
    TextView tvForgotLogin;
    public static String otp;

    private static final String _CHAR = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    Random random = new Random();

    InputStream inputStream = null;
    String result = null;
    String responceStatus, AdminStatus, responseUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_location);
        progress = new ProgressDialog(GetLocationActivity.this);
        // ActionBar actionBar = getSupportActionBar();
        //   actionBar.hide();

        ulatitude = 73.7898537;
        ulongitude = 73.7898537;
        cityName = "Pune";
        areaName = "kothrud";

        custcount = db.getAllCustomers();
        count = 0;
        for (Customer cn : custcount) {
            count++;
        }
        Typeface fontAwesomeFont = Typeface.createFromAsset(getAssets(), "fontawesome-webfont.ttf");
        if (count > 0) {
            //  new updateLocationDetails().execute();
        }
        if (TextUtils.isEmpty(fromintent)) {
            fromintent = "self";
        }

        pref = getApplicationContext().getSharedPreferences("mypref", MODE_PRIVATE);
        editor = pref.edit();

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {

                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);

                    SharedPreferences prefs = getSharedPreferences("token", MODE_PRIVATE);
                    DeviceId = prefs.getString("regId", "not define");

                    db.addCustomer(new Customer(DeviceId, 0, "null", "null", "null"));


                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    String message = intent.getStringExtra("message");
                    Toast.makeText(getApplicationContext(), "Push notification: " + message, Toast.LENGTH_LONG).show();

                }
            }
        };
//Sign In Decleration
        ll_SignIn = (LinearLayout) findViewById(R.id.ll_sign_in);
        EdtSignInEmailId = (EditText) findViewById(R.id.edt_sigin_emailid);
        EdtSignInPassword = (EditText) findViewById(R.id.edt_sign_in_password);
        tvForgotLogin = (TextView) findViewById(R.id.textviewForgotpass);
        tvForgotLogin.setText(Html.fromHtml("<u>Forgot Password</u>"));
        BtnSignIn = (Button) findViewById(R.id.btn_sign_in);

        //Sign Up Decleration

        ll_SignUP = (LinearLayout) findViewById(R.id.ll_sign_up);
        EdtSignUpEmailId = (EditText) findViewById(R.id.edt_sigup_emailid);
        EdtSignUPPassword = (EditText) findViewById(R.id.edt_sign_up_password);
        BtnSignUp = (Button) findViewById(R.id.btn_sign_up);


        BtnHideShow = (Button) findViewById(R.id.btn_hideshow);
        BtnHideShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (hideshow) {
                    ll_SignUP.setVisibility(View.VISIBLE);
                    ll_SignIn.setVisibility(View.GONE);
                    hideshow = false;
                    BtnHideShow.setText("Existing USer? Sign In");
                } else {

                    BtnHideShow.setText("New to Grocery Shop? SIGNUP");

                    ll_SignUP.setVisibility(View.GONE);
                    ll_SignIn.setVisibility(View.VISIBLE);
                    hideshow = true;
                }

            }
        });

        BtnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences prefs = getSharedPreferences("token", MODE_PRIVATE);
                DeviceId = prefs.getString("regId", "not define");

                EmailId = EdtSignInEmailId.getText().toString();
                Password = EdtSignInPassword.getText().toString();
                if (EmailId.equals("") || EmailId.equals(null) || EmailId.isEmpty()) {
                    EdtSignInEmailId.setError("Enter Email-Id");
                    EdtSignInEmailId.requestFocus();
                } else if (!isValidEmail(EmailId)) {
                    EdtSignInEmailId.setError("Enter valid Email-Id");
                    EdtSignInEmailId.requestFocus();

                } else if (Password.equals("") || Password.equals(null) || Password.isEmpty()) {
                    EdtSignInPassword.setError("Enter PIN");
                    EdtSignInPassword.requestFocus();
                } else if (Password.length() < 4) {
                    EdtSignInPassword.setError("Enter 4 Digit PIN");
                    EdtSignInPassword.requestFocus();
                } else {
                    if (isNetworkAvailable(GetLocationActivity.this)) {
                        progressDialog(progress, "Loading", "Please wait...");
                        new CheckCustomerExist1().execute();
                    } else {
                        Toast.makeText(GetLocationActivity.this, "Check Your Network Connection", Toast.LENGTH_SHORT).show();
                        //      }
                    }
                }
            }
        });
        BtnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getSharedPreferences("token", MODE_PRIVATE);
                DeviceId = prefs.getString("regId", "not define");

                EmailId = EdtSignUpEmailId.getText().toString();
                Password = EdtSignUPPassword.getText().toString();
                if (EmailId.equals("") || EmailId.equals(null) || EmailId.isEmpty()) {
                    EdtSignUpEmailId.setError("Enter Email-Id");
                    EdtSignUpEmailId.requestFocus();
                } else if (!isValidEmail(EmailId)) {
                    EdtSignUpEmailId.setError("Enter valid Email-Id");
                    EdtSignUpEmailId.requestFocus();

                } else if (Password.equals("") || Password.equals(null) || Password.isEmpty()) {
                    EdtSignUPPassword.setError("Enter PIN");
                    EdtSignUPPassword.requestFocus();
                } else if (Password.length() < 4) {
                    EdtSignUPPassword.setError("Enter 4 Digit PIN");
                    EdtSignUPPassword.requestFocus();
                } else {
                    if (isNetworkAvailable(GetLocationActivity.this)) {
                        progressDialog(progress, "Loading", "Please wait...");
                        new CheckCustomerExist2().execute();

                    } else {
                        Toast.makeText(GetLocationActivity.this, "Check Your Network Connection", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

        tvForgotLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendOtp();
            }
        });


        if (count > 0) {
            //  new updateLocationDetails().execute();

            DatabaseHandler db = new DatabaseHandler(GetLocationActivity.this);
            Customer user1 = db.getCustomerDetails();
            LoginStatus = user1.getLogin_status();
            userId = user1.getCust_id();

            if (LoginStatus.equals("success")) {
                editor.putString("city", cityName);
                editor.putString("area", areaName);
                editor.apply();
                Intent HomeIntent = new Intent(GetLocationActivity.this, MainActivity.class);
                HomeIntent.putExtra("city", cityName);
                HomeIntent.putExtra("area", areaName);
                startActivity(HomeIntent);
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(getApplicationContext());
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c.getTime());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            //mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLocation != null) {
            ulatitude = mLocation.getLatitude();
            ulongitude = mLocation.getLongitude();

            // tvlat.setText("lat = "+String.valueOf(ulatitude)+"lang"+String.valueOf(ulongitude));

            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(ulatitude, ulongitude, 1);
                cityName = addresses.get(0).getLocality();
                areaName = addresses.get(0).getSubLocality();

                custcount = db.getAllCustomers();
                count = 0;

//                for (Customer cn : custcount) {
//                    count++;
//                }
//                if (count <= 0) {
//                    new SendRegistrationDetails().execute();
//                } else {
//                    new updateLocationDetails().execute();
//                }
            } catch (Exception e) {
                DateFormat dff = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                String date = dff.format(Calendar.getInstance().getTime());
                Log.d("InputStream", e.getLocalizedMessage());
                e.printStackTrace();
                appendLog(GetLocationActivity.this, "1 GetLocationActivity " + e.toString() + date);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (checkGPSService(GetLocationActivity.this)) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK: {
                        mGoogleApiClient = new GoogleApiClient.Builder(GetLocationActivity.this)
                                .addConnectionCallbacks(GetLocationActivity.this)
                                .addOnConnectionFailedListener(GetLocationActivity.this)
                                .addApi(LocationServices.API)
                                .build();
                        mGoogleApiClient.connect();
                        // All required changes were successfully made
                        Toast.makeText(GetLocationActivity.this, "Location enabled by user!", Toast.LENGTH_LONG).show();
                        break;
                    }
                    case Activity.RESULT_CANCELED: {
                        // The user was asked to change settings, but chose not to
                        Toast.makeText(GetLocationActivity.this, "Location not enabled, user cancelled.", Toast.LENGTH_LONG).show();
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
        }
    }

    // Double back press exit
    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    public class CheckCustomerExist1 extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {

                SharedPreferences prefs = getSharedPreferences("token", MODE_PRIVATE);
                DeviceId = prefs.getString("regId", "not define");

                //  DatabaseHandler db = new DatabaseHandler(GetLocationActivity.this);
                //    Customer user1 = db.getCustomerDetails();
                //   custId = user1.getCust_id();
                //    DeviceId = user1.getDeviceId();

                String url = baseUrl1 + "CheckCustomerExist";
                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost(url);
                String json = "";
                // 3. build jsonObject

                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("EmailId", EmailId);
                jsonObject.accumulate("DeviceId", DeviceId);

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
                HttpResponse response = httpclient.execute(httpPost);

                // StatusLine stat = response.getStatusLine();
                status = response.getStatusLine().getStatusCode();

                if (status == 200) {
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);
                    JSONArray mainObject = new JSONArray(data);
                    //[{"Status":"success","UserId":"6"}]
                    JSONObject othposjsonobj = mainObject.getJSONObject(0);
                    Status = othposjsonobj.getString("Status");
                    userId = Integer.parseInt(othposjsonobj.getString("UserId"));//75

                }

            } catch (Exception e) {
                DateFormat dff = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                String date = dff.format(Calendar.getInstance().getTime());
                Log.d("InputStream", e.getLocalizedMessage());
                e.printStackTrace();
                appendLog(GetLocationActivity.this, "3 GetLocationActivity " + e.toString() + date);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            if (progress.isShowing()) {
                progress.dismiss();
            }
            if (status == 200) {
                if (Status.equals("EmailId not exist")) {
                    Toast.makeText(GetLocationActivity.this, "New User! Please SIGNUP", Toast.LENGTH_SHORT).show();
                } else if (Status.equals("success")) {

                    new SendLoginDetails().execute();

                } else {
                    Toast.makeText(GetLocationActivity.this, "Error,Please try letter", Toast.LENGTH_SHORT).show();

                }
            }

        }


    }

    public class CheckCustomerExist extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {

                SharedPreferences prefs = getSharedPreferences("token", MODE_PRIVATE);
                DeviceId = prefs.getString("regId", "not define");

                //  DatabaseHandler db = new DatabaseHandler(GetLocationActivity.this);
                //    Customer user1 = db.getCustomerDetails();
                //   custId = user1.getCust_id();
                //    DeviceId = user1.getDeviceId();

                String url = baseUrl1 + "CheckCustomerExist";
                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost(url);
                String json = "";
                // 3. build jsonObject

                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("EmailId", EmailId);
                jsonObject.accumulate("DeviceId", DeviceId);

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
                HttpResponse response = httpclient.execute(httpPost);

                // StatusLine stat = response.getStatusLine();
                status = response.getStatusLine().getStatusCode();

                if (status == 200) {
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);
                    JSONArray mainObject = new JSONArray(data);
                    //[{"Status":"success","UserId":"6"}]
                    JSONObject othposjsonobj = mainObject.getJSONObject(0);
                    Status = othposjsonobj.getString("Status");
                    userId = Integer.parseInt(othposjsonobj.getString("UserId"));//75

                }

            } catch (Exception e) {
                DateFormat dff = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                String date = dff.format(Calendar.getInstance().getTime());
                Log.d("InputStream", e.getLocalizedMessage());
                e.printStackTrace();
                appendLog(GetLocationActivity.this, "3 GetLocationActivity " + e.toString() + date);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            if (progress.isShowing()) {
                progress.dismiss();
            }
            if (status == 200) {
                if (Status.equals("EmailId not exist")) {
                    Toast.makeText(GetLocationActivity.this, "New User! Please SIGNUP", Toast.LENGTH_SHORT).show();
                    // new SendRegistrationDetails().execute();
                } else if (Status.equals("success")) {

                    //  Toast.makeText(GetLocationActivity.this, "Email Id already exist.", Toast.LENGTH_SHORT).show();
                    new SendOtpDetails().execute();
                    // new SendRegistrationDetails().execute();


                    // db.addCustomer(new Customer(Integer.parseInt(userId), "null", "null"));
//                    db.addCustomer(new Customer(DeviceId, Integer.parseInt(userId), "null", "null", LoginStatus));
//                    //    db.updateCustomer(new Customer(DeviceId, Integer.parseInt(userId), "null", "null"));
//                    //    db.updateCustomer(new Customer(Integer.parseInt(userId), "null", "null"));
//                    ll_SignUP.setVisibility(View.VISIBLE);
//                    ll_SignIn.setVisibility(View.GONE);
//                    hideshow = true;
//                    EdtSignInEmailId.setText(EmailId);
//                    alreadyExist = true;


                } else {
                    Toast.makeText(GetLocationActivity.this, "Error,Please try letter", Toast.LENGTH_SHORT).show();

                }
            }

        }


    }

    public class CheckCustomerExist2 extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {

                SharedPreferences prefs = getSharedPreferences("token", MODE_PRIVATE);
                DeviceId = prefs.getString("regId", "not define");

                String url = baseUrl1 + "CheckCustomerExist";
                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost(url);
                String json = "";
                // 3. build jsonObject

                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("EmailId", EmailId);
                jsonObject.accumulate("DeviceId", DeviceId);

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
                HttpResponse response = httpclient.execute(httpPost);

                // StatusLine stat = response.getStatusLine();
                status = response.getStatusLine().getStatusCode();

                if (status == 200) {
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);
                    JSONArray mainObject = new JSONArray(data);
                    //[{"Status":"success","UserId":"6"}]
                    JSONObject othposjsonobj = mainObject.getJSONObject(0);
                    Status = othposjsonobj.getString("Status");
                    userId = Integer.parseInt(othposjsonobj.getString("UserId"));//75

                }

            } catch (Exception e) {
                DateFormat dff = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                String date = dff.format(Calendar.getInstance().getTime());
                Log.d("InputStream", e.getLocalizedMessage());
                e.printStackTrace();
                appendLog(GetLocationActivity.this, "3 GetLocationActivity " + e.toString() + date);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            if (progress.isShowing()) {
                progress.dismiss();
            }
            if (status == 200) {
                if (Status.equals("EmailId not exist")) {
                    //  Toast.makeText(GetLocationActivity.this, "New User! Please SIGNUP", Toast.LENGTH_SHORT).show();
                    new SendRegistrationDetails().execute();
                } else if (Status.equals("success")) {

                    Toast.makeText(GetLocationActivity.this, "Email Id already exist.", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(GetLocationActivity.this, "Error,Please try letter", Toast.LENGTH_SHORT).show();

                }
            }

        }


    }

    public class SendRegistrationDetails extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                SharedPreferences prefs = getSharedPreferences("token", MODE_PRIVATE);
                DeviceId = prefs.getString("regId", "not define");

                String url = baseUrl1 + "InsertRegistrationDetailsOfCustomer";
                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost(url);
                String json = "";
                // 3. build jsonObject

                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("EmailId", EmailId);
                jsonObject.accumulate("DeviceId", DeviceId);
                jsonObject.accumulate("Password", Password);

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
                HttpResponse response = httpclient.execute(httpPost);

                // StatusLine stat = response.getStatusLine();
                status = response.getStatusLine().getStatusCode();

                if (status == 200) {
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);
                    JSONArray mainObject = new JSONArray(data);
                    //[{"Status":"success","UserId":"6"}]
                    JSONObject othposjsonobj = mainObject.getJSONObject(0);
                    Status = othposjsonobj.getString("Status");
                    userId = Integer.parseInt(othposjsonobj.getString("UserId"));

                }

            } catch (Exception e) {
                DateFormat dff = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                String date = dff.format(Calendar.getInstance().getTime());
                Log.d("InputStream", e.getLocalizedMessage());
                e.printStackTrace();
                appendLog(GetLocationActivity.this, "3 GetLocationActivity " + e.toString() + date);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (progress.isShowing()) {
                progress.dismiss();
            }
            if (status == 200) {
                if (Status.equals("success")) {
                    LoginStatus = "success";
                    Customer customer = new Customer();
                    customer.setDeviceId(DeviceId);
                    customer.setCust_id(userId);
                    customer.setLogin_status(LoginStatus);

                    int result1 = db.updateCustomerdetails(customer);
                    if (result1 == 1) {
                        editor.putString("city", cityName);
                        editor.putString("area", areaName);
                        editor.apply();
                        Intent HomeIntent = new Intent(GetLocationActivity.this, MainActivity.class);
                        HomeIntent.putExtra("city", cityName);
                        HomeIntent.putExtra("area", areaName);
                        startActivity(HomeIntent);
                        Toast.makeText(GetLocationActivity.this, "Register Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(GetLocationActivity.this, "update fail", Toast.LENGTH_SHORT).show();

                    }
                }
                if (Status.equals("EmailId already exist")) {
                    Toast.makeText(GetLocationActivity.this, "EmailId already exist", Toast.LENGTH_SHORT).show();

                }
            } else {
                Toast.makeText(GetLocationActivity.this, Status, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class SendLoginDetails extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                String url = baseUrl1 + "CustomerLogin";
                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost(url);
                String json = "";
                // 3. build jsonObject

                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("CustomerId", userId);
                jsonObject.accumulate("EmailId", EmailId);
                jsonObject.accumulate("Password", Password);

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
                HttpResponse response = httpclient.execute(httpPost);

                // StatusLine stat = response.getStatusLine();
                status = response.getStatusLine().getStatusCode();

                if (status == 200) {
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);
                    JSONArray mainObject = new JSONArray(data);
                    //[{"Status":"success","UserId":"6"}]
                    JSONObject othposjsonobj = mainObject.getJSONObject(0);
                    Status = othposjsonobj.getString("Status");
                    //      userId = othposjsonobj.getString("UserId");

                }

            } catch (Exception e) {
                DateFormat dff = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                String date = dff.format(Calendar.getInstance().getTime());
                Log.d("InputStream", e.getLocalizedMessage());
                e.printStackTrace();
                appendLog(GetLocationActivity.this, "3 GetLocationActivity " + e.toString() + date);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (progress.isShowing()) {
                progress.dismiss();
            }
            if (status == 200) {
                if (Status.equals("success")) {
                    LoginStatus = "success";
                    //   db.updateLoginStatus(new Customer(LoginStatus));
                    Customer customer = new Customer();
                    customer.setDeviceId(DeviceId);
                    customer.setCust_id(userId);
                    customer.setLogin_status(LoginStatus);

                    int result1 = db.updateCustomerdetails(customer);
                    if (result1 == 1) {
                        Intent HomeIntent = new Intent(GetLocationActivity.this, MainActivity.class);
                        HomeIntent.putExtra("city", cityName);
                        HomeIntent.putExtra("area", areaName);
                        startActivity(HomeIntent);
                    } else {
                        Toast.makeText(GetLocationActivity.this, "update fail", Toast.LENGTH_SHORT).show();

                    }
                } else if (Status.equals("fail")) {
                    Toast.makeText(GetLocationActivity.this, "Incorrect Password", Toast.LENGTH_SHORT).show();

                }
            } else {
                Toast.makeText(GetLocationActivity.this, "Temporary service not available", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class updateLocationDetails extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            http:
//192.168.10.128/GroceryWebAPI/api/Home/InsertRegistrationDetailsOfCustomer
            try {
                DatabaseHandler db = new DatabaseHandler(GetLocationActivity.this);
                Customer user1 = db.getCustomerDetails();
                int custId = user1.getCust_id();
                String url = baseUrl1 + "UpdateLocationDetailsOfCustomer";
                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost(url);
                String json = "";
                // 3. build jsonObject
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("Lattitude", ulatitude);
                jsonObject.accumulate("Longitude", ulongitude);
                jsonObject.accumulate("City", cityName);
                jsonObject.accumulate("Area", areaName);
                jsonObject.accumulate("CustomerId", custId);
// {"CustLattitude":18.5088514,"CustLongitude":73.7897732,"ShopCity":"pune","ShopArea":"Kothrud","CustomerId":36}
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
                HttpResponse response = httpclient.execute(httpPost);

                // StatusLine stat = response.getStatusLine();
                status = response.getStatusLine().getStatusCode();

                if (status == 200) {
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);
                    JSONArray mainObject = new JSONArray(data);
                    //[{"Status":"success","UserId":"6"}]
                    JSONObject othposjsonobj = mainObject.getJSONObject(0);
                    Status = othposjsonobj.getString("Status");

                }

            } catch (Exception e) {
                DateFormat dff = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                String date = dff.format(Calendar.getInstance().getTime());
                Log.d("InputStream", e.getLocalizedMessage());
                e.printStackTrace();
                appendLog(GetLocationActivity.this, "4 GetLocationActivity " + e.toString() + date);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (status == 200) {
                if (Status.equals("success")) {
                    editor.putString("city", cityName);
                    editor.putString("area", areaName);
                    editor.apply();
                    Intent HomeIntent = new Intent(GetLocationActivity.this, MainActivity.class);
                    startActivity(HomeIntent);

                } else {
                    Toast.makeText(getApplicationContext(), " Need to Re-Install app again", Toast.LENGTH_LONG).show();

                }
            }
        }
    }

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public class SendOtpDetails extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {

                String url = baseUrl1 + "SendEmailOtp";

                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost(url);
                String json = "";

                // 3. build jsonObject
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("Email", EmailId);
                jsonObject.accumulate("OTP", otp);

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
                inputStream = httpResponse.getEntity().getContent();
                // 10. convert inputstream to string
                if (inputStream != null) {
                    result = convertInputStreamToString(inputStream);
                    result = result.replace("[", "");
                    result = result.replace("]", "");
                    JSONObject mainObject = new JSONObject(result);

                    responceStatus = mainObject.getString("Status");

                } else
                    result = "Did not work!";
            } catch (Exception e) {
                DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                String date = df.format(Calendar.getInstance().getTime());
                Log.d("InputStream", e.getLocalizedMessage());
                e.printStackTrace();
                appendLog(GetLocationActivity.this, "1 GetLocationActivity " + e.toString() + date);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Intent i = new Intent(GetLocationActivity.this, SubmitOTPActivity.class);
            i.putExtra("OTP", otp);
            startActivity(i);
            Toast.makeText(GetLocationActivity.this, "OTP sends successfully on your email id", Toast.LENGTH_SHORT).show();
            if (progress.isShowing()) {
                progress.dismiss();
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

    // generate random OTP
    public String random() {
        StringBuffer randStr = new StringBuffer();
        for (int i = 0; i < 4; i++) {
            int number = getRandomNumber();
            char ch = _CHAR.charAt(number);
            randStr.append(ch);
        }
        return randStr.toString();
    }

    private int getRandomNumber() {
        int randomInt = 0;
        randomInt = random.nextInt(_CHAR.length());
        if (randomInt - 1 == -1) {
            return randomInt;
        } else {
            return randomInt - 1;
        }
    }

    public void sendOtp() {
        if (isNetworkAvailable(GetLocationActivity.this)) {
//                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(GetLocationActivity.this);
//
//
//
//                    alertDialogBuilder.setMessage("Are you sure you want to send OTP to " + EmailId+"?");
//                    alertDialogBuilder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface arg0, int arg1) {
//                            progressDialog(progress, "", "Sending OTP...");
//                            otp = random();
//                            new SendOtpDetails().execute();
//                        }
//                    });
//
//                    alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            // do nothing
//                        }
//                    });
//
//                    AlertDialog alertDialog = alertDialogBuilder.create();
//                    alertDialog.show();


            // final Context context = this;
            // get popup view to edit price
            // get prompts.xml view
            final Context context = this;
            LayoutInflater li = LayoutInflater.from(context);
            View promptsView = li.inflate(R.layout.send_otp_emailid_popup, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    context);

            // set prompts.xml to alertdialog builder
            alertDialogBuilder.setView(promptsView);

            final EditText userInputPrice = (EditText) promptsView
                    .findViewById(R.id.popup_edt_emailid);

            // set dialog message
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    EmailId = userInputPrice.getText().toString();

                                    if (EmailId.equals("") || EmailId == null || EmailId.isEmpty() || !isValidEmail(EmailId)) {

                                        Toast.makeText(getApplicationContext(), "Enter vallid Email Id", Toast.LENGTH_LONG).show();

                                    } else {
//
                                        otp = random();
                                        new CheckCustomerExist().execute();

                                        progressDialog(progress, "Sending OTP", "Please wait...");
                                    }
                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();

        } else {
            Toast.makeText(getApplicationContext(), "No network Available!", Toast.LENGTH_LONG).show();
        }
    }
}


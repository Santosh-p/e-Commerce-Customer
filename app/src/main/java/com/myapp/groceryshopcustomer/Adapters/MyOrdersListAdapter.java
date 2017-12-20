package com.myapp.groceryshopcustomer.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.myapp.groceryshopcustomer.Activity.MyOrdersDetailsActivity;
import com.myapp.groceryshopcustomer.MyOrdersDetails;
import com.myapp.groceryshopcustomer.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by SSPL on 08-09-2017.
 */

/*
public class MyOrdersListAdapter extends BaseAdapter implements View.OnClickListener {
    TextView TvCustomerName, TvDate, TvTime, Tvnextbtn, TvCancel, TvOrderId, TvTotalPrice, TvOrderStatus;
    Button BtnCancelOrder;
    private Activity myContext;
    private ArrayList<MyOrdersDetails> datas;
    private ArrayList<MyOrdersDetails> items;
    SharedPreferences sharedpreferences;
    public static final String mypreference = "mypref";
    public static final String ProductIdKey = "ProductIdKey";
    private LayoutInflater v;

    MyOrdersDetails post;

    public MyOrdersListAdapter(Context context, int textViewResourceId,
                               ArrayList<MyOrdersDetails> objects) {
        // TODO Auto-generated constructor stub
        myContext = (Activity) context;
        this.datas = objects;
        this.items = new ArrayList<MyOrdersDetails>();
        this.items.addAll(datas);
        v = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public void onClick(View v) {
    }

    static class ViewHolder {
        TextView postTitleView;
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final MyOrdersListAdapter.ViewHolder holder;
        holder = new MyOrdersListAdapter.ViewHolder();
        post = (MyOrdersDetails) datas.get(position);
        MyOrdersDetails ei = (MyOrdersDetails) post;
        vi = v.inflate(R.layout.my_orders_list, null);
        Typeface fontAwesomeFont = Typeface.createFromAsset(myContext.getAssets(), "fontawesome-webfont.ttf");
        // MyOrdersDetails atomPayment = null;
        TvCustomerName = (TextView) vi.findViewById(R.id.tv_customername);
        TvDate = (TextView) vi.findViewById(R.id.tv_date);
        TvTime = (TextView) vi.findViewById(R.id.tv_time);
        TvOrderId = (TextView) vi.findViewById(R.id.tv_orderId);
        TvTotalPrice = (TextView) vi.findViewById(R.id.tv_total_price);
        TvOrderStatus = (TextView) vi.findViewById(R.id.tv_order_status);

        BtnCancelOrder = (Button) vi.findViewById(R.id.btn_cancel);
        BtnCancelOrder.setTag(post);

        // TvCancel = (TextView) vi.findViewById(R.id.tv_);
        Tvnextbtn = (TextView) vi.findViewById(R.id.tvGoAhed1);
        Tvnextbtn.setTypeface(fontAwesomeFont);

        TvCustomerName.setText(post.get_shopName());
        TvDate.setText(post.get_date());
        TvTime.setText(post.get_time());
        TvOrderId.setText("Order Id : " + post.get_order_id());
        TvTotalPrice.setText("Total : ₹ " + post.get_total_price());
        TvOrderStatus.setText("Order Status : " + post.get_orderStatus());

        if (post.get_orderStatus().equals("Cancelled") || post.get_orderStatus().equals("Dispatch") || post.get_orderStatus().equals("Delivered")) {
            BtnCancelOrder.setVisibility(View.GONE);
        }
        BtnCancelOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(myContext, "BtnCancelOrder", Toast.LENGTH_SHORT).show();
            }
        });


        vi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(myContext, "Pending", Toast.LENGTH_LONG).show();
                sharedpreferences = myContext.getSharedPreferences(mypreference,
                        Context.MODE_PRIVATE);

//                MyOrdersDetails product = datas.get(position);
//                SharedPreferences.Editor editor = sharedpreferences.edit();
//                editor.putString(ProductIdKey, product.get_order_id());
//                editor.commit();

//                Intent selectCategoryIntent = new Intent(myContext, MyOrderDetailsActivity.class);
//                myContext.startActivity(selectCategoryIntent);


            }
        });
        vi.setTag(holder);
        return vi;
    }

    // Filter Class
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());

        datas.clear();

        if (charText.length() == 0) {
            datas.addAll(items);
        } else {
            for (MyOrdersDetails wp : items) {
                if (wp.get_order_id().toLowerCase(Locale.getDefault()).contains(charText) || wp.get_shopName().toLowerCase(Locale.getDefault()).contains(charText)) {
                    datas.add(wp);
                }
            }
        }
        this.notifyDataSetChanged();
    }
}
*/





public class MyOrdersListAdapter extends BaseAdapter implements View.OnClickListener {

    Spinner qtySpinner;
    private Activity myContext;
    private ArrayList<MyOrdersDetails> datas;
    private ArrayList<MyOrdersDetails> arraylist;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    private LayoutInflater v;

    // Product post;

    public MyOrdersListAdapter(Context context, int textViewResourceId,
                               ArrayList<MyOrdersDetails> objects) {
        // TODO Auto-generated constructor stub
        myContext = (Activity) context;
        this.datas = objects;
        this.arraylist = new ArrayList<MyOrdersDetails>();
        this.arraylist.addAll(datas);
        v = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void onClick(View v) {
    }

    static class ViewHolder {
        MyOrdersDetails myOrdersdetails;

        TextView TvCustomerName, TvDate, TvTime, Tvnextbtn, TvCancel, TvOrderId, TvTotalPrice, TvOrderStatus;
        Button BtnCancelOrder;

    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;
        holder = new ViewHolder();
        vi = v.inflate(R.layout.my_orders_list, null);
        //post = (Product) datas.get(position);
        try {
            if (datas != null) {
                // if (holder.product != null) {
                if (datas.size() >= position) {
                    holder.myOrdersdetails = datas.get(position);

                    Typeface fontAwesomeFont = Typeface.createFromAsset(myContext.getAssets(), "fontawesome-webfont.ttf");
                    // MyOrdersDetails atomPayment = null;
                    holder.TvCustomerName = (TextView) vi.findViewById(R.id.tv_customername);
                    holder.TvDate = (TextView) vi.findViewById(R.id.tv_date);
                    holder.TvTime = (TextView) vi.findViewById(R.id.tv_time);
                    holder.TvOrderId = (TextView) vi.findViewById(R.id.tv_orderId);
                    holder.TvTotalPrice = (TextView) vi.findViewById(R.id.tv_total_price);
                    holder.TvOrderStatus = (TextView) vi.findViewById(R.id.tv_order_status);

                    holder.BtnCancelOrder = (Button) vi.findViewById(R.id.btn_cancel);
                    holder.BtnCancelOrder.setTag(holder.myOrdersdetails);

                    // TvCancel = (TextView) vi.findViewById(R.id.tv_);
                    holder.Tvnextbtn = (TextView) vi.findViewById(R.id.tvGoAhed1);
                    holder.Tvnextbtn.setTypeface(fontAwesomeFont);

                    holder.TvCustomerName.setText(holder.myOrdersdetails.get_shopName());
                    holder.TvDate.setText(holder.myOrdersdetails.get_date());
                    holder.TvTime.setText(holder.myOrdersdetails.get_time());
                    holder.TvOrderId.setText("Order Id : " + holder.myOrdersdetails.get_order_id());
                    holder.TvTotalPrice.setText("Total : ₹ " + holder.myOrdersdetails.get_total_price());
                    holder.TvOrderStatus.setText("Order Status : " + holder.myOrdersdetails.get_orderStatus());

                    if (holder.myOrdersdetails.get_orderStatus().equals("Accepted")||holder.myOrdersdetails.get_orderStatus().equals("Cancelled") || holder.myOrdersdetails.get_orderStatus().equals("Dispatch") || holder.myOrdersdetails.get_orderStatus().equals("Delivered")) {
                        holder.BtnCancelOrder.setVisibility(View.GONE);
                    }
//                    holder.BtnCancelOrder.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            Toast.makeText(myContext, "BtnCancelOrder", Toast.LENGTH_SHORT).show();
//                        }
//                    });


                  //  holder.imageproduct = (ImageView) vi.findViewById(R.id.img_productimage);

                    //   Product ei = (Product) post;

                }
            }
        } catch (Exception e) {
            DateFormat dff = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
            String date = dff.format(Calendar.getInstance().getTime());
            Log.d("InputStream", e.getLocalizedMessage());
            e.printStackTrace();
            // appendLog(myContext, "1 MyOrdersListAdapter " + e.toString() + date);
        }

        vi.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                pref = myContext.getSharedPreferences("mypref", MODE_PRIVATE);
                editor = pref.edit();
                MyOrdersDetails myOrdersdetails = datas.get(position);
                editor.putString("orderId", myOrdersdetails.get_order_id());
                editor.commit();
                Intent selectCategoryIntent = new Intent(myContext, MyOrdersDetailsActivity.class);
                myContext.startActivity(selectCategoryIntent);
            }
        });

        return vi;

    }

    // Filter Class
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        datas.clear();
        if (charText.length() == 0) {
            datas.addAll(arraylist);
        } else {
            for (MyOrdersDetails wp : arraylist) {
                if (wp.get_order_id().toLowerCase(Locale.getDefault()).contains(charText) || wp.get_shopName().toLowerCase(Locale.getDefault()).contains(charText)) {
                    datas.add(wp);
                }
            }
        }
        this.notifyDataSetChanged();
    }
}





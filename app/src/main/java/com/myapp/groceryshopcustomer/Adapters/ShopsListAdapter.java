package com.myapp.groceryshopcustomer.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.myapp.groceryshopcustomer.Activity.ProductListActivity;
import com.myapp.groceryshopcustomer.DBHandler.Merchant;
import com.myapp.groceryshopcustomer.R;

import java.util.ArrayList;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;
import static com.myapp.groceryshopcustomer.Constant.Constants.baseUrl1;
import static com.myapp.groceryshopcustomer.Constant.Constants.mid;


/**
 * Created by pramod on 11/17/16..
 */
public class ShopsListAdapter extends BaseAdapter implements View.OnClickListener {

    private Activity myContext;
    private ArrayList<Merchant> datas;
    private ArrayList<Merchant> arraylist;
    private LayoutInflater v;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Merchant post;

    public ShopsListAdapter(Context context, int textViewResourceId,
                            ArrayList<Merchant> objects) {
        // TODO Auto-generated constructor stub
        myContext = (Activity) context;
        this.datas = objects;
        this.arraylist = new ArrayList<Merchant>();
        this.arraylist.addAll(datas);
        v = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public void onClick(View v) {
    }

    static class ViewHolder {
        TextView postShopName;
        TextView postAddress;
        TextView postDistance;
        TextView postDeliveryStatus;
        ImageView postShopImage;
        TextView tvGoAhed1;
        TextView tvLocSymbol;

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
        post = (Merchant) datas.get(position);
        Merchant ei = (Merchant) post;
        vi = v.inflate(R.layout.main_view_list_row, null);

        Typeface fontAwesomeFont = Typeface.createFromAsset(myContext.getAssets(), "fontawesome-webfont.ttf");
        holder.postShopName = (TextView) vi.findViewById(R.id.tvShopName);
        holder.postAddress = (TextView) vi.findViewById(R.id.tvAddress);

        holder.postDistance =(TextView)vi.findViewById(R.id.tvDistance);
        holder.postDeliveryStatus =(TextView)vi.findViewById(R.id.tvDeliveryStatus);
        holder.postShopImage =(ImageView)vi.findViewById(R.id.imgShopImage);
        holder.tvGoAhed1 =(TextView)vi.findViewById(R.id.tvGoAhed1);
        holder.tvLocSymbol =(TextView)vi.findViewById(R.id.tvLocSymbol);

        String url=baseUrl1+"GetShopImage?filename="+post.getShopImage();
        Picasso.with(myContext).load(url).fit().into(holder.postShopImage);


       holder.tvGoAhed1.setTypeface(fontAwesomeFont);
        holder.tvLocSymbol.setTypeface(fontAwesomeFont);

        holder.postShopName.setText(post.getShopName());
        holder.postAddress.setText(post.getShopAddress());
        holder.postDistance.setText(post.getDistance());
        holder.postDeliveryStatus.setText(post.getProvidesDelivery());
        // Listen for ListView Item Click
        vi.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                pref = myContext.getSharedPreferences("mypref", MODE_PRIVATE);
                editor = pref.edit();
                Merchant merchant=datas.get(position);

             //   int mId=merchant.getMid();
             //   editor.putInt("mid",mId);
                editor.putString("shopName",merchant.getShopName());
                editor.apply();
                Intent selectCategoryIntent=new Intent(myContext,ProductListActivity.class);
                selectCategoryIntent.putExtra("mid",mid);
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
        }
        else
        {
            for (Merchant wp : arraylist)
            {
                if (wp.getShopName().toLowerCase(Locale.getDefault()).contains(charText))
                {
                    datas.add(wp);
                }
            }
        }
        this.notifyDataSetChanged();
    }


}
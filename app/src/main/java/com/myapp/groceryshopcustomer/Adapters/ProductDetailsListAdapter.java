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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.myapp.groceryshopcustomer.Activity.ProductDetailsActivity;
import com.myapp.groceryshopcustomer.DBHandler.Product;
import com.myapp.groceryshopcustomer.DBHandler.ProductUnit;
import com.myapp.groceryshopcustomer.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;
import static com.myapp.groceryshopcustomer.Constant.Constants.baseUrl1;


/**
 * Created by pramod on 11/17/16..
 */
public class ProductDetailsListAdapter extends BaseAdapter implements View.OnClickListener {

    Spinner qtySpinner;
    private Activity myContext;
    private ArrayList<Product> datas;
    private ArrayList<Product> arraylist;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    private LayoutInflater v;

    // Product post;

    public ProductDetailsListAdapter(Context context, int textViewResourceId,
                                     ArrayList<Product> objects) {
        // TODO Auto-generated constructor stub
        myContext = (Activity) context;
        this.datas = objects;
        this.arraylist = new ArrayList<Product>();
        this.arraylist.addAll(datas);
        v = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void onClick(View v) {
    }

    static class ViewHolder {
        Product product;

        ProductUnit productUnit;
        Button btnAddSingleToCart, btnIncreaseSingle, btnDecreaseSingle;
        LinearLayout llqty;
        TextView postTitleView;
        TextView txtproductname, txtManufacturar, txtCategory, txtprice, txtmrp, txtquantity, txtsave, txtNext, txtCountQuantity, txtQuantity;
        ImageView imageproduct;

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
        vi = v.inflate(R.layout.product_details_list, null);
        //post = (Product) datas.get(position);
        try {
            if (datas != null) {
                // if (holder.product != null) {
                if (datas.size() >= position) {
                    holder.product = datas.get(position);

                    //   Product ei = (Product) post;

                    Typeface fontAwesomeFont = Typeface.createFromAsset(myContext.getAssets(), "fontawesome-webfont.ttf");

                    holder.imageproduct = (ImageView) vi.findViewById(R.id.img_productimage);
                    holder.txtproductname = (TextView) vi.findViewById(R.id.tv_product_name);
                    holder.txtCategory = (TextView) vi.findViewById(R.id.tv_category);
                    holder.txtManufacturar = (TextView) vi.findViewById(R.id.tv_product_manufacturar);
                    holder.txtprice = (TextView) vi.findViewById(R.id.tv_price);
                    holder.txtmrp = (TextView) vi.findViewById(R.id.tv_mrp);
                    holder.txtquantity = (TextView) vi.findViewById(R.id.tv_quantity);
                    holder.txtsave = (TextView) vi.findViewById(R.id.tv_save);
                    holder.txtNext = (TextView) vi.findViewById(R.id.tvGoAhed1);
                    holder.llqty = (LinearLayout) vi.findViewById(R.id.ll_qty);
                    holder.btnIncreaseSingle = (Button) vi.findViewById(R.id.btn_increase);
                    holder.txtCountQuantity = (TextView) vi.findViewById(R.id.tv_quantityy);
                    holder.btnDecreaseSingle = (Button) vi.findViewById(R.id.btn_decrease);
                    holder.txtQuantity = (TextView) vi.findViewById(R.id.tv_quantity);
                    holder.btnAddSingleToCart = (Button) vi.findViewById(R.id.btn_addtocart);

                    holder.txtquantity.setTag(holder.product);
                    holder.product.setPosition(position);
                    holder.btnAddSingleToCart.setTag(holder.product);
                 //   holder.btnAddSingleToCart.setTag(position);
                    holder.btnIncreaseSingle.setTag(holder.product);
                    holder.btnDecreaseSingle.setTag(holder.product);

                    holder.txtNext.setTypeface(fontAwesomeFont);
                    String url = baseUrl1 + "GetProductImage?filename=" + holder.product.getProductImage();
                    Picasso.with(myContext).load(url).fit().into(holder.imageproduct);

                    holder.txtCategory.setText(holder.product.getProductCategory());
                    holder.txtproductname.setText(holder.product.getProductName());
                    holder.txtManufacturar.setText(holder.product.getProductManufacturar());
                }
            }
        } catch (Exception e) {
            DateFormat dff = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
            String date = dff.format(Calendar.getInstance().getTime());
            Log.d("InputStream", e.getLocalizedMessage());
            e.printStackTrace();
           // appendLog(myContext, "1 ProductDetailsListAdapter " + e.toString() + date);
        }
        try {
            List<ProductUnit> uData = new ArrayList<ProductUnit>();

            uData = holder.product.getProductUnitArr();
            List<String> unitList = new ArrayList<String>();
            double tempprice = 0;
            for (int u = 0; u < uData.size(); u++) {

                if (tempprice == 0) {

                    int qty = uData.get(u).getCart_qty();
                    if (qty >= 1) {
                        holder.btnAddSingleToCart.setVisibility(View.GONE);
                        holder.llqty.setVisibility(View.VISIBLE);
                        holder.txtCountQuantity.setText(String.valueOf(qty));
                    }
                    tempprice = uData.get(u).getPrice();
                    holder.txtprice.setText("Price : ₹" + String.valueOf(uData.get(u).getPrice()));
                    holder.txtmrp.setText("MRP : ₹" + String.valueOf(uData.get(u).getMrp()));
                    holder.txtquantity.setText(uData.get(u).getUnit());
                }
                if (tempprice > uData.get(u).getPrice()) {

                    int qty = uData.get(u).getCart_qty();
                    if (qty >= 1) {
                        holder.btnAddSingleToCart.setVisibility(View.GONE);
                        holder.llqty.setVisibility(View.VISIBLE);
                        holder.txtCountQuantity.setText(String.valueOf(qty));
                    }

                    tempprice = uData.get(u).getPrice();
                    holder.txtprice.setText("Price : ₹" + String.valueOf(uData.get(u).getPrice()));
                    holder.txtmrp.setText("MRP : ₹" + String.valueOf(uData.get(u).getMrp()));
                    holder.txtquantity.setText(uData.get(u).getUnit());
                }

                double save = (uData.get(u).getSave_rs());
                if (save <= 0) {
                    holder.txtsave.setVisibility(View.GONE);
                } else {
                    holder.txtsave.setText("Save : ₹" + String.format("%.02f", save));
                }
            }
        } catch (Exception e) {
            DateFormat dff = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
            String date = dff.format(Calendar.getInstance().getTime());
            Log.d("InputStream", e.getLocalizedMessage());
            e.printStackTrace();
           // appendLog(myContext, "2 ProductDetailsListAdapter " + e.toString() + date);
        }

        vi.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                pref = myContext.getSharedPreferences("mypref", MODE_PRIVATE);
                editor = pref.edit();
                Product product = datas.get(position);
                editor.putInt("productId", product.getProductId());
                editor.commit();
                Intent selectCategoryIntent = new Intent(myContext, ProductDetailsActivity.class);
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
            for (Product wp : arraylist) {
                if (wp.getProductName().toLowerCase(Locale.getDefault()).contains(charText)||wp.getProductCategory().toLowerCase(Locale.getDefault()).contains(charText)) {
                    datas.add(wp);
                }
            }
        }
        this.notifyDataSetChanged();
    }
}
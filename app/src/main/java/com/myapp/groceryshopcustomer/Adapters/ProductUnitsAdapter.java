package com.myapp.groceryshopcustomer.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.myapp.groceryshopcustomer.DBHandler.Customer;
import com.myapp.groceryshopcustomer.DBHandler.DatabaseHandler;
import com.myapp.groceryshopcustomer.DBHandler.ProductUnit;
import com.myapp.groceryshopcustomer.R;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by apple on 8/22/17.
 */

public class ProductUnitsAdapter extends BaseAdapter implements View.OnClickListener {

    private Activity myContext;
    private ArrayList<ProductUnit> datas;
    int custId, productId, unitQty, status;
    String strUnit, result;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    LinearLayout llqty;
    private LayoutInflater v;
    public ProductUnitsAdapter imageItemAdapter;
    ProductUnit post;

    public ProductUnitsAdapter(Context context, int textViewResourceId,
                               ArrayList<ProductUnit> objects) {
        // TODO Auto-generated constructor stub
        myContext = (Activity) context;
        this.datas = objects;

        pref = myContext.getSharedPreferences("mypref", MODE_PRIVATE);
        editor = pref.edit();
        //mid = pref.getInt("mid", 0);
        //catId = pref.getInt("categoryId", 0);
        productId = pref.getInt("productId", 0);
        DatabaseHandler db = new DatabaseHandler(myContext);
        Customer user1 = db.getCustomerDetails();
        custId = user1.getCust_id();
        v = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }


    @Override
    public void onClick(View v) {
    }

    static class ViewHolder {
        ProductUnit productUnit;
        TextView postTitleView;
        Button postButtonAddToCart, btnIncrease, btnDecrease;
        TextView txtQuantity;
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
        post = (ProductUnit) datas.get(position);
        holder.productUnit = datas.get(position);
        ProductUnit ei = (ProductUnit) post;
        vi = v.inflate(R.layout.product_unit_item, null);

        final TextView postProductName = (TextView) vi.findViewById(R.id.tv_unit);
        final TextView postProductPrice = (TextView) vi.findViewById(R.id.tv_price);
        final TextView postProductMRP = (TextView) vi.findViewById(R.id.tv_mrp);
        final TextView postProductSave = (TextView) vi.findViewById(R.id.tv_save);
        holder.postButtonAddToCart = (Button) vi.findViewById(R.id.btn_addtocart);
        holder.btnIncrease = (Button) vi.findViewById(R.id.btn_increase);
        holder.btnDecrease = (Button) vi.findViewById(R.id.btn_decrease);
        holder.txtQuantity = (TextView) vi.findViewById(R.id.tv_quantity);
        llqty = (LinearLayout) vi.findViewById(R.id.ll_qty);

        postProductName.setText(post.getUnit());
        postProductPrice.setText("Price : ₹" + post.getPrice().toString());
        postProductMRP.setText("MRP : ₹" + post.getMrp().toString());

        double save = post.getSave_rs();
        if (save <= 0) {
            postProductSave.setVisibility(View.GONE);
        } else {
            postProductSave.setText("Save : ₹" +  String.format("%.02f", save));
        }
        int qty = post.getQuantity();
        if (qty > 0) {
            holder.postButtonAddToCart.setVisibility(View.GONE);
            vi.findViewById(R.id.ll_qty).setVisibility(View.VISIBLE);
            holder.txtQuantity.setText(String.valueOf(qty));
        } else {
            holder.postButtonAddToCart.setVisibility(View.VISIBLE);
            llqty.setVisibility(View.GONE);
        }

        holder.btnIncrease.setTag(holder.productUnit);
        holder.btnDecrease.setTag(holder.productUnit);
        holder.postButtonAddToCart.setTag(holder.productUnit);


//        holder.postButtonAddToCart.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                post = (ProductUnit) datas.get(position);
//                Toast.makeText(myContext, "Unit =" + post.getUnit() + " Price = " + post.getPrice().toString(), Toast.LENGTH_SHORT).show();
//
//                unitQty = 1;
//                strUnit = post.getUnit();
//                post.setQuantity(unitQty);
//                new JSONAsyncTaskToAddProductInCart().execute();
//
//            }
//        });

        return vi;
    }

}
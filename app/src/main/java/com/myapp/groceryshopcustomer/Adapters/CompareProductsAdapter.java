package com.myapp.groceryshopcustomer.Adapters;


import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.myapp.groceryshopcustomer.DBHandler.Cart;
import com.myapp.groceryshopcustomer.R;

import java.util.ArrayList;

import static com.myapp.groceryshopcustomer.Constant.Constants.baseUrl1;

/**
 * Created by apple on 8/29/17.
 */

public class CompareProductsAdapter extends BaseAdapter implements View.OnClickListener {

    private Activity myContext;
    private ArrayList<Cart> datas;

    private LayoutInflater v;

    Cart post;

    public CompareProductsAdapter(Context context, int textViewResourceId,
                                  ArrayList<Cart> objects) {
        // TODO Auto-generated constructor stub
        myContext = (Activity) context;
        this.datas = objects;
        v = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public void onClick(View v) {
    }

    static class ViewHolder {
        Cart cart;
        TextView txtproductname,txtManufacturar,txtCategory,  txtquantity, txtUnit, txtprice, txtTotalPrice,txtCompPrice, txtCompTotalPrice;
        ImageView imageproduct;
        //Button btnDecrease,btnIncrease;
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
        post = (Cart) datas.get(position);
        holder.cart=datas.get(position);
        vi = v.inflate(R.layout.compare_list_item, null);
        Typeface fontAwesomeFont = Typeface.createFromAsset(myContext.getAssets(), "fontawesome-webfont.ttf");

        holder.imageproduct = (ImageView) vi.findViewById(R.id.img_productimage);
        holder.txtproductname = (TextView) vi.findViewById(R.id.tv_product_name);
        holder.txtCategory = (TextView) vi.findViewById(R.id.tv_category);
        holder.txtManufacturar = (TextView) vi.findViewById(R.id.tv_product_manufacturar);
        holder.txtquantity = (TextView) vi.findViewById(R.id.tv_quantity1);
        holder.txtUnit = (TextView) vi.findViewById(R.id.tv_unit);
        holder.txtprice = (TextView) vi.findViewById(R.id.tv_price);
        holder.txtTotalPrice =(TextView)vi.findViewById(R.id.tv_total_price);
        holder.txtCompPrice = (TextView) vi.findViewById(R.id.tv_price1);
        holder.txtCompTotalPrice =(TextView)vi.findViewById(R.id.tv_total_price1);
       // holder.btnDecrease=(Button)vi.findViewById(R.id.btn_decrease);
       // holder.btnIncrease=(Button)vi.findViewById(R.id.btn_increase);

        // Picasso.with(myContext).load("http://www.iconshock.com/img_jpg/BRILLIANT/shopping/jpg/256/grocery_shop_icon.jpg").fit().into(holder.imageproduct);
        String url=baseUrl1+"GetProductImage?filename="+post.getProductImage();
        Picasso.with(myContext).load(url).fit().into(holder.imageproduct);
        holder.txtCategory.setText(post.getCategoryName());
        holder.txtproductname.setText(post.getProductName());
        holder.txtManufacturar.setText(post.getManufacturer());
        holder.txtquantity.setText((String.valueOf(post.getQty())));
        holder.txtUnit.setText(post.getUnit());
        holder.txtprice.setText(String.valueOf(post.getPrice()));
        holder.txtTotalPrice.setText(String.valueOf(post.getProductTotalPrice()));
        holder.txtCompPrice.setText(String.valueOf(post.getComPrice()));
        holder.txtCompTotalPrice.setText(String.valueOf(post.getCompProductTotalPrice()));
//        holder.btnDecrease=(Button)vi.findViewById(R.id.btn_decrease);
//        holder.btnIncrease=(Button)vi.findViewById(R.id.btn_increase);
//
//        holder.btnIncrease.setTag(holder.cart);
//        holder.btnDecrease.setTag(holder.cart);
        return vi;
    }


}

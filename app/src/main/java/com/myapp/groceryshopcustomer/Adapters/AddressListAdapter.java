package com.myapp.groceryshopcustomer.Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.myapp.groceryshopcustomer.DBHandler.Address;
import com.myapp.groceryshopcustomer.R;

import java.util.ArrayList;

/**
 * Created by apple on 9/7/17.
 */

public class AddressListAdapter extends BaseAdapter implements View.OnClickListener {

    private Activity myContext;
    private ArrayList<Address> datas;
    private ArrayList<Address> arraylist;
    private LayoutInflater v;

    Address post;

    public AddressListAdapter(Context context, int textViewResourceId,
                              ArrayList<Address> objects) {
        // TODO Auto-generated constructor stub
        myContext = (Activity) context;
        this.datas = objects;

        this.arraylist = new ArrayList<Address>();
        this.arraylist.addAll(datas);
        v = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public void onClick(View v) {
    }

    static class ViewHolder {
        Address address;
        TextView tvDeleteAddress, txtName, txtAddress, txtMobileNumber, txtAlternetMobileNumber;
        // RadioButton rbSelectAddress;
        Button btnEdit, btnDeliverHere;
        LinearLayout llButtons;
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
        post = (Address) datas.get(position);
        holder.address = datas.get(position);
        vi = v.inflate(R.layout.address_list_layout, null);
        Typeface fontAwesomeFont = Typeface.createFromAsset(myContext.getAssets(), "fontawesome-webfont.ttf");
        holder.tvDeleteAddress = (TextView) vi.findViewById(R.id.tv_delete_product);

        holder.tvDeleteAddress.setTypeface(fontAwesomeFont);
        holder.txtName = (TextView) vi.findViewById(R.id.tv_address_name);
        holder.txtAddress = (TextView) vi.findViewById(R.id.tv_address);
        holder.txtMobileNumber = (TextView) vi.findViewById(R.id.tv_address_mobile_number);
        holder.txtAlternetMobileNumber = (TextView) vi.findViewById(R.id.tv_address_alternate_mobile_number);
        holder.btnEdit = (Button) vi.findViewById(R.id.btn_address_edit);
        holder.btnDeliverHere = (Button) vi.findViewById(R.id.btn_address_deliver_here);
        // holder.rbSelectAddress=(RadioButton)vi.findViewById(R.id.rb_address_select);
        holder.llButtons = (LinearLayout) vi.findViewById(R.id.layout_buttons);

        holder.txtName.setText(post.getName());
        if (!post.getLandmark().equals("null")) {
            holder.txtAddress.setText(post.getBuilding() + ", " + post.getArea() + ", " + post.getLandmark() + ", " + post.getCity() + ", " + post.getState() + ", " + post.getPincode());

        } else {
            holder.txtAddress.setText(post.getBuilding() + ", " + post.getArea() + post.getCity() + ", " + post.getState() + ", " + post.getPincode());

        }
        holder.txtMobileNumber.setText(post.getPhonenumber());
        if (!post.getAlernatenumber().equals("null")) {
            holder.txtAlternetMobileNumber.setText(post.getAlernatenumber());

        }

        if (post.getStatus().equals("yes")) {
            holder.llButtons.setVisibility(View.VISIBLE);
        } else {
            holder.llButtons.setVisibility(View.GONE);
        }

        holder.tvDeleteAddress.setTag(holder.address);
        holder.btnEdit.setTag(holder.address);
        holder.btnDeliverHere.setTag(holder.address);
        // holder.rbSelectAddress.setTag(holder.address);


//        holder.rbSelectAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//
//                if(holder.rbSelectAddress.isChecked()){
//                    Toast.makeText(myContext,"checked",Toast.LENGTH_SHORT).show();
//                }else {
//                    Toast.makeText(myContext,"unchecked",Toast.LENGTH_SHORT).show();
//                }
//            }
//        });


        vi.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Address address = datas.get(position);

                int addressId = address.getAddressId();
                filter(addressId);
            }
        });
        return vi;
    }

    // Filter Class
    public void filter(int intVal) {

        datas.clear();
        for (Address wp : arraylist) {
            if (wp.getAddressId() == intVal) {
                wp.setStatus("yes");
                datas.add(wp);
            } else {
                wp.setStatus("no");
                datas.add(wp);
            }
        }

        this.notifyDataSetChanged();
    }

}

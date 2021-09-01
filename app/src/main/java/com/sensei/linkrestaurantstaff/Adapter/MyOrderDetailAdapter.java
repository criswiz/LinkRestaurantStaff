package com.sensei.linkrestaurantstaff.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sensei.linkrestaurantstaff.Common.Common;
import com.sensei.linkrestaurantstaff.Interface.IOnRecyclerViewClickListener;
import com.sensei.linkrestaurantstaff.Model.Addon;
import com.sensei.linkrestaurantstaff.Model.Order;
import com.sensei.linkrestaurantstaff.Model.OrderDetail;
import com.sensei.linkrestaurantstaff.OrderDetailActivity;
import com.sensei.linkrestaurantstaff.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyOrderDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    List<OrderDetail> orderDetailList;

    public MyOrderDetailAdapter(Context context, List<OrderDetail> orderDetailList) {
        this.context = context;
        this.orderDetailList = orderDetailList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return viewType == 0? new MyViewHolder(LayoutInflater.from(context)
        .inflate(R.layout.layout_order_detail_item, parent, false)) :
                new MyViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.layout_order_detail_item_addon, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        position = holder.getAdapterPosition();
        if (holder instanceof MyViewHolder){
            MyViewHolder myViewHolder = (MyViewHolder) holder;
            Picasso.get().load(orderDetailList.get(position).getImage()).into(myViewHolder.img_food_image);
            myViewHolder.txt_food_name.setText(orderDetailList.get(position).getName());
            myViewHolder.txt_food_quantity.setText(new StringBuilder("Quantity: ").append(orderDetailList.get(position).getQuantity()));
            myViewHolder.txt_food_size.setText(new StringBuilder("Size: ").append(orderDetailList.get(position).getSize()));

            myViewHolder.setListener(new IOnRecyclerViewClickListener() {
                @Override
                public void onClick(View view, int index) {
                    Common.currentOrder = orderDetailList.get(index);
                    context.startActivity(new Intent(context, OrderDetailActivity.class));
                }
            });
        }else if (holder instanceof MyViewHolderAddon){
            MyViewHolderAddon myViewHolderAddon = (MyViewHolderAddon) holder;
            Picasso.get().load(orderDetailList.get(position).getImage()).into(myViewHolderAddon.img_food_image);
            myViewHolderAddon.txt_food_name.setText(orderDetailList.get(position).getName());
            myViewHolderAddon.txt_food_quantity.setText(new StringBuilder("Quantity: ").append(orderDetailList.get(position).getQuantity()));
            myViewHolderAddon.txt_food_size.setText(new StringBuilder("Size: ").append(orderDetailList.get(position).getSize()));

            List<Addon> addons = new Gson().fromJson(orderDetailList.get(position).getAddOn(),
                    new TypeToken<List<Addon>>(){}.getType());
            StringBuilder add_on_text = new StringBuilder();
            for (Addon addon: addons)
                add_on_text.append(addon.getName()).append("\n");
            myViewHolderAddon.txt_add_on.setText(add_on_text);
        }
    }

    @Override
    public int getItemCount() {
        return orderDetailList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (orderDetailList.get(position).getAddOn().toLowerCase().equals("none") ||
            orderDetailList.get(position).getAddOn().toLowerCase().equals("normal")){
            return 0;
        }else {
            return 1;
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.img_food_image)
        ImageView img_food_image;
        @BindView(R.id.txt_food_name)
        TextView txt_food_name;
        @BindView(R.id.txt_food_quantity)
        TextView txt_food_quantity;
        @BindView(R.id.txt_food_size)
        TextView txt_food_size;


        IOnRecyclerViewClickListener listener;

        public void setListener(IOnRecyclerViewClickListener listener) {
            this.listener = listener;
        }

        Unbinder unbinder;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
        }
    }

    public class MyViewHolderAddon extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.img_food_image)
        ImageView img_food_image;
        @BindView(R.id.txt_food_name)
        TextView txt_food_name;
        @BindView(R.id.txt_food_quantity)
        TextView txt_food_quantity;
        @BindView(R.id.txt_food_size)
        TextView txt_food_size;
        @BindView(R.id.txt_add_on)
        TextView txt_add_on;


        IOnRecyclerViewClickListener listener;

        public void setListener(IOnRecyclerViewClickListener listener) {
            this.listener = listener;
        }

        Unbinder unbinder;
        public MyViewHolderAddon(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onClick(v, getAdapterPosition());
        }
    }
}

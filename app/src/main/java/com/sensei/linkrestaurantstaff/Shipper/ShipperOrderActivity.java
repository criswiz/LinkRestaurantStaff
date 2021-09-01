package com.sensei.linkrestaurantstaff.Shipper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.sensei.linkrestaurantstaff.Adapter.MyOrderAdapter;
import com.sensei.linkrestaurantstaff.Adapter.MyOrderNeedShipAdapter;
import com.sensei.linkrestaurantstaff.Common.Common;
import com.sensei.linkrestaurantstaff.HomeActivity;
import com.sensei.linkrestaurantstaff.Interface.ILoadMore;
import com.sensei.linkrestaurantstaff.Model.MaxOrderModel;
import com.sensei.linkrestaurantstaff.Model.Order;
import com.sensei.linkrestaurantstaff.Model.OrderModel;
import com.sensei.linkrestaurantstaff.Model.ShipperOrder;
import com.sensei.linkrestaurantstaff.Model.ShipperOrderModel;
import com.sensei.linkrestaurantstaff.R;
import com.sensei.linkrestaurantstaff.Retrofit.ILinkRestaurantAPI;
import com.sensei.linkrestaurantstaff.Retrofit.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ShipperOrderActivity extends AppCompatActivity implements ILoadMore {

    ILinkRestaurantAPI iLinkRestaurantAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    android.app.AlertDialog dialog;

    int maxData=0;
    MyOrderNeedShipAdapter orderAdapter;
    List<ShipperOrder> orderList;

    @BindView(R.id.recycler_order_need_ship)
    RecyclerView recycler_order_need_ship;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipper_order);

        init();
        initView();

        getMaxOrder();
    }

    private void getMaxOrder() {
        dialog.show();
        compositeDisposable.add(iLinkRestaurantAPI.getMaxOrderNeedShip(Common.API_KEY,
                Common.currentRestaurantOwner.getRestaurantId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<MaxOrderModel>() {
                    @Override
                    public void accept(MaxOrderModel maxOrderModel) throws Exception {
                        if (maxOrderModel.isSuccess()){
                            maxData = maxOrderModel.getResult().get(0).getMaxRowNum();
                            dialog.dismiss();

                            getAllOrder(1, 10, false);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        dialog.dismiss();
                        Toast.makeText(ShipperOrderActivity.this, "[GET ORDER NEED SHIP]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    private void getAllOrder(int from, int to, boolean isRefresh) {
        dialog.dismiss();
        compositeDisposable.add(iLinkRestaurantAPI.getShippingOrder(Common.API_KEY,
                Common.currentRestaurantOwner.getRestaurantId(),
                from,
                to)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ShipperOrderModel>() {
                    @Override
                    public void accept(ShipperOrderModel shipperOrderModel) throws Exception {
                        if (shipperOrderModel.isSuccess()){
                            if (shipperOrderModel.getResult().size() > 0){
                                if (orderAdapter == null){
                                    orderList = new ArrayList<>();
                                    orderList = (shipperOrderModel.getResult());
                                    orderAdapter = new MyOrderNeedShipAdapter(ShipperOrderActivity.this, orderList, recycler_order_need_ship);
                                    orderAdapter.setILoadMore(ShipperOrderActivity.this);

                                    recycler_order_need_ship.setAdapter(orderAdapter);
                                }else {
                                    if (isRefresh) {
                                        orderList.remove(orderList.size() - 1);
                                        orderList = (shipperOrderModel.getResult());
                                        orderAdapter.addItem(orderList);
                                    }else {
                                        orderList = new ArrayList<>();
                                        orderList = shipperOrderModel.getResult();
                                        orderAdapter = new MyOrderNeedShipAdapter(ShipperOrderActivity.this, orderList, recycler_order_need_ship);
                                        orderAdapter.setILoadMore(ShipperOrderActivity.this);

                                        recycler_order_need_ship.setAdapter(orderAdapter);
                                    }
                                }
                            }
                            dialog.dismiss();
                        }
                    }

                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        dialog.dismiss();
                        Toast.makeText(ShipperOrderActivity.this, "[GET ORDER NEED SHIPPING]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    private void init() {
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        iLinkRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(ILinkRestaurantAPI.class);
    }

    private void initView() {
        ButterKnife.bind(this);

        toolbar.setTitle(getString(R.string.shipper));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_order_need_ship.setLayoutManager(layoutManager);
        recycler_order_need_ship.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLoadMore() {
        if (orderAdapter.getItemCount() < maxData){
            orderList.add(null);
            orderAdapter.notifyItemInserted(orderList.size() - 1);

            getAllOrder(orderAdapter.getItemCount()+1, orderAdapter.getItemCount()+10, false);

            orderAdapter.notifyDataSetChanged();
            orderAdapter.setLoaded();
        }else{
            Toast.makeText(ShipperOrderActivity.this, "Max Data to load", Toast.LENGTH_SHORT).show();
        }
    }
}
package com.sensei.linkrestaurantstaff;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.sensei.linkrestaurantstaff.Adapter.MyOrderAdapter;
import com.sensei.linkrestaurantstaff.Common.Common;
import com.sensei.linkrestaurantstaff.Interface.ILoadMore;
import com.sensei.linkrestaurantstaff.Model.HotFood;
import com.sensei.linkrestaurantstaff.Model.MaxOrderModel;
import com.sensei.linkrestaurantstaff.Model.Order;
import com.sensei.linkrestaurantstaff.Model.OrderModel;
import com.sensei.linkrestaurantstaff.Retrofit.ILinkRestaurantAPI;
import com.sensei.linkrestaurantstaff.Retrofit.RetrofitClient;
import com.sensei.linkrestaurantstaff.Shipper.ShipperOrderActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ILoadMore {

    TextView txt_user_name, txt_user_phone;
    @BindView(R.id.recycler_restaurant)
    RecyclerView recyclerView;

    ILinkRestaurantAPI iLinkRestaurantAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    android.app.AlertDialog dialog;

    int maxData=0;
    MyOrderAdapter orderAdapter;
    List<Order> orderList;
    ILoadMore iLoadMore;

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        init();
        initView();
        subscribeToTopic(Common.getTopicChannel(Common.currentRestaurantOwner.getRestaurantId()));
        getMaxOrder();
    }

    private void subscribeToTopic(String topicChannel) {
        FirebaseMessaging.getInstance()
                .subscribeToTopic(topicChannel)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HomeActivity.this, "Subsribe Failed! You may not receive new or order notification ", Toast.LENGTH_SHORT).show();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){

                }else{
                    Toast.makeText(HomeActivity.this, "Failed: "+task.isSuccessful(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getMaxOrder() {
        dialog.show();
        compositeDisposable.add(iLinkRestaurantAPI.getMaxOrder(Common.API_KEY,
                String.valueOf(Common.currentRestaurantOwner.getRestaurantId()))
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
                        Toast.makeText(HomeActivity.this, "[GET MAX ORDER]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    private void getAllOrder(int from, int to, boolean isRefresh) {
        dialog.dismiss();
        compositeDisposable.add(iLinkRestaurantAPI.getOrder(Common.API_KEY,
                String.valueOf(Common.currentRestaurantOwner.getRestaurantId()),
                from,
                to)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<OrderModel>() {
                    @Override
                    public void accept(OrderModel orderModel) throws Exception {
                        if (orderModel.isSuccess()){
                            if (orderModel.getResult().size() > 0){
                                if (orderAdapter == null){
                                    orderList = new ArrayList<>();
                                    orderList = (orderModel.getResult());
                                    orderAdapter = new MyOrderAdapter(HomeActivity.this, orderList, recyclerView);
                                    orderAdapter.setILoadMore(HomeActivity.this);

                                    recyclerView.setAdapter(orderAdapter);
                                }else {
                                    if (isRefresh) {
                                        orderList.remove(orderList.size() - 1);
                                        orderList = (orderModel.getResult());
                                        orderAdapter.addItem(orderList);
                                    }else {
                                        orderList = new ArrayList<>();
                                        orderList = orderModel.getResult();
                                        orderAdapter = new MyOrderAdapter(HomeActivity.this, orderList, recyclerView);
                                        orderAdapter.setILoadMore(HomeActivity.this);

                                        recyclerView.setAdapter(orderAdapter);
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
                        Toast.makeText(HomeActivity.this, "[GET ORDER]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }));
    }


    private void initView() {
        ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Restaurant Order");
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                drawer,toolbar,R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));


    }

    private void init() {

        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        iLinkRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(ILinkRestaurantAPI.class);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_log_out){
            signOut();
        } else if (id == R.id.nav_hot_food) {
            startActivity(new Intent(HomeActivity.this, HotFoodActivity.class));
        }else if (id == R.id.nav_shipper){
            startActivity(new Intent(HomeActivity.this, ShipperOrderActivity.class));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_refresh){
            getAllOrder(0, 10, true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void signOut() {
        AlertDialog confirmExit = new AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Do you really want to Sign Out")
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        FirebaseAuth.getInstance().signOut();
                        Intent intent  = new Intent(HomeActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                }).create();

        confirmExit.show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }



    //Register Event Bus
    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
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
            Toast.makeText(HomeActivity.this, "Max Data to load", Toast.LENGTH_SHORT).show();
        }
    }

}
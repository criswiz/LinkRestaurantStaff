package com.sensei.linkrestaurantstaff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.sensei.linkrestaurantstaff.Adapter.MyOrderDetailAdapter;
import com.sensei.linkrestaurantstaff.Common.Common;
import com.sensei.linkrestaurantstaff.Interface.IFCMService;
import com.sensei.linkrestaurantstaff.Model.FCMResponse;
import com.sensei.linkrestaurantstaff.Model.FCMSendData;
import com.sensei.linkrestaurantstaff.Model.OrderDetailModel;
import com.sensei.linkrestaurantstaff.Model.ShipperOrderModel;
import com.sensei.linkrestaurantstaff.Model.Status;
import com.sensei.linkrestaurantstaff.Model.TokenModel;
import com.sensei.linkrestaurantstaff.Model.UpdateOrderModel;
import com.sensei.linkrestaurantstaff.Retrofit.ILinkRestaurantAPI;
import com.sensei.linkrestaurantstaff.Retrofit.RetrofitClient;
import com.sensei.linkrestaurantstaff.Retrofit.RetrofitFCMClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class OrderDetailActivity extends AppCompatActivity {

    @BindView(R.id.txt_order_number)
    TextView txt_order_number;
    @BindView(R.id.spinner_status)
    AppCompatSpinner spinner_status;
    @BindView(R.id.recycler_order_detail)
    RecyclerView recycler_order_detail;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    ILinkRestaurantAPI iLinkRestaurantAPI;
    IFCMService ifcmService;
    CompositeDisposable compositeDisposable;
    AlertDialog dialog;

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.order_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            finish();
            return true;
        }else if (id == R.id.action_save){
            updateOrder();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateOrder() {
        int status = Common.convertStringToStatus(spinner_status.getSelectedItem().toString());
        if (status == 1){
            //shipping status
            compositeDisposable.add(iLinkRestaurantAPI.updateOrderModel("dasdd",
                    Common.currentOrder.getOrderId(),
                    Common.convertStringToStatus(spinner_status.getSelectedItem().toString()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<UpdateOrderModel>() {
                        @Override
                        public void accept(UpdateOrderModel updateOrderModel) throws Exception {
                            Common.currentOrder.setOrderStatus(Common.convertStringToStatus(spinner_status.getSelectedItem().toString()));

                            compositeDisposable.add(iLinkRestaurantAPI.setShippingOrder("sdfs",
                                    Common.currentOrder.getOrderId(),
                                    Common.currentRestaurantOwner.getRestaurantId())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<ShipperOrderModel>() {
                                @Override
                                public void accept(ShipperOrderModel shipperOrderModel) throws Exception {
                                    if (shipperOrderModel.isSuccess()){
                                        compositeDisposable.add(iLinkRestaurantAPI.getToken("dsfasd",
                                                Common.currentOrder.getOrderFBID())
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new Consumer<TokenModel>() {
                                                    @Override
                                                    public void accept(TokenModel tokenModel) throws Exception {
                                                        if (tokenModel.isSuccess()){
                                                            Map<String, String> messageSend = new HashMap<>();
                                                            messageSend.put(Common.NOTIFI_TITLE, "Your order has been updated");
                                                            messageSend.put(Common.NOTIFI_CONTENT, new StringBuilder("Your order ")
                                                                    .append(Common.currentOrder.getOrderId())
                                                                    .append(" has been updated to ")
                                                                    .append(Common.convertStatusToString(Common.currentOrder.getOrderStatus())).toString());

                                                            FCMSendData fcmSendData = new FCMSendData(tokenModel.getResult().get(0).getToken(), messageSend);

                                                            compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
                                                                    .subscribeOn(Schedulers.io())
                                                                    .observeOn(AndroidSchedulers.mainThread())
                                                                    .subscribe(new Consumer<FCMResponse>() {
                                                                        @Override
                                                                        public void accept(FCMResponse fcmResponse) throws Exception {
                                                                            Toast.makeText(OrderDetailActivity.this, "Order was Updated", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }, new Consumer<Throwable>() {
                                                                        @Override
                                                                        public void accept(Throwable throwable) throws Exception {
                                                                            Toast.makeText(OrderDetailActivity.this, "Order was updated but can't send notification", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }));

                                                        }
                                                    }
                                                }, new Consumer<Throwable>() {
                                                    @Override
                                                    public void accept(Throwable throwable) throws Exception {
                                                        Toast.makeText(OrderDetailActivity.this, "[GET TOKEN]", Toast.LENGTH_SHORT).show();
                                                    }
                                                }));
                                    }else {
                                        Toast.makeText(OrderDetailActivity.this, "[SET SHIPPER]"+shipperOrderModel.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    Toast.makeText(OrderDetailActivity.this, "[SET SHIPPER]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }));
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            Toast.makeText(OrderDetailActivity.this, "[UPDATE ORDER]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }));
        }else {
            compositeDisposable.add(iLinkRestaurantAPI.updateOrderModel("dasdd",
                    Common.currentOrder.getOrderId(),
                    Common.convertStringToStatus(spinner_status.getSelectedItem().toString()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<UpdateOrderModel>() {
                        @Override
                        public void accept(UpdateOrderModel updateOrderModel) throws Exception {
                            Common.currentOrder.setOrderStatus(Common.convertStringToStatus(spinner_status.getSelectedItem().toString()));

                            compositeDisposable.add(iLinkRestaurantAPI.getToken("dsfasd",
                                    Common.currentOrder.getOrderFBID())
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Consumer<TokenModel>() {
                                        @Override
                                        public void accept(TokenModel tokenModel) throws Exception {
                                            if (tokenModel.isSuccess()){
                                                Map<String, String> messageSend = new HashMap<>();
                                                messageSend.put(Common.NOTIFI_TITLE, "Your order has been updated");
                                                messageSend.put(Common.NOTIFI_CONTENT, new StringBuilder("Your order ")
                                                        .append(Common.currentOrder.getOrderId())
                                                        .append(" has been updated to ")
                                                        .append(Common.convertStatusToString(Common.currentOrder.getOrderStatus())).toString());

                                                FCMSendData fcmSendData = new FCMSendData(tokenModel.getResult().get(0).getToken(), messageSend);

                                                compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
                                                        .subscribeOn(Schedulers.io())
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .subscribe(new Consumer<FCMResponse>() {
                                                            @Override
                                                            public void accept(FCMResponse fcmResponse) throws Exception {
                                                                Toast.makeText(OrderDetailActivity.this, "Order was Updated", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }, new Consumer<Throwable>() {
                                                            @Override
                                                            public void accept(Throwable throwable) throws Exception {
                                                                Toast.makeText(OrderDetailActivity.this, "Order was updated but can't send notification", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }));

                                            }
                                        }
                                    }, new Consumer<Throwable>() {
                                        @Override
                                        public void accept(Throwable throwable) throws Exception {
                                            Toast.makeText(OrderDetailActivity.this, "[GET TOKEN]", Toast.LENGTH_SHORT).show();
                                        }
                                    }));
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            Toast.makeText(OrderDetailActivity.this, "[UPDATE ORDER]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }));
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        init();
        initView();
    }

    private void initView() {
        ButterKnife.bind(this);

        toolbar.setTitle(getString(R.string.order_detail));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_order_detail.setLayoutManager(layoutManager);
        recycler_order_detail.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));

        txt_order_number.setText(new StringBuilder("Order Number: #").append(Common.currentOrder.getOrderId()));

        initStatusSpinner();
        loadOOrderDetail();
    }

    private void loadOOrderDetail() {
        dialog.show();

        compositeDisposable.add(iLinkRestaurantAPI.getOrderDetail(Common.API_KEY,
                Common.currentOrder.getOrderId())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<OrderDetailModel>() {
            @Override
            public void accept(OrderDetailModel orderDetailModel) throws Exception {
                if (orderDetailModel.isSuccess()){
                    if (orderDetailModel.getResult().size() > 0){
                        MyOrderDetailAdapter adapter = new MyOrderDetailAdapter(OrderDetailActivity.this, orderDetailModel.getResult());
                        recycler_order_detail.setAdapter(adapter);
                    }
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                dialog.dismiss();
                Toast.makeText(OrderDetailActivity.this, "[GET ORDER DETAIL]", Toast.LENGTH_SHORT).show();
            }
        }));
    }

    private void initStatusSpinner() {
        List<Status> statusList = new ArrayList<Status>();

        statusList.add(new Status(0, "Placed"));
        statusList.add(new Status(1, "Shipping"));
        //statusList.add(new Status(2, "Shipped"));
        statusList.add(new Status(-1, "Cancelled"));

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item,statusList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_status.setAdapter(adapter);
        spinner_status.setSelection(Common.convertStatusToIndex(Common.currentOrder.getOrderStatus()));
    }

    private void init(){
        ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        iLinkRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(ILinkRestaurantAPI.class);
    }

}
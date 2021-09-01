package com.sensei.linkrestaurantstaff;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sensei.linkrestaurantstaff.Common.Common;
import com.sensei.linkrestaurantstaff.Model.RestaurantOwnerModel;
import com.sensei.linkrestaurantstaff.Model.UpdateRestaurantModel;
import com.sensei.linkrestaurantstaff.Retrofit.ILinkRestaurantAPI;
import com.sensei.linkrestaurantstaff.Retrofit.RetrofitClient;

import butterknife.BindView;
import dmax.dialog.SpotsDialog;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class UpdateInformationActivity extends AppCompatActivity {

    ILinkRestaurantAPI iLinkRestaurantAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    AlertDialog dialog;

    @BindView(R.id.edt_username)
    EditText edt_username;
    @BindView(R.id.btn_update)
    Button btn_update;
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
        setContentView(R.layout.activity_update_information);

        init();
        initView();
    }

    private void initView() {
        toolbar.setTitle(getString(R.string.update_information));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();

                compositeDisposable.add(iLinkRestaurantAPI.updateRestaurantOwner(Common.API_KEY,
                        "",
                        TextUtils.isEmpty(edt_username.getText().toString())?"Unk Name": edt_username.getText().toString(),
                        "")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<UpdateRestaurantModel>() {
                    @Override
                    public void accept(UpdateRestaurantModel updateRestaurantModel) throws Exception {
                        if (updateRestaurantModel.isSuccess()){
                            compositeDisposable.add(iLinkRestaurantAPI.getRestaurantOwner(Common.API_KEY,
                                    "")
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Consumer<RestaurantOwnerModel>() {
                                        @Override
                                        public void accept(RestaurantOwnerModel restaurantOwnerModel) throws Exception {
                                            if (restaurantOwnerModel.isSuccess()){
                                                Common.currentRestaurantOwner = restaurantOwnerModel.getResult().get(0);
                                                if (Common.currentRestaurantOwner.isStatus()){
                                                    startActivity(new Intent(UpdateInformationActivity.this, HomeActivity.class));
                                                    finish();
                                                }else {
                                                    Toast.makeText(UpdateInformationActivity.this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                                                }
                                            }else {
                                                dialog.dismiss();
                                                Toast.makeText(UpdateInformationActivity.this, ""+restaurantOwnerModel.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }, new Consumer<Throwable>() {
                                        @Override
                                        public void accept(Throwable throwable) throws Exception {
                                            dialog.dismiss();
                                            Toast.makeText(UpdateInformationActivity.this, "[GET USER]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }));
                        }else{
                            dialog.dismiss();
                            Toast.makeText(UpdateInformationActivity.this, ""+updateRestaurantModel.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        dialog.dismiss();
                        Toast.makeText(UpdateInformationActivity.this, "[UPDATE RESTAURANT]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }));
            }
        });
    }

    private void init() {
        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(this).build();
        iLinkRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(ILinkRestaurantAPI.class);
    }
}
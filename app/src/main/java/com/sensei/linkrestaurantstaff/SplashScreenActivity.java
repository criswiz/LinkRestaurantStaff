package com.sensei.linkrestaurantstaff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.installations.InstallationTokenResult;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.DexterBuilder;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.sensei.linkrestaurantstaff.Common.Common;
import com.sensei.linkrestaurantstaff.Model.RestaurantOwnerModel;
import com.sensei.linkrestaurantstaff.Model.TokenModel;
import com.sensei.linkrestaurantstaff.Retrofit.ILinkRestaurantAPI;
import com.sensei.linkrestaurantstaff.Retrofit.RetrofitClient;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class SplashScreenActivity extends AppCompatActivity {

    ILinkRestaurantAPI iLinkRestaurantAPI;
    CompositeDisposable compositeDisposable;
    AlertDialog dialog;

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();

        Dexter.withContext(getApplicationContext())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        dialog.show();

                        FirebaseInstallations.getInstance()
                                .getToken(true)
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(SplashScreenActivity.this, "[GET TOKEN]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnCompleteListener(new OnCompleteListener<InstallationTokenResult>() {
                            @Override
                            public void onComplete(@NonNull Task<InstallationTokenResult> task) {
                                if (task.isSuccessful()){



                                    dialog.show();

                                    compositeDisposable.add(iLinkRestaurantAPI.updateTokenToServer(Common.API_KEY,
                                            "dfasefasd",
                                            task.getResult().getToken())
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Consumer<TokenModel>() {
                                        @Override
                                        public void accept(TokenModel tokenModel) throws Exception {
                                            compositeDisposable.add(iLinkRestaurantAPI.getRestaurantOwner(Common.API_KEY,
                                                    "dshaofeiodis")
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(new Consumer<RestaurantOwnerModel>() {
                                                        @Override
                                                        public void accept(RestaurantOwnerModel restaurantOwnerModel) throws Exception {
                                                            if (restaurantOwnerModel.isSuccess()){
                                                                Common.currentRestaurantOwner = restaurantOwnerModel.getResult().get(0);
                                                                if (Common.currentRestaurantOwner.isStatus()){
                                                                    startActivity(new Intent(SplashScreenActivity.this, HomeActivity.class));
                                                                    finish();
                                                                }else {
                                                                    Toast.makeText(SplashScreenActivity.this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                                                                }
                                                            }else{
                                                                //If user is new
                                                                startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                                                                finish();
                                                            }

                                                            dialog.dismiss();
                                                        }
                                                    }, new Consumer<Throwable>() {
                                                        @Override
                                                        public void accept(Throwable throwable) throws Exception {
                                                            Toast.makeText(SplashScreenActivity.this, "[GET USER]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }));
                                        }
                                    }, new Consumer<Throwable>() {
                                        @Override
                                        public void accept(Throwable throwable) throws Exception {
                                            Toast.makeText(SplashScreenActivity.this, "[UPDATE TOKEN]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }));
                                }
                            }
                        });


                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(SplashScreenActivity.this, "You must enable this permission", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                }).check();
    }

    private void init(){
        Paper.init(this);
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        iLinkRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(ILinkRestaurantAPI.class);
    }
}
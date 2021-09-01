package com.sensei.linkrestaurantstaff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.sensei.linkrestaurantstaff.Common.Common;
import com.sensei.linkrestaurantstaff.Model.HotFood;
import com.sensei.linkrestaurantstaff.Model.HotFoodModel;
import com.sensei.linkrestaurantstaff.Retrofit.ILinkRestaurantAPI;
import com.sensei.linkrestaurantstaff.Retrofit.RetrofitClient;
import com.sensei.linkrestaurantstaff.Retrofit.RetrofitFCMClient;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class HotFoodActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.piechart)
    PieChart pieChart;

    CompositeDisposable compositeDisposable = new CompositeDisposable();

    ILinkRestaurantAPI iLinkRestaurantAPI;
    List<PieEntry> entryList;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }

    @Override
    protected void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hot_food);

        init();
        initView();

        loadChart();
    }

    private void loadChart() {
        entryList = new ArrayList<>();
        compositeDisposable.add(iLinkRestaurantAPI.getHotFood("tyvuiu")
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<HotFoodModel>() {
            @Override
            public void accept(HotFoodModel hotFoodModel) throws Exception {
                if (hotFoodModel.isSuccess()){
                    int i = 0;
                    for (HotFood hotFood: hotFoodModel.getResult()){
                        entryList.add(new PieEntry(Float.parseFloat(String.valueOf(hotFood.getPercent())),
                                hotFood.getName()));
                        i++;
                    }
                    PieDataSet dataSet = new PieDataSet(entryList, "Hottest Food");

                    PieData data = new PieData();
                    data.setDataSet(dataSet);
                    data.setValueTextSize(14f);
                    data.setValueFormatter(new PercentFormatter(pieChart));

                    dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

                    pieChart.setData(data);
                    pieChart.animateXY(2000, 2000);
                    pieChart.setUsePercentValues(true);
                    pieChart.getDescription().setEnabled(false);

                    pieChart.invalidate();
                }else{
                    Toast.makeText(HotFoodActivity.this, ""+hotFoodModel.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                Toast.makeText(HotFoodActivity.this, ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }));
    }

    private void init() {
        iLinkRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(ILinkRestaurantAPI.class);
    }

    private void initView() {
        ButterKnife.bind(this);

        toolbar.setTitle("HOT FOOD");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
}
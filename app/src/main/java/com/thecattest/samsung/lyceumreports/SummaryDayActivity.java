package com.thecattest.samsung.lyceumreports;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.thecattest.samsung.lyceumreports.Adapters.SummaryDayAdapter;
import com.thecattest.samsung.lyceumreports.DataServices.SummaryDay.SummaryDay;
import com.thecattest.samsung.lyceumreports.DataServices.SummaryDay.SummaryDayService;
import com.thecattest.samsung.lyceumreports.Managers.DatePickerManager;
import com.thecattest.samsung.lyceumreports.Managers.LoginManager;
import com.thecattest.samsung.lyceumreports.Managers.StatusManager;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SummaryDayActivity extends AppCompatActivity {

    private final static String SUMMARY_DAY = "SUMMARY_DAY";

    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView summaryDayListView;
    private TextView datePickerTrigger;

    private final FragmentManager fragmentManager = getSupportFragmentManager();

    private LoginManager loginManager;
    private StatusManager statusManager;
    private DatePickerManager datePickerManager;

    private SummaryDay summaryDay = new SummaryDay();

    private SummaryDayService summaryDayService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary_day);

        initRetrofit();
        findViews();
        setListeners();
        initManagers();

        swipeRefreshLayout.setEnabled(false);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        datePickerManager.loadFromBundle(savedInstanceState);
        if (statusManager.loadLayoutType(savedInstanceState)) {
            String summaryDayJson = savedInstanceState.getString(SUMMARY_DAY);
            if (summaryDayJson != null && !summaryDayJson.isEmpty()) {
                Gson gson = new Gson();
                summaryDay = gson.fromJson(summaryDayJson, SummaryDay.class);
            }
            updateSummaryDayView();
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (summaryDay != null) {
            Gson gson = new Gson();
            outState.putString(SUMMARY_DAY, gson.toJson(summaryDay));
        }
        datePickerManager.saveToBundle(outState);
        statusManager.saveLayoutType(outState);
    }

    protected void initRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        summaryDayService = retrofit.create(SummaryDayService.class);
    }

    private void findViews() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        summaryDayListView = findViewById(R.id.summaryDayList);
        datePickerTrigger = findViewById(R.id.datePickerTrigger);
    }

    private void setListeners() {
        swipeRefreshLayout.setOnRefreshListener(this::onRefresh);
    }

    private void initManagers() {
        loginManager = new LoginManager(this);
        statusManager = new StatusManager(swipeRefreshLayout, fragmentManager, this::onRetryButtonClick);
        datePickerManager = new DatePickerManager(
                getResources().getString(R.string.select_date_label),
                datePickerTrigger,
                getSupportFragmentManager(),
                this::updateSummaryDay);
    }

    public void onRefresh() {
        updateSummaryDay();
        swipeRefreshLayout.setRefreshing(false);
    }

    public void onRetryButtonClick(View v) {
        updateSummaryDay();
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(SummaryDayActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    private void updateSummaryDay() {
        setLoadingStatus();
        String formattedDate = datePickerManager.getDate();
        Call<SummaryDay> call = summaryDayService.getSummaryDay(loginManager.getCookie(), formattedDate);
        call.enqueue(new DefaultCallback<SummaryDay>(loginManager, swipeRefreshLayout) {
            @Override
            public void onResponse200(Response<SummaryDay> response) {
                summaryDay = response.body();
                updateSummaryDayView();
            }

            @Override
            protected void onResponse401() {}

            @Override
            public void onFailure(Call<SummaryDay> call, Throwable t) {
                Log.d("DayCall", t.toString());
                Toast.makeText(SummaryDayActivity.this, "Error accessing server", Toast.LENGTH_SHORT).show();
                statusManager.setServerErrorLayout();
            }
        });
    }

    private void updateSummaryDayView() {
        statusManager.setMainLayout();
        Log.d("Update", summaryDay.toString());
        updateSummaryDayAdapterData();
        swipeRefreshLayout.setEnabled(!summaryDay.groups.isEmpty());
    }

    private void updateSummaryDayAdapterData() {
        SummaryDayAdapter summaryDayAdapter = new SummaryDayAdapter(this, summaryDay.groups);
        summaryDayListView.setAdapter(summaryDayAdapter);
    }

    private void setLoadingStatus() {
        summaryDay = new SummaryDay();
        statusManager.setLoadingLayout();
    }
}
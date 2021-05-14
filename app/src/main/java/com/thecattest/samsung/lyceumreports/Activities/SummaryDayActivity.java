package com.thecattest.samsung.lyceumreports.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.thecattest.samsung.lyceumreports.Adapters.SummaryDayAdapter;
import com.thecattest.samsung.lyceumreports.DataModels.SummaryDay.SummaryDay;
import com.thecattest.samsung.lyceumreports.DataModels.SummaryDay.SummaryDayService;
import com.thecattest.samsung.lyceumreports.DefaultCallback;
import com.thecattest.samsung.lyceumreports.Managers.DatePickerManager;
import com.thecattest.samsung.lyceumreports.Managers.LoginManager;
import com.thecattest.samsung.lyceumreports.Managers.StatusManager;
import com.thecattest.samsung.lyceumreports.R;
import com.thecattest.samsung.lyceumreports.URLConfig;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SummaryDayActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView summaryDayListView;
    private TextView datePickerTrigger;

    private LoginManager loginManager;
    private StatusManager statusManager;
    private DatePickerManager datePickerManager;

    private SummaryDay summaryDay = new SummaryDay();

    private SummaryDayService summaryDayService;
    private Call<SummaryDay> getCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary_day);

        initRetrofit();
        findViews();
        setListeners();
        initManagers();

        updateSummaryDayView();
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        datePickerManager.loadFromBundle(savedInstanceState);
        if (statusManager.loadFromBundle(savedInstanceState)) {
            summaryDay.loadFromBundle(savedInstanceState);
            updateSummaryDayView();
        } else {
            updateSwipeRefreshLayout();
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        summaryDay.saveToBundle(outState);
        datePickerManager.saveToBundle(outState);
        statusManager.saveToBundle(outState);
    }

    private void initRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URLConfig.BASE_URL)
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
        FragmentManager fragmentManager = getSupportFragmentManager();
        loginManager = new LoginManager(this);
        statusManager = new StatusManager(this, swipeRefreshLayout, v -> {updateSummaryDay();});
        datePickerManager = new DatePickerManager(
                this,
                datePickerTrigger,
                fragmentManager,
                this::updateSummaryDay);
    }

    public void onRefresh() {
        updateSummaryDay();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onBackPressed() {
        if (!cancelGetCall()) {
            Intent i = new Intent(SummaryDayActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        }
    }

    private void updateSummaryDay() {
        setLoadingStatus();
        datePickerManager.setEnabled(false);

        String formattedDate = datePickerManager.getDate();

        Call<SummaryDay> call = summaryDayService.getSummaryDay(loginManager.getCookie(), formattedDate);
        getCall = call;
        call.enqueue(new DefaultCallback<SummaryDay>(this, loginManager, swipeRefreshLayout) {
            @Override
            public void onResponse200(Response<SummaryDay> response) {
                summaryDay = response.body();
                updateSummaryDayView();
            }

            @Override
            public void onResponse500(Response<SummaryDay> response) {
                super.onResponse500(response);
                statusManager.setServerErrorLayout();
            }

            public void onResponseFailure(Call<SummaryDay> call, Throwable t) {
                if (call.isCanceled()) {
                    statusManager.setMainLayout();
                    Snackbar.make(
                            swipeRefreshLayout,
                            R.string.snackbar_request_cancelled,
                            Snackbar.LENGTH_LONG
                    ).show();
                } else {
                    Snackbar.make(
                            swipeRefreshLayout,
                            R.string.snackbar_server_error,
                            Snackbar.LENGTH_LONG
                    ).show();
                    statusManager.setServerErrorLayout();
                }
            }

            @Override
            public void onPostExecute() {
                getCall = null;
                datePickerManager.setEnabled(true);
            }
        });
    }

    private boolean cancelGetCall() {
        if (getCall == null)
            return false;
        getCall.cancel();
        getCall = null;
        return true;
    }

    private void updateSummaryDayView() {
        statusManager.setMainLayout();
        updateSummaryDayAdapterData();
        updateSwipeRefreshLayout();
    }

    private void updateSwipeRefreshLayout() {
        swipeRefreshLayout.setEnabled(!datePickerManager.isEmpty());
    }

    private void updateSummaryDayAdapterData() {
        SummaryDayAdapter summaryDayAdapter = new SummaryDayAdapter(this, summaryDay.groups);
        summaryDayListView.setAdapter(summaryDayAdapter);
    }

    private void setLoadingStatus() {
        summaryDay = new SummaryDay();
        updateSummaryDayView();
        statusManager.setLoadingLayout();
    }
}
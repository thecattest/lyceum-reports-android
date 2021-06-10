package com.thecattest.samsung.lyceumreports.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.thecattest.samsung.lyceumreports.Adapters.SummaryDayAdapter;
import com.thecattest.samsung.lyceumreports.Data.ApiService;
import com.thecattest.samsung.lyceumreports.Data.AppDatabase;
import com.thecattest.samsung.lyceumreports.Data.Models.Relations.DayWithAbsentAndGroup;
import com.thecattest.samsung.lyceumreports.Data.Repositories.DayRepository;
import com.thecattest.samsung.lyceumreports.Data.Repositories.GroupRepository;
import com.thecattest.samsung.lyceumreports.Managers.DatePickerManager;
import com.thecattest.samsung.lyceumreports.Managers.LoginManager;
import com.thecattest.samsung.lyceumreports.Managers.RetrofitManager;
import com.thecattest.samsung.lyceumreports.Managers.StatusManager;
import com.thecattest.samsung.lyceumreports.R;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import retrofit2.Retrofit;

public class SummaryDayActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView summaryDayListView;
    private TextView datePickerTrigger;

    private LoginManager loginManager;
    private StatusManager statusManager;
    private DatePickerManager datePickerManager;

    private GroupRepository groupRepository;
    private DayRepository dayRepository;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary_day);

        findViews();
        setListeners();
        initManagers();
        initRetrofit();
        initRepositories();
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
        statusManager = new StatusManager(this, swipeRefreshLayout);
        datePickerManager = new DatePickerManager(
                this,
                datePickerTrigger,
                fragmentManager,
                () -> loadData(true));
    }

    private void initRetrofit() {
        Retrofit retrofit = RetrofitManager.getInstance(loginManager);
        apiService = retrofit.create(ApiService.class);
    }

    private void initRepositories() {
        groupRepository = new GroupRepository(this, loginManager, swipeRefreshLayout, apiService);
        dayRepository = groupRepository.dayRepository;
    }

    public void onRefresh() { refreshData(); }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(SummaryDayActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    private void refreshData() {
        statusManager.setLoadingLayout();
        datePickerManager.setEnabled(false);

        String formattedDate = datePickerManager.getDate();
        groupRepository.refreshDaySummary(
                () -> {
                    statusManager.setMainLayout();
                    datePickerManager.setEnabled(true);
                    swipeRefreshLayout.setRefreshing(false);
                    loadData(false);
                }, () -> {}, formattedDate
        );
    }

    @SuppressLint("CheckResult")
    private void loadData(boolean firstTime) {
        dayRepository.getByDate(datePickerManager.getDate())
                .subscribeOn(AppDatabase.scheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        days -> {
                            if (!days.isEmpty())
                                updateView(new ArrayList<>(days));
                            else if (firstTime)
                                refreshData();
                            else
                                updateView(new ArrayList<>());
                        });
    }

    private void updateView(ArrayList<DayWithAbsentAndGroup> days) {
        statusManager.setMainLayout();
        swipeRefreshLayout.setEnabled(!datePickerManager.isEmpty());

        SummaryDayAdapter summaryDayAdapter = new SummaryDayAdapter(this, days);
        summaryDayListView.setAdapter(summaryDayAdapter);

        if (days.isEmpty())
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.dialog_title_no_data)
                    .setMessage(R.string.dialog_text_no_data)
                    .setPositiveButton(R.string.button_ok, ((dialog, which) -> dialog.dismiss()))
                    .show();
    }
}
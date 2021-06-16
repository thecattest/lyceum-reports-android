package com.thecattest.samsung.lyceumreports.Fragments;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
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
import com.thecattest.samsung.lyceumreports.R;
import com.thecattest.samsung.lyceumreports.Services.SyncService;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import retrofit2.Retrofit;

public class TableFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar loadingProgressBar;
    private ListView summaryDayListView;
    private TextView datePickerTrigger;

    private LoginManager loginManager;
    private DatePickerManager datePickerManager;

    private GroupRepository groupRepository;
    private DayRepository dayRepository;
    private ApiService apiService;

    public TableFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_table, container, false);
        findViews(view);
        setListeners();
        initManagers();
        initRetrofit();
        initRepositories();

        swipeRefreshLayout.setEnabled(false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("Updates", "broadcast got");
                loadData(false);
            }
        }, new IntentFilter(SyncService.REDRAW_BROADCAST));
    }

    private void findViews(View view) {
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        summaryDayListView = view.findViewById(R.id.summaryDayList);
        datePickerTrigger = view.findViewById(R.id.datePickerTrigger);
    }

    private void setListeners() {
        swipeRefreshLayout.setOnRefreshListener(this::onRefresh);
    }

    private void initManagers() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        loginManager = new LoginManager(getContext());
        datePickerManager = new DatePickerManager(
                getContext(),
                datePickerTrigger,
                fragmentManager,
                () -> loadData(true));
    }

    private void initRetrofit() {
        Retrofit retrofit = RetrofitManager.getInstance(loginManager);
        apiService = retrofit.create(ApiService.class);
    }

    private void initRepositories() {
        groupRepository = new GroupRepository(getContext(), loginManager, swipeRefreshLayout, apiService);
        dayRepository = groupRepository.dayRepository;
    }

    public void onRefresh() { refreshData(); }

    private void refreshData() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        datePickerManager.setEnabled(false);

        String formattedDate = datePickerManager.getDate();
        groupRepository.refreshDaySummary(
                () -> {
                    loadingProgressBar.setVisibility(View.GONE);
                    datePickerManager.setEnabled(true);
                    swipeRefreshLayout.setRefreshing(false);
                    loadData(false);
                }, () -> {}, formattedDate
        );
    }

    @SuppressLint("CheckResult")
    private void loadData(boolean firstTime) {
        if (datePickerManager.isEmpty())
            return;
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
        loadingProgressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setEnabled(!datePickerManager.isEmpty());

        SummaryDayAdapter summaryDayAdapter = new SummaryDayAdapter(getContext(), days);
        summaryDayListView.setAdapter(summaryDayAdapter);

        if (days.isEmpty())
            new MaterialAlertDialogBuilder(getContext())
                    .setTitle(R.string.dialog_title_no_data)
                    .setMessage(R.string.dialog_text_no_data)
                    .setPositiveButton(R.string.button_ok, ((dialog, which) -> dialog.dismiss()))
                    .show();
    }
}
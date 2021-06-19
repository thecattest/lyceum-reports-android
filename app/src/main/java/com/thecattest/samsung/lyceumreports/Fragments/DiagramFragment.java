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
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.thecattest.samsung.lyceumreports.Data.ApiService;
import com.thecattest.samsung.lyceumreports.Data.AppDatabase;
import com.thecattest.samsung.lyceumreports.Data.Models.Group;
import com.thecattest.samsung.lyceumreports.Data.Models.Relations.DayWithAbsent;
import com.thecattest.samsung.lyceumreports.Data.Repositories.GroupRepository;
import com.thecattest.samsung.lyceumreports.Managers.LoginManager;
import com.thecattest.samsung.lyceumreports.Managers.RetrofitManager;
import com.thecattest.samsung.lyceumreports.R;
import com.thecattest.samsung.lyceumreports.Services.SyncService;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import retrofit2.Retrofit;

public class DiagramFragment extends Fragment {

    private HorizontalBarChart chart;
    private ConstraintLayout noChart;
    private Button classPickerButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar loadingProgressBar;

    private int[] groupIds;
    private String[] groupLabels;
    private Integer groupId;

    private GroupRepository groupRepository;

    public DiagramFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diagram, container, false);

        findViews(view);
        setListeners();
        initRepositories();

        swipeRefreshLayout.setEnabled(false);
        classPickerButton.setEnabled(false);
        loadGroupsLabelsAndIds();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("Updates", "broadcast got");
                loadGroup(false);
            }
        }, new IntentFilter(SyncService.REDRAW_BROADCAST));
    }

    private void findViews(View view) {
        chart = view.findViewById(R.id.barChart);
        noChart = view.findViewById(R.id.chartPlaceholder);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);
        classPickerButton = view.findViewById(R.id.classPickerTrigger);
    }

    private void setListeners() {
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);
        classPickerButton.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(getContext())
                    .setTitle(R.string.dialog_title_choose_group)
                    .setItems(groupLabels, ((dialog, which) -> {
                        String label = groupLabels[which];
                        groupId = groupIds[which];
                        classPickerButton.setText(label);
                        swipeRefreshLayout.setEnabled(true);
                        loadGroup(true);
                    }))
                    .show();
        });
    }

    private void initRepositories() {
        LoginManager loginManager = new LoginManager(getContext());
        Retrofit retrofit = RetrofitManager.getInstance(loginManager);
        ApiService apiService = retrofit.create(ApiService.class);
        groupRepository = new GroupRepository(getContext(), loginManager, swipeRefreshLayout, apiService);
    }

    @SuppressLint("CheckResult")
    private void loadGroupsLabelsAndIds() {
        groupRepository.get()
                .subscribeOn(AppDatabase.scheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(groups -> {
                    groupLabels = new String[groups.size()];
                    groupIds = new int[groups.size()];
                     for (int i = 0; i < groups.size(); i++) {
                         Group group = groups.get(i).group;
                         groupIds[i] = group.gid;
                         groupLabels[i] = group.getLabel();
                     }
                     classPickerButton.setEnabled(true);
                });
    }

    private void refreshData() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setRefreshing(true);
        groupRepository.refreshGroupSummary(
                () -> {
                    loadingProgressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                },
                () -> loadGroup(false), groupId
        );
    }

    @SuppressLint("CheckResult")
    private void loadGroup(boolean firstTime) {
        groupRepository.dayRepository.getSummary(groupId)
                .subscribeOn(AppDatabase.scheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(days -> {
                    if (days.size() == 0 && firstTime)
                        refreshData();
                    else
                        updateDiagram(new ArrayList<>(days));
                });
    }

    private void updateDiagram(ArrayList<DayWithAbsent> days) {
        ArrayList<BarEntry> absentCount = new ArrayList<>();
        ArrayList<String> absentDate = new ArrayList<>();
        for (int i = 0; i < days.size(); i++) {
            DayWithAbsent day = days.get(i);
            absentCount.add(new BarEntry(day.absent.size(), i));
            try {
                absentDate.add(Group.getHumanDate(getContext(), day.day.date));
            } catch (NullPointerException ignored) {
                return;
            }
        }
        BarDataSet dataSet = new BarDataSet(absentCount, "Отсутствующие");
        dataSet.setColors(ColorTemplate.LIBERTY_COLORS);
        BarData barData = new BarData(absentDate, dataSet);
        barData.setValueFormatter((v, entry, i, viewPortHandler) -> String.valueOf(Math.round(v)));
        barData.setValueTextSize(12);
        barData.setHighlightEnabled(false);

        chart.getXAxis().setTextSize(13);
        chart.getAxisRight().setTextSize(10);
        chart.getAxisLeft().setTextSize(10);
        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisLeft().setAxisMinValue(0);
        chart.getAxisRight().setAxisMinValue(0);
        chart.setDescription("");
        chart.setPinchZoom(true);
        chart.setDoubleTapToZoomEnabled(false);

        noChart.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.VISIBLE);
        chart.setData(barData);
        chart.invalidate();
    }
}
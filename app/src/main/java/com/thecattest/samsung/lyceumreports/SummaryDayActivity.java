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

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.gson.Gson;
import com.thecattest.samsung.lyceumreports.Adapters.SummaryDayAdapter;
import com.thecattest.samsung.lyceumreports.DataServices.Day.Day;
import com.thecattest.samsung.lyceumreports.DataServices.SummaryDay.SummaryDay;
import com.thecattest.samsung.lyceumreports.DataServices.SummaryDay.SummaryDayService;
import com.thecattest.samsung.lyceumreports.Fragments.ServerErrorFragment;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SummaryDayActivity extends AppCompatActivity {

    private final static String SUMMARY_DAY = "SUMMARY_DAY";
    private final static String CURRENT_SELECTION = "CURRENT_SELECTION";
    private final static String DATE_PICKER_TRIGGER_TEXT = "DATE_PICKER_TRIGGER_TEXT";

    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView summaryDayListView;
    private TextView datePickerTrigger;

    private MaterialDatePicker<Long> datePicker;

    private final FragmentManager fragmentManager = getSupportFragmentManager();

    private LoginManager loginManager;
    private StatusManager statusManager;

    private SummaryDay summaryDay = new SummaryDay();
    private Long currentSelection;

    private SummaryDayService summaryDayService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary_day);

        initRetrofit();
        findViews();
        initDatePicker();
        setListeners();

        loginManager = new LoginManager(this);
        statusManager = new StatusManager(swipeRefreshLayout, fragmentManager, this::onRetryButtonClick);

        swipeRefreshLayout.setEnabled(false);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        long selection = savedInstanceState.getLong(CURRENT_SELECTION);
        String datePickerText = savedInstanceState.getString(DATE_PICKER_TRIGGER_TEXT);

        currentSelection = selection;
        if (datePickerText != null && !datePickerText.isEmpty())
            datePickerTrigger.setText(datePickerText);
        if (statusManager.loadLayoutType(savedInstanceState)) {
            String summaryDayJson = savedInstanceState.getString(SUMMARY_DAY);
            if (summaryDayJson != null && !summaryDayJson.isEmpty()) {
                Gson gson = new Gson();
                summaryDay = gson.fromJson(summaryDayJson, SummaryDay.class);
                swipeRefreshLayout.setEnabled(summaryDay.groups.size() != 0);
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
        if (currentSelection != null)
            outState.putLong(CURRENT_SELECTION, currentSelection);
        outState.putString(DATE_PICKER_TRIGGER_TEXT, (String) datePickerTrigger.getText());
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

    protected void initDatePicker() {
        datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getResources().getString(R.string.select_date_label))
                .build();
        datePickerTrigger.setOnClickListener(v -> datePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER"));
    }

    private void setListeners() {
        datePicker.addOnPositiveButtonClickListener(this::onPositiveDatePickerButtonClick);
        swipeRefreshLayout.setOnRefreshListener(this::onRefresh);
    }

    public void onPositiveDatePickerButtonClick(Long selection) {
        datePickerTrigger.setText(datePicker.getHeaderText());
        currentSelection = selection;
        updateSummaryDay();
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
        String formattedDate = DayActivity.formatDate(currentSelection);
        Call<SummaryDay> call = summaryDayService.getSummaryDay(loginManager.getCookies(), formattedDate);
        call.enqueue(new DefaultCallback<SummaryDay>(loginManager, swipeRefreshLayout) {
            @Override
            public void onResponse200(Response<SummaryDay> response) {
                summaryDay = response.body();
                updateSummaryDayView();
                swipeRefreshLayout.setEnabled(true);
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
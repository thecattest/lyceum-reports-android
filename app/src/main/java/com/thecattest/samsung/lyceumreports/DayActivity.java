package com.thecattest.samsung.lyceumreports;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.fragment.app.FragmentManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.thecattest.samsung.lyceumreports.Adapters.StudentsAdapter;
import com.thecattest.samsung.lyceumreports.DataServices.Day.Day;
import com.thecattest.samsung.lyceumreports.DataServices.Day.DayPost;
import com.thecattest.samsung.lyceumreports.DataServices.Day.DayService;
import com.thecattest.samsung.lyceumreports.DataServices.Day.Student;
import com.thecattest.samsung.lyceumreports.Managers.DatePickerManager;
import com.thecattest.samsung.lyceumreports.Managers.LoginManager;
import com.thecattest.samsung.lyceumreports.Managers.StatusManager;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DayActivity extends AppCompatActivity {

    public static final String GROUP_ID = "GROUP_ID";
    public static final String GROUP_LABEL = "GROUP_LABEL";

    private TextView classLabel;
    private ListView studentsListView;
    private Button confirmButton;
    private Button cancelButton;
    private Button datePickerTrigger;
    private RelativeLayout buttonsGroup;
    private RelativeLayout mainLayout;
    private SwipeRefreshLayout swipeRefreshLayout;

    private LoginManager loginManager;
    private StatusManager statusManager;
    private DatePickerManager datePickerManager;

    private Day currentDay = new Day();
    private StudentsAdapter studentsAdapter;

    private DayService dayService;

    private int groupId;
    private String defaultGroupLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day);

        initRetrofit();
        findViews();
        setListeners();
        initManagers();

        groupId = getIntent().getIntExtra(GROUP_ID, 6);
        defaultGroupLabel = getIntent().getStringExtra(GROUP_LABEL);
        currentDay.name = defaultGroupLabel;
        updateDayView();
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        datePickerManager.loadFromBundle(savedInstanceState);
        if (statusManager.loadFromBundle(savedInstanceState)) {
            currentDay.loadFromBundle(savedInstanceState);
            updateDayView();
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        currentDay.saveToBundle(outState);
        datePickerManager.saveToBundle(outState);
        statusManager.saveToBundle(outState);
    }

    private void findViews() {
        classLabel = findViewById(R.id.classLabel);
        studentsListView = findViewById(R.id.studentsList);
        confirmButton = findViewById(R.id.confirmButton);
        cancelButton = findViewById(R.id.cancelButton);
        datePickerTrigger = findViewById(R.id.datePickerTrigger);
        buttonsGroup = findViewById(R.id.buttonsGroup);

        mainLayout = findViewById(R.id.main);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
    }

    private void initRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        dayService = retrofit.create(DayService.class);
    }

    private void setListeners() {
        studentsListView.setOnItemClickListener(this::onStudentItemClick);
        confirmButton.setOnClickListener(this::onConfirmButtonClick);
        cancelButton.setOnClickListener(this::onCancelButtonClick);
        swipeRefreshLayout.setOnRefreshListener(this::onRefresh);
    }

    private void initManagers() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        loginManager = new LoginManager(this);
        statusManager = new StatusManager(mainLayout, fragmentManager, v -> {updateDay();});
        datePickerManager = new DatePickerManager(
                getResources().getString(R.string.select_date_label),
                datePickerTrigger,
                fragmentManager,
                this::updateDay);
    }

    // Students list item click
    public void onStudentItemClick(AdapterView<?> parent, View view, int position, long id) {
        Student student = (Student)parent.getItemAtPosition(position);
        student.absent = !student.absent;
        studentsAdapter.notifyDataSetChanged();
        updateConfirmButton();
    }

    // Refresh action
    public void onRefresh() {
        if (!currentDay.isEmpty())
            updateDay();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(DayActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    // Cancel button click
    public void onCancelButtonClick(View v) {
        onBackPressed();
    }

    // Confirm button click
    public void onConfirmButtonClick(View v) {
        setLoadingStatus(true);
        String formattedDate = datePickerManager.getDate();
        String absentStudentsIdsString = currentDay.getAbsentStudentsIdsString();
        Call<Void> call = dayService.updateDay(loginManager.getCookie(), groupId, new DayPost(formattedDate, absentStudentsIdsString));
        call.enqueue(new DefaultCallback<Void>(loginManager, mainLayout) {
            @Override
            public void onResponse200(Response<Void> response) {
                Snackbar.make(
                        mainLayout,
                        "Сработало :) код " + response.code(),
                        Snackbar.LENGTH_SHORT
                ).setAnchorView(buttonsGroup).show();
                updateDay();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.d("UpdateDayCall", call.toString());
                Snackbar.make(
                        mainLayout,
                        "Ошибка, попробуйте ещё раз позднее",
                        Snackbar.LENGTH_LONG
                ).setAnchorView(buttonsGroup).show();
                statusManager.setMainLayout();
            }

            @Override
            public void onResponse401() {}
        });
    }

    private void updateDay() {
        setLoadingStatus();
        String formattedDate = datePickerManager.getDate();
        Call<Day> call = dayService.getDay(loginManager.getCookie(), groupId, formattedDate);
        call.enqueue(new DefaultCallback<Day>(loginManager, mainLayout) {
            @Override
            public void onResponse200(Response<Day> response) {
                currentDay = response.body();
                currentDay.updateLoadedAbsent();
                updateDayView();
            }

            @Override
            public void onFailure(Call<Day> call, Throwable t) {
                Log.d("DayCall", t.toString());
                Toast.makeText(DayActivity.this, "Error accessing server", Toast.LENGTH_SHORT).show();
                statusManager.setServerErrorLayout();
            }

            @Override
            public void onResponse401() {}
        });
    }

    private void updateDayView() {
        statusManager.setMainLayout();
        classLabel.setText(currentDay.name);
        updateStudentsAdapterData();
        updateConfirmButton();
        if (currentDay.noInfo()) {
            Snackbar.make(
                    mainLayout,
                    getResources().getString(R.string.no_info_for_day),
                    Snackbar.LENGTH_LONG
            ).setAnchorView(buttonsGroup).show();
        } else if (currentDay.noAbsent()) {
            Snackbar.make(
                    mainLayout,
                    getResources().getString(R.string.no_absent),
                    Snackbar.LENGTH_LONG
            ).setAnchorView(buttonsGroup).show();
        }
        buttonsGroup.setVisibility(currentDay.isEmpty() ? View.GONE : View.VISIBLE);
        confirmButton.setVisibility(currentDay.canEdit ? View.VISIBLE : View.GONE);
        swipeRefreshLayout.setEnabled(!currentDay.isEmpty());
    }

    private void updateStudentsAdapterData() {
        studentsAdapter = new StudentsAdapter(this, currentDay.students);
        studentsListView.setAdapter(studentsAdapter);
    }

    private void updateConfirmButton() {
        confirmButton.setEnabled(!currentDay.noChanges() || currentDay.noInfo());
        if (currentDay.noAbsent() || currentDay.noInfo() && currentDay.getAbsentStudentsIds().size() == 0)
            confirmButton.setText(getResources().getString(R.string.confirm_button_no_one_absent));
        else
            confirmButton.setText(getResources().getString(R.string.confirm_button_default));
    }

    private void setLoadingStatus(boolean mainIsVisible) {
        if (!mainIsVisible) {
            currentDay = new Day();
            currentDay.name = defaultGroupLabel;
            updateDayView();
        }
        statusManager.setLoadingLayout(mainIsVisible);
    }

    private void setLoadingStatus() {
        setLoadingStatus(false);
    }
}
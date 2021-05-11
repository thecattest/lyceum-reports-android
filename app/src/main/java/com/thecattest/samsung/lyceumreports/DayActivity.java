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

import com.google.android.material.snackbar.Snackbar;
import com.thecattest.samsung.lyceumreports.Adapters.StudentsAdapter;
import com.thecattest.samsung.lyceumreports.DataServices.Day.Day;
import com.thecattest.samsung.lyceumreports.DataServices.Day.DayPost;
import com.thecattest.samsung.lyceumreports.DataServices.Day.DayService;
import com.thecattest.samsung.lyceumreports.DataServices.Day.Student;
import com.thecattest.samsung.lyceumreports.DataServices.Summary.SummaryWithPermissions;
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
    private Call<Void> updateCall = null;
    private Call<Day> getCall = null;

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
        } else {
            updateSwipeRefreshLayout();
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
//        cancelGetCall();
//        cancelUpdateCall();
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
        updateDay();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onBackPressed() {
        if (!cancelGetCall() && !cancelUpdateCall()) {
            Intent i = new Intent(DayActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        }
    }

    // Cancel button click
    public void onCancelButtonClick(View v) {
        onBackPressed();
    }

    // Confirm button click
    public void onConfirmButtonClick(View v) {
        v.setEnabled(false);
        datePickerManager.setEnabled(false);
        setLoadingStatus(true);

        String formattedDate = datePickerManager.getDate();
        String absentStudentsIdsString = currentDay.getAbsentStudentsIdsString();
        Call<Void> call = dayService.updateDay(loginManager.getCookie(), groupId, new DayPost(formattedDate, absentStudentsIdsString));
        updateCall = call;
        call.enqueue(new DefaultCallback<Void>(loginManager, mainLayout) {
            @Override
            public void onResponse200(Response<Void> response) {
                Snackbar.make(
                        mainLayout,
                        "Сработало :) код " + response.code(),
                        Snackbar.LENGTH_SHORT
                ).setAnchorView(buttonsGroup).show();
                updateDay();
                Log.d("Update", "Updated");
            }

            public void onResponseFailure(Call<Void> call, Throwable t) {
                if (call.isCanceled()) {
                    statusManager.setMainLayout();
                    Snackbar.make(
                            mainLayout,
                            "Отмена",
                            Snackbar.LENGTH_LONG
                    ).setAnchorView(buttonsGroup).show();
                } else {
                    Log.d("UpdateDayCall", call.toString());
                    Snackbar.make(
                            mainLayout,
                            "Ошибка, попробуйте ещё раз позднее",
                            Snackbar.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onPostExecute() {
                v.setEnabled(true);
                datePickerManager.setEnabled(true);
                updateCall = null;
            }
        });
    }

    private boolean cancelUpdateCall() {
        if (updateCall == null)
            return false;
        updateCall.cancel();
        updateCall = null;
        String formattedDate = datePickerManager.getDate();
        String absentStudentsIdsString = currentDay.getLoadedAbsentStudentsIdsString();
        Call<Void> call = dayService.updateDay(loginManager.getCookie(), groupId, new DayPost(formattedDate, absentStudentsIdsString));
        call.enqueue(new DefaultCallback<Void>(loginManager, mainLayout) {
            @Override
            public void onResponse200(Response<Void> response) {}

            @Override
            public void onResponseFailure(Call<Void> call, Throwable t) {}
        });
        return true;
    }

    private void updateDay() {
        setLoadingStatus();
        datePickerManager.setEnabled(false);
        String formattedDate = datePickerManager.getDate();
        Call<Day> call = dayService.getDay(loginManager.getCookie(), groupId, formattedDate);
        getCall = call;
        call.enqueue(new DefaultCallback<Day>(loginManager, mainLayout) {
            @Override
            public void onResponse200(Response<Day> response) {
                currentDay = response.body();
                currentDay.updateLoadedAbsent();
                updateDayView();
            }

            @Override
            public void onResponse500(Response<Day> response) {
                statusManager.setServerErrorLayout();
                Snackbar.make(
                        mainLayout,
                        "Сервер выдал ошибку 500, попробуйте позднее",
                        Snackbar.LENGTH_LONG
                ).show();
            }

            public void onResponseFailure(Call<Day> call, Throwable t) {
                if (call.isCanceled()) {
                    statusManager.setMainLayout();
                    Snackbar.make(
                            mainLayout,
                            "Отмена",
                            Snackbar.LENGTH_LONG
                    ).show();
                } else {
                    statusManager.setServerErrorLayout();
                    Log.d("DayCall", t.toString());
                    Snackbar.make(
                            mainLayout,
                            "Ошибка, попробуйте ещё раз позднее",
                            Snackbar.LENGTH_LONG
                    ).show();
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

    private void updateDayView() {
        statusManager.setMainLayout();
        classLabel.setText(currentDay.name);
        updateStudentsAdapterData();

        updateConfirmButton();
        updateSwipeRefreshLayout();

        if (currentDay.noInfo()) {
            Snackbar.make(
                    mainLayout,
                    getResources().getString(R.string.no_info_for_day),
                    Snackbar.LENGTH_LONG
            ).setAnchorView(buttonsGroup).show();
        } else if (currentDay.noLoadedAbsent()) {
            Snackbar.make(
                    mainLayout,
                    getResources().getString(R.string.no_absent),
                    Snackbar.LENGTH_LONG
            ).setAnchorView(buttonsGroup).show();
        }
        buttonsGroup.setVisibility(currentDay.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void updateStudentsAdapterData() {
        studentsAdapter = new StudentsAdapter(this, currentDay.students);
        studentsListView.setAdapter(studentsAdapter);
    }

    private void updateSwipeRefreshLayout() {
        swipeRefreshLayout.setEnabled(!datePickerManager.isEmpty());
    }

    private void updateConfirmButton() {
        confirmButton.setVisibility(currentDay.canEdit ? View.VISIBLE : View.GONE);
        confirmButton.setEnabled(!currentDay.noChanges() || currentDay.noInfo());
        if (currentDay.noCurrentAbsent())
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
package com.thecattest.samsung.lyceumreports.Activities;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.thecattest.samsung.lyceumreports.Adapters.StudentsAdapter;
import com.thecattest.samsung.lyceumreports.Data.ApiService;
import com.thecattest.samsung.lyceumreports.Data.Models.Relations.DayWithAbsent;
import com.thecattest.samsung.lyceumreports.Data.Models.Relations.GroupWithStudents;
import com.thecattest.samsung.lyceumreports.Data.Models.Student;
import com.thecattest.samsung.lyceumreports.Data.Repositories.DayRepository;
import com.thecattest.samsung.lyceumreports.Data.Repositories.GroupRepository;
import com.thecattest.samsung.lyceumreports.Data.Repositories.StudentRepository;
import com.thecattest.samsung.lyceumreports.Managers.DatePickerManager;
import com.thecattest.samsung.lyceumreports.Managers.LoginManager;
import com.thecattest.samsung.lyceumreports.Managers.RetrofitManager;
import com.thecattest.samsung.lyceumreports.Managers.StatusManager;
import com.thecattest.samsung.lyceumreports.R;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

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

    private StudentsAdapter studentsAdapter;
    private GroupRepository groupRepository;
    private DayRepository dayRepository;
    private ApiService apiService;

    private int groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day);

        findViews();
        setListeners();
        initManagers();
        initRetrofit();
        initRepositories();

        groupId = getIntent().getIntExtra(GROUP_ID, 6);
        String groupLabel = getIntent().getStringExtra(GROUP_LABEL);
        classLabel.setText(groupLabel);

        loadGroup();
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        datePickerManager.loadFromBundle(savedInstanceState);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        datePickerManager.saveToBundle(outState);
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

    private void setListeners() {
        confirmButton.setOnClickListener(this::onConfirmButtonClick);
        cancelButton.setOnClickListener(this::onCancelButtonClick);
        swipeRefreshLayout.setOnRefreshListener(this::onRefresh);
    }

    private void initManagers() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        loginManager = new LoginManager(this);
        statusManager = new StatusManager(this, mainLayout);
        datePickerManager = new DatePickerManager(
                this,
                datePickerTrigger,
                fragmentManager,
                () -> loadDay(true));
    }

    private void initRetrofit() {
        Retrofit retrofit = RetrofitManager.getInstance(loginManager);
        apiService = retrofit.create(ApiService.class);
    }

    private void initRepositories() {
        StudentRepository studentRepository = new StudentRepository(this);
        dayRepository = new DayRepository(this, studentRepository);
        groupRepository = new GroupRepository(this, dayRepository, studentRepository, apiService);
    }

    public void onStudentItemClick(AdapterView<?> parent, View view, int position, long id) {
        Student student = (Student)parent.getItemAtPosition(position);
        studentsAdapter.toggleAbsent(student);
        studentsAdapter.notifyDataSetChanged();
        Log.d("DayActivityDebug", "updating button");
        updateConfirmButtonState();
    }

    public void onRefresh() { refreshGroupAndDay(); }

    public void onCancelButtonClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(DayActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    public void onConfirmButtonClick(View v) {
//        v.setEnabled(false);
//        datePickerManager.setEnabled(false);
//        statusManager.setLoadingLayout();
//
//        String formattedDate = datePickerManager.getDate();
//        String absentStudentsIdsString = currentDay.getAbsentStudentsIdsString();
//
//        Call<Void> call = dayService.updateDay(groupId, new DayPost(formattedDate, absentStudentsIdsString));
//        call.enqueue(new DefaultCallback<Void>(this, loginManager, mainLayout) {
//            @Override
//            public void onResponse200(Response<Void> response) {
//                Snackbar.make(
//                        mainLayout,
//                        R.string.snackbar_server_ok,
//                        Snackbar.LENGTH_SHORT
//                ).setAnchorView(buttonsGroup).show();
//                update();
//            }
//
//            @Override
//            public void onResponse500(Response<Void> response) {
//                Snackbar.make(
//                        mainLayout,
//                        R.string.snackbar_server_error_code_500,
//                        Snackbar.LENGTH_LONG
//                ).setAnchorView(buttonsGroup).show();
//                statusManager.setMainLayout();
//            }
//
//            public void onResponseFailure(Call<Void> call, Throwable t) {
//                if (call.isCanceled()) {
//                    statusManager.setMainLayout();
//                    Snackbar.make(
//                            mainLayout,
//                            R.string.snackbar_request_cancelled,
//                            Snackbar.LENGTH_LONG
//                    ).setAnchorView(buttonsGroup).show();
//                } else {
//                    Snackbar.make(
//                            mainLayout,
//                            R.string.snackbar_server_error,
//                            Snackbar.LENGTH_LONG
//                    ).show();
//                }
//            }
//
//            @Override
//            public void onPostExecute() {
//                v.setEnabled(true);
//                datePickerManager.setEnabled(true);
//            }
//        });
    }

    private void refreshGroupAndDay() {
        statusManager.setLoadingLayout();
        datePickerManager.setEnabled(false);

        String formattedDate = datePickerManager.getDate();
        groupRepository.refreshGroup(getApplicationContext(), loginManager, mainLayout,
                () -> {
                    datePickerManager.setEnabled(true);
                    swipeRefreshLayout.setRefreshing(false);
                    statusManager.setMainLayout();
                }, this::loadGroup, groupId, formattedDate);
    }

    @SuppressLint("CheckResult")
    private void loadGroup() {
        groupRepository.getById(groupId)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::updateView);
        loadDay(false);
    }

    private void updateAdapterGroup(GroupWithStudents group) {
        if (group.students == null)
            return;
        studentsAdapter = new StudentsAdapter(this, group);
        studentsListView.setAdapter(studentsAdapter);
    }

    private void updateView(GroupWithStudents group) {
        statusManager.setMainLayout();

        if (group == null) {
            updateAdapterGroup(new GroupWithStudents());
            buttonsGroup.setVisibility(View.GONE);
        } else {
            updateAdapterGroup(group);

            classLabel.setText(group.group.getLabel());
            buttonsGroup.setVisibility(View.VISIBLE);
        }
        updateSwipeRefreshLayoutState();
    }

    @SuppressLint("CheckResult")
    private void loadDay(boolean firstTime) {
        if (datePickerManager.isEmpty())
            return;
        dayRepository.getByGroupIdAndDate(groupId, datePickerManager.getDate())
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::updateAdapterDay,
                        (t) -> {},
                        () -> {
                            if (firstTime)
                                refreshGroupAndDay();
                            else
                                updateAdapterDay(new DayWithAbsent());
                        });
    }

    private void updateAdapterDay(DayWithAbsent day) {
        studentsAdapter.updateDay(day);

        updateConfirmButtonState();
        updateSwipeRefreshLayoutState();
        studentsListView.setOnItemClickListener(this::onStudentItemClick);
        if (studentsAdapter.noLoadedAbsent())
            Snackbar.make(
                    mainLayout,
                    R.string.snackbar_no_absent,
                    Snackbar.LENGTH_SHORT
            ).setAnchorView(buttonsGroup).show();
        if (day.day == null)
            Snackbar.make(
                    mainLayout,
                    R.string.snackbar_no_info,
                    Snackbar.LENGTH_SHORT)
                    .setAction(R.string.button_check_again, v -> refreshGroupAndDay())
                    .setAnchorView(buttonsGroup).show();
    }

    private void updateSwipeRefreshLayoutState() {
        swipeRefreshLayout.setEnabled(!datePickerManager.isEmpty());
    }

    private void updateConfirmButtonState() {
        confirmButton.setVisibility(loginManager.getPermissions().canEdit && !datePickerManager.isEmpty() ? View.VISIBLE : View.GONE);
        confirmButton.setEnabled(studentsAdapter.buttonEnabled());
        if (studentsAdapter.noAbsent())
            confirmButton.setText(getResources().getString(R.string.button_submit_day_no_one_absent));
        else
            confirmButton.setText(getResources().getString(R.string.button_submit_day_default));
    }
}
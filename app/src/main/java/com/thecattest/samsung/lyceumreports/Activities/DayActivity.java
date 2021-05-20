package com.thecattest.samsung.lyceumreports.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import java.util.ArrayList;

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

        updateSource();
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
//        studentsListView.setOnItemClickListener(this::onStudentItemClick);
        confirmButton.setOnClickListener(this::onConfirmButtonClick);
        cancelButton.setOnClickListener(this::onCancelButtonClick);
        swipeRefreshLayout.setOnRefreshListener(this::onRefresh);
    }

    private void initManagers() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        loginManager = new LoginManager(this);
        statusManager = new StatusManager(this, mainLayout, v -> {
            update();});
        datePickerManager = new DatePickerManager(
                this,
                datePickerTrigger,
                fragmentManager,
                this::updateSource);
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

//    public void onStudentItemClick(AdapterView<?> parent, View view, int position, long id) {
//        Student student = (Student)parent.getItemAtPosition(position);
//        student.absent = !student.absent;
//        studentsAdapter.notifyDataSetChanged();
//        updateConfirmButton();
//    }

    public void onRefresh() {
        update();
    }

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

    @SuppressLint("CheckResult")
    private void updateSource() {
        Log.d("Groups", "updating source");
        groupRepository.getById(groupId)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        dbGroups -> updateView(new ArrayList<>(dbGroups)));
        if (datePickerManager.isEmpty())
            return;
        dayRepository.getByGroupIdAndDate(groupId, datePickerManager.getDate())
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(dbDays -> updateDay(new ArrayList<>(dbDays)));
    }

    private void update() {
        statusManager.setLoadingLayout();
        datePickerManager.setEnabled(false);

        String formattedDate = datePickerManager.getDate();
        groupRepository.refreshGroup(getApplicationContext(), loginManager,
                mainLayout, () -> {
                    datePickerManager.setEnabled(true);
                    swipeRefreshLayout.setRefreshing(false);
                    statusManager.setMainLayout();
                }, groupId, formattedDate);
    }

    @SuppressLint("CheckResult")
    private void updateView(ArrayList<GroupWithStudents> groups) {
        statusManager.setMainLayout();
        Log.d("Groups", groups.toString());
        if (groups.isEmpty()) {
            updateStudentsAdapterData(new GroupWithStudents());
            buttonsGroup.setVisibility(View.GONE);
        } else {
            GroupWithStudents group = groups.get(0);

            Log.d("Groups", group.group.toString());
            Log.d("Groups", group.students.toString());

            updateStudentsAdapterData(group);

            classLabel.setText(group.group.getLabel());
            buttonsGroup.setVisibility(View.VISIBLE);
        }
        updateSwipeRefreshLayout();
    }

    private void updateDay(ArrayList<DayWithAbsent> days) {
        DayWithAbsent day = new DayWithAbsent();
        if (days.isEmpty()) {
            Snackbar.make(
                    mainLayout,
                    R.string.snackbar_no_info,
                    Snackbar.LENGTH_LONG
            ).setAnchorView(buttonsGroup).show();
        } else {
            day = days.get(0);
            if (day.absent.isEmpty())
                Snackbar.make(
                    mainLayout,
                    R.string.snackbar_no_absent,
                    Snackbar.LENGTH_LONG
            ).setAnchorView(buttonsGroup).show();
        }
        studentsAdapter.updateDay(day);
    }

    private void updateStudentsAdapterData(GroupWithStudents group) {
        if (group.students == null)
            return;
        studentsAdapter = new StudentsAdapter(this, group);
        studentsListView.setAdapter(studentsAdapter);
    }

    private void updateSwipeRefreshLayout() {
        swipeRefreshLayout.setEnabled(!datePickerManager.isEmpty());
    }

    private void updateConfirmButton() {
//        confirmButton.setVisibility(currentDay.canEdit ? View.VISIBLE : View.GONE);
//        confirmButton.setEnabled(!currentDay.noChanges() || currentDay.noInfo());
//        if (currentDay.noCurrentAbsent())
//            confirmButton.setText(getResources().getString(R.string.button_submit_day_no_one_absent));
//        else
//            confirmButton.setText(getResources().getString(R.string.button_submit_day_default));
    }
}
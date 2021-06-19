package com.thecattest.samsung.lyceumreports.Activities;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.thecattest.samsung.lyceumreports.Adapters.StudentsAdapter;
import com.thecattest.samsung.lyceumreports.Data.ApiService;
import com.thecattest.samsung.lyceumreports.Data.AppDatabase;
import com.thecattest.samsung.lyceumreports.Data.Models.Day;
import com.thecattest.samsung.lyceumreports.Data.Models.Relations.DayWithAbsent;
import com.thecattest.samsung.lyceumreports.Data.Models.Relations.GroupWithStudents;
import com.thecattest.samsung.lyceumreports.Data.Models.Student;
import com.thecattest.samsung.lyceumreports.Data.Repositories.DayRepository;
import com.thecattest.samsung.lyceumreports.Data.Repositories.GroupRepository;
import com.thecattest.samsung.lyceumreports.Managers.DatePickerManager;
import com.thecattest.samsung.lyceumreports.Managers.LoginManager;
import com.thecattest.samsung.lyceumreports.Managers.RetrofitManager;
import com.thecattest.samsung.lyceumreports.R;
import com.thecattest.samsung.lyceumreports.Services.SyncService;

import io.reactivex.android.schedulers.AndroidSchedulers;
import retrofit2.Retrofit;

public class DayActivity extends AppCompatActivity {

    public static final String GROUP_ID = "GROUP_ID";
    public static final String GROUP_LABEL = "GROUP_LABEL";

    private TextView classLabel;
    private ListView studentsListView;
    private Button confirmButton;
    private Button datePickerTrigger;
    private ProgressBar loadingProgressBar;
    private ConstraintLayout buttonsGroup;
    private ConstraintLayout mainLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MaterialToolbar toolbar;

    private LoginManager loginManager;
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


        groupId = getIntent().getIntExtra(GROUP_ID, -1);
        if (groupId == -1) {
            finish();
        }
        String groupLabel = getIntent().getStringExtra(GROUP_LABEL);
        classLabel.setText(groupLabel);

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadGroup();
            }
        }, new IntentFilter(SyncService.REDRAW_BROADCAST));

        loadGroup();
    }

    @Override
    protected void onStop() {
        super.onStop();
        swipeRefreshLayout.setRefreshing(false);
        loadingProgressBar.setVisibility(View.GONE);
    }

    private void findViews() {
        classLabel = findViewById(R.id.classLabel);
        studentsListView = findViewById(R.id.studentsList);
        confirmButton = findViewById(R.id.confirmButton);
        datePickerTrigger = findViewById(R.id.datePickerTrigger);
        buttonsGroup = findViewById(R.id.buttonsGroup);

        mainLayout = findViewById(R.id.main);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);

        toolbar = findViewById(R.id.topAppBar);
    }

    private void setListeners() {
        confirmButton.setOnClickListener(this::onConfirmButtonClick);
        swipeRefreshLayout.setOnRefreshListener(this::refreshGroupAndDay);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initManagers() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        loginManager = new LoginManager(this);
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
        groupRepository = new GroupRepository(this, loginManager, mainLayout, apiService);
        dayRepository = groupRepository.dayRepository;
    }

    public void onStudentItemClick(AdapterView<?> parent, View view, int position, long id) {
        Student student = (Student)parent.getItemAtPosition(position);
        studentsAdapter.toggleAbsent(student);
        studentsAdapter.notifyDataSetChanged();
        updateConfirmButtonState();
    }

    public void onConfirmButtonClick(View v) { sendDay(); }

    private void refreshGroupAndDay() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        datePickerManager.setEnabled(false);

        String formattedDate = datePickerManager.getDate();
        groupRepository.refreshGroup(
                () -> {
                    datePickerManager.setEnabled(true);
                    swipeRefreshLayout.setRefreshing(false);
                    loadingProgressBar.setVisibility(View.GONE);
                    loadGroup();
                }, () -> {}, groupId, formattedDate, buttonsGroup);
    }

    private void sendDay() {
        confirmButton.setEnabled(false);
        datePickerManager.setEnabled(false);
        loadingProgressBar.setVisibility(View.VISIBLE);
        try {
            Day day = studentsAdapter.getDay();
            dayRepository.update(day, AppDatabase.scheduler, AndroidSchedulers.mainThread());
            dayRepository.sendDay(
                    () -> {
                        confirmButton.setEnabled(true);
                        datePickerManager.setEnabled(true);
                        loadingProgressBar.setVisibility(View.GONE);
                    },
                    () -> {
                        Snackbar snackbar = Snackbar.make(
                                mainLayout,
                                R.string.snackbar_server_ok,
                                Snackbar.LENGTH_SHORT);
//                        snackbar.setAction(R.string.button_refresh, btn -> refreshGroupAndDay());
                        snackbar.setAnchorView(buttonsGroup);
                        snackbar.show();
                    },
                    day, buttonsGroup);
        } catch (NullPointerException ignored) {}
    }

    @SuppressLint("CheckResult")
    private void loadGroup() {
        groupRepository.getById(groupId)
                .subscribeOn(AppDatabase.scheduler)
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
        loadingProgressBar.setVisibility(View.GONE);

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
                .subscribeOn(AppDatabase.scheduler)
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
        studentsAdapter.updateDay(day, datePickerManager.getDate());

        updateConfirmButtonState();
        updateSwipeRefreshLayoutState();
        studentsListView.setOnItemClickListener(this::onStudentItemClick);
        if (studentsAdapter.noLoadedAbsent()) {
            Snackbar snackbar = Snackbar.make(
                    mainLayout,
                    R.string.snackbar_no_absent,
                    Snackbar.LENGTH_SHORT
            );
            snackbar.setAnchorView(buttonsGroup);
            snackbar.show();
        }
        if (day.day == null) {
            Snackbar snackbar = Snackbar.make(
                    mainLayout,
                    R.string.snackbar_no_info,
                    Snackbar.LENGTH_SHORT);
            snackbar.setAction(R.string.button_check_again, v -> refreshGroupAndDay());
            snackbar.setAnchorView(buttonsGroup);
            snackbar.show();
        }
        else {
            try {
                checkUnsavedChanges();
            } catch (WindowManager.BadTokenException ignored) {}
        }
    }

    private void checkUnsavedChanges() {
        Day day = studentsAdapter.getDay();
        if (day != null && !day.isSyncedWithServer) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.dialog_title_unsaved_changes)
                    .setMessage(R.string.dialog_text_unsaved_changes)
                    .setNegativeButton(R.string.button_discard, (dialog, which) -> {
                        dayRepository.deleteByGroupIdAndDate(groupId, datePickerManager.getDate());
                        dialog.dismiss();
                        loadDay(true);
                    })
                    .setPositiveButton(R.string.button_ok, (dialog, which) -> dialog.dismiss())
                    .show();
        }
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
package com.thecattest.samsung.lyceumreports.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.thecattest.samsung.lyceumreports.Adapters.GroupsAdapter;
import com.thecattest.samsung.lyceumreports.Data.ApiService;
import com.thecattest.samsung.lyceumreports.Data.Models.Group;
import com.thecattest.samsung.lyceumreports.Data.Models.GroupWithDaysAndStudents;
import com.thecattest.samsung.lyceumreports.Data.Repositories.DayRepository;
import com.thecattest.samsung.lyceumreports.Data.Repositories.GroupRepository;
import com.thecattest.samsung.lyceumreports.Data.Repositories.StudentRepository;
import com.thecattest.samsung.lyceumreports.Managers.LoginManager;
import com.thecattest.samsung.lyceumreports.Managers.RetrofitManager;
import com.thecattest.samsung.lyceumreports.Managers.StatusManager;
import com.thecattest.samsung.lyceumreports.R;

import java.util.ArrayList;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private ListView groupsListView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MaterialToolbar toolbar;

    private LoginManager loginManager;
    private StatusManager statusManager;

    private GroupRepository groupRepository;
    private ApiService apiService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Intent i = new Intent(MainActivity.this, TestActivity.class);
//        startActivity(i);
//        finish();

        findViews();
        setListeners();
        initManagers();
        initRetrofit();

        StudentRepository studentRepository = new StudentRepository(this);
        DayRepository dayRepository = new DayRepository(this, studentRepository);
        groupRepository = new GroupRepository(this, dayRepository, studentRepository, apiService);

        groupRepository.get()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(groupsWithDaysAndStudent -> updateView(new ArrayList<>(groupsWithDaysAndStudent)));

//        if (summaryWithPermissions.getSummaryStringFromBundle(savedInstanceState) == null)
//            updateSummary();
    }

//    @Override
//    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//
//        if(statusManager.loadFromBundle(savedInstanceState)) {
//            summaryWithPermissions.loadFromBundle(savedInstanceState);
//            updateSummaryView();
//        }
//    }

//    @SuppressLint("MissingSuperCall")
//    @Override
//    protected void onSaveInstanceState(@NonNull Bundle outState) {
//        summaryWithPermissions.saveToBundle(outState);
//        statusManager.saveToBundle(outState);
//    }

    private void initRetrofit() {
        Retrofit retrofit = RetrofitManager.getInstance(loginManager);
        apiService = retrofit.create(ApiService.class);
    }

    private void findViews() {
        groupsListView = findViewById(R.id.groupsList);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        toolbar = findViewById(R.id.topAppBar);
    }

    private void setListeners() {
        swipeRefreshLayout.setOnRefreshListener(this::onRefresh);
        toolbar.setOnMenuItemClickListener(this::onMenuItemClick);
    }

    private void initManagers() {
        loginManager = new LoginManager(this);
        statusManager = new StatusManager(this, swipeRefreshLayout, v -> update());
    }

    private void onRefresh() {
        update();
        swipeRefreshLayout.setRefreshing(false);
    }

    private boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                loginManager.logout();
                return true;
            case R.id.daySummaryTable:
                Intent i = new Intent(MainActivity.this, SummaryDayActivity.class);
                startActivity(i);
                finish();
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {}

    private void update() {
        statusManager.setLoadingLayout();
        groupRepository.refreshGroups(this, loginManager, swipeRefreshLayout, () -> statusManager.setMainLayout());
    }

    private void updateView(ArrayList<GroupWithDaysAndStudents> groups) {
        GroupsAdapter groupsAdapter = new GroupsAdapter(this, groups, true);
        groupsListView.setAdapter(groupsAdapter);
        Menu menu = toolbar.getMenu();
        menu.findItem(R.id.daySummaryTable).setVisible(true);
        toolbar.invalidate();
    }
}
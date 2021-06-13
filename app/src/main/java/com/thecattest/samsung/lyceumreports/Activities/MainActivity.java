package com.thecattest.samsung.lyceumreports.Activities;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.thecattest.samsung.lyceumreports.Adapters.GroupsAdapter;
import com.thecattest.samsung.lyceumreports.Data.ApiService;
import com.thecattest.samsung.lyceumreports.Data.AppDatabase;
import com.thecattest.samsung.lyceumreports.Data.Models.Permissions;
import com.thecattest.samsung.lyceumreports.Data.Models.Relations.GroupWithDaysAndStudents;
import com.thecattest.samsung.lyceumreports.Data.Repositories.GroupRepository;
import com.thecattest.samsung.lyceumreports.Managers.LoginManager;
import com.thecattest.samsung.lyceumreports.Managers.RetrofitManager;
import com.thecattest.samsung.lyceumreports.Managers.StatusManager;
import com.thecattest.samsung.lyceumreports.R;
import com.thecattest.samsung.lyceumreports.Services.SyncService;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private ListView groupsListView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MaterialToolbar toolbar;

    private LoginManager loginManager;
    private StatusManager statusManager;

    private GroupRepository groupRepository;
    private ApiService apiService;

    private Permissions permissions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
        setListeners();
        initManagers();
        initRetrofit();
        initRepositories();

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("Updates", "broadcast got");
                loadData();
            }
        }, new IntentFilter(SyncService.CHANNEL));

        permissions = loginManager.getPermissions();
        updateMenu();
        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startService(new Intent(this, SyncService.class));
    }

    @Override
    protected void onStop() {
        super.onStop();
        swipeRefreshLayout.setRefreshing(false);
        statusManager.setMainLayout();
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
        statusManager = new StatusManager(this, swipeRefreshLayout);
    }

    private void initRetrofit() {
        Retrofit retrofit = RetrofitManager.getInstance(loginManager);
        apiService = retrofit.create(ApiService.class);
    }

    private void initRepositories() {
        groupRepository = new GroupRepository(this, loginManager, swipeRefreshLayout, apiService);
    }

    private void onRefresh() {
        refreshData();
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
            case R.id.refresh:
                refreshData();
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {}

    private void refreshData() {
        statusManager.setLoadingLayout();
        groupRepository.refreshGroups(
                () -> {statusManager.setMainLayout(); swipeRefreshLayout.setRefreshing(false);},
                this::loadData
        );
    }

    @SuppressLint("CheckResult")
    private void loadData() {
        groupRepository.get()
                .subscribeOn(AppDatabase.scheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        groupsWithDaysAndStudent -> updateView(new ArrayList<>(groupsWithDaysAndStudent), false),
                        (t) -> {},
                        () -> updateView(new ArrayList<>(), true));
    }

    private void updateView(ArrayList<GroupWithDaysAndStudents> groups, boolean noData) {
        if (groups.isEmpty() && !noData)
            refreshData();
        GroupsAdapter groupsAdapter = new GroupsAdapter(this, groups, permissions.canEdit);
        groupsListView.setAdapter(groupsAdapter);
    }

    private void updateMenu() {
        Menu menu = toolbar.getMenu();
        menu.findItem(R.id.daySummaryTable).setVisible(permissions.canViewTable);
        toolbar.invalidate();
    }
}
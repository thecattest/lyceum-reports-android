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
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import com.thecattest.samsung.lyceumreports.R;
import com.thecattest.samsung.lyceumreports.Services.SyncService;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private long lastTimeBackPressed = 0;

    private ListView groupsListView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MaterialToolbar toolbar;
    private ProgressBar loadingProgressBar;

    private LoginManager loginManager;
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

        if (!loginManager.isLoggedIn())
            loginManager.handleNotAuthorized();

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("Updates", "broadcast got");
                loadData();
            }
        }, new IntentFilter(SyncService.REDRAW_BROADCAST));

        permissions = loginManager.getPermissions();
        updateMenu();
        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startService(new Intent(this, SyncService.class));
        loadData();
    }

    @Override
    protected void onStop() {
        super.onStop();
        swipeRefreshLayout.setRefreshing(false);
        loadingProgressBar.setVisibility(View.GONE);
    }

    private void findViews() {
        groupsListView = findViewById(R.id.groupsList);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        toolbar = findViewById(R.id.topAppBar);
    }

    private void setListeners() {
        swipeRefreshLayout.setOnRefreshListener(this::onRefresh);
        toolbar.setOnMenuItemClickListener(this::onMenuItemClick);
    }

    private void initManagers() {
        loginManager = new LoginManager(this);
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
        Intent i;
        int itemId = item.getItemId();
        if (itemId == R.id.logout) {
            loginManager.logout();
            return true;
        }
        if (itemId == R.id.statistics) {
            i = new Intent(MainActivity.this, StatisticsActivity.class);
            startActivity(i);
            return true;
        }
        if (itemId == R.id.settings) {
            i = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(i);
            return true;
        }
        if (itemId == R.id.refresh) {
            refreshData();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis() / 1000L;
        if (currentTime - lastTimeBackPressed <= 3) {
            super.onBackPressed();
        } else {
            lastTimeBackPressed = currentTime;
            Toast.makeText(this, R.string.snackbar_back_again_to_exit, Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshData() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        groupRepository.refreshGroups(
                () -> {
                    loadingProgressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    loadData();
                },
                () -> {}
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
        menu.findItem(R.id.statistics).setVisible(permissions.canViewTable);
        toolbar.invalidate();
    }
}
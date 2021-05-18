package com.thecattest.samsung.lyceumreports.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.thecattest.samsung.lyceumreports.Adapters.SummaryAdapter;
import com.thecattest.samsung.lyceumreports.DataModels.Legacy.Summary.SummaryService;
import com.thecattest.samsung.lyceumreports.DataModels.Legacy.Summary.SummaryWithPermissions;
import com.thecattest.samsung.lyceumreports.DefaultCallback;
import com.thecattest.samsung.lyceumreports.Managers.LoginManager;
import com.thecattest.samsung.lyceumreports.Managers.RetrofitManager;
import com.thecattest.samsung.lyceumreports.Managers.StatusManager;
import com.thecattest.samsung.lyceumreports.R;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private ListView summaryListView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MaterialToolbar toolbar;

    private LoginManager loginManager;
    private StatusManager statusManager;

    private SummaryWithPermissions summaryWithPermissions = new SummaryWithPermissions();

    SummaryService summaryService;
    Call<SummaryWithPermissions> dataGetCall;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent i = new Intent(MainActivity.this, TestActivity.class);
        startActivity(i);
        finish();

        findViews();
        setListeners();
        initManagers();
        initRetrofit();

        if (summaryWithPermissions.getSummaryStringFromBundle(savedInstanceState) == null)
            updateSummary();
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if(statusManager.loadFromBundle(savedInstanceState)) {
            summaryWithPermissions.loadFromBundle(savedInstanceState);
            updateSummaryView();
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        summaryWithPermissions.saveToBundle(outState);
        statusManager.saveToBundle(outState);
    }

    private void initRetrofit() {
        Retrofit retrofit = RetrofitManager.getInstance(loginManager);
        summaryService = retrofit.create(SummaryService.class);
    }

    private void findViews() {
        summaryListView = findViewById(R.id.summaryList);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        toolbar = findViewById(R.id.topAppBar);
    }

    private void setListeners() {
        swipeRefreshLayout.setOnRefreshListener(this::onRefresh);
        toolbar.setOnMenuItemClickListener(this::onMenuItemClick);
    }

    private void initManagers() {
        loginManager = new LoginManager(this);
        statusManager = new StatusManager(this, swipeRefreshLayout, v -> {updateSummary();});
    }

    private void onRefresh() {
        updateSummary();
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
    public void onBackPressed() {
        cancelDataGetCall();
    }

    private void updateSummary() {
        setLoadingStatus();

        Call<SummaryWithPermissions> call = summaryService.getSummary();
        dataGetCall = call;
        call.enqueue(new DefaultCallback<SummaryWithPermissions>(this, loginManager, swipeRefreshLayout) {
            @Override
            public void onResponse200(Response<SummaryWithPermissions> response) {
                summaryWithPermissions = response.body();
                updateSummaryView();
            }

            @Override
            public void onResponse500(Response<SummaryWithPermissions> response) {
                super.onResponse500(response);
                statusManager.setServerErrorLayout();
            }

            public void onResponseFailure(Call<SummaryWithPermissions> call, Throwable t) {
                if (call.isCanceled()) {
                    statusManager.setMainLayout();
                    Snackbar.make(
                            swipeRefreshLayout,
                            R.string.snackbar_request_cancelled,
                            Snackbar.LENGTH_LONG
                    ).show();
                } else {
                    Snackbar.make(
                            swipeRefreshLayout,
                            R.string.snackbar_server_error,
                            Snackbar.LENGTH_LONG
                    ).show();
                    statusManager.setServerErrorLayout();
                }
            }

            @Override
            public void onPostExecute() {
                dataGetCall = null;
            }
        });
    }

    private boolean cancelDataGetCall() {
        if (dataGetCall == null)
            return false;
        dataGetCall.cancel();
        dataGetCall = null;
        return true;
    }

    private void updateSummaryView() {
        statusManager.setMainLayout();
        updateSummaryAdapterData();
        Menu menu = toolbar.getMenu();
        menu.findItem(R.id.daySummaryTable).setVisible(summaryWithPermissions.canViewTable);
        toolbar.invalidate();
    }

    private void updateSummaryAdapterData() {
        SummaryAdapter summaryAdapter = new SummaryAdapter(this, summaryWithPermissions.summary, summaryWithPermissions.canEdit);
        summaryListView.setAdapter(summaryAdapter);
    }

    private void setLoadingStatus() {
        summaryWithPermissions = new SummaryWithPermissions();
        updateSummaryView();
        statusManager.setLoadingLayout();
    }
}
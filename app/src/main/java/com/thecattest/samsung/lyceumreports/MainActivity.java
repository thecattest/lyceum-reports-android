package com.thecattest.samsung.lyceumreports;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.thecattest.samsung.lyceumreports.Adapters.SummaryAdapter;
import com.thecattest.samsung.lyceumreports.DataServices.Summary.Summary;
import com.thecattest.samsung.lyceumreports.DataServices.Summary.SummaryService;
import com.thecattest.samsung.lyceumreports.DataServices.Summary.SummaryWithPermissions;
import com.thecattest.samsung.lyceumreports.Managers.LoginManager;
import com.thecattest.samsung.lyceumreports.Managers.StatusManager;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {



    private ListView summaryListView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MaterialToolbar toolbar;

    private final FragmentManager fragmentManager = getSupportFragmentManager();

    private LoginManager loginManager;
    private StatusManager statusManager;

    private SummaryWithPermissions summaryWithPermissions = new SummaryWithPermissions();

    SummaryService summaryService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initRetrofit();
        findViews();
        setListeners();

        loginManager = new LoginManager(this);
        statusManager = new StatusManager(swipeRefreshLayout, fragmentManager, this::onRetryButtonClick);

        Log.d("Summary", "check");
        if (summaryWithPermissions.getSummaryStringFromBundle(savedInstanceState) == null) {
            updateSummary();
            Log.d("Summary", "Updated");
        }
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

    protected void initRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
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

    public void onRefresh() {
        updateSummary();
        swipeRefreshLayout.setRefreshing(false);
    }

    public boolean onMenuItemClick(MenuItem item) {
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

    // Retry button click
    public void onRetryButtonClick(View v) {
        updateSummary();
    }

    @Override
    public void onBackPressed() {}

    private void updateSummary() {
        setLoadingStatus();
        Call<SummaryWithPermissions> call = summaryService.getSummary(loginManager.getCookie());
        call.enqueue(new DefaultCallback<SummaryWithPermissions>(loginManager, swipeRefreshLayout) {
            @Override
            public void onResponse200(Response<SummaryWithPermissions> response) {
                summaryWithPermissions = response.body();
                Log.d("Summary", summaryWithPermissions.toString());
                updateSummaryView();
            }

            @Override
            public void onFailure(Call<SummaryWithPermissions> call, Throwable t) {
                Log.d("SummaryCall", t.toString());
                Toast.makeText(MainActivity.this, "Error loading summary", Toast.LENGTH_SHORT).show();
                statusManager.setServerErrorLayout();
            }

            @Override
            protected void onResponse401() {}
        });
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
        statusManager.setLoadingLayout();
    }
}
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
import android.widget.ListView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.thecattest.samsung.lyceumreports.Adapters.SummaryAdapter;
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

    private LoginManager loginManager;
    private StatusManager statusManager;

    private SummaryWithPermissions summaryWithPermissions = new SummaryWithPermissions();

    SummaryService summaryService;
    Call<SummaryWithPermissions> getCall;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initRetrofit();
        findViews();
        setListeners();

        FragmentManager fragmentManager = getSupportFragmentManager();
        loginManager = new LoginManager(this);
        statusManager = new StatusManager(this, swipeRefreshLayout, v -> {updateSummary();});

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

    private void initRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URLConfig.BASE_URL)
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
                Log.d("LoginManager", MainActivity.this.isFinishing() ? "true" : "false");
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        cancelGetCall();
    }

    private void updateSummary() {
        setLoadingStatus();
        Call<SummaryWithPermissions> call = summaryService.getSummary(loginManager.getCookie());
        getCall = call;
        call.enqueue(new DefaultCallback<SummaryWithPermissions>(loginManager, swipeRefreshLayout) {
            @Override
            public void onResponse200(Response<SummaryWithPermissions> response) {
                summaryWithPermissions = response.body();
                Log.d("Summary", summaryWithPermissions.toString());
                updateSummaryView();
            }

            @Override
            public void onResponse500(Response<SummaryWithPermissions> response) {
                statusManager.setServerErrorLayout();
                Snackbar.make(
                        swipeRefreshLayout,
                        "Сервер выдал ошибку 500, попробуйте позднее",
                        Snackbar.LENGTH_LONG
                ).show();
            }

            public void onResponseFailure(Call<SummaryWithPermissions> call, Throwable t) {
                if (call.isCanceled()) {
                    statusManager.setMainLayout();
                    Snackbar.make(
                            swipeRefreshLayout,
                            "Отмена",
                            Snackbar.LENGTH_LONG
                    ).show();
                } else {
                    Log.d("SummaryCall", t.toString());
                    Snackbar.make(
                            swipeRefreshLayout,
                            "Ошибка, попробуйте ещё раз позднее",
                            Snackbar.LENGTH_LONG
                    ).show();
                    statusManager.setServerErrorLayout();
                }
            }

            @Override
            public void onPostExecute() {
                getCall = null;
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
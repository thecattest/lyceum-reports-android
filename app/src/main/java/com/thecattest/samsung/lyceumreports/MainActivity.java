package com.thecattest.samsung.lyceumreports;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.thecattest.samsung.lyceumreports.Adapters.SummaryAdapter;
import com.thecattest.samsung.lyceumreports.DataServices.Summary.Summary;
import com.thecattest.samsung.lyceumreports.DataServices.Summary.SummaryService;
import com.thecattest.samsung.lyceumreports.DataServices.Summary.SummaryWithPermissions;
import com.thecattest.samsung.lyceumreports.Fragments.LoadingFragment;
import com.thecattest.samsung.lyceumreports.Fragments.ServerErrorFragment;

import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String LAYOUT_TYPE = "LAYOUT_TYPE";
    private static final String LAYOUT_TYPE_MAIN = "LAYOUT_TYPE_MAIN";
    private static final String LAYOUT_TYPE_SERVER_ERROR = "LAYOUT_TYPE_SERVER_ERROR";
    private static final String SUMMARY = "SUMMARY";

    private ListView summaryListView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private final FragmentManager fragmentManager = getSupportFragmentManager();
    private LoadingFragment loadingFragment;
    private ServerErrorFragment serverErrorFragment;

    private SummaryWithPermissions summaryWithPermissions = new SummaryWithPermissions();

    SummaryService summaryService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initRetrofit();
        findViews();
        createFragments();
        setListeners();

        if (savedInstanceState == null || savedInstanceState.getString(SUMMARY) == null || savedInstanceState.getString(SUMMARY).isEmpty()) {
            updateSummary();
            Log.d("Summary", "Updated");
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        String layout = savedInstanceState.getString(LAYOUT_TYPE);
        switch (layout) {
            case LAYOUT_TYPE_SERVER_ERROR:
                setServerErrorLayout();
                break;
            case LAYOUT_TYPE_MAIN:
                setMainLayout();
//                String summaryJsonString = savedInstanceState.getString(SUMMARY);
//                Gson gson = new Gson();
//                JsonElement summaryJsonObject = new JsonParser().parse(summaryJsonString);
//                for(JsonElement s : summaryJsonObject.getAsJsonArray()) {
//                    summaryWithPermissions.summary.add(gson.fromJson(s, Summary.class));
//                }
//                Log.d("Summary", "Loaded");
//                updateSummaryView();
                updateSummary();
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (summaryWithPermissions.summary.size() > 0) {
            Gson gson = new Gson();
            outState.putString(SUMMARY, gson.toJson(summaryWithPermissions));
        }

        String layout = "";
        if (swipeRefreshLayout.getVisibility() == View.VISIBLE) {
            layout = LAYOUT_TYPE_MAIN;
        } else if (!Objects.requireNonNull(fragmentManager.findFragmentByTag(ServerErrorFragment.TAG)).isHidden()) {
            layout = LAYOUT_TYPE_SERVER_ERROR;
        }
        outState.putString(LAYOUT_TYPE, layout);
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
    }

    private void createFragments() {
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.disallowAddToBackStack();

        loadingFragment = new LoadingFragment();
        ft.add(R.id.loadingLayout, loadingFragment, LoadingFragment.TAG);
        ft.hide(loadingFragment);

        serverErrorFragment = new ServerErrorFragment(this::onRetryButtonClick);
        ft.add(R.id.serverErrorLayout, serverErrorFragment, ServerErrorFragment.TAG);
        ft.hide(serverErrorFragment);

        ft.commit();
    }

    private void setListeners() {
        swipeRefreshLayout.setOnRefreshListener(this::onRefresh);
    }

    public void onRefresh() {
        updateSummary();
        swipeRefreshLayout.setRefreshing(false);
    }

    // Retry button click
    public void onRetryButtonClick(View v) {
        updateSummary();
    }

    private void updateSummary() {
        setLoadingStatus();
        Call<SummaryWithPermissions> call = summaryService.getSummary();
        call.enqueue(new Callback<SummaryWithPermissions>() {
            @Override
            public void onResponse(Call<SummaryWithPermissions> call, Response<SummaryWithPermissions> response) {
                summaryWithPermissions = response.body();
                Log.d("Summary", summaryWithPermissions.toString());
                updateSummaryView();
            }

            @Override
            public void onFailure(Call<SummaryWithPermissions> call, Throwable t) {
                Log.d("SummaryCall", t.toString());
                Toast.makeText(MainActivity.this, "Error loading summary", Toast.LENGTH_SHORT).show();
                setServerErrorLayout();
            }
        });
    }

    private void updateSummaryView() {
        setMainLayout();
        updateSummaryAdapterData();
    }

    private void updateSummaryAdapterData() {
        SummaryAdapter summaryAdapter = new SummaryAdapter(this, summaryWithPermissions.summary);
        summaryListView.setAdapter(summaryAdapter);
    }

    private void setServerErrorLayout() {
        swipeRefreshLayout.setVisibility(View.GONE);
        setLoadingFragmentVisibility(false);
        setServerErrorFragmentVisibility(true);
    }

    private void setLoadingLayout() {
        swipeRefreshLayout.setVisibility(View.GONE);
        setLoadingFragmentVisibility(true);
        setServerErrorFragmentVisibility(false);
    }

    private void setMainLayout() {
        swipeRefreshLayout.setVisibility(View.VISIBLE);
        setLoadingFragmentVisibility(false);
        setServerErrorFragmentVisibility(false);
    }

    private void setLoadingStatus() {
        summaryWithPermissions = new SummaryWithPermissions();
        setLoadingLayout();
    }

    private void setLoadingFragmentVisibility(boolean visible) {
        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (visible) {
            ft.show(loadingFragment);
        } else {
            ft.hide(loadingFragment);
        }
        ft.commit();
    }

    private void setServerErrorFragmentVisibility(boolean visible) {
        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (visible) {
            ft.show(serverErrorFragment);
        } else {
            ft.hide(serverErrorFragment);
        }
        ft.commit();
    }
}
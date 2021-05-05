package com.thecattest.samsung.lyceumreports;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.thecattest.samsung.lyceumreports.Adapters.SummaryAdapter;
import com.thecattest.samsung.lyceumreports.DataServices.Summary.Summary;
import com.thecattest.samsung.lyceumreports.DataServices.Summary.SummaryService;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private ListView summaryListView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private final FragmentManager fragmentManager = getSupportFragmentManager();
    private LoadingFragment loadingFragment;
    private ServerErrorFragment serverErrorFragment;

    private ArrayList<Summary> summary = new ArrayList<>();

    SummaryService summaryService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initRetrofit();
        findViews();
        createFragments();
        setListeners();

        updateSummary();
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
        ft.add(R.id.loadingLayout, loadingFragment, "LOADING_FRAGMENT");
        ft.hide(loadingFragment);

        serverErrorFragment = new ServerErrorFragment(this::onRetryButtonClick);
        ft.add(R.id.serverErrorLayout, serverErrorFragment, "SERVER_ERROR_FRAGMENT");
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
        Call<ArrayList<Summary>> call = summaryService.getSummary();
        call.enqueue(new Callback<ArrayList<Summary>>() {
            @Override
            public void onResponse(Call<ArrayList<Summary>> call, Response<ArrayList<Summary>> response) {
                summary = response.body();
                Log.d("Summary", summary.toString());
                updateSummaryView();
            }

            @Override
            public void onFailure(Call<ArrayList<Summary>> call, Throwable t) {
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
        SummaryAdapter summaryAdapter = new SummaryAdapter(this, summary);
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
        summary = new ArrayList<>();
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
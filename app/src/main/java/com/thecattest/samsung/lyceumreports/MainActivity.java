package com.thecattest.samsung.lyceumreports;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.thecattest.samsung.lyceumreports.DataServices.DayService;
import com.thecattest.samsung.lyceumreports.DataServices.Summary;
import com.thecattest.samsung.lyceumreports.DataServices.SummaryService;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private ListView summaryListView;

    private ArrayList<Summary> summary = new ArrayList<>();
    private SummaryAdapter summaryAdapter;

    SummaryService summaryService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initRetrofit();
        findViews();
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
    }

    private void setListeners() {

    }

    private void updateSummary() {
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
            }
        });
    }

    private void updateSummaryView() {
        updateSummaryAdapterData();
    }

    private void updateSummaryAdapterData() {
        summaryAdapter = new SummaryAdapter(this, summary);
        summaryListView.setAdapter(summaryAdapter);
    }
}
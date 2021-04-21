package com.thecattest.samsung.lyceumreports;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.thecattest.samsung.lyceumreports.DataServices.Day;
import com.thecattest.samsung.lyceumreports.DataServices.DayService;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private static String URL = "http:92.53.124.98:8002";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new GetDayAsyncTask().execute();
    }

    class GetDayAsyncTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            DayService service = retrofit.create(DayService.class);
            Call<Day> call = service.getDay(6, "2021-06-04");
            try {
                Response<Day> dayResponse = call.execute();
                Day d = dayResponse.body();
                Log.d("GET_DAY", "Day: " + d);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
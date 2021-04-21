package com.thecattest.samsung.lyceumreports;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.thecattest.samsung.lyceumreports.DataServices.Day;
import com.thecattest.samsung.lyceumreports.DataServices.DayService;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private static final String URL = "http:92.53.124.98:8002";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day);

        Button datePickerTrigger = findViewById(R.id.datePickerTrigger);
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("SELECT A DATE")
                .build();
        datePickerTrigger.setOnClickListener(v -> datePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER"));
        datePicker.addOnPositiveButtonClickListener(
                selection -> {
                    datePickerTrigger.setText(datePicker.getHeaderText());
                    Log.d("DatePicker", selection.toString());
                    Date selectedDate = new Date(selection);
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    String formattedDate = df.format(selectedDate);
                    Log.d("DatePicker", formattedDate);

                    new GetDayAsyncTask().execute(formattedDate);
                });
    }

    class GetDayAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            DayService service = retrofit.create(DayService.class);
            Call<Day> call = service.getDay(6, params[0]);
            try {
                Response<Day> dayResponse = call.execute();
                Day d = dayResponse.body();
                Log.d("GET_DAY", "Day " + params[0] + ": " + d);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
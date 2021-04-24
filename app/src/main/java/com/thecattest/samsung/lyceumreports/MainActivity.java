package com.thecattest.samsung.lyceumreports;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.thecattest.samsung.lyceumreports.DataServices.Day;
import com.thecattest.samsung.lyceumreports.DataServices.DayService;
import com.thecattest.samsung.lyceumreports.DataServices.Student;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private static final String URL = "http:92.53.124.98:8002";
    private TextView classLabel;
    private ListView studentsListView;
    private Button confirmButton;
    private Day currentDay = new Day();
    private ArrayList<Integer> currentAbsent = new ArrayList<>();
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day);
        context = this;

        classLabel = findViewById(R.id.classLabel);
        studentsListView = findViewById(R.id.studentsList);
        confirmButton = findViewById(R.id.confirmButton);
        Button datePickerTrigger = findViewById(R.id.datePickerTrigger);
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getResources().getString(R.string.selectDateLabel))
                .build();
        datePickerTrigger.setOnClickListener(v -> datePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER"));
        datePicker.addOnPositiveButtonClickListener(
                selection -> {
                    datePickerTrigger.setText(datePicker.getHeaderText());
                    Log.d("DatePicker", selection.toString());
                    Date selectedDate = new Date(selection);
                    @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat(getResources().getString(R.string.serverDateFormat));
                    String formattedDate = df.format(selectedDate);
                    Log.d("DatePicker", formattedDate);

                    new GetDayAsyncTask().execute(formattedDate);
                });
    }

    protected void updateDay() {
        classLabel.setText(currentDay.name);
        StudentsAdapter studentsAdapter = new StudentsAdapter(this, currentDay.students);
        studentsListView.setAdapter(studentsAdapter);
        currentAbsent = currentDay.getAbsentStudents();
        updateButton();
        studentsListView.setOnItemClickListener((parent, view, position, id) -> {
            Student student = (Student)parent.getItemAtPosition(position);
            student.absent = !student.absent;
            Log.d("ItemClick", student.name + (student.absent ? " absent" : "not absent"));
            studentsAdapter.notifyDataSetChanged();
            updateButton();
        });
    }

    protected void updateButton() {
        if(currentAbsent.equals(currentDay.getAbsentStudents()) && currentDay.status.equals(Day.STATUS.OK)) {
            if (currentDay.getAbsentStudents().size() == 0)
                confirmButton.setText(getResources().getString(R.string.confirmButtonNoOneAbsent));
            else
                confirmButton.setText(getResources().getString(R.string.confirmButtonDefault));
            confirmButton.setEnabled(false);
        } else if (currentDay.getAbsentStudents().size() == 0){
            confirmButton.setText(getResources().getString(R.string.confirmButtonNoOneAbsent));
            confirmButton.setEnabled(true);
        } else {
            confirmButton.setText(R.string.confirmButtonDefault);
            confirmButton.setEnabled(true);
        }
    }

    class GetDayAsyncTask extends AsyncTask<String, Void, Day> {
        @Override
        protected Day doInBackground(String... params) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            DayService service = retrofit.create(DayService.class);
            Call<Day> call = service.getDay(6, params[0]);
            try {
                Response<Day> dayResponse = call.execute();
                Day day = dayResponse.body();
                Log.d("GET_DAY", "Day " + params[0] + ": " + day);
                return day;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new Day();
        }

        @Override
        protected void onPostExecute(Day day) {
            currentDay = day;
            updateDay();
        }
    }
}
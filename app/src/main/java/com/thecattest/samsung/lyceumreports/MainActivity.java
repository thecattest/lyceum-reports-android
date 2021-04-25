package com.thecattest.samsung.lyceumreports;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.thecattest.samsung.lyceumreports.DataServices.Day;
import com.thecattest.samsung.lyceumreports.DataServices.DayService;
import com.thecattest.samsung.lyceumreports.DataServices.Student;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements MaterialPickerOnPositiveButtonClickListener<Long>, AdapterView.OnItemClickListener{
    private static final String URL = "http:92.53.124.98:8002";

    private TextView classLabel;
    private ListView studentsListView;
    private Button confirmButton;
    private Button datePickerTrigger;
    private RelativeLayout buttonsGroup;
    private RelativeLayout main;
    private LinearLayout loading;

    private MaterialDatePicker<Long> datePicker;

    private Day currentDay = new Day();
    private ArrayList<Integer> loadedAbsent = new ArrayList<>();
    private StudentsAdapter studentsAdapter;

    private DayService dayService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day);

        initRetrofit();
        findViews();
        initDatePicker();

        studentsAdapter = new StudentsAdapter(this, new ArrayList<>());
        studentsListView.setOnItemClickListener(this);
    }

    protected void findViews() {
        classLabel = findViewById(R.id.classLabel);
        studentsListView = findViewById(R.id.studentsList);
        confirmButton = findViewById(R.id.confirmButton);
        datePickerTrigger = findViewById(R.id.datePickerTrigger);
        buttonsGroup = findViewById(R.id.buttonsGroup);
        main = findViewById(R.id.main);
        loading = findViewById(R.id.loadingLayout);
    }

    protected void initRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        dayService = retrofit.create(DayService.class);
    }

    protected void initDatePicker() {
        datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getResources().getString(R.string.selectDateLabel))
                .build();
        datePickerTrigger.setOnClickListener(v -> datePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER"));
        datePicker.addOnPositiveButtonClickListener(this);
    }

    // Date picker positive button click
    @Override
    public void onPositiveButtonClick(Long selection) {
        datePickerTrigger.setText(datePicker.getHeaderText());
        String formattedDate = formatDate(selection);
        setLoading();
        Call<Day> call = dayService.getDay(6, formattedDate);
        call.enqueue(new Callback<Day>() {
            @Override
            public void onResponse(Call<Day> call, Response<Day> response) {
                currentDay = response.body();
                updateDayView();
            }

            @Override
            public void onFailure(Call<Day> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error accessing server", Toast.LENGTH_LONG).show();
                currentDay = new Day();
                updateDayView();
            }
        });
    }

    // Students list item click
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Student student = (Student)parent.getItemAtPosition(position);
        student.absent = !student.absent;
        Log.d("ItemClick", student.name + (student.absent ? " absent" : "not absent"));
        studentsAdapter.notifyDataSetChanged();
        updateConfirmButton();
    }

    protected void setLoading() {
        currentDay = new Day();
        updateDayView();
        main.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
    }

    protected String formatDate(Long selection) {
        Date selectedDate = new Date(selection);
        String serverDateFormat = getResources().getString(R.string.serverDateFormat);
        @SuppressLint("SimpleDateFormat")
        DateFormat df = new SimpleDateFormat(serverDateFormat);
        String formattedDate = df.format(selectedDate);

        Log.d("DatePicker", formattedDate);
        return formattedDate;
    }

    protected void updateDayView() {
        main.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
        classLabel.setText(currentDay.name);
        studentsAdapter = new StudentsAdapter(this, currentDay.students);
        studentsListView.setAdapter(studentsAdapter);
        loadedAbsent = currentDay.getAbsentStudents();
        updateConfirmButton();
    }

    protected void updateConfirmButton() {
        confirmButton.setEnabled(!loadedAbsent.equals(currentDay.getAbsentStudents()) || !currentDay.status.equals(Day.STATUS.OK));
        if (currentDay.getAbsentStudents().size() == 0)
            confirmButton.setText(getResources().getString(R.string.confirmButtonNoOneAbsent));
        else
            confirmButton.setText(getResources().getString(R.string.confirmButtonDefault));
    }
}
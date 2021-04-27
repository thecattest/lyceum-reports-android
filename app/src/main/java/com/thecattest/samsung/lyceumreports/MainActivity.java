package com.thecattest.samsung.lyceumreports;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.thecattest.samsung.lyceumreports.DataServices.Day;
import com.thecattest.samsung.lyceumreports.DataServices.DayPost;
import com.thecattest.samsung.lyceumreports.DataServices.DayService;
import com.thecattest.samsung.lyceumreports.DataServices.Student;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private static final String URL = "http:92.53.124.98:8002";
    private static final int groupId = 6;

    private final static String CURRENT_DAY = "CURRENT_DAY";
    private final static String CURRENT_SELECTION = "CURRENT_SELECTION";
    private final static String DATE_PICKER_TRIGGER_TEXT = "DATE_PICKER_TRIGGER_TEXT";

    private TextView classLabel;
    private ListView studentsListView;
    private Button confirmButton;
    private Button datePickerTrigger;
    private TextView retry;
    private RelativeLayout buttonsGroup;
    private RelativeLayout main;
    private LinearLayout loading;
    private LinearLayout serverError;

    private MaterialDatePicker<Long> datePicker;

    private Day currentDay = new Day(true);
    private StudentsAdapter studentsAdapter;
    private Long currentSelection;

    private DayService dayService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day);

        initRetrofit();
        findViews();
        initDatePicker();
        setListeners();

        if (savedInstanceState != null)
            onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        String dayJson = savedInstanceState.getString(CURRENT_DAY);
        long selection = savedInstanceState.getLong(CURRENT_SELECTION);
        String datePickerText = savedInstanceState.getString(DATE_PICKER_TRIGGER_TEXT);

        if (dayJson != null && !dayJson.isEmpty()) {
            Gson gson = new Gson();
            currentDay = gson.fromJson(dayJson, Day.class);
        }
        currentSelection = selection;
        if (datePickerText != null && !datePickerText.isEmpty())
            datePickerTrigger.setText(datePickerText);

        updateDayView();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (currentDay != null) {
            Gson gson = new Gson();
            outState.putString(CURRENT_DAY, gson.toJson(currentDay));
        }
        if (currentSelection != null)
            outState.putLong(CURRENT_SELECTION, currentSelection);
        outState.putString(DATE_PICKER_TRIGGER_TEXT, (String) datePickerTrigger.getText());
        super.onSaveInstanceState(outState);
    }

    protected void findViews() {
        classLabel = findViewById(R.id.classLabel);
        studentsListView = findViewById(R.id.studentsList);
        confirmButton = findViewById(R.id.confirmButton);
        datePickerTrigger = findViewById(R.id.datePickerTrigger);
        retry = findViewById(R.id.retry);
        buttonsGroup = findViewById(R.id.buttonsGroup);

        main = findViewById(R.id.main);
        loading = findViewById(R.id.loadingLayout);
        serverError = findViewById(R.id.serverError);
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
    }

    protected void setListeners() {
        datePicker.addOnPositiveButtonClickListener(this::onPositiveDatePickerButtonClick);
        studentsListView.setOnItemClickListener(this::onStudentItemClick);
        retry.setOnClickListener(this::onRetryButtonClick);
        confirmButton.setOnClickListener(this::onConfirmButtonClick);
    }

    // Date picker positive button click
    public void onPositiveDatePickerButtonClick(Long selection) {
        datePickerTrigger.setText(datePicker.getHeaderText());
        currentSelection = selection;
        updateDay();
    }

    // Students list item click
    public void onStudentItemClick(AdapterView<?> parent, View view, int position, long id) {
        Student student = (Student)parent.getItemAtPosition(position);
        student.absent = !student.absent;
        studentsAdapter.notifyDataSetChanged();
        updateConfirmButton();
    }

    // Retry button click
    public void onRetryButtonClick(View v) {
        updateDay();
    }

    public void onConfirmButtonClick(View v) {
        String formattedDate = formatDate(currentSelection);
        String absentStudentsIdsString = currentDay.getAbsentStudentsIdsString();
        Call<Void> call = dayService.updateDay(groupId, new DayPost(formattedDate, absentStudentsIdsString));
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                int code = response.code();
                if (code == 200) {
                    Snackbar.make(
                            main,
                            "Сработало :) код " + code,
                            Snackbar.LENGTH_LONG
                    ).setAnchorView(buttonsGroup).show();
                    updateDay();
                } else {
                    Snackbar.make(
                            main,
                            "Не сработало :( код " + code,
                            Snackbar.LENGTH_LONG
                    ).setAnchorView(buttonsGroup).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.d("UpdateDayCall", call.toString());
                Snackbar.make(
                        main,
                        "Ошибка, попробуйте ещё раз позднее",
                        Snackbar.LENGTH_LONG
                ).setAnchorView(buttonsGroup).show();
            }
        });
    }

    private void updateDay() {
        setLoadingStatus();
        String formattedDate = formatDate(currentSelection);
        Call<Day> call = dayService.getDay(groupId, formattedDate);
        call.enqueue(new Callback<Day>() {
            @Override
            public void onResponse(Call<Day> call, Response<Day> response) {
                currentDay = response.body();
                currentDay.updateLoadedAbsent();
                updateDayView();
            }

            @Override
            public void onFailure(Call<Day> call, Throwable t) {
                Log.d("DayCall", t.toString());
                Toast.makeText(MainActivity.this, "Error accessing server", Toast.LENGTH_SHORT).show();
                setServerErrorLayout();
            }
        });
    }

    protected void updateDayView() {
        setMainLayout();
        classLabel.setText(currentDay.name);
        updateStudentsAdapterData();
        updateConfirmButton();
        if (currentDay.noInfo()) {
            Snackbar.make(
                    main,
                    getResources().getString(R.string.no_info_for_day),
                    Snackbar.LENGTH_LONG
            ).setAnchorView(buttonsGroup).show();
        } else if (currentDay.noAbsent()) {
            Snackbar.make(
                    main,
                    getResources().getString(R.string.no_absent),
                    Snackbar.LENGTH_LONG
            ).setAnchorView(buttonsGroup).show();
        }
    }

    private void updateStudentsAdapterData() {
        studentsAdapter = new StudentsAdapter(this, currentDay.students);
        studentsListView.setAdapter(studentsAdapter);
    }

    protected void updateConfirmButton() {
        confirmButton.setEnabled(!currentDay.noChanges() || currentDay.noInfo());
        if (currentDay.noAbsent())
            confirmButton.setText(getResources().getString(R.string.confirmButtonNoOneAbsent));
        else
            confirmButton.setText(getResources().getString(R.string.confirmButtonDefault));
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

    private void setMainLayout() {
        main.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
        serverError.setVisibility(View.GONE);
    }

    private void setLoadingLayout() {
        loading.setVisibility(View.VISIBLE);
        main.setVisibility(View.GONE);
        serverError.setVisibility(View.GONE);
    }

    private void setServerErrorLayout() {
        serverError.setVisibility(View.VISIBLE);
        main.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);
    }

    protected void setLoadingStatus() {
        currentDay = new Day(true);
        updateDayView();
        setLoadingLayout();
    }
}
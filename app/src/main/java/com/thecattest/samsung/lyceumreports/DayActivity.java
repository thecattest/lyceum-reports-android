package com.thecattest.samsung.lyceumreports;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.thecattest.samsung.lyceumreports.Adapters.StudentsAdapter;
import com.thecattest.samsung.lyceumreports.DataServices.Day.Day;
import com.thecattest.samsung.lyceumreports.DataServices.Day.DayPost;
import com.thecattest.samsung.lyceumreports.DataServices.Day.DayService;
import com.thecattest.samsung.lyceumreports.DataServices.Day.Student;
import com.thecattest.samsung.lyceumreports.Fragments.LoadingFragment;
import com.thecattest.samsung.lyceumreports.Fragments.ServerErrorFragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DayActivity extends AppCompatActivity {

    public static final String GROUP_ID = "GROUP_ID";
    public static final String GROUP_LABEL = "GROUP_LABEL";

    private final static String CURRENT_DAY = "CURRENT_DAY";
    private final static String CURRENT_SELECTION = "CURRENT_SELECTION";
    private final static String DATE_PICKER_TRIGGER_TEXT = "DATE_PICKER_TRIGGER_TEXT";
    private static final String LAYOUT_TYPE = "LAYOUT_TYPE";
    private static final String LAYOUT_TYPE_MAIN = "LAYOUT_TYPE_MAIN";
    private static final String LAYOUT_TYPE_SERVER_ERROR = "LAYOUT_TYPE_SERVER_ERROR";

    private TextView classLabel;
    private ListView studentsListView;
    private Button confirmButton;
    private Button cancelButton;
    private Button datePickerTrigger;
    private RelativeLayout buttonsGroup;
    private RelativeLayout mainLayout;
    private SwipeRefreshLayout swipeRefreshLayout;

    private MaterialDatePicker<Long> datePicker;

    private final FragmentManager fragmentManager = getSupportFragmentManager();

    private LoginManager loginManager;
    private StatusManager statusManager;

    private Day currentDay = new Day(true);
    private StudentsAdapter studentsAdapter;
    private Long currentSelection;

    private DayService dayService;

    private int groupId;
    private String defaultGroupLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day);

        initRetrofit();
        findViews();
        initDatePicker();
        setListeners();

        loginManager = new LoginManager(this);
        statusManager = new StatusManager(mainLayout, fragmentManager, this::onRetryButtonClick);

        groupId = getIntent().getIntExtra(GROUP_ID, 6);
        defaultGroupLabel = getIntent().getStringExtra(GROUP_LABEL);
        currentDay.name = defaultGroupLabel;
        updateDayView();

        swipeRefreshLayout.setEnabled(false);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        long selection = savedInstanceState.getLong(CURRENT_SELECTION);
        String datePickerText = savedInstanceState.getString(DATE_PICKER_TRIGGER_TEXT);

        currentSelection = selection;
        if (datePickerText != null && !datePickerText.isEmpty())
            datePickerTrigger.setText(datePickerText);
        if (statusManager.loadLayoutType(savedInstanceState)) {
            String dayJson = savedInstanceState.getString(CURRENT_DAY);
            if (dayJson != null && !dayJson.isEmpty()) {
                Gson gson = new Gson();
                currentDay = gson.fromJson(dayJson, Day.class);
                swipeRefreshLayout.setEnabled(!currentDay.empty);
            }
            updateDayView();
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (currentDay != null) {
            Gson gson = new Gson();
            outState.putString(CURRENT_DAY, gson.toJson(currentDay));
        }
        if (currentSelection != null)
            outState.putLong(CURRENT_SELECTION, currentSelection);
        outState.putString(DATE_PICKER_TRIGGER_TEXT, (String) datePickerTrigger.getText());
        statusManager.saveLayoutType(outState);
    }

    protected void findViews() {
        classLabel = findViewById(R.id.classLabel);
        studentsListView = findViewById(R.id.studentsList);
        confirmButton = findViewById(R.id.confirmButton);
        cancelButton = findViewById(R.id.cancelButton);
        datePickerTrigger = findViewById(R.id.datePickerTrigger);
        buttonsGroup = findViewById(R.id.buttonsGroup);

        mainLayout = findViewById(R.id.main);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
    }

    protected void initRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        dayService = retrofit.create(DayService.class);
    }

    protected void initDatePicker() {
        datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getResources().getString(R.string.select_date_label))
                .build();
        datePickerTrigger.setOnClickListener(v -> datePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER"));
    }

    protected void setListeners() {
        datePicker.addOnPositiveButtonClickListener(this::onPositiveDatePickerButtonClick);
        studentsListView.setOnItemClickListener(this::onStudentItemClick);
        confirmButton.setOnClickListener(this::onConfirmButtonClick);
        cancelButton.setOnClickListener(this::onCancelButtonClick);
        swipeRefreshLayout.setOnRefreshListener(this::onRefresh);
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

    // Refresh action
    public void onRefresh() {
        if (!currentDay.empty)
            updateDay();
        swipeRefreshLayout.setRefreshing(false);
    }

    // Retry button click
    public void onRetryButtonClick(View v) {
        updateDay();
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(DayActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    // Cancel button click
    public void onCancelButtonClick(View v) {
        onBackPressed();
    }

    // Confirm button click
    public void onConfirmButtonClick(View v) {
        setLoadingStatus(true);
        String formattedDate = formatDate(currentSelection);
        String absentStudentsIdsString = currentDay.getAbsentStudentsIdsString();
        Call<Void> call = dayService.updateDay(loginManager.getCookies(), groupId, new DayPost(formattedDate, absentStudentsIdsString));
        call.enqueue(new DefaultCallback<Void>(loginManager, mainLayout) {
            @Override
            public void onResponse200(Response<Void> response) {
                Snackbar.make(
                        mainLayout,
                        "Сработало :) код " + response.code(),
                        Snackbar.LENGTH_SHORT
                ).setAnchorView(buttonsGroup).show();
                updateDay();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.d("UpdateDayCall", call.toString());
                Snackbar.make(
                        mainLayout,
                        "Ошибка, попробуйте ещё раз позднее",
                        Snackbar.LENGTH_LONG
                ).setAnchorView(buttonsGroup).show();
                statusManager.setMainLayout();
            }

            @Override
            protected void onResponse401() {}
        });
    }

    private void updateDay() {
        setLoadingStatus();
        String formattedDate = formatDate(currentSelection);
        Call<Day> call = dayService.getDay(loginManager.getCookies(), groupId, formattedDate);
        call.enqueue(new DefaultCallback<Day>(loginManager, mainLayout) {
            @Override
            public void onResponse200(Response<Day> response) {
                currentDay = response.body();
                currentDay.updateLoadedAbsent();
                updateDayView();
                swipeRefreshLayout.setEnabled(true);
            }

            @Override
            public void onFailure(Call<Day> call, Throwable t) {
                Log.d("DayCall", t.toString());
                Toast.makeText(DayActivity.this, "Error accessing server", Toast.LENGTH_SHORT).show();
                statusManager.setServerErrorLayout();
            }

            @Override
            protected void onResponse401() {}
        });
    }

    protected void updateDayView() {
        statusManager.setMainLayout();
        classLabel.setText(currentDay.name);
        updateStudentsAdapterData();
        updateConfirmButton();
        if (currentDay.noInfo()) {
            Snackbar.make(
                    mainLayout,
                    getResources().getString(R.string.no_info_for_day),
                    Snackbar.LENGTH_LONG
            ).setAnchorView(buttonsGroup).show();
        } else if (currentDay.noAbsent()) {
            Snackbar.make(
                    mainLayout,
                    getResources().getString(R.string.no_absent),
                    Snackbar.LENGTH_LONG
            ).setAnchorView(buttonsGroup).show();
        }
        buttonsGroup.setVisibility(currentDay.empty ? View.GONE : View.VISIBLE);
        confirmButton.setVisibility(currentDay.canEdit ? View.VISIBLE : View.GONE);
    }

    private void updateStudentsAdapterData() {
        studentsAdapter = new StudentsAdapter(this, currentDay.students);
        studentsListView.setAdapter(studentsAdapter);
    }

    protected void updateConfirmButton() {
        confirmButton.setEnabled(!currentDay.noChanges() || currentDay.noInfo());
        if (currentDay.noAbsent() || currentDay.noInfo() && currentDay.getAbsentStudentsIds().size() == 0)
            confirmButton.setText(getResources().getString(R.string.confirm_button_no_one_absent));
        else
            confirmButton.setText(getResources().getString(R.string.confirm_button_default));
    }

    public static String formatDate(Long selection) {
        Date selectedDate = new Date(selection);
        String serverDateFormat = "yyyy-MM-dd";
        @SuppressLint("SimpleDateFormat")
        DateFormat df = new SimpleDateFormat(serverDateFormat);
        String formattedDate = df.format(selectedDate);

        Log.d("DatePicker", formattedDate);
        return formattedDate;
    }

    protected void setLoadingStatus(boolean mainIsVisible) {
        if (!mainIsVisible) {
            currentDay = new Day(true);
            currentDay.name = defaultGroupLabel;
            updateDayView();
        }
        statusManager.setLoadingLayout(mainIsVisible);
    }

    protected void setLoadingStatus() {
        setLoadingStatus(false);
    }
}
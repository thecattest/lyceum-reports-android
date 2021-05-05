package com.thecattest.samsung.lyceumreports;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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

import retrofit2.Call;
import retrofit2.Callback;
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
    private LinearLayout serverErrorLayout;
    private SwipeRefreshLayout swipeRefreshLayout;

    private MaterialDatePicker<Long> datePicker;

    private final FragmentManager fragmentManager = getSupportFragmentManager();
    private LoadingFragment loadingFragment;
    private ServerErrorFragment serverErrorFragment;

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
        createFragments();
        setListeners();

        groupId = getIntent().getIntExtra(GROUP_ID, 6);
        defaultGroupLabel = getIntent().getStringExtra(GROUP_LABEL);

        currentDay.name = defaultGroupLabel;
        updateDayView();
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        String dayJson = savedInstanceState.getString(CURRENT_DAY);
        long selection = savedInstanceState.getLong(CURRENT_SELECTION);
        String datePickerText = savedInstanceState.getString(DATE_PICKER_TRIGGER_TEXT);
        String layout = savedInstanceState.getString(LAYOUT_TYPE);
        Log.d(LAYOUT_TYPE + " in", layout);

        if (dayJson != null && !dayJson.isEmpty()) {
            Gson gson = new Gson();
            currentDay = gson.fromJson(dayJson, Day.class);
        }
        currentSelection = selection;
        if (datePickerText != null && !datePickerText.isEmpty())
            datePickerTrigger.setText(datePickerText);
        switch (layout) {
            case LAYOUT_TYPE_SERVER_ERROR:
                setServerErrorLayout();
                break;
            case LAYOUT_TYPE_MAIN:
                setMainLayout();
                updateDayView();
        }
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
        String layout = "";
        if (mainLayout.getVisibility() == View.VISIBLE) {
            layout = LAYOUT_TYPE_MAIN;
        }
        else if (serverErrorLayout.getVisibility() == View.VISIBLE) {
            layout = LAYOUT_TYPE_SERVER_ERROR;
        }
        Log.d(LAYOUT_TYPE + " out", layout);
        outState.putString(LAYOUT_TYPE, layout);
        super.onSaveInstanceState(outState);
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
        serverErrorLayout = findViewById(R.id.serverErrorLayout);
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
                .setTitleText(getResources().getString(R.string.selectDateLabel))
                .build();
        datePickerTrigger.setOnClickListener(v -> datePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER"));
    }

    protected void createFragments() {
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

    // Cancel button click
    public void onCancelButtonClick(View v) {
        finish();
    }

    // Confirm button click
    public void onConfirmButtonClick(View v) {
        setLoadingStatus(true);
        String formattedDate = formatDate(currentSelection);
        String absentStudentsIdsString = currentDay.getAbsentStudentsIdsString();
        Call<Void> call = dayService.updateDay(groupId, new DayPost(formattedDate, absentStudentsIdsString));
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                int code = response.code();
                if (code == 200) {
                    Snackbar.make(
                            mainLayout,
                            "Сработало :) код " + code,
                            Snackbar.LENGTH_SHORT
                    ).setAnchorView(buttonsGroup).show();
                    updateDay();
                } else {
                    Snackbar.make(
                            mainLayout,
                            "Не сработало :( код " + code,
                            Snackbar.LENGTH_LONG
                    ).setAnchorView(buttonsGroup).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.d("UpdateDayCall", call.toString());
                Snackbar.make(
                        mainLayout,
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
                Toast.makeText(DayActivity.this, "Error accessing server", Toast.LENGTH_SHORT).show();
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
        mainLayout.setVisibility(View.VISIBLE);
        setLoadingFragmentVisibility(false);
        setServerErrorFragmentVisibility(false);
    }

    private void setLoadingLayout() {
        setLoadingLayout(false);
    }

    private void setLoadingLayout(boolean mainIsVisible) {
        setLoadingFragmentVisibility(true);
        setServerErrorFragmentVisibility(false);
        if (mainIsVisible) {
            mainLayout.setVisibility(View.VISIBLE);
        } else {
            mainLayout.setVisibility(View.GONE);
        }
    }

    private void setServerErrorLayout() {
        setServerErrorFragmentVisibility(true);
        mainLayout.setVisibility(View.GONE);
        setLoadingFragmentVisibility(false);
    }

    protected void setLoadingStatus(boolean mainIsVisible) {
        if (!mainIsVisible) {
            currentDay = new Day(true);
            currentDay.name = defaultGroupLabel;
            updateDayView();
        }
        setLoadingLayout(mainIsVisible);
    }

    protected void setLoadingStatus() {
        setLoadingStatus(false);
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
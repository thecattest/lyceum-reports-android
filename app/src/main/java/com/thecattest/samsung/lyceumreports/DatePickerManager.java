package com.thecattest.samsung.lyceumreports;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;

import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DatePickerManager {

    private final static String CURRENT_SELECTION = "CURRENT_SELECTION";
    private final static String DATE_PICKER_TRIGGER_TEXT = "DATE_PICKER_TRIGGER_TEXT";

    private MaterialDatePicker<Long> datePicker;
    private Long currentSelection;

    private final String defaultTitle;
    private final TextView datePickerTrigger;
    private final FragmentManager fragmentManager;
    private final DatePickerListener callback;

    public DatePickerManager(String titleText,
                             TextView datePickerTrigger,
                             FragmentManager fragmentManager,
                             DatePickerListener callback) {
        defaultTitle = titleText;
        this.datePickerTrigger = datePickerTrigger;
        this.fragmentManager = fragmentManager;
        this.callback = callback;

        initDatePicker();
    }

    private void initDatePicker() {
        datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(defaultTitle)
                .build();
        datePickerTrigger.setOnClickListener(v -> datePicker.show(fragmentManager, "MATERIAL_DATE_PICKER"));
        datePicker.addOnPositiveButtonClickListener(this::onPositiveDatePickerButtonClick);
    }

    private void onPositiveDatePickerButtonClick(Long selection) {
        datePickerTrigger.setText(datePicker.getHeaderText());
        currentSelection = selection;
        callback.onPositiveButtonClick();
    }

    public String getDate() {
        Date selectedDate = new Date(currentSelection);
        String serverDateFormat = "yyyy-MM-dd";
        @SuppressLint("SimpleDateFormat")
        DateFormat df = new SimpleDateFormat(serverDateFormat);
        String formattedDate = df.format(selectedDate);

        Log.d("DatePicker", formattedDate);
        return formattedDate;
    }

    public void saveToBundle(Bundle outState) {
        if (currentSelection != null)
            outState.putLong(CURRENT_SELECTION, currentSelection);
        outState.putString(DATE_PICKER_TRIGGER_TEXT, (String) datePickerTrigger.getText());
    }

    public void loadFromBundle(Bundle savedInstanceState) {
        currentSelection = savedInstanceState.getLong(CURRENT_SELECTION);

        String datePickerText = savedInstanceState.getString(DATE_PICKER_TRIGGER_TEXT);
        if (datePickerText != null && !datePickerText.isEmpty())
            datePickerTrigger.setText(datePickerText);
    }

    public interface DatePickerListener {
        void onPositiveButtonClick();
    }
}

package com.thecattest.samsung.lyceumreports.Managers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.thecattest.samsung.lyceumreports.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DatePickerManager {

    private final static String TAG = "MATERIAL_DATE_PICKER";

    private final static String CURRENT_SELECTION = "CURRENT_SELECTION";
    private final static String DATE_PICKER_TRIGGER_TEXT = "DATE_PICKER_TRIGGER_TEXT";

    private MaterialDatePicker<Long> datePicker;
    private Long currentSelection = null;

    private boolean enabled = true;

    private final String defaultTitle;
    private final String serverDateFormat;
    private final TextView datePickerTrigger;
    private final FragmentManager fragmentManager;
    private final DatePickerListener callback;

    public DatePickerManager(Context context,
                             TextView datePickerTrigger,
                             FragmentManager fragmentManager,
                             DatePickerListener callback) {
        defaultTitle = context.getResources().getString(R.string.button_date_picker_trigger);
        serverDateFormat = context.getResources().getString(R.string.date_format);
        this.datePickerTrigger = datePickerTrigger;
        this.fragmentManager = fragmentManager;
        this.callback = callback;

        initDatePicker();
    }

    private void initDatePicker() {
        datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(defaultTitle)
                .build();
        datePickerTrigger.setOnClickListener(this::showDatePicker);
        datePicker.addOnPositiveButtonClickListener(this::onPositiveDatePickerButtonClick);
    }

    public void showDatePicker() {
        if (enabled)
            datePicker.show(fragmentManager, TAG);
    }

    public void showDatePicker(View v) {
        showDatePicker();
    }

    private void onPositiveDatePickerButtonClick(Long selection) {
        datePickerTrigger.setText(datePicker.getHeaderText());
        currentSelection = selection;
        callback.onPositiveButtonClick();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDate() {
        if (currentSelection == null)
            return null;
        Date selectedDate = new Date(currentSelection);
        @SuppressLint("SimpleDateFormat")
        DateFormat df = new SimpleDateFormat(serverDateFormat);

        return df.format(selectedDate);
    }

    public boolean isEmpty() {
        return getDate() == null;
    }

    public void saveToBundle(Bundle outState) {
        if (!isEmpty())
            outState.putLong(CURRENT_SELECTION, currentSelection);
        outState.putString(DATE_PICKER_TRIGGER_TEXT, (String) datePickerTrigger.getText());
    }

    public void loadFromBundle(Bundle savedInstanceState) {
        currentSelection = savedInstanceState.getLong(CURRENT_SELECTION, -1);
        if (currentSelection < 0)
            currentSelection = null;

        String datePickerText = savedInstanceState.getString(DATE_PICKER_TRIGGER_TEXT);
        if (datePickerText != null && !datePickerText.isEmpty())
            datePickerTrigger.setText(datePickerText);
    }

    public interface DatePickerListener {
        void onPositiveButtonClick();
    }
}

package com.thecattest.samsung.lyceumreports.DataModels.SummaryDay;

import android.os.Bundle;

import com.google.gson.Gson;

import java.util.ArrayList;

public class SummaryDay {

    private final static String SUMMARY_DAY = "SUMMARY_DAY";

    public String date;
    public ArrayList<SummaryDayRow> groups = new ArrayList<>();

    public boolean isEmpty() {
        return groups.size() == 0;
    }

    public void saveToBundle(Bundle outState) {
        if (!isEmpty()) {
            Gson gson = new Gson();
            outState.putString(SUMMARY_DAY, gson.toJson(this));
        }
    }

    public void loadFromBundle(Bundle savedInstanceState) {
        String summaryDayJson = savedInstanceState.getString(SUMMARY_DAY);
        if (summaryDayJson != null && !summaryDayJson.isEmpty()) {
            Gson gson = new Gson();
            SummaryDay loadedSummaryDay = gson.fromJson(summaryDayJson, SummaryDay.class);

            this.date = loadedSummaryDay.date;
            this.groups = loadedSummaryDay.groups;
        }
    }

    @Override
    public String toString() {
        return "SummaryDay{" +
                "date='" + date + '\'' +
                ", groups=" + groups +
                '}';
    }
}

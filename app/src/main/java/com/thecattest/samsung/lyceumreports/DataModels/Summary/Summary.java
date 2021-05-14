package com.thecattest.samsung.lyceumreports.DataModels.Summary;

import android.content.Context;

public class Summary implements SummaryRowInterface {
    public int id;
    public String letter;
    public short number;
    public SummaryRows days;

    public int getId() {
        return id;
    }

    public String getLabel() {
        return number + letter;
    }

    @Override
    public String getTodayDate(Context context) {
        return days.getTodayDate(context);
    }

    @Override
    public String getTodayAbsentStudentsString(Context context) {
        return days.getTodayAbsentStudentsString(context);
    }

    @Override
    public String getYesterdayDate(Context context) {
        return days.getYesterdayDate(context);
    }

    @Override
    public String getYesterdayAbsentStudentsString(Context context) {
        return days.getYesterdayAbsentStudentsString(context);
    }

    @Override
    public String toString() {
        return "Summary{" +
                "id=" + id +
                ", letter='" + letter + '\'' +
                ", number=" + number +
                ", days=" + days +
                '}';
    }
}

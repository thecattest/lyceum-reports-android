package com.thecattest.samsung.lyceumreports.DataServices;

public class SummaryDays implements SummaryDayInterface {
    public SummaryDay today;
    public SummaryDay yesterday;

    public String getTodayDate() {
        return today.getHumanDate();
    }

    public String getTodayAbsentStudentsString() {
        return today.getAbsentStudentsString();
    }

    public String getYesterdayDate() {
        return yesterday.getHumanDate();
    }

    public String getYesterdayAbsentStudentsString() {
        return yesterday.getAbsentStudentsString();
    }
}

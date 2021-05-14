package com.thecattest.samsung.lyceumreports.DataModels.Summary;

public class SummaryRows implements SummaryRowInterface {
    public SummaryRow today;
    public SummaryRow yesterday;

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

    @Override
    public String toString() {
        return "SummaryDays{" +
                "today=" + today +
                ", yesterday=" + yesterday +
                '}';
    }
}

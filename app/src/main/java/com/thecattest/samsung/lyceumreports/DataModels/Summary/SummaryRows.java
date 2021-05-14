package com.thecattest.samsung.lyceumreports.DataModels.Summary;

import android.content.Context;

public class SummaryRows implements SummaryRowInterface {
    public SummaryRow today;
    public SummaryRow yesterday;

    public String getTodayDate(Context context) {
        return today.getHumanDate(context);
    }

    public String getTodayAbsentStudentsString(Context context) {
        return today.getAbsentStudentsString(context);
    }

    public String getYesterdayDate(Context context) {
        return yesterday.getHumanDate(context);
    }

    public String getYesterdayAbsentStudentsString(Context context) {
        return yesterday.getAbsentStudentsString(context);
    }

    @Override
    public String toString() {
        return "SummaryDays{" +
                "today=" + today +
                ", yesterday=" + yesterday +
                '}';
    }
}

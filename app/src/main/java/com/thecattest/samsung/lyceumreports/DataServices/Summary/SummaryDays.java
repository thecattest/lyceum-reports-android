package com.thecattest.samsung.lyceumreports.DataServices.Summary;

import com.thecattest.samsung.lyceumreports.DataServices.Summary.SummaryDay;
import com.thecattest.samsung.lyceumreports.DataServices.Summary.SummaryDayInterface;

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

    @Override
    public String toString() {
        return "SummaryDays{" +
                "today=" + today +
                ", yesterday=" + yesterday +
                '}';
    }
}

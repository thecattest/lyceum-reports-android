package com.thecattest.samsung.lyceumreports.DataServices.SummaryDay;

import java.util.ArrayList;

public class SummaryDay {
    public String date;
    public ArrayList<SummaryDayRow> groups = new ArrayList<>();

    @Override
    public String toString() {
        return "SummaryDay{" +
                "date='" + date + '\'' +
                ", groups=" + groups +
                '}';
    }
}

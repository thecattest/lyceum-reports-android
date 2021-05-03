package com.thecattest.samsung.lyceumreports.DataServices;

public class Summary implements SummaryDayInterface {
    public int id;
    public String letter;
    public short number;
    public SummaryDays days;

    public int getId() {
        return id;
    }

    public String getLabel() {
        return number + letter;
    }

    @Override
    public String getTodayDate() {
        return days.getTodayDate();
    }

    @Override
    public String getTodayAbsentStudentsString() {
        return days.getTodayAbsentStudentsString();
    }

    @Override
    public String getYesterdayDate() {
        return days.getYesterdayDate();
    }

    @Override
    public String getYesterdayAbsentStudentsString() {
        return days.getYesterdayAbsentStudentsString();
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

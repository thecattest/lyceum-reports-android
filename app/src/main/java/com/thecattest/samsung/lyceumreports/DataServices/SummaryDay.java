package com.thecattest.samsung.lyceumreports.DataServices;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SummaryDay {
    public String date;
    public String status;
    public ArrayList<String> students;

    public long getUnix() {
        @SuppressLint("SimpleDateFormat")
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        try {
            date = df.parse(getDate());
        } catch (ParseException e) {
            date = null;
        }
        return (date != null ? date.getTime() : 0) / 1000;
    }

    public String getDate() {
        return date;
    }

    public String getHumanDate() {
        @SuppressLint("SimpleDateFormat")
        DateFormat df = new SimpleDateFormat("dd MMM");
        return df.format(getUnix());
    }

    public String getAbsentStudentsString() {
        if (status.equals(STATUS.OK)) {
            StringBuilder absentStudentsString = new StringBuilder();
            for (String absentStudent : students)
                absentStudentsString.append(absentStudent);
            return absentStudentsString.toString();
        } else {
            return "Все в классе";
        }
    }

    public static class STATUS {
        public static String OK = "ok";
        public static String EMPTY = "empty";
    }
}

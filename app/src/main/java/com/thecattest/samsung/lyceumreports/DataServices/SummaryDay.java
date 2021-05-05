package com.thecattest.samsung.lyceumreports.DataServices;

import android.annotation.SuppressLint;
import android.util.Log;

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
        Log.d("Date", date);
        Date date;
        try {
            date = df.parse(getDate());
        } catch (ParseException e) {
            date = null;
        }
        long unix = (date != null ? date.getTime() : 0) / 1000;
        Log.d("Date Unix", String.valueOf(unix));
        return unix;
    }

    public String getDate() {
        return date;
    }

    public String getHumanDate() {
        String[] months = {
                "", "янв", "фев", "мар",
                "апр", "мая", "июн", "июл",
                "авг", "сен", "окт", "ноя", "дек"
        };
        String[] dateArr = getDate().split("-");

        String day = dateArr[2];
        if (day.charAt(0) == '0')
            day = String.valueOf(day.charAt(1));
        String month = months[Integer.parseInt(dateArr[1])];

        String formattedDate = day + " " + month;
        Log.d("Formatted date", formattedDate);
        return formattedDate;
    }

    public String getAbsentStudentsString() {
        if (status.equals(STATUS.OK)) {
            if (students.size() != 0) {
                StringBuilder absentStudentsString = new StringBuilder();
                for (String absentStudent : students)
                    absentStudentsString.append(absentStudent);
                return absentStudentsString.toString();
            } else {
                return "Все в классе";
            }
        } else {
            return "Нет данных";
        }
    }

    public static class STATUS {
        public static String OK = "ok";
        public static String EMPTY = "empty";
    }

    @Override
    public String toString() {
        return "SummaryDay{" +
                "date='" + date + '\'' +
                ", status='" + status + '\'' +
                ", students=" + students +
                '}';
    }
}

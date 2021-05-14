package com.thecattest.samsung.lyceumreports.DataModels.Summary;

import android.util.Log;

import java.util.ArrayList;

public class SummaryDay {
    public String date;
    public String status;
    public ArrayList<String> students;

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
                for (String absentStudent : students) {
                    absentStudentsString.append(absentStudent);
                    absentStudentsString.append(", ");
                }
                return absentStudentsString.substring(0, absentStudentsString.length() - 2);
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

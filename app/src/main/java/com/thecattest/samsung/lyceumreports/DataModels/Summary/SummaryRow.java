package com.thecattest.samsung.lyceumreports.DataModels.Summary;

import android.content.Context;

import com.thecattest.samsung.lyceumreports.R;

import java.util.ArrayList;

public class SummaryRow {
    public String date;
    public String status;
    public ArrayList<String> students;

    public String getDate() {
        return date;
    }

    public String getHumanDate(Context context) {
        String[] months = context.getResources().getStringArray(R.array.date_months_shortened);
        String[] dateArr = getDate().split("-");

        String day = dateArr[2];
        if (day.charAt(0) == '0')
            day = String.valueOf(day.charAt(1));
        String month = months[Integer.parseInt(dateArr[1])];

        String formattedDate = day + " " + month;
        return formattedDate;
    }

    public String getAbsentStudentsString(Context context) {
        if (status.equals(STATUS.OK)) {
            if (students.size() != 0) {
                StringBuilder absentStudentsString = new StringBuilder();
                for (String absentStudent : students) {
                    absentStudentsString.append(absentStudent);
                    absentStudentsString.append(", ");
                }
                return absentStudentsString.substring(0, absentStudentsString.length() - 2);
            } else {
                return context.getResources().getString(R.string.summary_status_no_absent);
            }
        } else {
            return context.getResources().getString(R.string.summary_status_no_info);
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

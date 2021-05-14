package com.thecattest.samsung.lyceumreports.DataModels.SummaryDay;

import android.content.Context;

import com.thecattest.samsung.lyceumreports.R;

import java.util.ArrayList;

public class SummaryDayRow {
    public int id;
    public String name;
    public String status;
    public ArrayList<String> students;

    public String getName() {
        return name;
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
        return "SummaryDayRow{" +
                "id=" + id +
                ", status='" + status + '\'' +
                ", name='" + name + '\'' +
                ", students=" + students.toString() +
                '}';
    }
}

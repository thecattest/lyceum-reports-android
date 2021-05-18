package com.thecattest.samsung.lyceumreports.DataModels;

import android.content.Context;

import com.google.gson.annotations.SerializedName;
import com.thecattest.samsung.lyceumreports.R;

import java.util.ArrayList;

public class Day {
    public int id;
    public String date;
    @SerializedName("group_id")
    public int groupId;

    public ArrayList<Student> absent;

    public String getHumanDate(Context context) {
        String[] months = context.getResources().getStringArray(R.array.date_months_shortened);
        String[] dateArr = date.split("-");

        String day = dateArr[2];
        if (day.charAt(0) == '0')
            day = String.valueOf(day.charAt(1));
        String month = months[Integer.parseInt(dateArr[1])];

        return day + " " + month;
    }

    public String getAbsentStudentsString(Context context) {
        if (absent.size() != 0) {
            StringBuilder absentStudentsString = new StringBuilder();
            for (Student absentStudent : absent) {
                absentStudentsString.append(absentStudent.surname);
                absentStudentsString.append(", ");
            }
            return absentStudentsString.substring(0, absentStudentsString.length() - 2);
        } else {
            return context.getResources().getString(R.string.summary_status_no_absent);
        }
    }

    @Override
    public String toString() {
        return "Day{" +
                "id=" + id +
                ", date='" + date + '\'' +
                ", groupId=" + groupId +
                ", absent=" + absent +
                '}';
    }
}

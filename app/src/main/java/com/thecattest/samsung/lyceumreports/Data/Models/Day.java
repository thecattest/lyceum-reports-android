package com.thecattest.samsung.lyceumreports.Data.Models;

import android.content.Context;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.thecattest.samsung.lyceumreports.R;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "days")
public class Day {
    @SerializedName("id")
    @ColumnInfo(name = "did")
    @PrimaryKey
    public int did;

    @SerializedName("date")
    @ColumnInfo(name = "date")
    public String date;

    @SerializedName("group_id")
    @ColumnInfo(name = "group_id")
    public int groupId;

    @Ignore
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

    public String getAbsentStudentsString(Context context, List<Student> absent) {
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
                "id=" + did +
                ", date='" + date + '\'' +
                ", groupId=" + groupId +
                ", absent=" + absent +
                '}';
    }
}

package com.thecattest.samsung.lyceumreports.Data.Models;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.thecattest.samsung.lyceumreports.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class GroupWithDaysAndStudents {
    @Embedded public Group group;
    @Relation(
            parentColumn = "gid",
            entityColumn = "group_id",
            entity = Day.class
    )
    public List<DayWithAbsent> days;
    @Relation(
            parentColumn = "gid",
            entityColumn = "group_id"
    )
    public List<Student> students;

    private Date yesterday() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }

    private Date today() {
        return Calendar.getInstance().getTime();
    }

    public String getServerFormatDate(Context context, Date date) {
        @SuppressLint("SimpleDateFormat")
        DateFormat dateFormat = new SimpleDateFormat(context.getResources().getString(R.string.date_format));
        return dateFormat.format(date);
    }

    public String getHumanDate(Context context, String serverFormatDate) {
        String[] months = context.getResources().getStringArray(R.array.date_months_shortened);
        String[] dateArr = serverFormatDate.split("-");

        String day = dateArr[2];
        if (day.charAt(0) == '0')
            day = String.valueOf(day.charAt(1));
        String month = months[Integer.parseInt(dateArr[1])];

        return day + " " + month;
    }

    public String getYesterdayDateServerFormat(Context context) {
        return getServerFormatDate(context, yesterday());
    }

    public String getTodayDateServerFormat(Context context) {
        return getServerFormatDate(context, today());
    }
}

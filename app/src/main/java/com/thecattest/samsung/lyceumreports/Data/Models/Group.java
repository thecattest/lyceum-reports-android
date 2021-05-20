package com.thecattest.samsung.lyceumreports.Data.Models;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.thecattest.samsung.lyceumreports.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

@Entity(tableName = "groups",
        indices = {
        @Index(
                value = {"number", "letter"},
                unique = true)
})
public class Group {
    @SerializedName("id")
    @ColumnInfo(name = "gid")
    @PrimaryKey
    public int gid;

    @SerializedName("number")
    @ColumnInfo(name = "number")
    public short number;

    @SerializedName("letter")
    @ColumnInfo(name = "letter")
    public String letter;

    @Ignore
    public ArrayList<Student> students;
    @Ignore
    public ArrayList<Day> days;

    public String getLabel() {
        return number + letter;
    }

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

    @Override
    public String toString() {
        return "Group{" +
                "id=" + gid +
                ", number=" + number +
                ", letter='" + letter + '\'' +
                ", students=" + students +
                ", days=" + days +
                '}';
    }
}

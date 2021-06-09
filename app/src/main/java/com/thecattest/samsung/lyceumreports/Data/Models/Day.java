package com.thecattest.samsung.lyceumreports.Data.Models;

import android.content.Context;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.thecattest.samsung.lyceumreports.Data.Models.Relations.DayWithAbsent;
import com.thecattest.samsung.lyceumreports.R;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "days",
        indices = {
        @Index(
                value = {"group_id", "date"},
                unique = true)
})
public class Day {
    @SerializedName("id")
    @ColumnInfo(name = "did")
    @PrimaryKey(autoGenerate = true)
    public int did;

    @SerializedName("date")
    @ColumnInfo(name = "date")
    public String date;

    @SerializedName("group_id")
    @ColumnInfo(name = "group_id")
    public int groupId;

    @ColumnInfo(name = "is_synced")
    public boolean isSyncedWithServer = true;

    @Ignore
    public ArrayList<Student> absent = new ArrayList<>();

    @Ignore
    public ArrayList<Student> loadedAbsent = new ArrayList<>();
    @Ignore
    public boolean isLoadedFromServer = true;

    public Day() {}

    public Day(int groupId, String date, DayWithAbsent dayWithAbsent) {
        this.groupId = groupId;
        if (dayWithAbsent.day != null) {
            this.date = dayWithAbsent.day.date;
            absent = new ArrayList<>(dayWithAbsent.absent);
            loadedAbsent = new ArrayList<>(dayWithAbsent.absent);
            isSyncedWithServer = dayWithAbsent.day.isSyncedWithServer;
        } else {
            this.date = date;
            isSyncedWithServer = false;
            isLoadedFromServer = false;
        }
    }

    public void toggleAbsent(Student student) {
        if (absent.contains(student)) {
            absent.remove(student);
        } else {
            absent.add(student);
        }

        boolean equals = true;
        for (Student st : loadedAbsent) {
            equals = absent.contains(st);
            if (!equals)
                break;
        }
        if (equals)
            for (Student st : absent) {
                equals = loadedAbsent.contains(st);
                if (!equals)
                    break;
            }
        isSyncedWithServer = isLoadedFromServer && equals;
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

    public boolean noLoadedAbsent() {
        return isLoadedFromServer && loadedAbsent.isEmpty();
    }

    public boolean noAbsent() {
        return absent.isEmpty();
    }

    public boolean isChanged() {
        return !isSyncedWithServer;
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

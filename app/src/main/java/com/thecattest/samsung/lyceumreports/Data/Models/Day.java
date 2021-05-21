package com.thecattest.samsung.lyceumreports.Data.Models;

import android.content.Context;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
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

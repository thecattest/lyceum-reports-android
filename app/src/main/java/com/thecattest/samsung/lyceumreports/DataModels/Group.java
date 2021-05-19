package com.thecattest.samsung.lyceumreports.DataModels;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

@Entity(tableName = "groups")
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

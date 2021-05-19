package com.thecattest.samsung.lyceumreports.Data.Models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "students")
public class Student {
    @PrimaryKey
    @SerializedName("id")
    @ColumnInfo(name = "sid")
    public int sid;

    @SerializedName("surname")
    @ColumnInfo(name = "surname")
    public String surname;

    @SerializedName("name")
    @ColumnInfo(name = "name")
    public String name;

    @SerializedName("group_id")
    @ColumnInfo(name = "group_id")
    public int groupId;

    public String getName() {
        return surname + " " + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Student student = (Student) o;

        return sid == student.sid;
    }

    @Override
    public int hashCode() {
        return sid;
    }

    @Override
    public String toString() {
        return "Student{" +
                "sid=" + sid +
                ", surname='" + surname + '\'' +
                ", name='" + name + '\'' +
                ", groupId=" + groupId +
                '}';
    }
}

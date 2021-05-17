package com.thecattest.samsung.lyceumreports.DataModels;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Day {
    public int id;
    public String date;
    @SerializedName("group_id")
    public int groupId;

    public ArrayList<Student> absent;

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

package com.thecattest.samsung.lyceumreports.DataModels;

import com.google.gson.annotations.SerializedName;

public class Student {
    public int id;
    public String surname;
    public String name;
    @SerializedName("group_id")
    public int groupId;

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", surname='" + surname + '\'' +
                ", name='" + name + '\'' +
                ", groupId=" + groupId +
                '}';
    }
}

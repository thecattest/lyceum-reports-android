package com.thecattest.samsung.lyceumreports.DataModels;

import com.google.gson.annotations.SerializedName;

public class Student {
    public int id;
    public String surname;
    public String name;
    @SerializedName("group_id")
    public int groupId;

    public String getName() {
        return surname + " " + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Student student = (Student) o;

        return id == student.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

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

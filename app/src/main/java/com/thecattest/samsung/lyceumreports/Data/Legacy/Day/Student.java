package com.thecattest.samsung.lyceumreports.Data.Legacy.Day;

public class Student {
    public int id;
    public boolean absent;
    public String name;

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", absent=" + absent +
                ", name='" + name + '\'' +
                '}';
    }
}
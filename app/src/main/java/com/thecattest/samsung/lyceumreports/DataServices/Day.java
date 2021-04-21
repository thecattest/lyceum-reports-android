package com.thecattest.samsung.lyceumreports.DataServices;

import java.util.ArrayList;

public class Day {
    public int id;
    public String name;
    public String status;
    public ArrayList<Student> students;

    @Override
    public String toString() {
        return "Day{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", students=" + students.toString() +
                '}';
    }
}

class Student {
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

package com.thecattest.samsung.lyceumreports.DataModels;

import java.util.ArrayList;

public class Group {
    public int id;
    public short number;
    public String letter;

    public ArrayList<Student> students;
    public ArrayList<Day> days;

    @Override
    public String toString() {
        return "Group{" +
                "id=" + id +
                ", number=" + number +
                ", letter='" + letter + '\'' +
                ", students=" + students +
                ", days=" + days +
                '}';
    }
}

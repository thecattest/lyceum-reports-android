package com.thecattest.samsung.lyceumreports.DataServices;

import java.util.ArrayList;

public class Day {
    public int id;
    public String name = "...";
    public String status = STATUS.EMPTY;
    public ArrayList<Student> students = new ArrayList<>(0);

    public ArrayList<Integer> getAbsentStudents(){
        ArrayList<Integer> absentStudents = new ArrayList<>(0);
        for (Student st : students)
            if (st.absent)
                absentStudents.add(st.id);
        return absentStudents;
    }

    @Override
    public String toString() {
        return "Day{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", students=" + students.toString() +
                '}';
    }

    public static class STATUS {
        public static String EMPTY = "empty";
        public static String OK = "ok";
    }
}

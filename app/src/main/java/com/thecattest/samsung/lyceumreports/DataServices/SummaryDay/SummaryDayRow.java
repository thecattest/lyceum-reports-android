package com.thecattest.samsung.lyceumreports.DataServices.SummaryDay;

import java.util.ArrayList;

public class SummaryDayRow {
    public int id;
    public String name;
    public String status;
    public ArrayList<String> students;

    public String getName() {
        return name;
    }

    public String getAbsentStudentsString() {
        if (status.equals(STATUS.OK)) {
            if (students.size() != 0) {
                StringBuilder absentStudentsString = new StringBuilder();
                for (String absentStudent : students) {
                    absentStudentsString.append(absentStudent);
                    absentStudentsString.append(", ");
                }
                return absentStudentsString.substring(0, absentStudentsString.length() - 2);
            } else {
                return "Все в классе";
            }
        } else {
            return "Нет данных";
        }
    }

    public static class STATUS {
        public static String OK = "ok";
        public static String EMPTY = "empty";
    }

    @Override
    public String toString() {
        return "SummaryDayRow{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", students=" + getAbsentStudentsString() +
                '}';
    }
}

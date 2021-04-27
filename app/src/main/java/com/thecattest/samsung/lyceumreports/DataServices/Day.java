package com.thecattest.samsung.lyceumreports.DataServices;

import java.util.ArrayList;

public class Day {
    public int id;
    public String name = "";
    public String status = STATUS.EMPTY;
    public ArrayList<Student> students = new ArrayList<>();
    private ArrayList<Integer> loadedAbsent = new ArrayList<>();
    public boolean empty = false;

    public Day() {}

    public Day(boolean empty) {
        this.empty = empty;
    }

    public void updateLoadedAbsent() {
        loadedAbsent = getAbsentStudentsIds();
    }

    public ArrayList<Integer> getAbsentStudentsIds(){
        ArrayList<Integer> absentStudents = new ArrayList<>();
        for (Student st : students)
            if (st.absent)
                absentStudents.add(st.id);
        return absentStudents;
    }

    public String getAbsentStudentsIdsString() {
        ArrayList<Integer> absentStudentIds = getAbsentStudentsIds();
        if (absentStudentIds.size() != 0) {
            StringBuilder ids = new StringBuilder();
            ids.append(absentStudentIds.get(0));
            for (Integer id : absentStudentIds.subList(1, absentStudentIds.size())) {
                ids.append(",");
                ids.append(id.toString());
            }
            return ids.toString();
        } else
            return "";
    }

    public boolean noOneAbsent() {
        return !empty && status.equals(STATUS.EMPTY);
    }

    public boolean noChanges() {
        return loadedAbsent.equals(getAbsentStudentsIds());
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

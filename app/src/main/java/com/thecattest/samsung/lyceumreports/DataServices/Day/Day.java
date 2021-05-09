package com.thecattest.samsung.lyceumreports.DataServices.Day;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Day {
    public int id;
    public String name = "";
    public String status = STATUS.EMPTY;
    @SerializedName("can_edit")
    public boolean canEdit;
    public ArrayList<Student> students = new ArrayList<>();
    private ArrayList<Integer> loadedAbsent = new ArrayList<>();

    public Day() {}

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

    public boolean isEmpty() {
        return students.size() == 0;
    }

    public boolean noInfo() {
        return !isEmpty() && status.equals(STATUS.EMPTY);
    }

    public boolean noAbsent() {
        return !isEmpty() && status.equals(STATUS.OK) && getAbsentStudentsIds().size() == 0;
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

package com.thecattest.samsung.lyceumreports.DataModels.Day;

import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Day {
    private final static String CURRENT_DAY = "CURRENT_DAY";

    public int id;
    public String name = "";
    public String status = STATUS.EMPTY;
    @SerializedName("can_edit")
    public boolean canEdit;
    public ArrayList<Student> students = new ArrayList<>();
    private ArrayList<Integer> loadedAbsentStudentsIds = new ArrayList<>();

    public Day() {}

    public void updateLoadedAbsent() {
        loadedAbsentStudentsIds = getAbsentStudentsIds();
    }

    public ArrayList<Integer> getStudentsIds(ArrayList<Student> students){
        ArrayList<Integer> absentStudents = new ArrayList<>();
        for (Student st : students)
            if (st.absent)
                absentStudents.add(st.id);
        return absentStudents;
    }

    public String getStudentsIdsString(ArrayList<Integer> studentsIds) {
        if (studentsIds.size() != 0) {
            StringBuilder idsStringBuilder = new StringBuilder();
            idsStringBuilder.append(studentsIds.get(0));
            for (Integer id : studentsIds.subList(1, studentsIds.size())) {
                idsStringBuilder.append(",");
                idsStringBuilder.append(id.toString());
            }
            return idsStringBuilder.toString();
        } else
            return "";
    }

    public ArrayList<Integer> getAbsentStudentsIds(){
        return getStudentsIds(students);
    }

    public String getAbsentStudentsIdsString() {
        ArrayList<Integer> absentStudentIds = getStudentsIds(students);
        return getStudentsIdsString(absentStudentIds);
    }

    public ArrayList<Integer> getLoadedAbsentStudentsIds(){
        return loadedAbsentStudentsIds;
    }

    public String getLoadedAbsentStudentsIdsString() {
        return getStudentsIdsString(loadedAbsentStudentsIds);
    }

    public boolean isEmpty() {
        return students.size() == 0;
    }

    public boolean noInfo() {
        return !isEmpty() && status.equals(STATUS.EMPTY);
    }

    public boolean noLoadedAbsent() {
        return !isEmpty() && status.equals(STATUS.OK) && getLoadedAbsentStudentsIds().isEmpty();
    }

    public boolean noCurrentAbsent() {
        return !isEmpty() && getAbsentStudentsIds().isEmpty();
    }

    public boolean noChanges() {
        return loadedAbsentStudentsIds.equals(getAbsentStudentsIds());
    }

    public void saveToBundle(Bundle outState) {
        if (!isEmpty()) {
            Gson gson = new Gson();
            outState.putString(CURRENT_DAY, gson.toJson(this));
        }
    }

    public void loadFromBundle(Bundle savedInstanceState) {
        String dayJson = savedInstanceState.getString(CURRENT_DAY);
        if (dayJson != null && !dayJson.isEmpty()) {
            Gson gson = new Gson();
            Day loadedDay = gson.fromJson(dayJson, Day.class);

            this.id = loadedDay.id;
            this.name = loadedDay.name;
            this.status = loadedDay.status;
            this.canEdit = loadedDay.canEdit;
            this.students = loadedDay.students;
            this.loadedAbsentStudentsIds = loadedDay.loadedAbsentStudentsIds;
        }
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
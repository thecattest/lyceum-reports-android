package com.thecattest.samsung.lyceumreports.DataModels;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class GroupWithDaysAndStudents {
    @Embedded public Group group;
    @Relation(
            parentColumn = "gid",
            entityColumn = "group_id",
            entity = Day.class
    )
    public List<DayWithAbsent> days;
    @Relation(
            parentColumn = "gid",
            entityColumn = "group_id"
    )
    public List<Student> students;
}

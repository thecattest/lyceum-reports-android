package com.thecattest.samsung.lyceumreports.Data.Models.Relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.thecattest.samsung.lyceumreports.Data.Models.Day;
import com.thecattest.samsung.lyceumreports.Data.Models.Group;
import com.thecattest.samsung.lyceumreports.Data.Models.Student;

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

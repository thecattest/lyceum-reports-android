package com.thecattest.samsung.lyceumreports.Data.Models.Relations;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.thecattest.samsung.lyceumreports.Data.Models.Day;
import com.thecattest.samsung.lyceumreports.Data.Models.Group;
import com.thecattest.samsung.lyceumreports.Data.Models.Student;

import java.util.List;

public class DayWithAbsentAndGroup {
    @Embedded
    public Day day;
    @Relation(
            parentColumn = "did",
            entityColumn = "sid",
            associateBy = @Junction(DayAbsentCrossRef.class)
    )
    public List<Student> absent;
    @Relation(
            parentColumn = "group_id",
            entityColumn = "gid"
    )
    public Group group;
}

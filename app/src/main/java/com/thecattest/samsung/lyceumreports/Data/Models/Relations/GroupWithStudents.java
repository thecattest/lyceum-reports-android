package com.thecattest.samsung.lyceumreports.Data.Models.Relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.thecattest.samsung.lyceumreports.Data.Models.Group;
import com.thecattest.samsung.lyceumreports.Data.Models.Student;

import java.util.List;

public class GroupWithStudents {
    @Embedded
    public Group group;
    @Relation(
            parentColumn = "gid",
            entityColumn = "group_id"
    )
    public List<Student> students;
}

package com.thecattest.samsung.lyceumreports.DataModels.Dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.thecattest.samsung.lyceumreports.DataModels.Group;
import com.thecattest.samsung.lyceumreports.DataModels.GroupWithDaysAndStudents;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;

@Dao
public interface GroupDao {
    @Insert
    Maybe<Void> insert(Group group);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Maybe<Void> insert(List<Group> groups);

    @Delete
    Maybe<Void> delete(Group group);

    @Delete
    Maybe<Void> delete(List<Group> groups);

    @Transaction
    @Query("SELECT * FROM groups")
    Flowable<List<GroupWithDaysAndStudents>> get();

    @Transaction
    @Query("SELECT * FROM groups WHERE gid = :gid")
    Flowable<List<GroupWithDaysAndStudents>> get(int gid);
}

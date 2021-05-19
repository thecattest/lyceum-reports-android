package com.thecattest.samsung.lyceumreports.Data.Dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.thecattest.samsung.lyceumreports.Data.Models.Group;
import com.thecattest.samsung.lyceumreports.Data.Models.GroupWithDaysAndStudents;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;

@Dao
public interface GroupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Maybe<Void> insert(List<Group> groups);

    @Query("DELETE FROM groups")
    Maybe<Void> deleteAll();

    @Query("DELETE FROM groups WHERE gid in (:groupIds)")
    Maybe<Void> deleteByIds(List<Integer> groupIds);

    @Transaction
    @Query("SELECT * FROM groups")
    Flowable<List<GroupWithDaysAndStudents>> get();
}

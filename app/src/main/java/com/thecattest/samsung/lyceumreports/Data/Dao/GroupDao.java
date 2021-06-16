package com.thecattest.samsung.lyceumreports.Data.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.thecattest.samsung.lyceumreports.Data.Models.Group;
import com.thecattest.samsung.lyceumreports.Data.Models.Relations.GroupWithDaysAndStudents;
import com.thecattest.samsung.lyceumreports.Data.Models.Relations.GroupWithStudents;

import java.util.List;

import io.reactivex.Maybe;

@Dao
public interface GroupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Maybe<Void> insert(List<Group> groups);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Maybe<Void> insert(Group group);

    @Query("DELETE FROM groups " +
            "WHERE gid IN (:groupIds)")
    Maybe<Void> deleteByIds(List<Integer> groupIds);

    @Query("DELETE FROM groups " +
            "WHERE gid = :groupId")
    Maybe<Void> deleteById(int groupId);

    @Query("DELETE FROM groups " +
            "WHERE gid NOT IN (:groupIds)")
    Maybe<Void> deleteAllButIds(List<Integer> groupIds);

    @Transaction
    @Query("SELECT * " +
            "FROM groups " +
            "ORDER BY groups.number, groups.letter")
    Maybe<List<GroupWithDaysAndStudents>> get();

    @Transaction
    @Query("SELECT * " +
            "FROM groups " +
            "WHERE gid = :groupId " +
            "LIMIT 1")
    Maybe<GroupWithStudents> getById(int groupId);

    @Query("DELETE FROM groups")
    Maybe<Void> deleteAll();
}

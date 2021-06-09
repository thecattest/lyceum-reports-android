package com.thecattest.samsung.lyceumreports.Data.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.thecattest.samsung.lyceumreports.Data.Models.Student;

import java.util.List;

import io.reactivex.Maybe;

@Dao
public interface StudentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Maybe<Void> insert(List<Student> students);

    @Query("DELETE FROM students " +
            "WHERE group_id in (:groupIds)")
    Maybe<Void> deleteByGroupIds(List<Integer> groupIds);

    @Query("DELETE FROM students " +
            "WHERE group_id NOT IN (:groupIds)")
    Maybe<Void> deleteAllButGroupIds(List<Integer> groupIds);
}

package com.thecattest.samsung.lyceumreports.Data.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.thecattest.samsung.lyceumreports.Data.Models.Student;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;

@Dao
public interface StudentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Maybe<Void> insert(List<Student> students);

    @Query("DELETE FROM students")
    Maybe<Void> deleteAll();

    @Query("DELETE FROM students WHERE group_id in (:groupIds)")
    Maybe<Void> deleteByGroupIds(List<Integer> groupIds);

    @Query("DELETE FROM students WHERE group_id NOT in (:groupIds)")
    Maybe<Void> deleteAllButGroupIds(List<Integer> groupIds);

    @Transaction
    @Query("SELECT * FROM students")
    Flowable<List<Student>> get();
}

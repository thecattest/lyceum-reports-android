package com.thecattest.samsung.lyceumreports.Data.Dao;

import androidx.room.Dao;
import androidx.room.Delete;
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
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    Maybe<Void> insert(Student student);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Maybe<Void> insert(List<Student> students);

    @Delete
    Maybe<Void> delete(Student student);

    @Delete
    Maybe<Void> delete(List<Student> students);

    @Query("DELETE FROM students")
    Maybe<Void> deleteAll();

    @Transaction
    @Query("SELECT * FROM students")
    Flowable<List<Student>> get();

    @Transaction
    @Query("SELECT * FROM students WHERE sid = :sid")
    Flowable<List<Student>> get(int sid);
}

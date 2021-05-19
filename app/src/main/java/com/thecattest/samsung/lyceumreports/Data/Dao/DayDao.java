package com.thecattest.samsung.lyceumreports.Data.Dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.thecattest.samsung.lyceumreports.Data.Models.Day;
import com.thecattest.samsung.lyceumreports.Data.Models.DayAbsentCrossRef;
import com.thecattest.samsung.lyceumreports.Data.Models.DayWithAbsent;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;

@Dao
public interface DayDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Maybe<Void> insert(Day day);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Maybe<Void> insert(List<Day> day);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Maybe<Void> insertRefs(List<DayAbsentCrossRef> daysWithAbsent);

    @Query("DELETE FROM day_absent_cross_refs WHERE did = :did")
    Maybe<Void> deleteRefs(int did);

    @Delete
    Maybe<Void> delete(Day day);

    @Delete
    Maybe<Void> delete(List<Day> day);

    @Transaction
    @Query("SELECT * FROM days")
    Flowable<List<DayWithAbsent>> get();

    @Transaction
    @Query("SELECT * FROM days WHERE did = :did")
    Flowable<List<DayWithAbsent>> get(int did);

    @Transaction
    @Query("SELECT * FROM day_absent_cross_refs")
    Flowable<List<DayAbsentCrossRef>> getRefs();
}

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
    Maybe<Void> insert(List<Day> day);

    @Query("DELETE FROM days WHERE group_id in (:groupIds)")
    Maybe<Void> deleteByGroupIds(List<Integer> groupIds);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Maybe<Void> insertRefs(List<DayAbsentCrossRef> daysWithAbsent);

    @Query("DELETE FROM day_absent_cross_refs WHERE did in (SELECT did FROM days WHERE group_id in (:groupIds))")
    Maybe<Void> deleteRefsByGroupIds(List<Integer> groupIds);

    @Transaction
    @Query("SELECT * FROM days")
    Flowable<List<DayWithAbsent>> get();
}

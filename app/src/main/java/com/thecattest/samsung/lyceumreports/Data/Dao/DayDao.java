package com.thecattest.samsung.lyceumreports.Data.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.thecattest.samsung.lyceumreports.Data.Models.Day;
import com.thecattest.samsung.lyceumreports.Data.Models.Relations.DayAbsentCrossRef;
import com.thecattest.samsung.lyceumreports.Data.Models.Relations.DayWithAbsent;
import com.thecattest.samsung.lyceumreports.Data.Models.Relations.DayWithAbsentAndGroup;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;

@Dao
public interface DayDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Maybe<Void> insert(List<Day> day);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Maybe<Long> insert(Day day);

    @Query("DELETE FROM days " +
            "WHERE group_id = :groupId " +
            "AND date = :date")
    Maybe<Void> deleteByGroupIdAndDate(Integer groupId, String date);

    @Query("DELETE FROM days " +
            "WHERE group_id IN (:groupIds) " +
            "AND date IN (:dates)")
    Maybe<Void> deleteByGroupIdsAndDates(List<Integer> groupIds, List<String> dates);

    @Query("DELETE FROM days " +
            "WHERE group_id NOT IN (:groupIds)")
    Maybe<Void> deleteAllButGroupIds(List<Integer> groupIds);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Maybe<Void> insertRefs(List<DayAbsentCrossRef> daysWithAbsent);

    @Query("DELETE FROM day_absent_cross_refs " +
            "WHERE did IN " +
                "(SELECT did " +
                "FROM days " +
                "WHERE group_id = :groupId " +
                "AND date = :date" +
            ")")
    Maybe<Void> deleteRefsByGroupIdAndDate(Integer groupId, String date);

    @Query("DELETE FROM day_absent_cross_refs " +
            "WHERE did IN " +
                "(SELECT did " +
                "FROM days " +
                "WHERE group_id IN (:groupIds) " +
                "AND date IN (:dates)" +
            ")")
    Maybe<Void> deleteRefsByGroupIdsAndDates(List<Integer> groupIds, List<String> dates);

    @Query("DELETE FROM day_absent_cross_refs " +
            "WHERE did IN " +
            "(SELECT did " +
                "FROM days " +
                "WHERE group_id IN (:groupIds) " +
                "AND date IN (:dates) " +
                "AND is_synced = 1)")
    Maybe<Void> deleteSyncedRefsByGroupIdsAndDates(List<Integer> groupIds, List<String> dates);

    @Query("DELETE FROM day_absent_cross_refs " +
            "WHERE did NOT IN " +
                "(SELECT did " +
                "FROM days " +
                "WHERE group_id IN (:groupIds)" +
            ")")
    Maybe<Void> deleteAllRefsButGroupIds(List<Integer> groupIds);

    @Transaction
    @Query("SELECT * " +
            "FROM days " +
            "WHERE group_id = :groupId " +
            "AND date = :date " +
            "LIMIT 1")
    Maybe<DayWithAbsent> getByGroupIdAndDate(int groupId, String date);

    @Transaction
    @Query("SELECT * " +
            "FROM days " +
            "JOIN groups on days.group_id = groups.gid " +
            "WHERE date in (:date) " +
            "ORDER BY groups.number, groups.letter")
    Maybe<List<DayWithAbsentAndGroup>> getByDate(String date);

    @Transaction
    @Query("SELECT * " +
            "FROM days " +
            "WHERE is_synced = 0 " +
            "ORDER BY random() " +
            "LIMIT 1")
    Maybe<List<DayWithAbsent>> getNotSynced();
}

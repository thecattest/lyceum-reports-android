package com.thecattest.samsung.lyceumreports.Data.Models.Relations;

import androidx.room.Entity;
import androidx.room.Index;

@Entity(
        tableName = "day_absent_cross_refs",
        primaryKeys = {"did", "sid"},
        indices = {
                @Index(
                        value = {"sid", "did"},
                        unique = true)
        })
public class DayAbsentCrossRef {
    public int did;
    public int sid;

    public DayAbsentCrossRef(int did, int sid) {
        this.did = did;
        this.sid = sid;
    }

    @Override
    public String toString() {
        return "DayAbsentCrossRef{" +
                "did=" + did +
                ", sid=" + sid +
                '}';
    }
}
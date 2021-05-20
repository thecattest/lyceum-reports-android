package com.thecattest.samsung.lyceumreports.Data.Models;

import com.google.gson.annotations.SerializedName;

public class Permissions {
    @SerializedName("can_edit")
    public boolean canEdit;

    @SerializedName("can_view_table")
    public boolean canViewTable;

    @Override
    public String toString() {
        return "Permissions{" +
                "canEdit=" + canEdit +
                ", canViewTable=" + canViewTable +
                '}';
    }
}

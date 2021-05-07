package com.thecattest.samsung.lyceumreports.DataServices.Summary;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class SummaryWithPermissions {
    @SerializedName("can_edit")
    public boolean canEdit = false;

    @SerializedName("can_view_table")
    public boolean canViewTable = false;

    public ArrayList<Summary> summary = new ArrayList<>();
}

package com.thecattest.samsung.lyceumreports.Data.Legacy.Summary;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class SummaryWithPermissions {
    private static final String SUMMARY_WITH_PERMISSION = "SUMMARY_WITH_PERMISSION";

    @SerializedName("can_edit")
    public boolean canEdit = false;
    @SerializedName("can_view_table")
    public boolean canViewTable = false;
    public ArrayList<Summary> summary = new ArrayList<>();

    public boolean isEmpty() {
        return summary.size() == 0;
    }

    public void saveToBundle(Bundle outState) {
        if (!isEmpty()) {
            Gson gson = new Gson();
            outState.putString(SUMMARY_WITH_PERMISSION, gson.toJson(this));
        }
    }

    public void loadFromBundle(Bundle savedInstanceState) {
        String summaryString = getSummaryStringFromBundle(savedInstanceState);
        if (summaryString != null) {
            Gson gson = new Gson();
            SummaryWithPermissions summaryFromBundle = gson.fromJson(summaryString, SummaryWithPermissions.class);
            summary = summaryFromBundle.summary;
            canEdit = summaryFromBundle.canEdit;
            canViewTable = summaryFromBundle.canViewTable;
        }
    }

    @Nullable
    public String getSummaryStringFromBundle(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            String summaryString = savedInstanceState.getString(SUMMARY_WITH_PERMISSION);
            if (summaryString != null && !summaryString.isEmpty()) {
                return summaryString;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "SummaryWithPermissions{" +
                "canEdit=" + canEdit +
                ", canViewTable=" + canViewTable +
                ", summary=" + summary +
                '}';
    }
}

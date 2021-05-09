package com.thecattest.samsung.lyceumreports.DataServices.Summary;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class SummaryWithPermissions {
    private static final String SUMMARY = "SUMMARY";
    private static final String CAN_EDIT = "CAN_EDIT";
    private static final String CAN_VIEW_TABLE = "CAN_VIEW_TABLE";

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
            outState.putString(SUMMARY, gson.toJson(summary));
            outState.putBoolean(CAN_EDIT, canEdit);
            outState.putBoolean(CAN_VIEW_TABLE, canViewTable);
        }
    }

    public void loadFromBundle(Bundle savedInstanceState) {
        String summaryJsonString = getSummaryStringFromBundle(savedInstanceState);
        if (summaryJsonString != null) {
            Gson gson = new Gson();
            JsonElement summaryJsonObject = new JsonParser().parse(summaryJsonString);
            for (JsonElement s : summaryJsonObject.getAsJsonArray()) {
                summary.add(gson.fromJson(s, Summary.class));
            }
        }
        canEdit = savedInstanceState.getBoolean(CAN_EDIT, false);
        canViewTable = savedInstanceState.getBoolean(CAN_VIEW_TABLE, false);
    }

    @Nullable
    public String getSummaryStringFromBundle(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            String summaryJsonString = savedInstanceState.getString(SUMMARY);
            if (summaryJsonString != null && !summaryJsonString.isEmpty()) {
                return summaryJsonString;
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

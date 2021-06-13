package com.thecattest.samsung.lyceumreports.Managers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

import com.thecattest.samsung.lyceumreports.Activities.LoginActivity;
import com.thecattest.samsung.lyceumreports.Data.Models.Permissions;
import com.thecattest.samsung.lyceumreports.Services.SyncService;
import com.thecattest.samsung.lyceumreports.URLConfig;

public class LoginManager {
    private static final String KEY_COOKIES = "COOKIES";
    private static final String KEY_CAN_EDIT = "CAN_EDIT";
    private static final String KEY_CAN_VIEW_TABLE = "CAN_VIEW_TABLE";
    private static final String KEY_LAST_UPDATED = "LAST_UPDATED";

    private final SharedPreferences sharedPreferences;
    private AppCompatActivity activity;
    private Context context;

    public LoginManager(AppCompatActivity activity) {
        this.activity = activity;
        sharedPreferences = activity.getSharedPreferences(URLConfig.BASE_URL, Context.MODE_PRIVATE);
    }

    public LoginManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(URLConfig.BASE_URL, Context.MODE_PRIVATE);
    }

    public boolean isLoggedIn() {
        return !getCookie().isEmpty();
    }

    public String getCookie() {
        return sharedPreferences.getString(KEY_COOKIES, "");
    }

    public long setLastUpdated() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        long unixTime = System.currentTimeMillis() / 1000L;
        editor.putLong(KEY_LAST_UPDATED, unixTime);
        editor.apply();
        return unixTime;
    }

    public long getLastUpdated() {
        long lastUpdated = sharedPreferences.getLong(KEY_LAST_UPDATED, 0L);
        if (lastUpdated == 0L) {
            return setLastUpdated();
        }
        return lastUpdated;
    }

    public void setCookie(String cookies) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_COOKIES, cookies);
        editor.apply();
    }

    public Permissions getPermissions() {
        Permissions permissions = new Permissions();
        permissions.canEdit = sharedPreferences.getBoolean(KEY_CAN_EDIT, false);
        permissions.canViewTable = sharedPreferences.getBoolean(KEY_CAN_VIEW_TABLE, false);
        return permissions;
    }

    public void setPermissions(Permissions permissions) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_CAN_EDIT, permissions.canEdit);
        editor.putBoolean(KEY_CAN_VIEW_TABLE, permissions.canViewTable);
        editor.apply();
    }

    public void removeAll() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_COOKIES);
        editor.remove(KEY_CAN_VIEW_TABLE);
        editor.remove(KEY_CAN_EDIT);
        editor.remove(KEY_LAST_UPDATED);
        editor.apply();
    }

    public void logout() {
        removeAll();
        handleNotAuthorized();
    }

    public void handleNotAuthorized() {
        Intent serviceIntent = new Intent(activity, SyncService.class);
        if (activity != null) {
            activity.stopService(serviceIntent);
            Intent i = new Intent(activity, LoginActivity.class);
            activity.startActivity(i);
            activity.finish();
        } else if (context != null) {
            context.stopService(serviceIntent);
        }
    }
}

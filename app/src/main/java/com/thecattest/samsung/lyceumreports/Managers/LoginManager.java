package com.thecattest.samsung.lyceumreports.Managers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.thecattest.samsung.lyceumreports.Activities.LoginActivity;
import com.thecattest.samsung.lyceumreports.Data.Models.Permissions;
import com.thecattest.samsung.lyceumreports.URLConfig;

public class LoginManager {
    private static final String KEY_COOKIES = "COOKIES";
    private static final String KEY_CAN_EDIT = "CAN_EDIT";
    private static final String KEY_CAN_VIEW_TABLE = "CAN_VIEW_TABLE";

    private final SharedPreferences sharedPreferences;
    private final AppCompatActivity context;

    public LoginManager(AppCompatActivity context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(URLConfig.BASE_URL, Context.MODE_PRIVATE);
    }

    public void log() {
        Log.d("LoginManager", sharedPreferences.getAll().toString());
    }

    public String getCookie() {
        log();
        return sharedPreferences.getString(KEY_COOKIES, "");
    }

    public void setCookie(String cookies) {
        Log.d("LoginManager", cookies);
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
        Log.d("LoginManager", permissions.toString());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_CAN_EDIT, permissions.canEdit);
        editor.putBoolean(KEY_CAN_VIEW_TABLE, permissions.canViewTable);
        editor.apply();
    }

    public void removeAll() {
        log();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_COOKIES);
        editor.remove(KEY_CAN_VIEW_TABLE);
        editor.remove(KEY_CAN_EDIT);
        editor.apply();
    }

    public void logout() {
        removeAll();
        Log.d("LoginManager", sharedPreferences.getAll().toString());
        handleNotAuthorized();
    }

    public void handleNotAuthorized() {
        Log.d("LoginManager", "Handling");
        Intent i = new Intent(context, LoginActivity.class);
        context.startActivity(i);
        context.finish();
    }
}

package com.thecattest.samsung.lyceumreports.Managers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

import com.thecattest.samsung.lyceumreports.URLConfig;
import com.thecattest.samsung.lyceumreports.Activities.LoginActivity;

public class LoginManager {
    private static final String KEY_COOKIES = "COOKIES";

    private final SharedPreferences sharedPreferences;
    private final AppCompatActivity context;

    public LoginManager(AppCompatActivity context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(URLConfig.BASE_URL, Context.MODE_PRIVATE);
    }

    public String getCookie() {
        return sharedPreferences.getString(KEY_COOKIES, "");
    }

    public void setCookie(String cookies) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_COOKIES, cookies);
        editor.apply();
    }

    public void removeCookie() {
        setCookie("");
    }

    public void logout() {
        removeCookie();
        handleNotAuthorized();
    }

    public void handleNotAuthorized() {
        Intent i = new Intent(context, LoginActivity.class);
        context.startActivity(i);
        context.finish();
    }
}

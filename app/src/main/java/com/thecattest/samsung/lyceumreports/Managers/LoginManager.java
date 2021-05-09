package com.thecattest.samsung.lyceumreports.Managers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.thecattest.samsung.lyceumreports.Config;
import com.thecattest.samsung.lyceumreports.LoginActivity;

public class LoginManager {
    private static final String KEY_COOKIES = "COOKIES";

    private final SharedPreferences sharedPreferences;
    private final Context context;

    public LoginManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(Config.URL, Context.MODE_PRIVATE);
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
        ((Activity) context).finish();
    }
}

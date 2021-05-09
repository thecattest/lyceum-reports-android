package com.thecattest.samsung.lyceumreports;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class LoginManager {
    private final SharedPreferences sharedPreferences;
    private final Context context;

    public LoginManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(Config.URL, Context.MODE_PRIVATE);
    }

    public String getCookie() {
        return sharedPreferences.getString(Config.KEY_COOKIES, "");
    }

    public void setCookie(String cookies) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Config.KEY_COOKIES, cookies);
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

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

    public String getCookies() {
        return sharedPreferences.getString(Config.KEY_COOKIES, "");
    }

    public void setCookies(String cookies) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Config.KEY_COOKIES, cookies);
        editor.apply();
    }

    public void removeCookies() {
        setCookies("");
    }

    public boolean isNotAuthorized() {
        return getCookies().isEmpty();
    }

    public void handleNotAuthorized() {
        removeCookies();
        Intent i = new Intent(context, LoginActivity.class);
        context.startActivity(i);
        ((Activity) context).finish();
    }
}

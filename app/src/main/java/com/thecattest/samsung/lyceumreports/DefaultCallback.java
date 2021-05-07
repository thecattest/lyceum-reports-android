package com.thecattest.samsung.lyceumreports;

import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class DefaultCallback<T> implements Callback<T> {
    private final LoginManager loginManager;
    private final View mainLayout;

    public DefaultCallback(LoginManager loginManager, View mainLayout) {
        this.loginManager = loginManager;
        this.mainLayout = mainLayout;
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        int code = response.code();
        switch (code) {
            case 200:
                onResponse200(response);
                break;
                
            case 401:
                onResponse401();
                break;

            case 403:
                loginManager.handleNotAuthorized();
                break;

            default:
                Snackbar.make(
                        mainLayout,
                        "Ошибка при выполнении запроса :( код " + code,
                        Snackbar.LENGTH_SHORT
                ).show();
                break;
        }
    }

    public abstract void onResponse200(Response<T> response);

    protected abstract void onResponse401();
}

package com.thecattest.samsung.lyceumreports;

import android.content.Context;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.thecattest.samsung.lyceumreports.Managers.LoginManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class DefaultCallback<T> implements Callback<T> {
    private final String defaultCodeMessage;
    private final LoginManager loginManager;
    private View mainLayout;

    public DefaultCallback(Context context, LoginManager loginManager, View mainLayout) {
        this.loginManager = loginManager;
        this.mainLayout = mainLayout;
        defaultCodeMessage = context.getResources().getString(R.string.snackbar_server_error_code);
    }

    public DefaultCallback(Context context, LoginManager loginManager) {
        this.loginManager = loginManager;
        defaultCodeMessage = context.getResources().getString(R.string.snackbar_server_error_code);
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        int code = response.code();
        try {
            switch (code) {
                case 200:
                    onResponse200(response);
                    break;

                case 401:
                    onResponse401(response);
                    break;

                case 403:
                    loginManager.handleNotAuthorized();
                    break;

                case 500:
                    onResponse500(response);
                    break;

                default:
                    Snackbar.make(
                            mainLayout,
                            defaultCodeMessage + code,
                            Snackbar.LENGTH_SHORT
                    ).show();
                    break;
            }
        } catch (IllegalStateException ignored) {}
        onPostExecute();
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        try {
            onResponseFailure(call, t);
        } catch (IllegalStateException ignored) {}
        onPostExecute();
    }

    public abstract void onResponse200(Response<T> response);

    public void onResponse401(Response<T> response) {};

    public void onResponse500(Response<T> response) {
        Snackbar.make(
                mainLayout,
                R.string.snackbar_server_error_code_500,
                Snackbar.LENGTH_LONG
        ).show();
    }

    public abstract void onResponseFailure(Call<T> call, Throwable t);

    public void onPostExecute() {}
}

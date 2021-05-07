package com.thecattest.samsung.lyceumreports;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.thecattest.samsung.lyceumreports.DataServices.Login.LoginService;

import java.util.HashSet;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText login;
    private TextInputEditText password;
    private Button loginButton;
    private RelativeLayout loginFormLayout;

    private LoginManager loginManager;

    private LoginService loginService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginManager = new LoginManager(this);

        initRetrofit();
        findViews();
        setListeners();
    }

    protected void initRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        loginService = retrofit.create(LoginService.class);
    }

    private void findViews() {
        login = findViewById(R.id.login);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        loginFormLayout = findViewById(R.id.loginFormLayout);
    }

    private void setListeners() {
        loginButton.setOnClickListener(this::login);
    }

    public void login(View v) {
        hideKeyboard();

        String loginString = Objects.requireNonNull(login.getText()).toString();
        String passwordString = Objects.requireNonNull(password.getText()).toString();
        Call<Void> call = loginService.login(loginString, passwordString);
        call.enqueue(new DefaultCallback<Void>(loginManager, loginFormLayout) {
            @Override
            public void onResponse200(Response<Void> response) {
                Snackbar.make(
                        loginFormLayout,
                        "Авторизован",
                        Snackbar.LENGTH_LONG
                ).show();
                Log.d("Login", "ok");

                String cookies = response.headers().get("Set-Cookie");
                if(!cookies.isEmpty())
                    loginManager.setCookies(cookies);

                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }

            @Override
            protected void onResponse401() {
                Snackbar.make(
                        loginFormLayout,
                        "Неправильный логин или пароль",
                        Snackbar.LENGTH_LONG
                ).show();
                Log.d("Login", "wrong credentials");
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.d("LoginCall", t.toString());
                Toast.makeText(LoginActivity.this, "Error logging in", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = this.getCurrentFocus();
        if (view == null) {
            view = new View(this);
        }
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
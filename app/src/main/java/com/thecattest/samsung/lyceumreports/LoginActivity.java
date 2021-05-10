package com.thecattest.samsung.lyceumreports;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.thecattest.samsung.lyceumreports.DataServices.Login.LoginService;
import com.thecattest.samsung.lyceumreports.Managers.LoginManager;
import com.thecattest.samsung.lyceumreports.Managers.StatusManager;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    private RelativeLayout loginFormLayout;
    private TextInputEditText login;
    private TextInputEditText password;
    private Button loginButton;
    private ScrollView scrollView;

    private LoginManager loginManager;
    private StatusManager statusManager;

    private LoginService loginService;

    private boolean loginIsValid = false;
    private boolean passwordIsValid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initRetrofit();
        findViews();
        setListeners();
        updateButtonState();

        loginManager = new LoginManager(this);
        statusManager = new StatusManager(loginFormLayout, getSupportFragmentManager());
    }

    private void initRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        loginService = retrofit.create(LoginService.class);
    }

    private void findViews() {
        loginFormLayout = findViewById(R.id.loginFormLayout);
        login = findViewById(R.id.login);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        scrollView = findViewById(R.id.scrollView);
    }

    private void setListeners() {
        loginButton.setOnClickListener(this::login);
        login.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                loginIsValid = !s.toString().isEmpty();
                updateButtonState();
            }
        });
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                passwordIsValid = !s.toString().isEmpty();
                updateButtonState();
            }
        });
    }

    @Override
    public void onBackPressed() {}

    public void login(View v) {
//        hideKeyboard();
        statusManager.setLoadingLayout(true);
        String loginString = Objects.requireNonNull(login.getText()).toString();
        String passwordString = Objects.requireNonNull(password.getText()).toString();
        Call<Void> call = loginService.login(loginString, passwordString);
        call.enqueue(new DefaultCallback<Void>(loginManager, scrollView) {
            @Override
            public void onResponse200(Response<Void> response) {
                Snackbar.make(
                        scrollView,
                        "Авторизован",
                        Snackbar.LENGTH_LONG
                ).show();
                Log.d("Login", "ok");

                String cookies = response.headers().get("Set-Cookie");
                if(!cookies.isEmpty())
                    loginManager.setCookie(cookies);

                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }

            @Override
            public void onResponse401() {
                Snackbar.make(
                        scrollView,
                        "Неправильный логин или пароль",
                        Snackbar.LENGTH_LONG
                ).show();
                Log.d("Login", "wrong credentials");
                statusManager.setMainLayout();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.d("LoginCall", t.toString());
                Snackbar.make(
                        scrollView,
                        "Ошибка: сервер недоступен",
                        Snackbar.LENGTH_LONG
                ).show();
                statusManager.setMainLayout();
            }
        });
    }

    private void updateButtonState() {
        loginButton.setEnabled(loginIsValid && passwordIsValid);
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
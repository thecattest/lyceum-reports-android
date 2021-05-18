package com.thecattest.samsung.lyceumreports.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.thecattest.samsung.lyceumreports.DataModels.Login.LoginService;
import com.thecattest.samsung.lyceumreports.DefaultCallback;
import com.thecattest.samsung.lyceumreports.Managers.LoginManager;
import com.thecattest.samsung.lyceumreports.Managers.RetrofitManager;
import com.thecattest.samsung.lyceumreports.Managers.StatusManager;
import com.thecattest.samsung.lyceumreports.R;
import com.thecattest.samsung.lyceumreports.URLConfig;

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

        findViews();
        setListeners();
        initManagers();
        initRetrofit();
        updateButtonState();
    }

    private void initRetrofit() {
        Retrofit retrofit = RetrofitManager.getInstance();
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

    private void initManagers() {
        loginManager = new LoginManager(this);
        statusManager = new StatusManager(this, loginFormLayout);
    }

    @Override
    public void onBackPressed() {}

    public void login(View v) {
        statusManager.setLoadingLayout(true);

        String loginString = Objects.requireNonNull(login.getText()).toString();
        String passwordString = Objects.requireNonNull(password.getText()).toString();

        Call<Void> call = loginService.login(loginString, passwordString);
        call.enqueue(new DefaultCallback<Void>(this, loginManager, scrollView) {
            @Override
            public void onResponse200(Response<Void> response) {
                Snackbar.make(
                        scrollView,
                        R.string.snackbar_authorization_successful,
                        Snackbar.LENGTH_LONG
                ).show();

                String cookies = response.headers().get("Set-Cookie");
                if(!cookies.isEmpty())
                    loginManager.setCookie(cookies);

                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }

            @Override
            public void onResponse401(Response<Void> response) {
                Snackbar.make(
                        scrollView,
                        R.string.snackbar_wrong_login_password,
                        Snackbar.LENGTH_LONG
                ).show();
            }

            public void onResponseFailure(Call<Void> call, Throwable t) {
                Snackbar.make(
                        scrollView,
                        R.string.snackbar_server_error,
                        Snackbar.LENGTH_LONG
                ).show();
            }

            @Override
            public void onPostExecute() {
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
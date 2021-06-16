package com.thecattest.samsung.lyceumreports.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.thecattest.samsung.lyceumreports.Data.ApiService;
import com.thecattest.samsung.lyceumreports.Data.Models.Permissions;
import com.thecattest.samsung.lyceumreports.Data.Repositories.GroupRepository;
import com.thecattest.samsung.lyceumreports.DefaultCallback;
import com.thecattest.samsung.lyceumreports.Managers.LoginManager;
import com.thecattest.samsung.lyceumreports.Managers.RetrofitManager;
import com.thecattest.samsung.lyceumreports.R;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText login;
    private TextInputEditText password;
    private Button loginButton;
    private ScrollView scrollView;
    private ProgressBar loadingProgressBar;

    private LoginManager loginManager;

    private ApiService apiService;

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

        GroupRepository groupRepository = new GroupRepository(this, loginManager, apiService);
        groupRepository.deleteAll();
    }

    private void findViews() {
        login = findViewById(R.id.login);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        scrollView = findViewById(R.id.scrollView);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
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
    }

    private void initRetrofit() {
        Retrofit retrofit = RetrofitManager.getInstance();
        apiService = retrofit.create(ApiService.class);
    }

    @Override
    public void onBackPressed() {}

    public void login(View v) {
        loadingProgressBar.setVisibility(View.VISIBLE);

        String loginString = Objects.requireNonNull(login.getText()).toString();
        String passwordString = Objects.requireNonNull(password.getText()).toString();

        Call<Permissions> call = apiService.login(loginString, passwordString);
        call.enqueue(new DefaultCallback<Permissions>(this, loginManager, scrollView) {
            @Override
            public void onResponse200(Response<Permissions> response) {
                Snackbar.make(
                        scrollView,
                        R.string.snackbar_authorization_successful,
                        Snackbar.LENGTH_LONG
                ).show();

                String cookies = response.headers().get("Set-Cookie");
                if(!cookies.isEmpty()) {
                    loginManager.setCookie(cookies);
                    loginManager.setPermissions(response.body());
                }

                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }

            @Override
            public void onResponse401(Response<Permissions> response) {
                Snackbar.make(
                        scrollView,
                        R.string.snackbar_wrong_login_password,
                        Snackbar.LENGTH_LONG
                ).show();
            }

            public void onResponseFailure(Call<Permissions> call, Throwable t) {
                Snackbar.make(
                        scrollView,
                        R.string.snackbar_server_error,
                        Snackbar.LENGTH_LONG
                ).show();
            }

            @Override
            public void onPostExecute() {
                loadingProgressBar.setVisibility(View.GONE);
            }
        });
    }

    private void updateButtonState() {
        loginButton.setEnabled(loginIsValid && passwordIsValid);
    }
}
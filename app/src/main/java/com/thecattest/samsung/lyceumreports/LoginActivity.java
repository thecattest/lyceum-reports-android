package com.thecattest.samsung.lyceumreports;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class LoginActivity extends AppCompatActivity {

    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViews();
        setListeners();
    }

    private void findViews() {
        loginButton = findViewById(R.id.loginButton);
    }

    private void setListeners() {
        loginButton.setOnClickListener(this::login);
    }

    public void login(View v) {
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }
}
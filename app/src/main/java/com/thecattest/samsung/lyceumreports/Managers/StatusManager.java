package com.thecattest.samsung.lyceumreports.Managers;

import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.thecattest.samsung.lyceumreports.R;

public class StatusManager {

    private final View mainLayout;
    private final View loadingLayout;

    public StatusManager(AppCompatActivity activity, View mainLayout) {
        this.mainLayout = mainLayout;
        this.loadingLayout = activity.findViewById(R.id.fragmentLoading);
    }

    public void setLoadingLayout() {
        mainLayout.setVisibility(View.VISIBLE);
        loadingLayout.setVisibility(View.VISIBLE);
    }

    public void setMainLayout() {
        mainLayout.setVisibility(View.VISIBLE);
        loadingLayout.setVisibility(View.GONE);
    }
}

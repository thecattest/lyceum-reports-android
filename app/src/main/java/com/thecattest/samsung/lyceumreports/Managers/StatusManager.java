package com.thecattest.samsung.lyceumreports.Managers;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.thecattest.samsung.lyceumreports.R;

public class StatusManager {
    private static final String LAYOUT_TYPE = "LAYOUT_TYPE";
    private static final String LAYOUT_TYPE_MAIN = "LAYOUT_TYPE_MAIN";
    private static final String LAYOUT_TYPE_SERVER_ERROR = "LAYOUT_TYPE_SERVER_ERROR";

    private final View mainLayout;
    private final View loadingLayout;
    private View serverErrorLayout;

    private boolean serverErrorUsed = false;

    public StatusManager(AppCompatActivity activity, View mainLayout, View.OnClickListener onServerErrorRetryButtonClick) {
        this(activity, mainLayout);
        this.serverErrorLayout = activity.findViewById(R.id.serverErrorLayout);
        serverErrorUsed = true;
        serverErrorLayout.findViewById(R.id.retry).setOnClickListener(onServerErrorRetryButtonClick);
    }

    public StatusManager(AppCompatActivity activity, View mainLayout) {
        this.mainLayout = mainLayout;
        this.loadingLayout = activity.findViewById(R.id.fragmentLoading);
    }

    public void setServerErrorLayout() {
        if (!serverErrorUsed)
            return;
        mainLayout.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.GONE);
        serverErrorLayout.setVisibility(View.VISIBLE);
    }

    public void setLoadingLayout() {
        mainLayout.setVisibility(View.VISIBLE);
        loadingLayout.setVisibility(View.VISIBLE);
        if (serverErrorUsed)
            serverErrorLayout.setVisibility(View.GONE);
    }

    public void setMainLayout() {
        mainLayout.setVisibility(View.VISIBLE);
        loadingLayout.setVisibility(View.GONE);
        if (serverErrorUsed)
            serverErrorLayout.setVisibility(View.GONE);
    }
}

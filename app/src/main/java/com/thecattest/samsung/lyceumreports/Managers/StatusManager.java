package com.thecattest.samsung.lyceumreports.Managers;

import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.thecattest.samsung.lyceumreports.Fragments.LoadingFragment;
import com.thecattest.samsung.lyceumreports.Fragments.ServerErrorFragment;
import com.thecattest.samsung.lyceumreports.R;

import java.util.Objects;

public class StatusManager {
    private static final String LAYOUT_TYPE = "LAYOUT_TYPE";
    private static final String LAYOUT_TYPE_MAIN = "LAYOUT_TYPE_MAIN";
    private static final String LAYOUT_TYPE_SERVER_ERROR = "LAYOUT_TYPE_SERVER_ERROR";

    private final FragmentManager fragmentManager;

    private LoadingFragment loadingFragment;
    private ServerErrorFragment serverErrorFragment;

    private final View mainLayout;

    private View.OnClickListener onServerErrorRetryButtonClick = v -> {};
    private boolean serverErrorUsed = false;

    public StatusManager(View mainLayout, FragmentManager fragmentManager, View.OnClickListener onServerErrorRetryButtonClick) {
        this(mainLayout, fragmentManager);
        this.onServerErrorRetryButtonClick = onServerErrorRetryButtonClick;
        serverErrorUsed = true;
        createFragments();
    }

    public StatusManager(View mainLayout, FragmentManager fragmentManager) {
        this.mainLayout = mainLayout;
        this.fragmentManager = fragmentManager;
        createFragments();
    }

    private void createFragments() {
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.disallowAddToBackStack();

        loadingFragment = new LoadingFragment();
        ft.add(R.id.loadingLayout, loadingFragment, LoadingFragment.TAG);
        ft.hide(loadingFragment);

        if (serverErrorUsed) {
            serverErrorFragment = new ServerErrorFragment(this.onServerErrorRetryButtonClick);
            ft.add(R.id.serverErrorLayout, serverErrorFragment, ServerErrorFragment.TAG);
            ft.hide(serverErrorFragment);
        }

        ft.commit();
    }

    public void saveToBundle(Bundle outState) {
        String layout = "";
        if (mainLayout.getVisibility() == View.VISIBLE) {
            layout = LAYOUT_TYPE_MAIN;
        }
        else if (serverErrorUsed && !Objects.requireNonNull(fragmentManager.findFragmentByTag(ServerErrorFragment.TAG)).isHidden()) {
            layout = LAYOUT_TYPE_SERVER_ERROR;
        }
        outState.putString(LAYOUT_TYPE, layout);
    }

    public boolean loadFromBundle(Bundle savedInstanceState) {
        String layout = savedInstanceState.getString(LAYOUT_TYPE);
        switch (layout) {
            case LAYOUT_TYPE_SERVER_ERROR:
                setServerErrorLayout();
                break;
            case LAYOUT_TYPE_MAIN:
                setMainLayout();
                return true;
        }
        return false;
    }

    public void setServerErrorLayout() {
        if (!serverErrorUsed)
            return;
        mainLayout.setVisibility(View.GONE);
        setLoadingFragmentVisibility(false);
        setServerErrorFragmentVisibility(true);
    }

    public void setLoadingLayout(boolean mainIsVisible) {
        mainLayout.setVisibility(mainIsVisible ? View.VISIBLE : View.GONE);
        setLoadingFragmentVisibility(true);
        if (serverErrorUsed)
            setServerErrorFragmentVisibility(false);
    }

    public void setLoadingLayout() {
        setLoadingLayout(false);
    }

    public void setMainLayout() {
        mainLayout.setVisibility(View.VISIBLE);
        setLoadingFragmentVisibility(false);
        if (serverErrorUsed)
            setServerErrorFragmentVisibility(false);
    }

    private void setLoadingFragmentVisibility(boolean visible) {
        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (visible) {
            ft.show(loadingFragment);
        } else {
            ft.hide(loadingFragment);
        }
        ft.commit();
    }

    private void setServerErrorFragmentVisibility(boolean visible) {
        if (!serverErrorUsed)
            return;
        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (visible) {
            ft.show(serverErrorFragment);
        } else {
            ft.hide(serverErrorFragment);
        }
        ft.commit();
    }
}

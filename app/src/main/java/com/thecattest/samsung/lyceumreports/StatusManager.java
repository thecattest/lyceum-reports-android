package com.thecattest.samsung.lyceumreports;

import android.view.View;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.thecattest.samsung.lyceumreports.Fragments.LoadingFragment;
import com.thecattest.samsung.lyceumreports.Fragments.ServerErrorFragment;

public class StatusManager {
    private final FragmentManager fragmentManager;

    private LoadingFragment loadingFragment;
    private ServerErrorFragment serverErrorFragment;

    private final View mainLayout;

    public final View.OnClickListener onServerErrorRetryButtonClick;

    public StatusManager(View mainLayout, FragmentManager fragmentManager, View.OnClickListener onServerErrorRetryButtonClick) {
        this.mainLayout = mainLayout;
        this.fragmentManager = fragmentManager;
        this.onServerErrorRetryButtonClick = onServerErrorRetryButtonClick;
        createFragments();
    }

    private void createFragments() {
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.disallowAddToBackStack();

        loadingFragment = new LoadingFragment();
        ft.add(R.id.loadingLayout, loadingFragment, LoadingFragment.TAG);
        ft.hide(loadingFragment);

        serverErrorFragment = new ServerErrorFragment(this.onServerErrorRetryButtonClick);
        ft.add(R.id.serverErrorLayout, serverErrorFragment, ServerErrorFragment.TAG);
        ft.hide(serverErrorFragment);

        ft.commit();
    }

    public void setServerErrorLayout() {
        mainLayout.setVisibility(View.GONE);
        setLoadingFragmentVisibility(false);
        setServerErrorFragmentVisibility(true);
    }

    public void setLoadingLayout(boolean mainIsVisible) {
        mainLayout.setVisibility(mainIsVisible ? View.VISIBLE : View.GONE);
        setLoadingFragmentVisibility(true);
        setServerErrorFragmentVisibility(false);
    }

    public void setLoadingLayout() {
        setLoadingLayout(false);
    }

    public void setMainLayout() {
        mainLayout.setVisibility(View.VISIBLE);
        setLoadingFragmentVisibility(false);
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
        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (visible) {
            ft.show(serverErrorFragment);
        } else {
            ft.hide(serverErrorFragment);
        }
        ft.commit();
    }
}

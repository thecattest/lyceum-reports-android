package com.thecattest.samsung.lyceumreports;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ServerErrorFragment extends Fragment {

    private View.OnClickListener retryButtonOnClickListener;

    public ServerErrorFragment() {

    }

    public ServerErrorFragment(View.OnClickListener retryButtonOnClickListener) {
        this.retryButtonOnClickListener = retryButtonOnClickListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_server_error, container, false);
        view.findViewById(R.id.retry).setOnClickListener(retryButtonOnClickListener);
        return view;
    }
}
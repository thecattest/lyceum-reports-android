package com.thecattest.samsung.lyceumreports.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.thecattest.samsung.lyceumreports.R;

public class TableFragment extends Fragment {

    public TableFragment() {
    }

    public static TableFragment newInstance() {
        return new TableFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_table, container, false);
    }
}
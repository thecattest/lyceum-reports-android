package com.thecattest.samsung.lyceumreports;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.thecattest.samsung.lyceumreports.DataServices.Summary;

import java.util.ArrayList;

public class SummaryAdapter extends ArrayAdapter<Summary> {
    public SummaryAdapter(Context context, ArrayList<Summary> summaries) {
        super(context, R.layout.card_list_item, summaries);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final Summary summary = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.card_list_item, parent, false);
        }

        TextView classLabel = convertView.findViewById(R.id.cardClassLabel);
        Button addButton = convertView.findViewById(R.id.addButton);

        TextView todayDate = convertView.findViewById(R.id.todayDate);
        TextView todayAbsent = convertView.findViewById(R.id.todayAbsent);

        TextView yesterdayDate = convertView.findViewById(R.id.yesterdayDate);
        TextView yesterdayAbsent = convertView.findViewById(R.id.yesterdayAbsent);


        classLabel.setText(summary.getLabel());
        addButton.setTag(summary.getId());

        todayDate.setText(summary.getTodayDate());
        todayAbsent.setText(summary.getTodayAbsentStudentsString());

        yesterdayDate.setText(summary.getYesterdayDate());
        yesterdayAbsent.setText(summary.getYesterdayAbsentStudentsString());

        return convertView;
    }
}

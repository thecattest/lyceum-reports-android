package com.thecattest.samsung.lyceumreports.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.thecattest.samsung.lyceumreports.Activities.DayActivity;
import com.thecattest.samsung.lyceumreports.DataModels.Summary.Summary;
import com.thecattest.samsung.lyceumreports.R;

import java.util.ArrayList;

public class SummaryAdapter extends ArrayAdapter<Summary> {
    private boolean canEdit;

    public SummaryAdapter(Context context, ArrayList<Summary> summaries, boolean canEdit) {
        super(context, R.layout.summary_card, summaries);
        this.canEdit = canEdit;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final Summary summary = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.summary_card, parent, false);
        }

        TextView classLabel = convertView.findViewById(R.id.cardClassLabel);
        Button addButton = convertView.findViewById(R.id.addButton);

        TextView todayDate = convertView.findViewById(R.id.todayDate);
        TextView todayAbsent = convertView.findViewById(R.id.todayAbsent);

        TextView yesterdayDate = convertView.findViewById(R.id.yesterdayDate);
        TextView yesterdayAbsent = convertView.findViewById(R.id.yesterdayAbsent);


        classLabel.setText(summary.getLabel());
        if (canEdit)
            addButton.setOnClickListener(v -> {
                Intent i = new Intent(getContext(), DayActivity.class);
                i.putExtra(DayActivity.GROUP_ID, summary.getId());
                i.putExtra(DayActivity.GROUP_LABEL, summary.getLabel());
                getContext().startActivity(i);
            });
        else {
            addButton.setVisibility(View.GONE);
            convertView.setEnabled(false);
        }

        todayDate.setText(summary.getTodayDate(getContext()));
        todayAbsent.setText(summary.getTodayAbsentStudentsString(getContext()));

        yesterdayDate.setText(summary.getYesterdayDate(getContext()));
        yesterdayAbsent.setText(summary.getYesterdayAbsentStudentsString(getContext()));

        return convertView;
    }
}

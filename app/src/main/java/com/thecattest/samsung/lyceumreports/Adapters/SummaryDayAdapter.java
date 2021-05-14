package com.thecattest.samsung.lyceumreports.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.thecattest.samsung.lyceumreports.DataModels.SummaryDay.SummaryDayRow;
import com.thecattest.samsung.lyceumreports.R;

import java.util.ArrayList;

public class SummaryDayAdapter extends ArrayAdapter<SummaryDayRow> {
    public SummaryDayAdapter(Context context, ArrayList<SummaryDayRow> summaryDayRows) {
        super(context, R.layout.summary_day_row, summaryDayRows);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final SummaryDayRow summaryDayRow = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.summary_day_row, parent, false);
        }

        ((TextView) convertView.findViewById(R.id.text1)).setText(summaryDayRow.name);
        ((TextView) convertView.findViewById(R.id.text2)).setText(summaryDayRow.getAbsentStudentsString());

        convertView.setEnabled(false);

        return convertView;
    }
}

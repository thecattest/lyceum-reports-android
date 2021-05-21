package com.thecattest.samsung.lyceumreports.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.thecattest.samsung.lyceumreports.Data.Models.Relations.DayWithAbsentAndGroup;
import com.thecattest.samsung.lyceumreports.R;

import java.util.ArrayList;

public class SummaryDayAdapter extends ArrayAdapter<DayWithAbsentAndGroup> {
    public SummaryDayAdapter(Context context, ArrayList<DayWithAbsentAndGroup> days) {
        super(context, R.layout.summary_day_row, days);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final DayWithAbsentAndGroup day = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.summary_day_row, parent, false);
        }

        ((TextView) convertView.findViewById(R.id.text1)).setText(
                day.group.getLabel()
        );
        ((TextView) convertView.findViewById(R.id.text2)).setText(
                day.day.getAbsentStudentsString(
                        getContext(), day.absent
                )
        );

        convertView.setEnabled(false);

        return convertView;
    }
}

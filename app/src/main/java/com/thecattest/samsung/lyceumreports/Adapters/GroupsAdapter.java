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
import com.thecattest.samsung.lyceumreports.Data.Models.Relations.DayWithAbsent;
import com.thecattest.samsung.lyceumreports.Data.Models.Relations.GroupWithDaysAndStudents;
import com.thecattest.samsung.lyceumreports.R;

import java.util.ArrayList;

public class GroupsAdapter extends ArrayAdapter<GroupWithDaysAndStudents> {
    private final boolean canEdit;

    public GroupsAdapter(Context context, ArrayList<GroupWithDaysAndStudents> groups, boolean canEdit) {
        super(context, R.layout.list_item_summary_main, groups);
        this.canEdit = canEdit;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final GroupWithDaysAndStudents group = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_summary_main, parent, false);
        }

        TextView classLabel = convertView.findViewById(R.id.cardClassLabel);
        Button addButton = convertView.findViewById(R.id.addButton);

        TextView todayDate = convertView.findViewById(R.id.todayDate);
        TextView todayAbsent = convertView.findViewById(R.id.todayAbsent);

        TextView yesterdayDate = convertView.findViewById(R.id.yesterdayDate);
        TextView yesterdayAbsent = convertView.findViewById(R.id.yesterdayAbsent);


        classLabel.setText(group.group.getLabel());
        if (canEdit)
            addButton.setOnClickListener(v -> {
                Intent i = new Intent(getContext(), DayActivity.class);
                i.putExtra(DayActivity.GROUP_ID, group.group.gid);
                i.putExtra(DayActivity.GROUP_LABEL, group.group.getLabel());
                getContext().startActivity(i);
            });
        else {
            addButton.setVisibility(View.GONE);
            convertView.setEnabled(false);
        }

        String todayServerFormatDate = group.group.getTodayDateServerFormat(getContext());
        String yesterdayServerFormatDate = group.group.getYesterdayDateServerFormat(getContext());

        String absentDefault = getContext().getString(R.string.summary_status_no_info);
        todayDate.setText(group.group.getHumanDate(getContext(), todayServerFormatDate));
        yesterdayDate.setText(group.group.getHumanDate(getContext(), yesterdayServerFormatDate));
        todayAbsent.setText(absentDefault);
        yesterdayAbsent.setText(absentDefault);

        for (DayWithAbsent dayWithAbsent : group.days) {
            if (dayWithAbsent.day.date.equals(todayServerFormatDate)) {
                todayAbsent.setText(
                        dayWithAbsent.day.getAbsentStudentsString(
                                getContext(),
                                dayWithAbsent.absent));
            } else if (dayWithAbsent.day.date.equals(yesterdayServerFormatDate)) {
                yesterdayAbsent.setText(
                        dayWithAbsent.day.getAbsentStudentsString(
                                getContext(),
                                dayWithAbsent.absent));
            }
        }

        return convertView;
    }
}

package com.thecattest.samsung.lyceumreports.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.thecattest.samsung.lyceumreports.Data.Models.DayWithAbsent;
import com.thecattest.samsung.lyceumreports.Data.Models.GroupWithDaysAndStudents;
import com.thecattest.samsung.lyceumreports.Data.Models.Student;

import java.util.List;

public class TestListViewAdapter extends ArrayAdapter<Object> {
    public TestListViewAdapter(Context context, List<Object> objects) {
        super(context, android.R.layout.simple_list_item_2, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        }
        String text1 = "";
        String text2 = "";
        Object object = getItem(position);
        try {
            final Student student = (Student) object;
            text1 = student.getName();
            text2 = student.sid + " " + student.groupId;
        } catch (ClassCastException e) {
            try {
                final DayWithAbsent day = (DayWithAbsent) object;
                text1 = day.day.getHumanDate(getContext());
                text2 = day.day.getAbsentStudentsString(getContext(), day.absent);
            } catch (ClassCastException e2) {
                final GroupWithDaysAndStudents group = (GroupWithDaysAndStudents) object;
                text1 = group.group.number + group.group.letter + ": " + group.group.gid;
                text2 = group.days.toString();
            }
        }

        ((TextView) convertView.findViewById(android.R.id.text1)).setText(text1);
        ((TextView) convertView.findViewById(android.R.id.text2)).setText(text2);

        return convertView;
    }
}

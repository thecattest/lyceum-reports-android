package com.thecattest.samsung.lyceumreports.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.thecattest.samsung.lyceumreports.Data.Models.Day;
import com.thecattest.samsung.lyceumreports.Data.Models.Relations.DayWithAbsent;
import com.thecattest.samsung.lyceumreports.Data.Models.Relations.GroupWithStudents;
import com.thecattest.samsung.lyceumreports.Data.Models.Student;
import com.thecattest.samsung.lyceumreports.R;

import java.util.ArrayList;

public class StudentsAdapter extends ArrayAdapter<Student> {

    private final GroupWithStudents group;

    public StudentsAdapter(Context context, GroupWithStudents group) {
        super(context, android.R.layout.simple_list_item_1, group.students);
        this.group = group;
    }

    public void updateDay(DayWithAbsent dayWithAbsent) {
        Day day = new Day();
        day.groupId = group.group.gid;
        if (dayWithAbsent.day != null) {
            day.date = dayWithAbsent.day.date;
            day.absent = new ArrayList<>(dayWithAbsent.absent);
        }
        group.group.days = new ArrayList<>();
        group.group.days.add(day);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final Student student = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        String studentText = student.getName();
        ((TextView) convertView.findViewById(android.R.id.text1)).setText(studentText);
        if (group.group.days != null && !group.group.days.isEmpty()) {
            int backgroundColor = group.group.days.get(0).absent.contains(student) ? R.color.absent : R.color.white;
            convertView.setBackgroundColor(getContext().getResources().getColor(backgroundColor));
        }

        return convertView;
    }
}

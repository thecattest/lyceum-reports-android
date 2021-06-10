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
import com.thecattest.samsung.lyceumreports.Data.Repositories.DayRepository;
import com.thecattest.samsung.lyceumreports.R;

import java.util.ArrayList;

public class StudentsAdapter extends ArrayAdapter<Student> {

    private final GroupWithStudents group;

    public StudentsAdapter(Context context, GroupWithStudents group) {
        super(context, android.R.layout.simple_list_item_1, group.students);
        this.group = group;
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

    public Day getDay() throws NullPointerException {
        if (!group.group.days.isEmpty())
            return group.group.days.get(0);
        throw new NullPointerException();
    }

    public void updateDay(DayWithAbsent dayWithAbsent, String date) {
        Day day = new Day(group.group.gid, date, dayWithAbsent);
        group.group.days = new ArrayList<>();
        group.group.days.add(day);
        notifyDataSetChanged();
    }

    public void toggleAbsent(Student student, DayRepository dayRepository) {
        Day day = group.group.days.get(0);
        day.toggleAbsent(student, dayRepository);
    }

    public boolean noLoadedAbsent() {
        if (group.group.days == null || group.group.days.isEmpty())
            return false;
        Day day = group.group.days.get(0);
        return day.noLoadedAbsent();
    }

    public boolean noAbsent() {
        if (group.group.days == null || group.group.days.isEmpty())
            return false;
        Day day = group.group.days.get(0);
        return day.noAbsent();
    }

    public boolean buttonEnabled() {
        if (group.group.days == null || group.group.days.isEmpty())
            return false;
        Day day = group.group.days.get(0);
        return day.isChanged();
    }
}

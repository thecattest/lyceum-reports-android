package com.thecattest.samsung.lyceumreports.Adapters;

import android.content.Context;
import android.util.Log;
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
            Log.d("DayActivityDebug", "updating loaded absent");
            day.loadedAbsent = new ArrayList<>(dayWithAbsent.absent);
        } else {
            day.isSyncedWithServer = false;
            day.isLoadedFromServer = false;
        }
        group.group.days = new ArrayList<>();
        group.group.days.add(day);
        notifyDataSetChanged();
    }

    public void toggleAbsent(Student student) {
        Day day = group.group.days.get(0);
        if (day.absent.contains(student)) {
            day.absent.remove(student);
        } else {
            day.absent.add(student);
        }

        boolean equals = true;
        for (Student st : day.loadedAbsent) {
            equals = day.absent.contains(st);
            if (!equals)
                break;
        }
        if (equals)
            for (Student st : day.absent) {
                equals = day.loadedAbsent.contains(st);
                if (!equals)
                    break;
            }

        day.isSyncedWithServer = day.isLoadedFromServer && equals;
    }

    public boolean noLoadedAbsent() {
        if (group.group.days == null || group.group.days.isEmpty())
            return false;
        Day day = group.group.days.get(0);
        return day.isLoadedFromServer && day.loadedAbsent.isEmpty();
    }

    public boolean noAbsent() {
        if (group.group.days == null || group.group.days.isEmpty())
            return false;
        Day day = group.group.days.get(0);
        return day.absent.isEmpty();
    }

    public boolean buttonEnabled() {
        if (group.group.days == null || group.group.days.isEmpty())
            return false;
        return !group.group.days.get(0).isSyncedWithServer;
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

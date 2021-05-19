package com.thecattest.samsung.lyceumreports.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.thecattest.samsung.lyceumreports.Data.Legacy.Day.Student;
import com.thecattest.samsung.lyceumreports.R;

import java.util.ArrayList;

public class StudentsAdapter extends ArrayAdapter<Student> {
    public StudentsAdapter(Context context, ArrayList<Student> students) {
        super(context, android.R.layout.simple_list_item_1, students);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final Student student = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        String studentText = student.name;
        ((TextView) convertView.findViewById(android.R.id.text1)).setText(studentText);
        int backgroundColor = student.absent ? R.color.absent : R.color.white;
        convertView.setBackgroundColor(getContext().getResources().getColor(backgroundColor));

        return convertView;
    }
}

package com.thecattest.samsung.lyceumreports.DataModels.Repositories;

import android.content.Context;

import com.thecattest.samsung.lyceumreports.DataModels.AppDatabase;
import com.thecattest.samsung.lyceumreports.DataModels.Dao.GroupDao;
import com.thecattest.samsung.lyceumreports.DataModels.Day;
import com.thecattest.samsung.lyceumreports.DataModels.DayWithAbsent;
import com.thecattest.samsung.lyceumreports.DataModels.Group;
import com.thecattest.samsung.lyceumreports.DataModels.GroupWithDaysAndStudents;
import com.thecattest.samsung.lyceumreports.DataModels.Student;

import java.util.LinkedList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class GroupRepository {

    private final DayRepository dayRepository;
    private final StudentRepository studentRepository;
    private final GroupDao groupDao;
    private final Flowable<List<GroupWithDaysAndStudents>> groups;

    public GroupRepository(Context context,
                           DayRepository dayRepository,
                           StudentRepository studentRepository) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.dayRepository = dayRepository;
        this.studentRepository = studentRepository;
        groupDao = db.groupDao();
        groups = groupDao.get();
    }

    public Flowable<List<GroupWithDaysAndStudents>> get() { return groups; }

    public void insert(Group group) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            studentRepository.insert(group.students);
            dayRepository.insert(group.days);
            groupDao.insert(group)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(AppDatabase.getDefaultObserver());
        });
    }

    public void insert(List<Group> groups) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            LinkedList<Student> students = new LinkedList<>();
            LinkedList<Day> days = new LinkedList<>();
            for (Group group : groups) {
                days.addAll(group.days);
                students.addAll(group.students);
            }
            studentRepository.insert(students);
            dayRepository.insert(days);
            groupDao.insert(groups)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(AppDatabase.getDefaultObserver());
        });
    }

    public void delete(GroupWithDaysAndStudents groupWithDaysAndStudents) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            studentRepository.delete(groupWithDaysAndStudents.students);
            dayRepository.delete(groupWithDaysAndStudents.days);
            groupDao.delete(groupWithDaysAndStudents.group)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(AppDatabase.getDefaultObserver());
        });
    }

    public void delete(List<GroupWithDaysAndStudents> groupsWithDaysAndStudents) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            LinkedList<Student> students = new LinkedList<>();
            LinkedList<DayWithAbsent> days = new LinkedList<>();
            LinkedList<Group> groups = new LinkedList<>();
            for (GroupWithDaysAndStudents groupWithDaysAndStudents
                    : groupsWithDaysAndStudents) {
                groups.add(groupWithDaysAndStudents.group);
                days.addAll(groupWithDaysAndStudents.days);
                students.addAll(groupWithDaysAndStudents.students);
            }
            studentRepository.delete(students);
            dayRepository.delete(days);
            groupDao.delete(groups)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(AppDatabase.getDefaultObserver());
        });
    }
}

package com.thecattest.samsung.lyceumreports.Data.Repositories;

import android.content.Context;

import androidx.annotation.NonNull;

import com.thecattest.samsung.lyceumreports.Data.AppDatabase;
import com.thecattest.samsung.lyceumreports.Data.Dao.DayDao;
import com.thecattest.samsung.lyceumreports.Data.Models.Day;
import com.thecattest.samsung.lyceumreports.Data.Models.DayAbsentCrossRef;
import com.thecattest.samsung.lyceumreports.Data.Models.DayWithAbsent;
import com.thecattest.samsung.lyceumreports.Data.Models.Student;

import java.util.LinkedList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.MaybeObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DayRepository {

    private final DayDao dayDao;
    private final StudentRepository studentRepository;
    private final Flowable<List<DayWithAbsent>> days;

    public DayRepository(Context context, StudentRepository studentRepository) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.studentRepository = studentRepository;
        dayDao = db.dayDao();
        days = dayDao.get();
    }

    public Flowable<List<DayWithAbsent>> get() {
        return days;
    }

    public void insert(List<Day> days) {
        LinkedList<Student> students = new LinkedList<>();
        LinkedList<DayAbsentCrossRef> refs = new LinkedList<>();

        for (Day day : days) {
            students.addAll(day.absent);
            for (Student student : day.absent) {
                refs.add(new DayAbsentCrossRef(day.did, student.sid));
            }
        }

        studentRepository.insert(students);
        dayDao.insert(days)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(AppDatabase.getDefaultObserver());
        dayDao.insertRefs(refs)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(AppDatabase.getDefaultObserver());
    }

    public void deleteByGroupIds(List<Integer> groupIds) {
        dayDao.deleteRefsByGroupIds(groupIds)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(AppDatabase.getDefaultObserver());
        dayDao.deleteByGroupIds(groupIds)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(AppDatabase.getDefaultObserver());
    }
}

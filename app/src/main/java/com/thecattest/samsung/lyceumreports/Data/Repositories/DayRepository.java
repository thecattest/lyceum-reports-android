package com.thecattest.samsung.lyceumreports.Data.Repositories;

import android.content.Context;

import com.thecattest.samsung.lyceumreports.Data.AppDatabase;
import com.thecattest.samsung.lyceumreports.Data.Dao.DayDao;
import com.thecattest.samsung.lyceumreports.Data.Models.Day;
import com.thecattest.samsung.lyceumreports.Data.Models.DayAbsentCrossRef;
import com.thecattest.samsung.lyceumreports.Data.Models.DayWithAbsent;
import com.thecattest.samsung.lyceumreports.Data.Models.Student;

import java.util.LinkedList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
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

    public void insert(Day day) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            studentRepository.insert(day.absent);
            dayDao.insert(day)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(AppDatabase.getDefaultObserver());
            LinkedList<DayAbsentCrossRef> refs = new LinkedList<>();
            for (Student student : day.absent) {
                refs.add(new DayAbsentCrossRef(day.did, student.sid));
            }
            dayDao.insertRefs(refs)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(AppDatabase.getDefaultObserver());
        });
    }

    public void insert(List<Day> days) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
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
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(AppDatabase.getDefaultObserver());
            dayDao.insertRefs(refs)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(AppDatabase.getDefaultObserver());
        });
    }

    public void delete(DayWithAbsent dayWithAbsent) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            dayDao.delete(dayWithAbsent.day)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(AppDatabase.getDefaultObserver());
            dayDao.deleteRefs(dayWithAbsent.day.did)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(AppDatabase.getDefaultObserver());
        });
    }

    public void delete(List<DayWithAbsent> daysWithAbsent) {
        LinkedList<Day> days = new LinkedList<>();
        for (DayWithAbsent dayWithAbsent : daysWithAbsent) {
            days.add(dayWithAbsent.day);
            dayDao.deleteRefs(dayWithAbsent.day.did)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(AppDatabase.getDefaultObserver());
        }
        dayDao.delete(days)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(AppDatabase.getDefaultObserver());
    }
}

package com.thecattest.samsung.lyceumreports.Data.Repositories;

import android.content.Context;

import com.thecattest.samsung.lyceumreports.Data.AppDatabase;
import com.thecattest.samsung.lyceumreports.Data.Dao.DayDao;
import com.thecattest.samsung.lyceumreports.Data.Models.Day;
import com.thecattest.samsung.lyceumreports.Data.Models.Relations.DayAbsentCrossRef;
import com.thecattest.samsung.lyceumreports.Data.Models.Relations.DayWithAbsent;
import com.thecattest.samsung.lyceumreports.Data.Models.Student;

import java.util.LinkedList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
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

    public Maybe<DayWithAbsent> getByGroupIdAndDate(int groupId, String date) {
        return dayDao.getByGroupIdAndDate(groupId, date);
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

    public void deleteAll() {
        dayDao.deleteAllRefs()
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(AppDatabase.getDefaultObserver());
        dayDao.deleteAll()
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

    public void deleteByGroupIdAndDate(int groupId, String date) {
        dayDao.deleteRefsByGroupIdAndDate(groupId, date)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(AppDatabase.getDefaultObserver());
        dayDao.deleteByGroupIdAndDate(groupId, date)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(AppDatabase.getDefaultObserver());
    }

    public void deleteByGroupIdsAndDates(List<Integer> groupIds, List<String> dates) {
        dayDao.deleteRefsByGroupIdsAndDates(groupIds, dates)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(AppDatabase.getDefaultObserver());
        dayDao.deleteByGroupIdsAndDates(groupIds, dates)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(AppDatabase.getDefaultObserver());
    }

    public void deleteAllButGroupIds(List<Integer> groupIds) {
        dayDao.deleteAllRefsButGroupIds(groupIds)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(AppDatabase.getDefaultObserver());
    }
}

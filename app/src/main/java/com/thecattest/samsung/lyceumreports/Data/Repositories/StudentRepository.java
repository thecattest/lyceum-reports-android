package com.thecattest.samsung.lyceumreports.Data.Repositories;

import android.content.Context;

import com.thecattest.samsung.lyceumreports.Data.AppDatabase;
import com.thecattest.samsung.lyceumreports.Data.Dao.StudentDao;
import com.thecattest.samsung.lyceumreports.Data.Models.Student;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class StudentRepository {

    public final StudentDao studentDao;
    private final Flowable<List<Student>> students;

    public StudentRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        studentDao = db.studentDao();
        students = studentDao.get();
    }

    public Flowable<List<Student>> get() {
        return students;
    }

    public void insert(List<Student> students) {
        studentDao.insert(students)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(AppDatabase.getDefaultObserver());
    }

//    public void deleteAll() {
//        studentDao.deleteAll()
//                .subscribeOn(Schedulers.single())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(AppDatabase.getDefaultObserver());
//    }

    public void deleteAllButGroupIds(List<Integer> groupIds) {
        studentDao.deleteAllButGroupIds(groupIds)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(AppDatabase.getDefaultObserver());
    }

    public void deleteByGroupIds(List<Integer> groupIds) {
            studentDao.deleteByGroupIds(groupIds)
                    .subscribeOn(Schedulers.single())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(AppDatabase.getDefaultObserver());
    }
}

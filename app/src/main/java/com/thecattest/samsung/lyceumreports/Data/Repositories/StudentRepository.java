package com.thecattest.samsung.lyceumreports.Data.Repositories;

import com.thecattest.samsung.lyceumreports.Data.AppDatabase;
import com.thecattest.samsung.lyceumreports.Data.Dao.StudentDao;
import com.thecattest.samsung.lyceumreports.Data.Models.Student;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;

public class StudentRepository {

    public final StudentDao studentDao;

    public StudentRepository(AppDatabase db) {
        studentDao = db.studentDao();
    }

    public void insert(List<Student> students) {
        studentDao.insert(students)
                .subscribeOn(AppDatabase.scheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(AppDatabase.getDefaultObserver());
    }

    public void deleteAllButGroupIds(List<Integer> groupIds) {
        studentDao.deleteAllButGroupIds(groupIds)
                .subscribeOn(AppDatabase.scheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(AppDatabase.getDefaultObserver());
    }

    public void deleteByGroupIds(List<Integer> groupIds) {
        studentDao.deleteByGroupIds(groupIds)
                .subscribeOn(AppDatabase.scheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(AppDatabase.getDefaultObserver());
    }

    public void deleteAll() {
        studentDao.deleteAll()
                .subscribeOn(AppDatabase.scheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(AppDatabase.getDefaultObserver());
    }
}

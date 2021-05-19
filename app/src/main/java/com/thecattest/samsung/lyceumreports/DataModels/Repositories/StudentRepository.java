package com.thecattest.samsung.lyceumreports.DataModels.Repositories;

import android.content.Context;

import com.thecattest.samsung.lyceumreports.DataModels.AppDatabase;
import com.thecattest.samsung.lyceumreports.DataModels.Dao.StudentDao;
import com.thecattest.samsung.lyceumreports.DataModels.Student;

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

    public void insert(Student student) {
        AppDatabase.databaseWriteExecutor.execute(() -> studentDao.insert(student)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(AppDatabase.getDefaultObserver()));
    }

    public void insert(List<Student> students) {
        AppDatabase.databaseWriteExecutor.execute(() -> studentDao.insert(students)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(AppDatabase.getDefaultObserver()));
    }

    public void delete(Student student) {
        AppDatabase.databaseWriteExecutor.execute(() -> studentDao.delete(student)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(AppDatabase.getDefaultObserver()));
    }

    public void delete(List<Student> students) {
        AppDatabase.databaseWriteExecutor.execute(() -> studentDao.delete(students)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(AppDatabase.getDefaultObserver()));
    }

    public void deleteAll() {
        AppDatabase.databaseWriteExecutor.execute(() -> studentDao.deleteAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(AppDatabase.getDefaultObserver()));
    }
}

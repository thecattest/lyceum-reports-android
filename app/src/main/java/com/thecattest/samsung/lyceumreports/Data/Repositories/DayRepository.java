package com.thecattest.samsung.lyceumreports.Data.Repositories;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.thecattest.samsung.lyceumreports.Data.ApiService;
import com.thecattest.samsung.lyceumreports.Data.AppDatabase;
import com.thecattest.samsung.lyceumreports.Data.Dao.DayDao;
import com.thecattest.samsung.lyceumreports.Data.Models.Day;
import com.thecattest.samsung.lyceumreports.Data.Models.Relations.DayAbsentCrossRef;
import com.thecattest.samsung.lyceumreports.Data.Models.Relations.DayWithAbsent;
import com.thecattest.samsung.lyceumreports.Data.Models.Student;
import com.thecattest.samsung.lyceumreports.DefaultCallback;
import com.thecattest.samsung.lyceumreports.Managers.LoginManager;
import com.thecattest.samsung.lyceumreports.R;

import java.util.LinkedList;
import java.util.List;

import io.reactivex.Maybe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Response;

public class DayRepository {

    private final DayDao dayDao;
    private final StudentRepository studentRepository;

    private final Context context;
    private final LoginManager loginManager;
    private final ApiService apiService;

    public DayRepository(Context context,
                         LoginManager loginManager,
                         StudentRepository studentRepository,
                         ApiService apiService) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.context = context;
        this.loginManager = loginManager;
        this.studentRepository = studentRepository;
        dayDao = db.dayDao();
        this.apiService = apiService;
    }

    public Maybe<DayWithAbsent> getByGroupIdAndDate(int groupId, String date) {
        return dayDao.getByGroupIdAndDate(groupId, date);
    }

    public void sendDay(View mainLayout,
                        DefaultCallback.OnPost onPost, DefaultCallback.OnPost onSuccess,
                        Day day) {
        Call<Void> sendDayCall = apiService.sendDay(day);
        sendDayCall.enqueue(new DefaultCallback<Void>(context, loginManager, mainLayout) {
            @Override
            public void onResponse200(Response<Void> response) {
                onSuccess.execute();
                day.isSyncedWithServer = true;
                update(day);
            }

            @Override
            public void onResponseFailure(Call<Void> call, Throwable t) {
                Snackbar snackbar = Snackbar.make(
                        mainLayout,
                        R.string.snackbar_server_error,
                        Snackbar.LENGTH_SHORT
                );
                snackbar.setAction(R.string.button_dismiss, v -> snackbar.dismiss());
                snackbar.show();
            }

            @Override
            public void onPostExecute() { onPost.execute(); }
        });
    }

    @SuppressLint("CheckResult")
    public void update(Day day) {
        deleteByGroupIdAndDate(day.groupId, day.date);
        dayDao.insert(day)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(insertedDayId -> {
                    LinkedList<DayAbsentCrossRef> refs = new LinkedList<>();
                    for (Student student : day.absent) {
                        refs.add(new DayAbsentCrossRef(insertedDayId.intValue(), student.sid));
                    }
                    studentRepository.insert(day.absent);
                    dayDao.insertRefs(refs)
                            .subscribeOn(Schedulers.single())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(AppDatabase.getDefaultObserver());
                });
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

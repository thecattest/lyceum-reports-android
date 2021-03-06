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
import com.thecattest.samsung.lyceumreports.Data.Models.Relations.DayWithAbsentAndGroup;
import com.thecattest.samsung.lyceumreports.Data.Models.Student;
import com.thecattest.samsung.lyceumreports.DefaultCallback;
import com.thecattest.samsung.lyceumreports.Managers.LoginManager;
import com.thecattest.samsung.lyceumreports.R;

import java.util.LinkedList;
import java.util.List;

import io.reactivex.Maybe;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import retrofit2.Call;
import retrofit2.Response;

public class DayRepository {

    private final DayDao dayDao;
    public final StudentRepository studentRepository;

    private Context context;
    private LoginManager loginManager;
    private View mainLayout;
    private final ApiService apiService;

    public DayRepository(Context context,
                         LoginManager loginManager,
                         View mainLayout,
                         ApiService apiService) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.context = context;
        this.loginManager = loginManager;
        this.mainLayout = mainLayout;
        this.studentRepository = new StudentRepository(db);
        dayDao = db.dayDao();
        this.apiService = apiService;
    }

    public DayRepository(Context context,
                         ApiService apiService) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.studentRepository = new StudentRepository(db);
        dayDao = db.dayDao();
        this.apiService = apiService;
    }

    public Maybe<List<DayWithAbsent>> getNotSynced() {
        return dayDao.getNotSynced();
    }

    public Maybe<List<DayWithAbsent>> getSummary(int groupId) {
        return dayDao.getSummary(groupId);
    }

    public Maybe<DayWithAbsent> getByGroupIdAndDate(int groupId, String date) {
        return dayDao.getByGroupIdAndDate(groupId, date);
    }

    public Maybe<List<DayWithAbsentAndGroup>> getByDate(String date) {
        return dayDao.getByDate(date);
    }

    public void sendDay(DefaultCallback.OnPost onPost, DefaultCallback.OnPost onSuccess,
                        Day day, View buttonsGroup) {
        Call<Void> sendDayCall = apiService.sendDay(day);
        if (context != null && loginManager != null && mainLayout != null)
            sendDayCall.enqueue(new DefaultCallback<Void>(context, loginManager, mainLayout) {
                @Override
                public void onResponse200(Response<Void> response) {
                    onSuccess.execute();
                    day.isSyncedWithServer = true;
                    update(day, AppDatabase.scheduler, AndroidSchedulers.mainThread());
                }

                @Override
                public void onResponseFailure(Call<Void> call, Throwable t) {
                    Snackbar snackbar = Snackbar.make(
                            mainLayout,
                            R.string.snackbar_will_be_sent_later,
                            Snackbar.LENGTH_LONG
                    );
                    snackbar.setAnchorView(buttonsGroup);
                    snackbar.setAction(R.string.button_ok, v -> snackbar.dismiss());
                    snackbar.show();
                }

                @Override
                public void onPostExecute() { onPost.execute(); }
            });
        else
            sendDayCall.enqueue(new DefaultCallback<Void>() {
                @Override
                public void onResponse200(Response<Void> response) {
                    onSuccess.execute();
                    day.isSyncedWithServer = true;
                    update(day, AppDatabase.serviceScheduler, AppDatabase.serviceScheduler);
                }

                @Override
                public void onResponseFailure(Call<Void> call, Throwable t) {

                }

                @Override
                public void onPostExecute() { onPost.execute(); }
            });
    }

    @SuppressLint("CheckResult")
    public void update(Day day, Scheduler scheduler, Scheduler schedulerToObserveOn) {
        deleteByGroupIdAndDate(day.groupId, day.date, scheduler, schedulerToObserveOn);
        dayDao.insert(day)
                .subscribeOn(scheduler)
                .observeOn(schedulerToObserveOn)
                .subscribe(insertedDayId -> {
                    LinkedList<DayAbsentCrossRef> refs = new LinkedList<>();
                    for (Student student : day.absent) {
                        refs.add(new DayAbsentCrossRef(insertedDayId.intValue(), student.sid));
                    }
                    studentRepository.insert(day.absent, scheduler, schedulerToObserveOn);
                    dayDao.insertRefs(refs)
                            .subscribeOn(scheduler)
                            .observeOn(schedulerToObserveOn)
                            .subscribe(AppDatabase.getDefaultObserver());
                });
    }

    public void insert(List<Day> days) {
        insert(days, AppDatabase.scheduler, AndroidSchedulers.mainThread());
    }

    public void insert(List<Day> days, Scheduler scheduler, Scheduler schedulerToObserveOn) {
        LinkedList<Student> students = new LinkedList<>();
        LinkedList<DayAbsentCrossRef> refs = new LinkedList<>();

        for (Day day : days) {
            students.addAll(day.absent);
            for (Student student : day.absent) {
                refs.add(new DayAbsentCrossRef(day.did, student.sid));
            }
        }

        studentRepository.insert(students, scheduler, schedulerToObserveOn);
        dayDao.insert(days)
                .subscribeOn(scheduler)
                .observeOn(schedulerToObserveOn)
                .subscribe(AppDatabase.getDefaultObserver());
        dayDao.insertRefs(refs)
                .subscribeOn(scheduler)
                .observeOn(schedulerToObserveOn)
                .subscribe(AppDatabase.getDefaultObserver());
    }

    public void deleteByGroupIdAndDate(int groupId, String date) {
        deleteByGroupIdAndDate(groupId, date, AppDatabase.scheduler, AndroidSchedulers.mainThread());
    }

    public void deleteByGroupIdAndDate(int groupId, String date, Scheduler scheduler, Scheduler schedulerToObserveOn) {
        dayDao.deleteRefsByGroupIdAndDate(groupId, date)
                .subscribeOn(scheduler)
                .observeOn(schedulerToObserveOn)
                .subscribe(AppDatabase.getDefaultObserver());
        dayDao.deleteByGroupIdAndDate(groupId, date)
                .subscribeOn(scheduler)
                .observeOn(schedulerToObserveOn)
                .subscribe(AppDatabase.getDefaultObserver());
    }

    public void deleteByGroupIdsAndDates(List<Integer> groupIds, List<String> dates) {
        deleteByGroupIdsAndDates(groupIds, dates, AppDatabase.scheduler, AndroidSchedulers.mainThread());
    }

    public void deleteByGroupIdsAndDates(List<Integer> groupIds, List<String> dates,
                                         Scheduler scheduler, Scheduler schedulerToObserveOn) {
        dayDao.deleteRefsByGroupIdsAndDates(groupIds, dates)
                .subscribeOn(scheduler)
                .observeOn(schedulerToObserveOn)
                .subscribe(AppDatabase.getDefaultObserver());
        dayDao.deleteByGroupIdsAndDates(groupIds, dates)
                .subscribeOn(scheduler)
                .observeOn(schedulerToObserveOn)
                .subscribe(AppDatabase.getDefaultObserver());
    }

    public void deleteAllButGroupIds(List<Integer> groupIds) {
        dayDao.deleteAllButGroupIds(groupIds)
                .subscribeOn(AppDatabase.scheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(AppDatabase.getDefaultObserver());
    }

    public void deleteSyncedRefs(List<Integer> groupIds, List<String> dates) {
        dayDao.deleteSyncedRefsByGroupIdsAndDates(groupIds, dates)
                .subscribeOn(AppDatabase.scheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(AppDatabase.getDefaultObserver());
    }

    public void deleteAll() {
        dayDao.deleteAllRefs()
                .subscribeOn(AppDatabase.scheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(AppDatabase.getDefaultObserver());
        dayDao.deleteAll()
                .subscribeOn(AppDatabase.scheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(AppDatabase.getDefaultObserver());
    }
}

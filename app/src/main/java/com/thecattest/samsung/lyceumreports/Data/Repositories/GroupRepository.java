package com.thecattest.samsung.lyceumreports.Data.Repositories;

import android.content.Context;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.thecattest.samsung.lyceumreports.Data.ApiService;
import com.thecattest.samsung.lyceumreports.Data.AppDatabase;
import com.thecattest.samsung.lyceumreports.Data.Dao.GroupDao;
import com.thecattest.samsung.lyceumreports.Data.Models.Day;
import com.thecattest.samsung.lyceumreports.Data.Models.Group;
import com.thecattest.samsung.lyceumreports.Data.Models.Relations.GroupWithDaysAndStudents;
import com.thecattest.samsung.lyceumreports.Data.Models.Relations.GroupWithStudents;
import com.thecattest.samsung.lyceumreports.Data.Models.Student;
import com.thecattest.samsung.lyceumreports.DefaultCallback;
import com.thecattest.samsung.lyceumreports.Managers.LoginManager;
import com.thecattest.samsung.lyceumreports.R;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Response;

public class GroupRepository {

    private final DayRepository dayRepository;
    private final StudentRepository studentRepository;

    private final ApiService apiService;

    private final GroupDao groupDao;
    private final Flowable<List<GroupWithDaysAndStudents>> groups;

    public GroupRepository(Context context,
                           DayRepository dayRepository,
                           StudentRepository studentRepository,
                           ApiService apiService) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.dayRepository = dayRepository;
        this.studentRepository = studentRepository;
        this.apiService = apiService;
        groupDao = db.groupDao();
        groups = groupDao.get();
    }

    public Flowable<List<GroupWithDaysAndStudents>> get() { return groups; }

    public Flowable<List<GroupWithStudents>> getById(int groupId) {
        return groupDao.getById(groupId);
    }

    public void refreshGroups(Context context, LoginManager loginManager,
                              View mainLayout, DefaultCallback.OnPost onPost) {
        Call<ArrayList<Group>> groupsRefreshCall = apiService.getGroups();
        groupsRefreshCall.enqueue(new DefaultCallback<ArrayList<Group>>(context, loginManager, mainLayout) {
            @Override
            public void onResponse200(Response<ArrayList<Group>> response) {
                insert(response.body());
            }

            @Override
            public void onResponseFailure(Call<ArrayList<Group>> call, Throwable t) {
                Snackbar.make(
                        mainLayout,
                        R.string.snackbar_server_error,
                        Snackbar.LENGTH_LONG
                ).show();
            }

            @Override
            public void onPostExecute() { onPost.execute(); }
        });
    }

    public void refreshGroup(Context context, LoginManager loginManager,
                             View mainLayout, DefaultCallback.OnPost onPost,
                             int groupId, String date) {
        Call<Group> groupRefreshCall = apiService.getGroup(groupId, date);
        groupRefreshCall.enqueue(new DefaultCallback<Group>(context, loginManager, mainLayout) {
            @Override
            public void onResponse200(Response<Group> response) {
                insert(response.body());
            }

            @Override
            public void onResponseFailure(Call<Group> call, Throwable t) {
                Snackbar.make(
                        mainLayout,
                        R.string.snackbar_server_error,
                        Snackbar.LENGTH_LONG
                ).show();
            }

            @Override
            public void onPostExecute() { onPost.execute(); }
        });
    }

    public void insert(Group group) {
        studentRepository.insert(group.students);
        dayRepository.insert(group.days);
        groupDao.insert(group)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(AppDatabase.getDefaultObserver());
    }

    public void insert(List<Group> groups) {
        LinkedList<Student> students = new LinkedList<>();
        LinkedList<Day> days = new LinkedList<>();
        LinkedList<Integer> groupIds = new LinkedList<>();
        LinkedList<String> dates = new LinkedList<>();
        for (Group group : groups) {
            groupIds.add(group.gid);
            days.addAll(group.days);
            for (Day day : days)
                dates.add(day.date);
            students.addAll(group.students);
        }
        deleteByIdsAndDates(groupIds, dates);
        deleteAllButIds(groupIds);
        studentRepository.insert(students);
        dayRepository.insert(days);
        groupDao.insert(groups)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(AppDatabase.getDefaultObserver());
    }

    public void deleteByIdAndDate(int groupId, String date) {
        ArrayList<Integer> groupIds = new ArrayList<>();
        groupIds.add(groupId);
        studentRepository.deleteByGroupIds(groupIds);
        dayRepository.deleteByGroupIdAndDate(groupId, date);
        groupDao.deleteById(groupId)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(AppDatabase.getDefaultObserver());
    }

    public void deleteByIdsAndDates(List<Integer> groupIds, List<String> dates) {
        studentRepository.deleteByGroupIds(groupIds);
        dayRepository.deleteByGroupIdsAndDates(groupIds, dates);
        groupDao.deleteByIds(groupIds)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(AppDatabase.getDefaultObserver());
    }

//    public void deleteAll() {
//        studentRepository.deleteAll();
//        dayRepository.deleteAll();
//        groupDao.deleteAll()
//                .subscribeOn(Schedulers.single())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(AppDatabase.getDefaultObserver());
//    }

    public void deleteAllButIds(List<Integer> groupIds) {
        studentRepository.deleteAllButGroupIds(groupIds);
        dayRepository.deleteAllButGroupIds(groupIds);
        groupDao.deleteAllButIds(groupIds)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(AppDatabase.getDefaultObserver());
    }
}

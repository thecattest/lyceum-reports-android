package com.thecattest.samsung.lyceumreports.Data.Repositories;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

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

import io.reactivex.Maybe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Response;

public class GroupRepository {

    private final GroupDao groupDao;
    private final DayRepository dayRepository;
    private final StudentRepository studentRepository;

    private final Context context;
    private final LoginManager loginManager;
    private final View mainLayout;
    private final ApiService apiService;

    public GroupRepository(Context context,
                           LoginManager loginManager,
                           View mainLayout,
                           DayRepository dayRepository,
                           StudentRepository studentRepository,
                           ApiService apiService) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.context = context;
        this.loginManager = loginManager;
        this.mainLayout = mainLayout;
        this.dayRepository = dayRepository;
        this.studentRepository = studentRepository;
        this.apiService = apiService;
        groupDao = db.groupDao();
    }

    public Maybe<List<GroupWithDaysAndStudents>> get() { return groupDao.get(); }

    public Maybe<GroupWithStudents> getById(int groupId) { return groupDao.getById(groupId); }

    public void refreshGroups(DefaultCallback.OnPost onPost, DefaultCallback.OnPost onSuccess) {
        Call<ArrayList<Group>> groupsRefreshCall = apiService.getGroups();
        groupsRefreshCall.enqueue(new DefaultCallback<ArrayList<Group>>(context, loginManager, mainLayout) {
            @Override
            public void onResponse200(Response<ArrayList<Group>> response) {
                ArrayList<Group> groups = response.body();
                insert(groups);
                if (!groups.isEmpty())
                    onSuccess.execute();
            }

            @Override
            public void onResponseFailure(Call<ArrayList<Group>> call, Throwable t) {
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

    public void refreshGroup(DefaultCallback.OnPost onPost, DefaultCallback.OnPost onSuccess,
                             int groupId, String date) {
        Call<Group> groupRefreshCall = apiService.getGroup(groupId, date);
        groupRefreshCall.enqueue(new DefaultCallback<Group>(context, loginManager, mainLayout) {
            @Override
            public void onResponse200(Response<Group> response) {
                insert(response.body(), date);
                onSuccess.execute();
            }

            @Override
            public void onResponseFailure(Call<Group> call, Throwable t) {
                Toast.makeText(context, R.string.snackbar_server_error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPostExecute() { onPost.execute(); }
        });
    }

    public void refreshDaySummary(DefaultCallback.OnPost onPost, DefaultCallback.OnPost onSuccess,
                                  String date) {
        Call<ArrayList<Group>> refreshDaySummary = apiService.getDaySummary(date);
        refreshDaySummary.enqueue(new DefaultCallback<ArrayList<Group>>(context, loginManager, mainLayout) {
            @Override
            public void onResponse200(Response<ArrayList<Group>> response) {
                ArrayList<Group> groups = response.body();
                insert(groups);
                if (!groups.isEmpty())
                    onSuccess.execute();
            }

            @Override
            public void onResponseFailure(Call<ArrayList<Group>> call, Throwable t) {
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

    public void insert(Group group, String date) {
        deleteByIdAndDate(group.gid, date);
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
            if (group.days == null)
                continue;
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

    public void deleteAllButIds(List<Integer> groupIds) {
        studentRepository.deleteAllButGroupIds(groupIds);
        dayRepository.deleteAllButGroupIds(groupIds);
        groupDao.deleteAllButIds(groupIds)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(AppDatabase.getDefaultObserver());
    }
}

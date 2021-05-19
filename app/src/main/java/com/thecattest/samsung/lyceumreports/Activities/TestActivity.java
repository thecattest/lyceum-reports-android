package com.thecattest.samsung.lyceumreports.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.thecattest.samsung.lyceumreports.DataModels.ApiService;
import com.thecattest.samsung.lyceumreports.DataModels.DayWithAbsent;
import com.thecattest.samsung.lyceumreports.DataModels.Group;
import com.thecattest.samsung.lyceumreports.DataModels.GroupWithDaysAndStudents;
import com.thecattest.samsung.lyceumreports.DataModels.Repositories.DayRepository;
import com.thecattest.samsung.lyceumreports.DataModels.Repositories.GroupRepository;
import com.thecattest.samsung.lyceumreports.DataModels.Repositories.StudentRepository;
import com.thecattest.samsung.lyceumreports.DataModels.Student;
import com.thecattest.samsung.lyceumreports.Adapters.TestListViewAdapter;
import com.thecattest.samsung.lyceumreports.DefaultCallback;
import com.thecattest.samsung.lyceumreports.Managers.LoginManager;
import com.thecattest.samsung.lyceumreports.Managers.RetrofitManager;
import com.thecattest.samsung.lyceumreports.R;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

public class TestActivity extends AppCompatActivity {

    public static final String TAG = "TestActivityLog";
    ArrayList<Group> groups = new ArrayList<>();
    ApiService apiService;

    StudentRepository studentRepository;
    DayRepository dayRepository;
    GroupRepository groupRepository;

    LoginManager loginManager;

    ListView listViewStudents;
    ListView listViewDays;
    ListView listViewGroups;

    public Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        context = getApplicationContext();

        initManagers();
        initRetrofit();
        findViews();
        setListeners();
        initRepositories();

        updateGroups();
    }

    private void initManagers() {
        loginManager = new LoginManager(this);
    }

    private void initRetrofit() {
        Retrofit retrofit = RetrofitManager.getInstance(loginManager);
        apiService = retrofit.create(ApiService.class);
    }

    private void findViews() {
        listViewStudents = findViewById(R.id.listViewStudents);
        listViewDays = findViewById(R.id.listViewDays);
        listViewGroups = findViewById(R.id.listViewGroups);
    }

    private void setListeners() {
        listViewStudents.setOnItemClickListener((parent, view, position, id) -> {
            Student student = (Student) parent.getItemAtPosition(position);
            studentRepository.delete(student);
        });

        listViewDays.setOnItemClickListener((parent, view, position, id) -> {
            DayWithAbsent dayWithAbsent = (DayWithAbsent) parent.getItemAtPosition(position);
            dayRepository.delete(dayWithAbsent);
        });

        listViewGroups.setOnItemClickListener((parent, view, position, id) -> {
            GroupWithDaysAndStudents groupWithDaysAndStudents = (GroupWithDaysAndStudents) parent.getItemAtPosition(position);
            groupRepository.delete(groupWithDaysAndStudents);
        });
    }

    private void initRepositories() {
        studentRepository = new StudentRepository(this);
        dayRepository = new DayRepository(this, studentRepository);
        groupRepository = new GroupRepository(this, dayRepository, studentRepository);
    }

    private void updateGroups() {
        apiService.getGroups().enqueue(new DefaultCallback<ArrayList<Group>>(this, loginManager) {
            @Override
            public void onResponse200(Response<ArrayList<Group>> response) {
                groups = response.body();
                putIntoDB();
                Log.d(TAG, groups.toString());
            }

            @Override
            public void onResponseFailure(Call<ArrayList<Group>> call, Throwable t) {
                Log.d(TAG, "Failed to get groups");
            }
        });
    }

    @SuppressLint("CheckResult")
    private void putIntoDB() {
        groupRepository.insert(groups);
        studentRepository.get()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::updateStudentsAdapter);
        dayRepository.get()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::updateDaysAdapter);
        groupRepository.get()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::updateGroupsAdapter);
    }

    private void updateGroupsAdapter(List<GroupWithDaysAndStudents> groupsWithDaysAndStudents) {
        Log.d(TAG, groupsWithDaysAndStudents.toString());
        TestListViewAdapter adapter = new TestListViewAdapter(this, new ArrayList<>(groupsWithDaysAndStudents));
        listViewGroups.setAdapter(adapter);
    }

    private void updateDaysAdapter(List<DayWithAbsent> daysWithAbsent) {
        Log.d(TAG, daysWithAbsent.toString());
        TestListViewAdapter adapter = new TestListViewAdapter(this, new ArrayList<>(daysWithAbsent));
        listViewDays.setAdapter(adapter);
    }

    private void updateStudentsAdapter(List<Student> students) {
        TestListViewAdapter adapter = new TestListViewAdapter(this, new ArrayList<>(students));
        listViewStudents.setAdapter(adapter);
    }
}
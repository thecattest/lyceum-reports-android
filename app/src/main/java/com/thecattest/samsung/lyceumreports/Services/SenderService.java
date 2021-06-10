package com.thecattest.samsung.lyceumreports.Services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.thecattest.samsung.lyceumreports.Data.ApiService;
import com.thecattest.samsung.lyceumreports.Data.AppDatabase;
import com.thecattest.samsung.lyceumreports.Data.Models.Day;
import com.thecattest.samsung.lyceumreports.Data.Models.Relations.DayWithAbsent;
import com.thecattest.samsung.lyceumreports.Data.Repositories.DayRepository;
import com.thecattest.samsung.lyceumreports.Managers.LoginManager;
import com.thecattest.samsung.lyceumreports.Managers.RetrofitManager;

import java.util.List;

import io.reactivex.Maybe;
import retrofit2.Retrofit;

public class SenderService extends Service {
    private DayRepository dayRepository;
    private boolean running = true;

    public SenderService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        showToast("service created");
        LoginManager loginManager = new LoginManager(this);
        Retrofit retrofit = RetrofitManager.getInstance(loginManager);
        ApiService apiService = retrofit.create(ApiService.class);
        dayRepository = new DayRepository(this, apiService);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showToast("service start command");
        new MainTask().execute();
        // stopSelf();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        showToast("service destroyed");
        running = false;
        super.onDestroy();
    }

    class MainTask extends AsyncTask<Void, Void, Void> {
        @Override
        @SuppressLint("CheckResult")
        protected Void doInBackground(Void... voids) {
            while (running) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Maybe<List<DayWithAbsent>> notSynced = dayRepository.getNotSynced();
                notSynced
                        .subscribeOn(AppDatabase.scheduler)
                        .observeOn(AppDatabase.scheduler)
                        .subscribe(daysWithAbsent -> {
                            for (DayWithAbsent dayWithAbsent : daysWithAbsent) {
                                Day day = new Day(dayWithAbsent.day.groupId, dayWithAbsent.day.date, dayWithAbsent);
                                showToast(day.groupId + ": " + day.date);
                                dayRepository.sendDay(() -> {
                                }, () -> {
                                    showToast(day.groupId + ": " + day.date + " - sent!");
                                }, day);
                            }
                        });
            }
            return null;
        }
    }

    private void showToast(String text) {
        Message msg = new Message();
        msg.obj = text;
        handler.sendMessage(msg);
    }

    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            Toast.makeText(getApplicationContext(), (String) message.obj, Toast.LENGTH_SHORT).show();
        }
    };


}
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
import com.thecattest.samsung.lyceumreports.Data.Repositories.GroupRepository;
import com.thecattest.samsung.lyceumreports.Managers.LoginManager;
import com.thecattest.samsung.lyceumreports.Managers.RetrofitManager;

import java.util.List;

import io.reactivex.Maybe;
import retrofit2.Retrofit;

public class SyncService extends Service {
    public static String REDRAW_BROADCAST = "UPDATER_SERVICE_CHANNEL";

    private GroupRepository groupRepository;
    private DayRepository dayRepository;
    private LoginManager loginManager;
    private boolean running = true;
    private final boolean showToasts = false;

    public SyncService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        showToast("service created");
        loginManager = new LoginManager(this);
        Retrofit retrofit = RetrofitManager.getInstance(loginManager);
        ApiService apiService = retrofit.create(ApiService.class);
        groupRepository = new GroupRepository(this, loginManager, apiService);
        dayRepository = groupRepository.dayRepository;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showToast("service start command");
        new MainTask().execute();
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
        protected Void doInBackground(Void... voids) {
            while (running) {
                try {
                    Thread.sleep(5000);
                    if (loginManager.getAutoUpdate())
                        getUpdates();
                    else if (loginManager.getAutoSend())
                        sendNotSynced();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    private void getUpdates() {
        showToast("getting updates");
        groupRepository.getUpdates(this::sendNotSynced, () -> {
            showToast("got updates");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Intent i = new Intent(REDRAW_BROADCAST);
            sendBroadcast(i);
        });
    }

    @SuppressLint("CheckResult")
    private void sendNotSynced() {
        if (!loginManager.getAutoSend())
            return;
//        try {
//            Thread.sleep(800);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        Maybe<List<DayWithAbsent>> notSynced = dayRepository.getNotSynced();
        notSynced
                .subscribeOn(AppDatabase.serviceScheduler)
                .observeOn(AppDatabase.serviceScheduler)
                .subscribe(daysWithAbsent -> {
                    if (daysWithAbsent.isEmpty())
                        return;
                    DayWithAbsent dayWithAbsent = daysWithAbsent.get(0);
                    Day day = new Day(dayWithAbsent.day.groupId, dayWithAbsent.day.date, dayWithAbsent);
                    showToast(day.groupId + ": " + day.date);
                    dayRepository.sendDay(
                        () -> {
                            if (daysWithAbsent.size() > 1)
                                sendNotSynced();
                        }, () -> {
                            showToast(day.groupId + ": " + day.date + " - sent!");
                        }, day, null);
                });
    }

    private void showToast(String text) {
        if (!showToasts)
            return;
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
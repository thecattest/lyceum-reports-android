package com.thecattest.samsung.lyceumreports.Data;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.thecattest.samsung.lyceumreports.Data.Dao.DayDao;
import com.thecattest.samsung.lyceumreports.Data.Dao.GroupDao;
import com.thecattest.samsung.lyceumreports.Data.Dao.StudentDao;
import com.thecattest.samsung.lyceumreports.Data.Models.Day;
import com.thecattest.samsung.lyceumreports.Data.Models.DayAbsentCrossRef;
import com.thecattest.samsung.lyceumreports.Data.Models.Group;
import com.thecattest.samsung.lyceumreports.Data.Models.Student;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.MaybeObserver;
import io.reactivex.disposables.Disposable;

@Database(entities = {Group.class, Day.class, Student.class, DayAbsentCrossRef.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public abstract GroupDao groupDao();
    public abstract DayDao dayDao();
    public abstract StudentDao studentDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "Reports")
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static MaybeObserver<Object> getDefaultObserver() {
        return new MaybeObserver<Object>() {
            private final static String TAG = "Database";
            @Override
            public void onSubscribe(Disposable d) {}

            @Override
            public void onSuccess(Object o) {

            }

            @Override
            public void onComplete() {
                Log.d(TAG, "successful");
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, e.toString());
            }
        };
    }
}

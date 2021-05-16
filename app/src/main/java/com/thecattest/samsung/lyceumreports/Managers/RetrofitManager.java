package com.thecattest.samsung.lyceumreports.Managers;

import com.thecattest.samsung.lyceumreports.URLConfig;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitManager {
    private final Retrofit retrofit;

    public RetrofitManager() {
        retrofit = new Retrofit.Builder()
                .baseUrl(URLConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public RetrofitManager(LoginManager loginManager) {
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.addInterceptor(chain -> {
            Request request = chain
                    .request()
                    .newBuilder()
                    .addHeader("Cookie", loginManager.getCookie())
                    .build();
            return chain.proceed(request);
        });
        retrofit = new Retrofit.Builder()
                .baseUrl(URLConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client.build())
                .build();
    }

    public Retrofit getInstance() {
        return retrofit;
    }
}

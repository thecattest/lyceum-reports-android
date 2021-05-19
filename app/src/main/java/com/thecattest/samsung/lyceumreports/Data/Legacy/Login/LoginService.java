package com.thecattest.samsung.lyceumreports.Data.Legacy.Login;

import com.thecattest.samsung.lyceumreports.URLConfig;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface LoginService {
    @FormUrlEncoded
    @POST(URLConfig.LOGIN_ENDPOINT)
    Call<Void> login(
            @Field("login") String login,
            @Field("password") String password);
}

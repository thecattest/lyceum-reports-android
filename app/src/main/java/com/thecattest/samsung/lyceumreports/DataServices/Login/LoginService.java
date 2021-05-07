package com.thecattest.samsung.lyceumreports.DataServices.Login;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface LoginService {
    @FormUrlEncoded
    @POST("/api/login")
    Call<Void> login(@Field("login") String login, @Field("password") String password);
}

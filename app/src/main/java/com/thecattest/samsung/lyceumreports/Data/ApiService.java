package com.thecattest.samsung.lyceumreports.Data;

import com.thecattest.samsung.lyceumreports.Data.Models.Day;
import com.thecattest.samsung.lyceumreports.Data.Models.Group;
import com.thecattest.samsung.lyceumreports.URLConfig;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @FormUrlEncoded
    @POST(URLConfig.LOGIN_ENDPOINT)
    Call<Void> login(
            @Field("login") String login,
            @Field("password") String password);

    @GET(URLConfig.GROUPS_LIST_ENDPOINT)
    Call<ArrayList<Group>> getGroups();

    @GET(URLConfig.GROUP_ENDPOINT)
    Call<Group> getGroup(@Path("group_id") int groupId, @Path("date") String date);

    @POST(URLConfig.DAYS_LIST_ENDPOINT)
    Call<Void> updateDay(@Body Day day);
}

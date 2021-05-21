package com.thecattest.samsung.lyceumreports.Data;

import com.thecattest.samsung.lyceumreports.Data.Models.Day;
import com.thecattest.samsung.lyceumreports.Data.Models.Group;
import com.thecattest.samsung.lyceumreports.Data.Models.Permissions;
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
    @POST(URLConfig.V2_LOGIN_ENDPOINT)
    Call<Permissions> login(
            @Field("login") String login,
            @Field("password") String password);

    @GET(URLConfig.V2_GROUPS_LIST_ENDPOINT)
    Call<ArrayList<Group>> getGroups();

    @GET(URLConfig.V2_GROUP_ENDPOINT)
    Call<Group> getGroup(@Path("group_id") int groupId, @Path("date") String date);

    @POST(URLConfig.V2_DAYS_LIST_ENDPOINT)
    Call<Void> sendDay(@Body Day day);

    @GET(URLConfig.V2_DAYS_ENDPOINT)
    Call<ArrayList<Group>> getDaySummary(@Path("date") String date);
}

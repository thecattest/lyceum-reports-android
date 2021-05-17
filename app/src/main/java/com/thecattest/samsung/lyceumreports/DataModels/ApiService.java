package com.thecattest.samsung.lyceumreports.DataModels;

import com.thecattest.samsung.lyceumreports.URLConfig;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @GET(URLConfig.GROUPS_LIST_ENDPOINT)
    Call<ArrayList<Group>> getGroups();

    @GET(URLConfig.GROUP_ENDPOINT)
    Call<Group> getGroup(@Path("group_id") int groupId, @Path("date") String date);

    @POST(URLConfig.DAYS_LIST_ENDPOINT)
    Call<Void> updateDay(@Body Day day);
}

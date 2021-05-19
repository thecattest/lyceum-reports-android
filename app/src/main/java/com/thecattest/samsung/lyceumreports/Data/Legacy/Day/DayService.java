package com.thecattest.samsung.lyceumreports.Data.Legacy.Day;

import com.thecattest.samsung.lyceumreports.URLConfig;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface DayService {
    @GET(URLConfig.DAY_ENDPOINT)
    Call<Day> getDay(
            @Path("groupId") Integer groupId,
            @Query("date") String date);

    @POST(URLConfig.DAY_ENDPOINT)
    Call<Void> updateDay(
            @Path("groupId") Integer groupId,
            @Body DayPost day);
}

package com.thecattest.samsung.lyceumreports.DataServices.Day;
import com.thecattest.samsung.lyceumreports.DataServices.Day.Day;
import com.thecattest.samsung.lyceumreports.DataServices.Day.DayPost;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface DayService {
    @GET("/api/day/{groupId}")
    Call<Day> getDay(@Path("groupId") Integer groupId, @Query("date") String date);

    @POST("/api/day/{groupId}")
    Call<Void> updateDay(@Path("groupId") Integer groupId, @Body DayPost day);
}

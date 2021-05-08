package com.thecattest.samsung.lyceumreports.DataServices.SummaryDay;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface SummaryDayService {
    @GET("/api/summary/day/{date}")
    Call<SummaryDay> getSummaryDay(
            @Header("Cookie") String cookie,
            @Path("date") String date
    );
}

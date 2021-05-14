package com.thecattest.samsung.lyceumreports.DataServices.SummaryDay;

import com.thecattest.samsung.lyceumreports.URLConfig;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface SummaryDayService {
    @GET(URLConfig.SUMMARY_DAY_ENDPOINT)
    Call<SummaryDay> getSummaryDay(
            @Header("Cookie") String cookie,
            @Path("date") String date);
}

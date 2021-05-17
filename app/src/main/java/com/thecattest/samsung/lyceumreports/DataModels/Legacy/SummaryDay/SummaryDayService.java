package com.thecattest.samsung.lyceumreports.DataModels.Legacy.SummaryDay;

import com.thecattest.samsung.lyceumreports.URLConfig;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface SummaryDayService {
    @GET(URLConfig.SUMMARY_DAY_ENDPOINT)
    Call<SummaryDay> getSummaryDay(@Path("date") String date);
}

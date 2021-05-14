package com.thecattest.samsung.lyceumreports.DataModels.Summary;

import com.thecattest.samsung.lyceumreports.URLConfig;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface SummaryService {
    @GET(URLConfig.SUMMARY_ENDPOINT)
    Call<SummaryWithPermissions> getSummary(
            @Header("Cookie") String cookies);
}

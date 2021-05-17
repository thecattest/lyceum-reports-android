package com.thecattest.samsung.lyceumreports.DataModels.Legacy.Summary;

import com.thecattest.samsung.lyceumreports.URLConfig;

import retrofit2.Call;
import retrofit2.http.GET;

public interface SummaryService {
    @GET(URLConfig.SUMMARY_ENDPOINT)
    Call<SummaryWithPermissions> getSummary();
}

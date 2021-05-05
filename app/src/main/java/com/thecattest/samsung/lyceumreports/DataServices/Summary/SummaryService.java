package com.thecattest.samsung.lyceumreports.DataServices.Summary;

import com.thecattest.samsung.lyceumreports.DataServices.Summary.Summary;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.GET;

public interface SummaryService {
    @GET("/api/summary")
    Call<ArrayList<Summary>> getSummary();
}

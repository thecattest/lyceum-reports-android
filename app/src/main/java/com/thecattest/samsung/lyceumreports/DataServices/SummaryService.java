package com.thecattest.samsung.lyceumreports.DataServices;

import java.lang.reflect.Array;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.GET;

public interface SummaryService {
    @GET("/api/summary")
    Call<ArrayList<Summary>> getSummary();
}

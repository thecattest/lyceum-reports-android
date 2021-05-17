package com.thecattest.samsung.lyceumreports.DataModels.Legacy.Summary;

import android.content.Context;

public interface SummaryRowInterface {
    String getTodayDate(Context context);

    String getTodayAbsentStudentsString(Context context);

    String getYesterdayDate(Context context);

    String getYesterdayAbsentStudentsString(Context context);
}

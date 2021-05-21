package com.thecattest.samsung.lyceumreports;

public class URLConfig {
    public static final String BASE_URL = "http:92.53.124.98:8000";
    public static final String LOGIN_ENDPOINT = "/api/login";

    public static final String SUMMARY_ENDPOINT = "/api/summary";
    public static final String SUMMARY_DAY_ENDPOINT = "/api/summary/day/{date}";
    public static final String DAY_ENDPOINT = "/api/day/{groupId}";

    public static final String V2_LOGIN_ENDPOINT = "/api/v2/login/";
    public static final String V2_GROUPS_LIST_ENDPOINT = "/api/v2/groups/";
    public static final String V2_GROUP_ENDPOINT = "/api/v2/groups/{group_id}/{date}";
    public static final String V2_DAYS_LIST_ENDPOINT = "/api/v2/days/";
    public static final String V2_DAYS_ENDPOINT = "/api/v2/days/{date}";
}

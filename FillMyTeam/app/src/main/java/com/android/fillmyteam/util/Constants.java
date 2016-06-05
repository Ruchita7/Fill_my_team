package com.android.fillmyteam.util;

import com.android.fillmyteam.BuildConfig;

/**
 * Created by dgnc on 5/9/2016.
 */
public class Constants {

    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME =
            "com.android.fillmyteam";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME +
            ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
            ".LOCATION_DATA_EXTRA";

    public static final String APP_URL = BuildConfig.APP_ROOT_URL;

    public static final String LOCATION_USERS = "users";
    public static final String NEARBY_PLAYERS = "players";
    public static final String APP_URL_USERS = APP_URL + "/" + LOCATION_USERS;
    public static final String APP_PLAYERS_NEAR_URL = APP_URL + "/" + NEARBY_PLAYERS;

    public static final String NAME = "name";
    public static final String EMAIL = "email";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String SPORT = "sport";
    public static final String PLAYING_EVERYDAY = "playingEveryDay";
    public static final String PLAYINGTIME = "playingTime";
    public static final String NOTIFY = "notify";
    public static final String PLAYERS_NOTIFICATIONS = APP_URL + "/" + NOTIFY;
    public static final String NOTIFY_USER = "NOTIFY_USER";

    public static final String SPORTS_URL = "https://api.myjson.com/bins/5alsc";
    public static final String LIST = "list";
    public static final String YOUTUBE_KEY = BuildConfig.YOUTUBE_KEY;
    //  public static final String SPORTS="Sport";


    public static final int REQ_START_STANDALONE_PLAYER = 1;
    public static final int REQ_RESOLVE_SERVICE_MISSING = 2;

    public static final String STORE_LOCATOR_BASE_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json";
    public static final String QUERY="query";
    public static final String LOCATION_KEY="key";
    public static final String LOCATION="location";
    public static final String RADIUS="radius";
    public static final String TEN_KM_RADIUS="10000";
    public static final String LOCATION_BASE_URL = "https://maps.googleapis.com/maps/api/place/";
}

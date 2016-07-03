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
    public static final String MATCHES = "matches";
    public static final String PLAYERS_MATCHES = APP_URL + "/" + MATCHES;
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

    //public static final String SPORTS_URL = "https://api.myjson.com/bins/5alsc";
    public static final String SPORTS_URL = "https://api.myjson.com/bins/432j9";
    public static final String LIST = "list";
    public static final String YOUTUBE_KEY = BuildConfig.YOUTUBE_KEY;
    //  public static final String SPORTS="Sport";


    public static final int REQ_START_STANDALONE_PLAYER = 1;
    public static final int REQ_RESOLVE_SERVICE_MISSING = 2;

    public static final String STORE_LOCATOR_BASE_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json";
    public static final String QUERY = "query";
    public static final String LOCATION_KEY = "key";
    public static final String LOCATION = "location";
    public static final String RADIUS = "radius";
    public static final String TEN_KM_RADIUS = "10000";
    public static final String LOCATION_BASE_URL = "https://maps.googleapis.com/maps/api/place/";


    public static final String TYPE = "type";
    public static final String KEY = "key";
    public static final String TIME_PLACE = "time_place";
    public static final String MESSAGE = "msg";
    public static final String USER_DETAILS = "user";
    public static final String PM = " PM";
    public static final String AM = " AM";
    public static final String PLAY_TIME = "playingTime";
    public static final String PLAY_DATE = "playingDate";
    public static final String PLAYING_PLACE = "playingPlace";
    public static final String PLAYER_EMAIL = "playerEmail";
//    public static final String SPORT="Sport";

    public static final String ALL = "All";
    public static final String BASKETBALL = "Basketball";
    public static final String TENNIS = "Tennis";
    public static final String FOOTBALL = "Football";
    public static final String CRICKET = "Cricket";
    public static final String BADMINTON = "Badminton";
    public static final String HOCKEY = "Hockey";
    public static final String TABLE_TENNIS = "TableTennis";
    public static final String RUGBY = "Rugby";
    public static final String VOLLEY_BALL = "VolleyBall";
    public static final String BASEBALL = "BaseBall";

    //public static final String EMAIL="EMAIL";


    public static final String SEND_NOTIFICATION = "SEND NOTIFICATION";
    public static final String TIME_PICKER = "timePicker";

    public static final String USER_CREDENTIALS = "User Credentials";
    public static final String MATCH_SCHEDULED = "match scheduled";
    public static final String SPORTS_NAME = "name";
    public static final String OBJECTIVE = "objective";
    public static final String PLAYERS = "players";
    public static final String RULES = "rules";
    public static final String THUMBNAIL = "thumbnail";
    public static final String IMAGE = "image";
    public static final String VIDEO_REFERENCE = "video_reference";
    public static final String GET_REQUEST = "GET";
    public static final String SPORT_ID = "ID";
    public static final String ICON_VIEW = "iconView";
    public static final String ASC_ORDER = " ASC";
    //   public static final String GEO_LOCATION= "google.navigation:q=";
    public static final String GOOGLE_MAPS_PACKAGE = "com.google.android.apps.maps";

    public static final String RESULTS = "results";
    public static final String LAT = "lat";
    public static final String LNG = "lng";
    public static final String PLACE_NAME = "name";
    public static final String GEOMETRY = "geometry";
    public static final String PLACE_LOCATION = "location";
    public static final String FORMATTED_ADDRESS = "formatted_address";
    public static final String LOGGED_IN_USER_EMAIL = "user_already_logged_in";
    public static final String EVENT_ID = "event_id";
    public static final String CALENDAR_EVENT_CREATION = "calendar_created";
    public static final String PLAYING_WITH = "playingWith";

    public static final String MATCH_URL = "https://fill-my-team-1315.firebaseio.com/matches/";

    public static final String LOGOUT = "logout";


    // public static final String REFERENCE="reference";
    public static final String PHOTO_URL = "https://maps.googleapis.com/maps/api/place/photo";
    public static final String MAX_WIDTH = "maxwidth";
    public static final String REFERENCE_ID = "photoreference";
    public static final String WIDTH_VALUE = "100";
    public static final String PHOTOS = "photos";

    public static final String PHOTO_REFERENCE = "photo_reference";

    public static final String IMAGE_THUMBNAIL = "http://img.youtube.com/vi/";
    public static final String DEFAULT_IMG = "default.jpg";
    public static final String YOUTUBE_URL = "https://www.youtube.com/watch";
    public static final String VIDEO_REF = "v";

    public static final String USER_INFO = "user_info";


}

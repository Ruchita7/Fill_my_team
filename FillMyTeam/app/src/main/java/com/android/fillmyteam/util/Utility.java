package com.android.fillmyteam.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.view.inputmethod.InputMethodManager;

import com.android.fillmyteam.R;
import com.android.fillmyteam.model.User;
import com.android.fillmyteam.sync.SportsSyncAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

/**
 * Created by dgnc on 5/8/2016.
 */
public class Utility {
    public static final String months[] = {
            "Jan", "Feb", "Mar", "Apr",
            "May", "Jun", "Jul", "Aug",
            "Sep", "Oct", "Nov", "Dec"};

    public static final String timezone[] = {"AM", "PM"};

    public static String getCurrentDate(GregorianCalendar gcalendar) {
        String date = gcalendar.get(Calendar.DATE) + " " + months[gcalendar.get(Calendar.MONTH)] + " " + gcalendar.get(Calendar.YEAR);
        return date;
    }

    public static String getCurrentTime(GregorianCalendar gcalendar) {
        int minute = gcalendar.get(Calendar.MINUTE);
        String minuteString = "";
        if (minute < 10) {
            minuteString += "0" + minute;
        } else {
            minuteString += minute;
        }
        String time = gcalendar.get(Calendar.HOUR) + ":" + minuteString + " " + timezone[gcalendar.get(Calendar.AM_PM)];
        return time;
    }

    public static String getPlayingDate(String inputDate, String inputTime) {
        Date date = null;
        String displayDate = null;
        try {
            String dateString = inputDate.replace(" ", "-");
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:SS");
            SimpleDateFormat parseFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm a");
            date = parseFormat.parse(dateString + " " + inputTime);
            displayDate = displayFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return displayDate;
    }

    public static String encodeEmail(String emailId) {
        return emailId.replace(".", ",");
    }


    public static String decodeEmail(String emailId) {
        return emailId.replace(",", ".");
    }

    public static User retrieveUserObject(Map<String, Object> userMap) {
        User user = new User();
        String key;
        for (Map.Entry<String, Object> userValues : userMap.entrySet()) {
            key = userValues.getKey();
            switch (key) {
                case Constants.NAME:
                    user.setName((String) userValues.getValue());
                    break;
                case Constants.EMAIL:
                    user.setEmail((String) userValues.getValue());
                    break;
                case Constants.LATITUDE:
                    user.setLatitude((Double) userValues.getValue());
                    break;
                case Constants.LONGITUDE:
                    user.setLongitude((Double) userValues.getValue());
                    break;

                case Constants.PLAYINGTIME:
                    user.setPlayingTime((String) userValues.getValue());
                    break;
                case Constants.SPORT:
                    user.setSport((String) userValues.getValue());
                    break;
            }
        }
        return user;
    }

    public static String getPlayingDateInfoMarker(String inputDate, String inputTime) {
        Date date = null;
        String displayDate = null;
        try {
            String dateString = inputDate.replace(" ", "-");
            SimpleDateFormat parseFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:SS");
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm a");
            date = parseFormat.parse(dateString + " " + inputTime);
            displayDate = displayFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return displayDate;
    }

    public static String getPlayingTimeInfo(String inputTime) {
        Date date = null;
        String displayDate = null;
        try {
            //   String dateString = inputDate.replace(" ","-");
            SimpleDateFormat parseFormat = new SimpleDateFormat("HH:mm:SS");
            SimpleDateFormat displayFormat = new SimpleDateFormat("hh:mm a");
            date = parseFormat.parse(inputTime);
            displayDate = displayFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return displayDate;
    }

    public static String[] retrieveHourMinute(String inputTime) {
        int[] timeArr;
        String[] playingTime = inputTime.split(" ");
        String timeInterval;
        String[] timeStamps = null;
        if (playingTime.length == 2) {
            timeInterval = playingTime[1];
            timeStamps = playingTime[0].split(":");
            if (timeStamps.length == 2) {
                if (timeInterval.equalsIgnoreCase("PM")) {
                    timeStamps[0] = String.valueOf(Integer.parseInt(timeStamps[0]) + 12);
                }
            }
        }
        return timeStamps;
    }

    public static String updateTime(int hourOfDay, int minute) {

        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.set(Calendar.HOUR, hourOfDay);
        gregorianCalendar.set(Calendar.MINUTE, minute);
        String time;
        if (hourOfDay == 0) {
            time = "12:";
        } else {
            if (hourOfDay > 12) {
                time = (hourOfDay - 12) + ":";
            } else {
                time = hourOfDay + ":";
            }
        }
        if (minute < 10) {
            time += "0" + gregorianCalendar.get(Calendar.MINUTE);
        } else {
            time += gregorianCalendar.get(Calendar.MINUTE);
        }
        String playingTime;
        if (hourOfDay >= 12) {
            //     playingTime = time + " PM";
            playingTime = time + Constants.PM;

        } else {
            //    playingTime = time + " AM";
            playingTime = time + Constants.AM;
        }

        return playingTime;
        //  mUser.setPlayingTime(playingTime);
    }

    public static int retrieveSportsIcon(String sportName) {
        int sportDrawable = 0;
        switch (sportName) {

            case Constants.FOOTBALL:
                sportDrawable = R.drawable.ic_football;
                break;
            case Constants.CRICKET:
                sportDrawable = R.drawable.cricket;
                break;
            case Constants.BASKETBALL:
                sportDrawable = R.drawable.ic_basketball;
                break;
            case Constants.HOCKEY:
                sportDrawable = R.drawable.ic_hockey;
                break;
            case Constants.TENNIS:
                sportDrawable = R.drawable.ic_tennis;
                break;
            case Constants.BADMINTON:
                sportDrawable = R.drawable.ic_badminton;
                break;
            case Constants.BASE_BALL:
            case Constants.BASEBALL:
                sportDrawable = R.drawable.ic_baseball;
                break;
            case Constants.RUGBY:
                sportDrawable = R.drawable.ic_rugby;
                break;
            case Constants.VOLLEYBALL:
            case Constants.VOLLEY_BALL:
                sportDrawable = R.drawable.ic_volleyball;
                break;
            case Constants.TABLETENNIS:
            case Constants.TABLE_TENNIS:
                sportDrawable = R.drawable.ic_table_tennis;
                break;
        }
        return sportDrawable;
    }

    public static boolean compareDate(int date, int year, int month) {
        GregorianCalendar currenDate = new GregorianCalendar();

        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.set(Calendar.DATE, date);
        gregorianCalendar.set(Calendar.MONTH, month);
        gregorianCalendar.set(Calendar.YEAR, year);
        if (gregorianCalendar.before(currenDate)) {
            return true;
        }
        return false;
    }


    public static boolean compareTime(int hour, int minute) {
        GregorianCalendar currentTime = new GregorianCalendar();

        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.set(Calendar.HOUR_OF_DAY, hour);
        gregorianCalendar.set(Calendar.MINUTE, minute);
        //    gregorianCalendar.set(Calendar.YEAR,year);
        if (gregorianCalendar.before(currentTime)) {
            return true;
        }
        return false;
    }

    public static void hideSoftKeyboard(Activity activity) {
        if (activity.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }


    public static boolean checkNetworkState(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return ((networkInfo != null && networkInfo.isConnectedOrConnecting()));
    }


    public static void setNetworkState(Context context,  int locationStatus, String key) {

        String syncStatus = key;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(syncStatus, locationStatus);
        editor.commit();
    }

    public static int getNetworkState(Context context, String key) {
        //String syncStatus = context.getString(R.string.pref_location_status_key);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(key, SportsSyncAdapter.STATUS_UNKNOWN);


    }

    @SuppressWarnings("ResourceType")
    static public @SportsSyncAdapter.MatchStatus
    int getNetworkState(Context context) {
        //String syncStatus = context.getString(R.string.pref_location_status_key);
        String syncStatus = context.getString(R.string.network_status_key);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(syncStatus, SportsSyncAdapter.STATUS_UNKNOWN);



    }
}

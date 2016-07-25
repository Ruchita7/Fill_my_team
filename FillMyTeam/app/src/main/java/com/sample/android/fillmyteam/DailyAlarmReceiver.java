package com.sample.android.fillmyteam;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.sample.android.fillmyteam.util.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Class for creating inexact repeating alarm at players playing time
 *
 * @author Ruchita_Maheshwary
 */
public class DailyAlarmReceiver extends WakefulBroadcastReceiver {

    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;
    String mPlayingTime;
    public static final int ALARM_FREQUENCY = 24 * 1000 * 60 * 60;

    public void cancelAlarm() {
        if (alarmManager != null) {
            alarmManager.cancel(alarmIntent);
        }

    }

    /**
     * set alarm to notify user about his playing time
     *
     * @param context
     * @param playingTime
     * @param playingLocation
     * @param notifyBeforeInterval
     */
    public void setAlarmTime(Context context, String playingTime, String sport, String playingLocation, int notifyBeforeInterval) {
        mPlayingTime = playingTime;
        GregorianCalendar time = new GregorianCalendar();
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);

        try {
            time.setTime(sdf.parse(playingTime));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        int hour = time.get(Calendar.HOUR);
        int minute = time.get(Calendar.MINUTE);
        switch (notifyBeforeInterval) {
            case Constants.FIFTEEN_MINUTES:
                if (minute < Constants.FIFTEEN_MINUTES) {
                    hour = hour - 1;
                }
                minute = minute - Constants.FIFTEEN_MINUTES;
                break;
            case Constants.THIRTY_MINUTES:
                if (minute < Constants.THIRTY_MINUTES) {
                    hour = hour - Constants.ONE_HOUR;
                }
                minute = minute - Constants.THIRTY_MINUTES;
                break;
            case Constants.SIXTY_MINUTES:
                hour = hour - Constants.ONE_HOUR;
                break;
            case Constants.ONE_TWENTY_MINUTES:
                hour = hour - Constants.TWO_HOUR;
                break;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR, hour);
        calendar.set(Calendar.MINUTE, minute);

        Intent intent = new Intent(context, DailyAlarmReceiver.class);
        intent.putExtra(Constants.TIME_PLACE, context.getString(R.string.player_time_location, sport,playingLocation, mPlayingTime));
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), ALARM_FREQUENCY, alarmIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String timePlace = intent.getStringExtra(Constants.TIME_PLACE);
        Intent alarmIntent = new Intent(context, ScheduleDailyAlarmService.class);
        alarmIntent.putExtra(Constants.MESSAGE, context.getString(R.string.playing_message, timePlace));
        startWakefulService(context, alarmIntent);
    }


}

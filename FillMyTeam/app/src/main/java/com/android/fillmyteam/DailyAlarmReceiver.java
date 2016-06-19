package com.android.fillmyteam;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.android.fillmyteam.util.Constants;
import com.android.fillmyteam.util.Utility;

import java.util.Calendar;

/**
 * Created by dgnc on 6/5/2016.
 */
public class DailyAlarmReceiver extends WakefulBroadcastReceiver {

    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;
    String mPlayingTime;

    public void cancelAlarm() {
        if (alarmManager != null) {
            alarmManager.cancel(alarmIntent);
        }

    }

    public void setAlarmTime(Context context, String playingTime, String playingLocation, int notifyBeforeInterval) {
        mPlayingTime = playingTime;
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        String[] timeStamps = Utility.retrieveHourMinute(playingTime);
        int hour = Integer.parseInt(timeStamps[0]);
        int minute = Integer.parseInt(timeStamps[1]);
        switch (notifyBeforeInterval) {
            case 15:
                if (minute < 15) {
                    hour = hour - 1;
                }
                minute = minute - 15;
                break;
            case 30:
                if (minute < 30) {
                    hour = hour - 1;
                }
                minute = minute - 30;
                break;
            case 60:
                hour = hour - 1;
                break;
            case 120:
                hour = hour - 2;
                break;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR, hour);
        calendar.set(Calendar.MINUTE, minute);
        //    calendar.add(Calendar.MINUTE,-30);
        Intent intent = new Intent(context, DailyAlarmReceiver.class);
        //  intent.putExtra(Constants.TIME_PLACE,playingLocation+" at "+mPlayingTime);
        intent.putExtra(Constants.TIME_PLACE, context.getString(R.string.player_time_location, playingLocation, mPlayingTime));
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), (24 * 1000 * 60 * 60), alarmIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //  String timePlace = intent.getStringExtra("time_place");
        String timePlace = intent.getStringExtra(Constants.TIME_PLACE);
        Intent alarmIntent = new Intent(context, ScheduleDailyAlarmService.class);
        // alarmIntent.putExtra("MSG", "You are scheduled to play in "+timePlace);
        alarmIntent.putExtra(Constants.MESSAGE, context.getString(R.string.playing_message, timePlace));
        startWakefulService(context, alarmIntent);
    }


}

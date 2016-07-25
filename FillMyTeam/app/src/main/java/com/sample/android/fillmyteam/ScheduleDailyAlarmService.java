package com.sample.android.fillmyteam;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.sample.android.fillmyteam.util.Constants;

/**
 * Service to notify user about upcoming matches
 * @author Ruchita_Maheshwary
 *
 */
public class ScheduleDailyAlarmService extends IntentService {
    public static final int NOTIFICATION_ID = 1;

    public ScheduleDailyAlarmService() {
        super("ScheduleDailyAlarm");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.hasExtra(Constants.MESSAGE)) {
            String msg = intent.getStringExtra(Constants.MESSAGE);
            sendNotification(msg);
        }
        DailyAlarmReceiver.completeWakefulIntent(intent);
    }

    public void sendNotification(String message) {
        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(Constants.MATCH_SCHEDULED)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message))
                        .setContentText(message);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }


}

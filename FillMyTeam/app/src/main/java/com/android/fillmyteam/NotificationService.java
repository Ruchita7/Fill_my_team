package com.android.fillmyteam;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.fillmyteam.model.PlayerParcelable;
import com.android.fillmyteam.util.Constants;
import com.android.fillmyteam.util.Utility;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

/**
 * Created by dgnc on 5/22/2016.
 */
public class NotificationService extends IntentService implements ValueEventListener{
    Firebase mFirebase;
    String mEmailId;

    public static final String LOG_TAG=NotificationService.class.getSimpleName();
   public NotificationService()    {
       super(NotificationService.class.getName());
   }

    public NotificationService(String name) {
        super(name);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        PlayerParcelable parcelable = intent.getParcelableExtra(Constants.NOTIFY_USER);
        mEmailId = Utility.encodeEmail(parcelable.getUser().getEmail());
        mFirebase  =new Firebase(Constants.PLAYERS_NOTIFICATIONS).child(mEmailId);
        mFirebase.addValueEventListener(this);
    }



    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        String msg = dataSnapshot.child("msg").getValue().toString();
        //String email= PreferenceManager.getDefaultSharedPreferences(this).getString("email","");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String email=prefs.getString("email","");
        if(msg!=null&&!msg.isEmpty()&&!email.isEmpty()&&email.equals(mEmailId))   {
            showNotification(msg);

        }
    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {

    }

    private void showNotification(String msg){
        //Creating a notification
        Log.v(LOG_TAG,"in showNotification");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        Intent intent = new Intent(this,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setContentIntent(pendingIntent);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentTitle("Firebase Push Notification");
        builder.setContentText(msg);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
        mFirebase.child("msg").setValue("");
    }
}

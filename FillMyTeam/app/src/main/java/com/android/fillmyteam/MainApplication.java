package com.android.fillmyteam;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.firebase.client.Firebase;
import com.firebase.client.Logger;

/**
 * Created by dgnc on 5/22/2016.
 */
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        Firebase.getDefaultConfig().setLogLevel(Logger.Level.DEBUG);

       Firebase.getDefaultConfig().setPersistenceEnabled(true);
       // FirebaseDatabase.getInstance().setPersistenceEnabled(true);


    }
}

package com.sample.android.fillmyteam;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sample.android.fillmyteam.model.User;
import com.sample.android.fillmyteam.util.Constants;
import com.sample.android.fillmyteam.util.Utility;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity implements Preference.OnPreferenceChangeListener,ValueEventListener {

    DatabaseReference mUrlRef;
    String mEmail;
    DatabaseReference ref;
    User mUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        mEmail = sharedPreferences.getString(Constants.EMAIL, "");
        mUrlRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(Constants.APP_URL_USERS);
        ref = mUrlRef.child("/" + Utility.encodeEmail(mEmail));
        ref.addListenerForSingleValueEvent(this);
        addPreferencesFromResource(R.xml.pref_general);
        bindPreferenceSummaryToValue(findPreference(getString(R.string.notify_frequency_key)));

        CheckBoxPreference checkBoxPreference = (CheckBoxPreference) getPreferenceManager().findPreference(getString(R.string.cal_event_key));
        checkBoxPreference.setOnPreferenceChangeListener(this);

    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        if (preference instanceof ListPreference) {
            this.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));


        }
    }



    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final String interval = newValue.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(interval);
            preference.setSummary(
                    index >= 0
                            ? listPreference.getEntries()[index]
                            : null);

            if (mUser != null) {
                if (interval != null && !interval.isEmpty()) {
                    int notifyBeforeInterval = Integer.parseInt(interval);
                    DailyAlarmReceiver alarmReceiver = new DailyAlarmReceiver();
                    alarmReceiver.cancelAlarm();
                    alarmReceiver.setAlarmTime(this, mUser.getPlayingTime(), mUser.getSport(), mUser.getPlayingPlace(), notifyBeforeInterval);
                }
            }
        }

        else

        {
            SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(Constants.CALENDAR_EVENT_CREATION,(Boolean)newValue);
            editor.commit();
        }
        return true;
    }


    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        mUser = dataSnapshot.getValue(User.class);
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mUrlRef.removeEventListener(this);
    }


}

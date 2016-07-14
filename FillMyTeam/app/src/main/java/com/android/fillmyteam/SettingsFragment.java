package com.android.fillmyteam;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.android.fillmyteam.model.User;
import com.android.fillmyteam.util.Constants;
import com.android.fillmyteam.util.Utility;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Fragment to handle user settings
 * @author Ruchita_Maheshwary
 *
 */

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener,
       ValueEventListener {
    public static final String LOG_TAG = SettingsFragment.class.getSimpleName();
    
    public static SettingsFragment newInstance() {
        SettingsFragment settingsFragment = new SettingsFragment();

        return settingsFragment;
    }

    DatabaseReference mUrlRef;
    String mEmail;
    DatabaseReference ref;
    User mUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
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
    public void onDestroyView() {
        super.onDestroyView();
        mUrlRef.removeEventListener(this);
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
                    alarmReceiver.setAlarmTime(getActivity(), mUser.getPlayingTime(), mUser.getSport(), mUser.getPlayingPlace(), notifyBeforeInterval);
                }
            }
        }

       else

        {
            SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(getActivity());
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


}



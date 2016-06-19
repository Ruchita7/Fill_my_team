package com.android.fillmyteam;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;

import com.android.fillmyteam.model.User;
import com.android.fillmyteam.util.Constants;
import com.android.fillmyteam.util.Utility;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by dgnc on 6/17/2016.
 */


public class SettingsFragment extends Fragment {
    public static final String LOG_TAG = SettingsPrefFragment.class.getSimpleName();
    //  com.google.api.services.calendar.Calendar mService;

    Context mContext;
    // GoogleAccountCredential credential;

    Activity mActivity;

    public static SettingsFragment newInstance() {
        SettingsFragment settingsFragment = new SettingsFragment();

        return settingsFragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getContext();
        mActivity = getActivity();
        getActivity().getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, new SettingsPrefFragment())
                .commit();
    }

    private class SettingsPrefFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener,
            SharedPreferences.OnSharedPreferenceChangeListener, ValueEventListener {
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

            /*CheckBoxPreference checkBoxPreference = (CheckBoxPreference) getPreferenceManager().findPreference(getString(R.string.cal_event_key));
            checkBoxPreference.setOnPreferenceClickListener(this);*/
//       bindPreferenceSummaryToValue(findPreference(getString(R.string.cal_event_key)));
        }

        @Override
        public void onResume() {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            sp.registerOnSharedPreferenceChangeListener(this);
            super.onResume();
        }

        // Unregisters a shared preference change listener
        @Override
        public void onPause() {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            sp.unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
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
            // Trigger the listener immediately with the preference's
            // current value.
   /*    else {
           String value = PreferenceManager
                   .getDefaultSharedPreferences(preference.getContext())
                   .getString(preference.getKey(), "");
           preference.setSummary(value);
       }*/
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

                // ref.child(Constants.PLAYING_PLACE).
                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);


                //    String playingLocation=mUser.getPlayingPlace();
                //  String playingTime=mUser.getPlayingTime();
                if (mUser != null) {
                    if (interval != null && !interval.isEmpty()) {
                        int notifyBeforeInterval = Integer.parseInt(interval);
                        DailyAlarmReceiver alarmReceiver = new DailyAlarmReceiver();
                        alarmReceiver.cancelAlarm();
                        alarmReceiver.setAlarmTime(getActivity(), mUser.getPlayingTime(), mUser.getPlayingPlace(), notifyBeforeInterval);
                    }
                }
            }

//                preference.setSummary(stringValue);


            else

            {

            }

            return true;
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            //findPreference(getString(R.string.notify_frequency_key)).getSummary()
       /* if (key.equals(getString(R.string.notify_frequency_key))) {
            Log.v("SettingsPrefFragment", "notify_frequency_key");
        } else if (key.equals(getString(R.string.cal_event_key))) {
            Log.v("SettingsPrefFragment", "cal_event_key");
        }*/

        }


        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mUser = dataSnapshot.getValue(User.class);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }




    }


}

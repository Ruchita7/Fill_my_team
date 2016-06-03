package com.android.fillmyteam;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.android.fillmyteam.model.User;
import com.android.fillmyteam.util.Constants;
import com.android.fillmyteam.util.Utility;
import com.firebase.client.Firebase;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.GregorianCalendar;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditProfileFragment extends Fragment  implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    Context mContext;
    double mLatitude;
    double mLongitude;
  //  Firebase mUrlRef;
  DatabaseReference mUrlRef;
    static TextView mDate;
    static TextView mTime;
    Button submitButton;
    GeoFire mGeoFire;
    String mPlayingTime;
    public EditProfileFragment() {
        // Required empty public constructor
    }


    public static EditProfileFragment newInstance(double latitude, double longitude) {
        EditProfileFragment fragment = new EditProfileFragment();
        Bundle args = new Bundle();
        args.putDouble(Constants.LATITUDE, latitude);
        args.putDouble(Constants.LONGITUDE, longitude);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        if (getArguments() != null) {
            mLatitude = getArguments().getDouble(Constants.LATITUDE);
            mLongitude = getArguments().getDouble(Constants.LONGITUDE);
            //   mParam2 = getArguments().getString(ARG_PARAM2);
        }
      //  mPlayerParcelables = new ArrayList<>();
        //    GeoFire geoFire = new GeoFire(new Firebase(Constants.APP_PLAYERS_NEAR_URL));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        mGeoFire = new GeoFire(new Firebase(Constants.APP_PLAYERS_NEAR_URL));
      //  mUrlRef = new Firebase(Constants.APP_URL_USERS);
       mUrlRef = FirebaseDatabase.getInstance()
               .getReferenceFromUrl(Constants.APP_URL_USERS);
        GregorianCalendar gcalendar = new GregorianCalendar();
        mDate = (TextView) view.findViewById(R.id.date);
        mDate.setText(Utility.getCurrentDate(gcalendar));
        TextView placeTextView = (TextView) view.findViewById(R.id.place);
        placeTextView.setText(mLatitude + " " + mLongitude);
        mDate.setOnClickListener(this);
        mTime = (TextView) view.findViewById(R.id.time);
        mTime.setText(Utility.getCurrentTime(gcalendar));
        mTime.setOnClickListener(this);
        submitButton = (Button) view.findViewById(R.id.saveUserButton);
        submitButton.setOnClickListener(this);
      /*  mFindButton = (Button) view.findViewById(R.id.findButton);
        mFindButton.setOnClickListener(this);
        GeoQuery geoQuery = mGeoFire.queryAtLocation(new GeoLocation(28.6988839,77.1082443), 1);
        geoQuery.addGeoQueryEventListener(this);*/
        return  view;
    }


    @Override
    public void onClick(View v) {
        DialogFragment newFragment;
        switch (v.getId()) {

            case R.id.time:
                newFragment = new TimePickerFragment();
                newFragment.show(getActivity().getSupportFragmentManager(), "timePicker");
                break;

            case R.id.date:
                newFragment = new DatePickerFragment();
                newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
                break;

            case R.id.saveUserButton:
                mPlayingTime = Utility.getPlayingDate(mDate.getText().toString(), mTime.getText().toString());
                //    Log.v(LOG_TAG,"playing date is"+date);
                saveUserData();
                break;
        }
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Log.v("Time Dialog", hourOfDay + ":" + minute);
            updateTime(hourOfDay, minute);
        }
    }

    public static void updateTime(int hourOfDay, int minute) {

        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.set(Calendar.HOUR, hourOfDay);
        gregorianCalendar.set(Calendar.MINUTE, minute);
        String time = gregorianCalendar.get(Calendar.HOUR) + ":" + gregorianCalendar.get(Calendar.MINUTE);
        if (hourOfDay > 12) {
            mTime.setText(time + " PM");
        } else {
            mTime.setText(time + " AM");
        }


    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Log.v("Date Dialog", day + " " + month + "" + year);
            updateDate(day, month, year);
        }
    }

    public static void updateDate(int day, int month, int year) {
        mDate.setText(day + " " + Utility.months[month] + " " + year);
    }


    private void saveUserData() {

        User user1 = new User("Ruchita", "ruchita.maheshwary@gmail.com", "tennis", 28.6948631, 77.11113064, false, mPlayingTime);
        DatabaseReference pushRef =    mUrlRef.push();
        pushRef.setValue(user1);
        mGeoFire.setLocation(Utility.encodeEmail(user1.getEmail()), new GeoLocation(user1.getLatitude(), user1.getLongitude()));

        User user2 = new User("Rachit", "rachit.maheshwary@gmail.com", "football", 28.6987125, 77.1161068, false, mPlayingTime);
        pushRef =    mUrlRef.push();
        pushRef.setValue(user2);
        mGeoFire.setLocation(Utility.encodeEmail(user2.getEmail()), new GeoLocation(user2.getLatitude(), user2.getLongitude()));

        User user3 = new User("Purnima", "poomah29@gmail.com", "football", 28.6988839,77.1082443, false, mPlayingTime);
        pushRef =    mUrlRef.push();
        pushRef.setValue(user3);
        mGeoFire.setLocation(Utility.encodeEmail(user3.getEmail()), new GeoLocation(user3.getLatitude(), user3.getLongitude()));

    }

}

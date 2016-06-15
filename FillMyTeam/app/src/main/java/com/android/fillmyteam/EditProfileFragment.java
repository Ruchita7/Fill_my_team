package com.android.fillmyteam;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.fillmyteam.model.User;
import com.android.fillmyteam.util.Constants;
import com.android.fillmyteam.util.Utility;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.GregorianCalendar;

import retrofit.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditProfileFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {
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
    //  static TextView mDate;
    static TextView mTime;
    Button submitButton;
    //   GeoFire mGeoFire;
    String mPlayingTime;
    User mUser;
    ValueEventListener mCurrentUserRefListener;
    DatabaseReference mUserRef;
    Retrofit retrofit;
    public static final String LOG_TAG = EditProfileFragment.class.getSimpleName();
    String mParkLocation;
    TextView mPlaceTextView;
    ImageView mPlacePickerImageView;
    private static final int REQUEST_PLACE_PICKER = 1;
    Spinner mSportsListSpinner;
    ArrayAdapter<CharSequence> mAdapter;
    DailyAlarmReceiver alarmReceiver = new DailyAlarmReceiver();

    public EditProfileFragment() {
        // Required empty public constructor
    }


    public static EditProfileFragment newInstance(User user) {
        EditProfileFragment fragment = new EditProfileFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.USER_DETAILS, user);
        /*args.putDouble(Constants.LATITUDE, latitude);
        args.putDouble(Constants.LONGITUDE, longitude);*/
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
      /*  retrofit = new Retrofit.Builder()
                .baseUrl(Constants.LOCATION_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();*/
        if (getArguments() != null) {
            mUser = (User) getArguments().getSerializable(Constants.USER_DETAILS);
            /*mLatitude = getArguments().getDouble(Constants.LATITUDE);
            mLongitude = getArguments().getDouble(Constants.LONGITUDE);*/
            //   mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mUrlRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(Constants.APP_URL_USERS);


        mUserRef = mUrlRef.child("/" + Utility.encodeEmail(mUser.getEmail()));
        mCurrentUserRefListener = mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUser = dataSnapshot.getValue(User.class);
                updateViews();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //  mPlayerParcelables = new ArrayList<>();
        //    GeoFire geoFire = new GeoFire(new Firebase(Constants.APP_PLAYERS_NEAR_URL));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        //mGeoFire = new GeoFire(new Firebase(Constants.APP_PLAYERS_NEAR_URL));
        //  mUrlRef = new Firebase(Constants.APP_URL_USERS);


        GregorianCalendar gcalendar = new GregorianCalendar();
      /*  mDate = (TextView) view.findViewById(R.id.date);
        mDate.setText(Utility.getCurrentDate(gcalendar));*/
        mPlacePickerImageView = (ImageView) view.findViewById(R.id.picker_image_view);
        mPlacePickerImageView.setOnClickListener(this);
        mPlaceTextView = (TextView) view.findViewById(R.id.place_text_view);
        // mPlaceTextView.setText(mUser.getLatitude() + " " + mUser.getLongitude());
        // mDate.setOnClickListener(this);
        mTime = (TextView) view.findViewById(R.id.time);
        //  mTime.setText(Utility.getCurrentTime(gcalendar));

        mTime.setOnClickListener(this);
        submitButton = (Button) view.findViewById(R.id.saveUserButton);
        submitButton.setOnClickListener(this);
        mSportsListSpinner = (Spinner) view.findViewById(R.id.sports_list_spinner);
        mAdapter = ArrayAdapter.createFromResource(getContext(), R.array.sports_list, android.R.layout.simple_spinner_item);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSportsListSpinner.setAdapter(mAdapter);
        mSportsListSpinner.setOnItemSelectedListener(this);
        updateViews();
       /*  Call call = null;
       FindLocationsAPI locationsAPI = retrofit.create(FindLocationsAPI.class);
        call = locationsAPI.retrieveParksNearMe(mUser.getLatitude() + "," + mUser.getLongitude(), "park", "5000", getString(R.string.map_key));
        call.enqueue(new Callback<LocationResponse>() {
            @Override
            public void onResponse(Response<LocationResponse> response, Retrofit retrofit) {
                LocationResponse locationResponse = response.body();
                if (locationResponse == null) {
                    ResponseBody responseErrBody = response.errorBody();
                    if (responseErrBody != null) {

                        try {
                            String str = response.errorBody().string();
                        } catch (IOException e) {
                            Log.e(LOG_TAG, e.getMessage());
                            return;
                        }
                    }
                }
                List<Result> locationResults = locationResponse.getResults();
                Result firstResult = locationResults.get(0);
                mParkLocation = firstResult.getFormatted_address();
                double latitude=firstResult.getGeometry().getLocation().getLat();
                double longitude=firstResult.getGeometry().getLocation().getLng();
                mUser.setLatitude(latitude);
                mUser.setLongitude(longitude);
                mPlaceTextView.setText(mParkLocation);
                Log.v(LOG_TAG, "park location" + latitude+","+longitude);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(LOG_TAG, t.getMessage());
            }
        });*/

        return view;
    }



    private void updateViews() {
        mTime.setText(mUser.getPlayingTime());
        // LatLng latLng =new LatLng(mUser.getLatitude(),mUser.getLongitude());
        mPlaceTextView.setText(mUser.getPlayingPlace());
        //  mAdapter=(ArrayAdapter)mSportsListSpinner.getAdapter();
        mSportsListSpinner.setSelection(mAdapter.getPosition(mUser.getSport()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUserRef.removeEventListener(mCurrentUserRefListener);
    }

    @Override
    public void onClick(View v) {
        DialogFragment newFragment;
        switch (v.getId()) {

            case R.id.time:
                newFragment = new TimePickerFragment();
                newFragment.show(getActivity().getSupportFragmentManager(), Constants.TIME_PICKER);
                break;

           /* case R.id.date:
                newFragment = new DatePickerFragment();
                newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
                break;*/

            case R.id.saveUserButton:
                // mPlayingTime = Utility.getPlayingTimeInfo(mTime.getText().toString());
                mPlayingTime = mTime.getText().toString();
                //    Log.v(LOG_TAG,"playing date is"+date);
                saveUserData();
                break;

            case R.id.picker_image_view:
                try {
                    PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
                    Intent intent = intentBuilder.build(getActivity());
                    // Start the Intent by requesting a result, identified by a request code.
                    startActivityForResult(intent, REQUEST_PLACE_PICKER);

                } catch (GooglePlayServicesRepairableException e) {
                    GooglePlayServicesUtil
                            .getErrorDialog(e.getConnectionStatusCode(), getActivity(), 0);
                } catch (GooglePlayServicesNotAvailableException e) {
               //     Toast.makeText(getActivity(), "Google Play Services is not available.",
                    Toast.makeText(getActivity(), getString(R.string.google_services_unavailable),
                            Toast.LENGTH_LONG)
                            .show();
                }
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
        String playingTime;
        if (hourOfDay > 12) {
       //     playingTime = time + " PM";
            playingTime = time + Constants.PM;

        } else {
        //    playingTime = time + " AM";
            playingTime = time + Constants.AM;
        }

        mTime.setText(playingTime);
        //  mUser.setPlayingTime(playingTime);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // BEGIN_INCLUDE(activity_result)
        if (requestCode == REQUEST_PLACE_PICKER) {
            // This result is from the PlacePicker dialog.


            if (resultCode == Activity.RESULT_OK) {
                /* User has picked a place, extract data.
                   Data is extracted from the returned intent by retrieving a Place object from
                   the PlacePicker.
                 */
                final Place place = PlacePicker.getPlace(data, getActivity());
                mParkLocation = place.getAddress().toString();
                Log.v(LOG_TAG, "location chosen" + mParkLocation);
                mPlaceTextView.setText(mParkLocation);
                LatLng latLng = place.getLatLng();
                Log.v(LOG_TAG, "lat lng" + latLng.latitude + "," + latLng.longitude);
                mUser.setPlayingPlace(mParkLocation);
                mUser.setLatitude(latLng.latitude);
                mUser.setLongitude(latLng.longitude);


            }
        }
    }
/*

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
*/

    @Override
    public String toString() {
        return super.toString();
    }


    private void saveUserData() {

        //   mUser.setPlayingTime(mPlayingTime);
        /*User user1 = new User("Ruchita", "ruchita.maheshwary@gmail.com", "tennis", 28.6948631, 77.11113064, false, mPlayingTime);
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
        mGeoFire.setLocation(Utility.encodeEmail(user3.getEmail()), new GeoLocation(user3.getLatitude(), user3.getLongitude()));*/
        alarmReceiver.cancelAlarm();
        DatabaseReference ref = mUrlRef.child("/" + Utility.encodeEmail(mUser.getEmail()));
        //  mUser.setPlayingTime(mTime.getText().toString());
        //ref.child("playingTime").setValue(mPlayingTime);
        ref.child(Constants.PLAY_TIME).setValue(mPlayingTime);
        //ref.child("latitude").setValue(mUser.getLatitude());
        ref.child(Constants.LATITUDE).setValue(mUser.getLatitude());
        //ref.child("longitude").setValue(mUser.getLongitude());
        ref.child(Constants.LONGITUDE).setValue(mUser.getLongitude());
        //ref.child("playingPlace").setValue(mUser.getPlayingPlace());
        ref.child(Constants.PLAYING_PLACE).setValue(mUser.getPlayingPlace());
       // ref.child("sport").setValue(mUser.getSport());
        ref.child(Constants.SPORT).setValue(mUser.getSport());
       // Toast.makeText(getContext(), "Profile has been updated successfully!", Toast.LENGTH_LONG).show();
        Toast.makeText(getContext(), getString(R.string.profile_updated), Toast.LENGTH_LONG).show();

        alarmReceiver.setAlarmTime(getActivity(),mUser.getPlayingTime(),mUser.getPlayingPlace());

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String sport = (String) parent.getItemAtPosition(position);
        Log.v(LOG_TAG, "selected item" + sport);
        mUser.setSport(sport);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

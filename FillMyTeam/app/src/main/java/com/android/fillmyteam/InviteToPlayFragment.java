package com.android.fillmyteam;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.fillmyteam.model.PlayerParcelable;
import com.android.fillmyteam.model.User;
import com.android.fillmyteam.util.Constants;
import com.android.fillmyteam.util.Utility;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by dgnc on 6/19/2016.
 */
public class InviteToPlayFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    public static final String LOG_TAG = InviteToPlayFragment.class.getSimpleName();
    public static final String CURRENT_USER = "current_user";

    public static final String TO_PLAY_WITH_USER = "play_with_user";
    public static final String CURRENT_PLAYER = "current_player";

    public static final String PLAYING_PLAYER = "play_with_player";
    User mUser;
    User mPlayWithUser;
    @BindView(R.id.invite_play)
    TextView invitePlayTextView;

    @BindView(R.id.invite_place_text_view)
    TextView mPlaceTextView;
    @BindView(R.id.invite_picker_image_view)
    ImageView mPlaceImageView;
    @BindView(R.id.invite_sports_list_spinner)
    Spinner mSportsListSpinner;
    @BindView(R.id.invite_button)
    Button mInviteButton;
    @BindView(R.id.cancel_button)
    Button mCancelButton;
    ArrayAdapter<CharSequence> mAdapter;
    static EditText mPlayTimeEditText;
    DatabaseReference matchRef;
    static TextView mDateTextView;

    public static InviteToPlayFragment newInstance(User currentUser, User playWithUser) {

        Bundle args = new Bundle();
        args.putSerializable(CURRENT_USER, currentUser);
        args.putSerializable(TO_PLAY_WITH_USER, playWithUser);
        InviteToPlayFragment fragment = new InviteToPlayFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPlayWithUser = (User) getArguments().get(TO_PLAY_WITH_USER);
            mUser = (User) getArguments().get(CURRENT_USER);
        }
        matchRef = FirebaseDatabase.getInstance().getReferenceFromUrl(Constants.PLAYERS_MATCHES);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invite_user, container, false);
        ButterKnife.bind(this, view);
        if (savedInstanceState != null) {
            if (savedInstanceState.getParcelable(CURRENT_PLAYER) != null) {
                mUser = ((PlayerParcelable) savedInstanceState.getParcelable(CURRENT_PLAYER)).getUser();
            }
            if (savedInstanceState.getParcelable(PLAYING_PLAYER) != null) {
                mPlayWithUser = ((PlayerParcelable) savedInstanceState.getParcelable(PLAYING_PLAYER)).getUser();
            }
        }
        invitePlayTextView.setText(getString(R.string.invite_play, mPlayWithUser.getName()));
        GregorianCalendar gcalendar = new GregorianCalendar();
        mPlayTimeEditText = (EditText) view.findViewById(R.id.invite_time);
    //    mPlayTimeEditText.setText(mUser.getPlayingTime());
        mPlayTimeEditText.setText(Utility.getCurrentTime(gcalendar));
        mDateTextView = (TextView) view.findViewById(R.id.invite_date);


        mDateTextView.setText(Utility.getCurrentDate(gcalendar));
        mPlaceTextView.setText(mUser.getPlayingPlace());
        mInviteButton.setOnClickListener(this);
        mPlaceImageView.setOnClickListener(this);
        mPlayTimeEditText.setOnClickListener(this);
        mDateTextView.setOnClickListener(this);
        mAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.sports_list, android.R.layout.simple_spinner_item);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSportsListSpinner.setAdapter(mAdapter);
        mSportsListSpinner.setSelection(mAdapter.getPosition(mUser.getSport()));
       mSportsListSpinner.setOnItemSelectedListener(this);
        return view;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String sport = (String) parent.getItemAtPosition(position);
        Log.v(LOG_TAG, "selected item" + sport);
        mUser.setSport(sport);
    }

    @Override
    public void onClick(View v) {
        DialogFragment newFragment;

        switch (v.getId()) {
            case R.id.invite_time:
                newFragment = new TimePickerFragment();
                newFragment.show(getActivity().getFragmentManager(), Constants.TIME_PICKER);
                break;
            case R.id.invite_picker_image_view:
                try {
                    PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
                    Intent intent = intentBuilder.build(getActivity());
                    // Start the Intent by requesting a result, identified by a request code.
                    startActivityForResult(intent, EditProfileFragment.REQUEST_PLACE_PICKER);

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
            case R.id.invite_button:
                String playTime = mPlayTimeEditText.getText().toString();
                String playingDate = mDateTextView.getText().toString();
                mUser.setPlayingTime(playTime);
                mUser.setPlayingDate(playingDate);
                DatabaseReference ref = matchRef.child("/" + Utility.encodeEmail(mUser.getEmail()));
                Map<String, Object> currentUserMap = new HashMap<>();
                Map<String, Object> playingUserMap = new HashMap<>();
                currentUserMap.put(Constants.PLAY_TIME,playTime);
                currentUserMap.put(Constants.PLAY_DATE, playingDate);
                currentUserMap.put(Constants.PLAYING_PLACE, mUser.getPlayingPlace());
                currentUserMap.put(Constants.LATITUDE, mUser.getLatitude());
                currentUserMap.put(Constants.LONGITUDE, mUser.getLongitude());
                currentUserMap.put(Constants.SPORT, mUser.getSport());
                currentUserMap.put(Constants.PLAYING_WITH, mPlayWithUser.getName());
                currentUserMap.put(Constants.PLAYER_EMAIL, mPlayWithUser.getEmail());

                ref.push().setValue(currentUserMap);
                ref = matchRef.child("/" + Utility.encodeEmail(mPlayWithUser.getEmail()));
                playingUserMap.put(Constants.PLAY_TIME,playTime);
                playingUserMap.put(Constants.PLAY_DATE, playingDate);
                playingUserMap.put(Constants.PLAYING_PLACE, mUser.getPlayingPlace());
                playingUserMap.put(Constants.LATITUDE, mUser.getLatitude());
                playingUserMap.put(Constants.LONGITUDE, mUser.getLongitude());
                playingUserMap.put(Constants.SPORT, mUser.getSport());
                playingUserMap.put(Constants.PLAYING_WITH, mUser.getName());
                playingUserMap.put(Constants.PLAYER_EMAIL, mUser.getEmail());
                ref.push().setValue(playingUserMap);


               GregorianCalendar beginTime = new GregorianCalendar();
               GregorianCalendar endTime = new GregorianCalendar();
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.ENGLISH);

                try {
                    beginTime.setTime(sdf.parse(playingDate + " " + playTime));
                    endTime.setTime(sdf.parse(playingDate + " " + playTime));
                    endTime.add(Calendar.HOUR,1);

                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(Intent.ACTION_INSERT)
                        .setData(CalendarContract.Events.CONTENT_URI)
                        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                        .putExtra(CalendarContract.Events.TITLE, getString(R.string.lets_play,mUser.getSport()))
                        .putExtra(CalendarContract.Events.DESCRIPTION, getString(R.string.play_invitation,mUser.getSport(),playingDate+" "+playTime))
                        .putExtra(CalendarContract.Events.EVENT_LOCATION,  mUser.getPlayingPlace())
                        .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                        .putExtra(Intent.EXTRA_EMAIL, mUser.getEmail()+","+mPlayWithUser.getEmail());
                startActivity(intent);

              /*  ref.child(Constants.PLAY_TIME).setValue(mUser.getPlayingTime());
                ref.child(Constants.PLAYING_PLACE).setValue(mUser.getPlayingPlace());
                ref.child(Constants.LATITUDE).setValue(mUser.getLatitude());
                ref.child(Constants.LONGITUDE).setValue(mUser.getLongitude());
                ref.child(Constants.SPORT).setValue(mUser.getSport());
                ref.child(Constants.PLAYING_WITH).setValue(mPlayWithUser.getName());


                ref.child(Constants.PLAY_TIME).setValue(mUser.getPlayingTime());
                ref.child(Constants.PLAYING_PLACE).setValue(mUser.getPlayingPlace());
                ref.child(Constants.LATITUDE).setValue(mUser.getLatitude());
                ref.child(Constants.LONGITUDE).setValue(mUser.getLongitude());
                ref.child(Constants.SPORT).setValue(mUser.getSport());
                ref.child(Constants.PLAYING_WITH).setValue(mUser.getName());*/
                Toast.makeText(getActivity(), getString(R.string.invited_to_play, mPlayWithUser.getName()), Toast.LENGTH_LONG).show();
                break;

            case R.id.invite_date:
                newFragment = new DatePickerFragment();
                newFragment.show(getActivity().getFragmentManager(), "datePicker");
                break;

        }
    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        //  matchRef.removeEventListener(this);
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
            String playingTime = Utility.updateTime(hourOfDay, minute);
            mPlayTimeEditText.setText(playingTime);
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
        mDateTextView.setText(day + " " + Utility.months[month] + " " + year);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(CURRENT_PLAYER, new PlayerParcelable(mUser));
        outState.putParcelable(PLAYING_PLAYER, new PlayerParcelable(mPlayWithUser));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // BEGIN_INCLUDE(activity_result)
        if (requestCode == EditProfileFragment.REQUEST_PLACE_PICKER) {
            // This result is from the PlacePicker dialog.


            if (resultCode == Activity.RESULT_OK) {
                /* User has picked a place, extract data.
                   Data is extracted from the returned intent by retrieving a Place object from
                   the PlacePicker.
                 */
                final Place place = PlacePicker.getPlace(data, getActivity());
                String location = place.getAddress().toString();
                Log.v(LOG_TAG, "location chosen" + location);
                mPlaceTextView.setText(location);
                LatLng latLng = place.getLatLng();
                Log.v(LOG_TAG, "lat lng" + latLng.latitude + "," + latLng.longitude);
                mUser.setPlayingPlace(location);
                mUser.setLatitude(latLng.latitude);
                mUser.setLongitude(latLng.longitude);


            }
        }
    }
}

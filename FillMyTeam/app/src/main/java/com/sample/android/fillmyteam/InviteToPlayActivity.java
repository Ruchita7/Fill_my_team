package com.sample.android.fillmyteam;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sample.android.fillmyteam.model.PlayerParcelable;
import com.sample.android.fillmyteam.model.User;
import com.sample.android.fillmyteam.util.Constants;
import com.sample.android.fillmyteam.util.Utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;



public class InviteToPlayActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    public static final String LOG_TAG = InviteToPlayActivity.class.getSimpleName();
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

    ArrayAdapter<CharSequence> mAdapter;
    static EditText mPlayTimeEditText;
    DatabaseReference matchRef;
    static TextView mDateTextView;
    static boolean isDateEarlier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_to_play);
        if(getIntent()!=null)   {
            if(getIntent().hasExtra(CURRENT_USER))  {
                mUser = (User) getIntent().getSerializableExtra(CURRENT_USER);
            }
            if(getIntent().hasExtra(TO_PLAY_WITH_USER))  {
                mPlayWithUser = (User) getIntent().getSerializableExtra(TO_PLAY_WITH_USER);
            }
        }
        matchRef = FirebaseDatabase.getInstance().getReferenceFromUrl(Constants.PLAYERS_MATCHES);
        ButterKnife.bind(this);
        if (savedInstanceState != null) {
            if (savedInstanceState.getParcelable(CURRENT_PLAYER) != null) {
                mUser = ((PlayerParcelable) savedInstanceState.getParcelable(CURRENT_PLAYER)).getUser();
            }
            if (savedInstanceState.getParcelable(PLAYING_PLAYER) != null) {
                mPlayWithUser = ((PlayerParcelable) savedInstanceState.getParcelable(PLAYING_PLAYER)).getUser();
            }
        }

     /*   final Toolbar toolbar = (Toolbar) findViewById(R.id.invite_toolbar_layout);
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.ic_action_ic_arrow_back);
            toolbar.setNavigationContentDescription(getString(R.string.back_button));
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getFragmentManager().popBackStackImmediate();
                }
            });
        }*/
        invitePlayTextView.setText(getString(R.string.invite_play, mPlayWithUser.getName()));
        GregorianCalendar gcalendar = new GregorianCalendar();
        mPlayTimeEditText = (EditText) findViewById(R.id.invite_time);

        mPlayTimeEditText.setText(Utility.getCurrentTime(gcalendar));
        mDateTextView = (TextView) findViewById(R.id.invite_date);

        isDateEarlier = false;
        mDateTextView.setText(Utility.getCurrentDate(gcalendar));
        String place="";
        if(mUser.getPlayingPlace()!=null) {
            String playingPlace = mUser.getPlayingPlace().replace(",,", ", <br/>");
             place = Html.fromHtml(playingPlace).toString();
        }
        mPlaceTextView.setText(place);
        mInviteButton.setOnClickListener(this);
        mPlaceImageView.setOnClickListener(this);
        mPlayTimeEditText.setOnClickListener(this);
        mDateTextView.setOnClickListener(this);
        mAdapter = ArrayAdapter.createFromResource(this, R.array.sports_list, android.R.layout.simple_spinner_item);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSportsListSpinner.setAdapter(mAdapter);
        mSportsListSpinner.setSelection(mAdapter.getPosition(mUser.getSport()));
        mSportsListSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String sport = (String) parent.getItemAtPosition(position);

        mUser.setSport(sport);
    }

    @Override
    public void onClick(View v) {
        DialogFragment newFragment;

        switch (v.getId()) {
            case R.id.invite_time:
                newFragment = new TimePickerFragment();
                newFragment.show(getSupportFragmentManager(), Constants.TIME_PICKER);
                break;
            case R.id.invite_picker_image_view:
                if (!Utility.checkNetworkState(this)) {
                    EditProfileFragment.NetworkMessageDialogFragment msgFragment = new EditProfileFragment.NetworkMessageDialogFragment();
                    msgFragment.show(getFragmentManager(), getString(R.string.no_network));
                    return;
                }
                try {
                    PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
                    Intent intent = intentBuilder.build(this);
                    // Start the Intent by requesting a result, identified by a request code.
                    startActivityForResult(intent, EditProfileFragment.REQUEST_PLACE_PICKER);

                } catch (GooglePlayServicesRepairableException e) {
                    GooglePlayServicesUtil
                            .getErrorDialog(e.getConnectionStatusCode(), this, 0);
                } catch (GooglePlayServicesNotAvailableException e) {
                    Toast.makeText(this, getString(R.string.google_services_unavailable),
                            Toast.LENGTH_LONG)
                            .show();
                }
                break;
            case R.id.invite_button:
                if (!Utility.checkNetworkState(this)) {
                    EditProfileFragment.NetworkMessageDialogFragment msgFragment = new EditProfileFragment.NetworkMessageDialogFragment();
                    msgFragment.show(getFragmentManager(), getString(R.string.no_network));
                    return;
                }
                String playTime = mPlayTimeEditText.getText().toString();
                String playingDate = mDateTextView.getText().toString();
                GregorianCalendar beginTime = new GregorianCalendar();
                GregorianCalendar endTime = new GregorianCalendar();
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.ENGLISH);
                GregorianCalendar currentDate = new GregorianCalendar();
                try {
                    beginTime.setTime(sdf.parse(playingDate + " " + playTime));
                    endTime.setTime(sdf.parse(playingDate + " " + playTime));


                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (beginTime.before(currentDate)) {
                    DialogFragment dialogFragment = new MessageDialogFragment();
                    dialogFragment.show(getSupportFragmentManager(), getString(R.string.invalid_date_chosen));
                } else {
                    endTime.add(Calendar.HOUR, 1);


                    mUser.setPlayingTime(playTime);
                    mUser.setPlayingDate(playingDate);
                    DatabaseReference ref = matchRef.child("/" + Utility.encodeEmail(mUser.getEmail()));
                    Map<String, Object> currentUserMap = new HashMap<>();
                    Map<String, Object> playingUserMap = new HashMap<>();
                    currentUserMap.put(Constants.PLAY_TIME, playTime);
                    currentUserMap.put(Constants.PLAY_DATE, playingDate);
                    currentUserMap.put(Constants.PLAYING_PLACE, mUser.getPlayingPlace());
                    currentUserMap.put(Constants.LATITUDE, mUser.getLatitude());
                    currentUserMap.put(Constants.LONGITUDE, mUser.getLongitude());
                    currentUserMap.put(Constants.SPORT, mUser.getSport());
                    currentUserMap.put(Constants.PLAYING_WITH, mPlayWithUser.getName());
                    currentUserMap.put(Constants.PLAYER_EMAIL, mPlayWithUser.getEmail());

                    ref.push().setValue(currentUserMap);
                    ref = matchRef.child("/" + Utility.encodeEmail(mPlayWithUser.getEmail()));
                    playingUserMap.put(Constants.PLAY_TIME, playTime);
                    playingUserMap.put(Constants.PLAY_DATE, playingDate);
                    playingUserMap.put(Constants.PLAYING_PLACE, mUser.getPlayingPlace());
                    playingUserMap.put(Constants.LATITUDE, mUser.getLatitude());
                    playingUserMap.put(Constants.LONGITUDE, mUser.getLongitude());
                    playingUserMap.put(Constants.SPORT, mUser.getSport());
                    playingUserMap.put(Constants.PLAYING_WITH, mUser.getName());
                    playingUserMap.put(Constants.PLAYER_EMAIL, mUser.getEmail());
                    ref.push().setValue(playingUserMap);

                    CheckBox checkBoxView = (CheckBox) findViewById(R.id.calendar_notify);
                    if (checkBoxView.isChecked()) {
                        Intent intent = new Intent(Intent.ACTION_INSERT)
                                .setData(CalendarContract.Events.CONTENT_URI)
                                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                                .putExtra(CalendarContract.Events.TITLE, getString(R.string.lets_play, mUser.getSport()))
                                .putExtra(CalendarContract.Events.DESCRIPTION, getString(R.string.play_invitation, mUser.getSport(), playingDate + " " + playTime))
                                .putExtra(CalendarContract.Events.EVENT_LOCATION, mUser.getPlayingPlace())
                                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                                .putExtra(Intent.EXTRA_EMAIL, mUser.getEmail() + "," + mPlayWithUser.getEmail());
                        startActivity(intent);
                    }
                    Toast.makeText(this, getString(R.string.invited_to_play, mPlayWithUser.getName()), Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.invite_date:
                newFragment = new DatePickerFragment();
                newFragment.show(getSupportFragmentManager(), "datePicker");
                break;

        }
    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
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
            //Log.v("Date Dialog", day + " " + month + "" + year);
            boolean isDateBefore = Utility.compareDate(day, year, month);
            if (isDateBefore) {
                isDateEarlier = true;
                Toast.makeText(getActivity(), getString(R.string.invalid_date_chosen), Toast.LENGTH_SHORT).show();
            }
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
                final Place place = PlacePicker.getPlace(data, this);
                String location = place.getAddress().toString();
                //Log.v(LOG_TAG, "location chosen" + location);
                mPlaceTextView.setText(location);
                LatLng latLng = place.getLatLng();
                mUser.setPlayingPlace(location);
                mUser.setLatitude(latLng.latitude);
                mUser.setLongitude(latLng.longitude);


            }
        }
    }

    public static class MessageDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.invalid_date_time_chosen);
            builder.setMessage(R.string.invalid_date_time_message)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            MessageDialogFragment.this.getDialog().cancel();
                        }
                    });

            // Create the AlertDialog object and return it
            return builder.create();
        }
    }
}

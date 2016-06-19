package com.android.fillmyteam;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import java.util.Calendar;

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
        mPlayTimeEditText=(EditText) view.findViewById(R.id.invite_time);
        mPlayTimeEditText.setText(mUser.getPlayingTime());
        mPlaceTextView.setText(mUser.getPlayingPlace());
        mInviteButton.setOnClickListener(this);
        mPlaceImageView.setOnClickListener(this);
        mPlayTimeEditText.setOnClickListener(this);
        mAdapter = ArrayAdapter.createFromResource(getContext(), R.array.sports_list, android.R.layout.simple_spinner_item);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSportsListSpinner.setAdapter(mAdapter);
        mSportsListSpinner.setSelection(mAdapter.getPosition(mUser.getSport()));
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
                newFragment.show(getActivity().getSupportFragmentManager(), Constants.TIME_PICKER);
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
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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

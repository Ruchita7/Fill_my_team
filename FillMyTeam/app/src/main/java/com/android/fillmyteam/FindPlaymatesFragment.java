package com.android.fillmyteam;


import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.fillmyteam.api.Callback;
import com.android.fillmyteam.model.PlayerParcelable;
import com.android.fillmyteam.model.User;
import com.android.fillmyteam.util.Constants;
import com.android.fillmyteam.util.Utility;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;*/

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FindPlaymatesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FindPlaymatesFragment extends Fragment implements GeoQueryEventListener, ChildEventListener, OnMapReadyCallback, OnMarkerClickListener,
        InfoWindowAdapter, OnInfoWindowClickListener, GoogleMap.OnInfoWindowLongClickListener, AdapterView.OnItemSelectedListener, View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mLocation;
    //   private String mParam2;
    static TextView mDate;
    static TextView mTime;
    double mLatitude;
    double mLongitude;

    Button submitButton;
    Button mFindButton;
    public static final String LOG_TAG = FindPlaymatesFragment.class.getSimpleName();
    Context mContext;
    String mPlayingTime;
    GeoFire mGeoFire;
    //  Firebase mUrlRef;
    DatabaseReference mUrlRef;
    PlayerParcelable mPlayerParcelable;
    ArrayList<PlayerParcelable> mPlayerParcelables;
    private GoogleMap mMap;
    MarkerOptions markerOptions;
    Map<LatLng, PlayerParcelable> userLatLngMap;
    private View mWindow;
    //    CoordinatorLayout coordinatorLayout;
    FrameLayout frameLayout;
    private View mContents;
    int playerFoundCount;
    LatLng latLngCenter;
    //   LatLng latLngCenter = new LatLng(28.7514586,77.0994467);
 /*   final CameraPosition cameraPosition = CameraPosition.builder()
            .target(latLngCenter)
            .zoom(15)
            //  .bearing(0)
            //  .tilt(45)
            .build();*/
    CameraPosition cameraPosition;
    Map<String, MarkerOptions> markerMap;
    boolean mToken;
    boolean mGooglePlayServicesAvbl;
    // Firebase mNotificationRef;
    //  DatabaseReference mNotificationRef;
    User mUser;
    /* Button mClearButton;
     Button mResetButton;*/
    private RadioGroup mOptions;
    String mSport;
    GeoQuery geoQuery;
    Button searchSportsButton;

    Spinner sportSpinner;

    public FindPlaymatesFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
/*    public static FindPlaymatesFragment newInstance(String param1, String param2) {
        FindPlaymatesFragment fragment = new FindPlaymatesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }*/


    public static FindPlaymatesFragment newInstance(User user) {
        FindPlaymatesFragment fragment = new FindPlaymatesFragment();
        Bundle args = new Bundle();
        /*args.putDouble(Constants.LATITUDE, latitude);
        args.putDouble(Constants.LONGITUDE, longitude);*/
        //args.putSerializable("user", user);
        args.putSerializable(Constants.USER_DETAILS, user);
        //   args.putBoolean("GOOGLE_PLAY_SERVICES",isGooglePlayServices);
        fragment.setArguments(args);

        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mContext = getActivity();
        Firebase.setAndroidContext(mContext);
        mGeoFire = new GeoFire(new Firebase(Constants.APP_PLAYERS_NEAR_URL));
        //  mUrlRef = new Firebase(Constants.APP_URL_USERS);
        mUrlRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(Constants.APP_URL_USERS);
        userLatLngMap = new HashMap<>();
        //mNotificationRef = new Firebase(Constants.PLAYERS_NOTIFICATIONS);
        // mNotificationRef = FirebaseDatabase.getInstance().getReferenceFromUrl(Constants.PLAYERS_NOTIFICATIONS);
        mPlayerParcelables = new ArrayList<>();
        markerMap = new HashMap<>();
        if (getArguments() != null) {
         /*   mLatitude = getArguments().getDouble(Constants.LATITUDE);
            mLongitude = getArguments().getDouble(Constants.LONGITUDE);*/
            mUser = (User) getArguments().getSerializable(Constants.USER_DETAILS);
            // mGooglePlayServicesAvbl = getArguments().getBoolean("GOOGLE_PLAY_SERVICES");
            //   mParam2 = getArguments().getString(ARG_PARAM2);
        }
        latLngCenter = new LatLng(mUser.getLatitude(), mUser.getLongitude());
        cameraPosition = CameraPosition.builder()
                .target(latLngCenter)
                .zoom(15)
                //  .bearing(0)
                //  .tilt(45)
                .build();
        /*if (mGooglePlayServicesAvbl) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

            mToken = sharedPreferences.getBoolean(MainActivity.SENT_TOKEN_TO_SERVER, false);
        }
*/

        //    GeoFire geoFire = new GeoFire(new Firebase(Constants.APP_PLAYERS_NEAR_URL));

    }
/*
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_find_playmates, container, false);
        //  GeoQuery geoQuery = mGeoFire.queryAtLocation(new GeoLocation(28.6988839, 77.1082443), 1);

      /*  coordinatorLayout = (CoordinatorLayout) view.findViewById(R.id
                .coordinatorLayout);*/
     /*   mOptions = (RadioGroup) view.findViewById(R.id.sports_optioms);
        mOptions.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                handleMarkers(checkedId);

            }
        });*/

        frameLayout = (FrameLayout) view.findViewById(R.id.map);
        FragmentManager fragmentManager = getChildFragmentManager();
        MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.map);

        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fragmentManager.beginTransaction().replace(R.id.map, mapFragment).commit();
            mapFragment.getMapAsync(this);
        }


        mWindow = getActivity().getLayoutInflater().inflate(R.layout.custom_info_window, null);
        mContents = getActivity().getLayoutInflater().inflate(R.layout.custom_info_contents, null);
    /*    SupportMapFragment mapFragment =
                (SupportMapFragment) getFragmentManager().findFragmentById(R.id.map);
        if(mapFragment!=null)
        {
            mapFragment.getMapAsync(this);
        }*/
   /*     mClearButton = (Button) view.findViewById(R.id.clearButton);
        mResetButton = (Button) view.findViewById(R.id.resetButton);
        mClearButton.setOnClickListener(this);
        mResetButton.setOnClickListener(this);*/
      /*  searchSportsButton = (ImageView) view.findViewById(R.id.search_sports_button);
        registerForContextMenu(searchSportsButton);*/
        searchSportsButton = (Button) view.findViewById(R.id.search_sports_button);
        searchSportsButton.setOnClickListener(this);
        sportSpinner = (Spinner) view.findViewById(R.id.sports_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.all_sports, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        sportSpinner.setAdapter(adapter);
        sportSpinner.setOnItemSelectedListener(this);
        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        geoQuery.removeAllListeners();
        mUrlRef.removeEventListener(this);
    }

    @Override
    public void onClick(View v) {
        sportSpinner.performClick();
        //  searchSportsButton.setVisibility(View.INVISIBLE);
    }

    /*@Override
        public void onResume() {
            super.onResume();



            for (MarkerOptions markerOptions : markerMap.values()) {
                mMap.addMarker(markerOptions);
            }



        }*/
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String sport = (String) parent.getItemAtPosition(position);
        Log.v(LOG_TAG, "selected item" + sport);
        //   mUser.setSport(sport);
        switch (sport) {
            //   case "All":
            case Constants.ALL:
                mSport = "";
                fireGeoQuery();
                break;
            //case "Basketball":
            case Constants.BASKETBALL:
                Log.v(LOG_TAG, "basketball clicked");
                mSport = Constants.BASKETBALL;
                fireGeoQuery();

                break;
            //  case "Tennis":
            case Constants.TENNIS:
                Log.v(LOG_TAG, "tennis clicked");
                //  mSport = "tennis";
                mSport = Constants.TENNIS;
                fireGeoQuery();

                break;
            //    case "Football":
            case Constants.FOOTBALL:
                Log.v(LOG_TAG, "football clicked");
                //mSport = "football";
                mSport = Constants.FOOTBALL;
                fireGeoQuery();
                break;

            //case "Cricket":
            case Constants.CRICKET:
                Log.v(LOG_TAG, "cricket clicked");
                //   mSport = "cricket";
                mSport = Constants.CRICKET;
                fireGeoQuery();
                break;

            //   case "Badminton":
            case Constants.BADMINTON:
                Log.v(LOG_TAG, "badminton clicked");
                // mSport = "badminton";
                mSport = Constants.BADMINTON;
                fireGeoQuery();
                break;


            // case "Baseball":
            case Constants.BASEBALL:
                Log.v(LOG_TAG, "baseball clicked");
                //mSport = "baseball";
                mSport = Constants.BASEBALL;
                fireGeoQuery();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

/*    @Override
    public boolean onContextItemSelected(MenuItem item) {
       AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.all:
                mSport = "";
                fireGeoQuery();
                break;
            case R.id.basketball:
                Log.v(LOG_TAG, "basketball clicked");
                mSport = "basketball";
                fireGeoQuery();

                break;
            case R.id.tennis:
                Log.v(LOG_TAG, "tennis clicked");
                mSport = "tennis";
                fireGeoQuery();

                break;
            case R.id.football :
                Log.v(LOG_TAG, "football clicked");
                mSport = "football";
                fireGeoQuery();
                break;

            case R.id.cricket :
                Log.v(LOG_TAG, "cricket clicked");
                mSport = "cricket";
                fireGeoQuery();
                break;

            case R.id.badminton :
                Log.v(LOG_TAG, "badminton clicked");
                mSport = "badminton";
                fireGeoQuery();
                break;


            case R.id.baseball :
                Log.v(LOG_TAG, "baseball clicked");
                mSport = "baseball";
                fireGeoQuery();
                break;
        }

        return super.onContextItemSelected(item);
    }*/

   /* public void handleMarkers(int radioButtonId) {
        switch (radioButtonId) {
            case R.id.all:
                mSport = "";

               *//* clearMap();
                //   mSport = "basketball";
                geoQuery = mGeoFire.queryAtLocation(new GeoLocation(latLngCenter.latitude, latLngCenter.longitude), 1);
                geoQuery.addGeoQueryEventListener(this);*//*
                fireGeoQuery();
                break;
            case R.id.basketball:
                Log.v(LOG_TAG, "basketball clicked");
                mSport = "basketball";
                fireGeoQuery();
             *//*   geoQuery.removeAllListeners();
                clearMap();

                geoQuery = mGeoFire.queryAtLocation(new GeoLocation(latLngCenter.latitude, latLngCenter.longitude), 1);
                geoQuery.addGeoQueryEventListener(this);*//*
                break;
            case R.id.tennis:
                Log.v(LOG_TAG, "tennis clicked");
                mSport = "tennis";
                fireGeoQuery();
               *//* geoQuery.removeAllListeners();
                clearMap();

                geoQuery = mGeoFire.queryAtLocation(new GeoLocation(latLngCenter.latitude, latLngCenter.longitude), 1);
                geoQuery.addGeoQueryEventListener(this);*//*
                break;
            case R.id.football :
                Log.v(LOG_TAG, "football clicked");
                mSport = "football";
                fireGeoQuery();
                break;

            case R.id.cricket :
                Log.v(LOG_TAG, "cricket clicked");
                mSport = "cricket";
                fireGeoQuery();
                break;

            case R.id.badminton :
                Log.v(LOG_TAG, "badminton clicked");
                mSport = "badminton";
                fireGeoQuery();
                break;


            case R.id.baseball :
                Log.v(LOG_TAG, "baseball clicked");
                mSport = "baseball";
                fireGeoQuery();
                break;
        }
    }*/

    private void fireGeoQuery() {
        geoQuery.removeAllListeners();
        //   sportSpinner.setVisibility(View.INVISIBLE);
        // searchSportsButton.setVisibility(View.VISIBLE);
        clearMap();
        playerFoundCount = 0;
        //  mSport = "basketball";
        geoQuery = mGeoFire.queryAtLocation(new GeoLocation(latLngCenter.latitude, latLngCenter.longitude), 1);
        geoQuery.addGeoQueryEventListener(this);
    }

    private void initializeMap() {
        mMap.setOnMarkerClickListener(this);
        mMap.setInfoWindowAdapter(this);
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnInfoWindowLongClickListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        geoQuery = mGeoFire.queryAtLocation(new GeoLocation(latLngCenter.latitude, latLngCenter.longitude), 1);
        geoQuery.addGeoQueryEventListener(this);
        mMap.setOnMarkerClickListener(this);
       /* User user;
        for (PlayerParcelable playerParcelable : mPlayerParcelables) {
            user=playerParcelable.getUser();
            markerOptions = new MarkerOptions().position(new LatLng(user.getLatitude(),user.getLongitude())).title(user.getName());
            markerMap.put(user.getEmail(), markerOptions);
        }*/
     /*   for (MarkerOptions markerOptions : markerMap.values()) {
            mMap.addMarker(markerOptions);
        }
*/
        mMap.setInfoWindowAdapter(this);
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnInfoWindowLongClickListener(this);
    }

    private boolean checkReady() {
        if (mMap == null) {
            Toast.makeText(getActivity(), R.string.map_not_ready, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Called when the Clear button is clicked.
     */
    public void clearMap() {
        if (!checkReady()) {
            return;
        }
        mMap.clear();
    }
/*
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.resetButton:
                resetMap();
                break;
            case R.id.clearButton:
                clearMap();
                break;
        }
    }*/

    /**
     * Called when the Reset button is clicked.
     */
    public void resetMap() {
        if (!checkReady()) {
            return;
        }
        // Clear the map because we don't want duplicates of the markers.
        mMap.clear();
        //   addMarkersToMap();
        geoQuery = mGeoFire.queryAtLocation(new GeoLocation(latLngCenter.latitude, latLngCenter.longitude), 1);
        geoQuery.addGeoQueryEventListener(this);
        mMap.setOnMarkerClickListener(this);
       /* User user;
        for (PlayerParcelable playerParcelable : mPlayerParcelables) {
            user=playerParcelable.getUser();
            markerOptions = new MarkerOptions().position(new LatLng(user.getLatitude(),user.getLongitude())).title(user.getName());
            markerMap.put(user.getEmail(), markerOptions);
        }*/
     /*   for (MarkerOptions markerOptions : markerMap.values()) {
            mMap.addMarker(markerOptions);
        }
*/
        mMap.setInfoWindowAdapter(this);
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnInfoWindowLongClickListener(this);
    }



   /* @Override
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

            case R.id.findButton :
                findUsers();
                break;
        }
    }
*/
   /* private void findUsers()    {
       *//* GeoQuery geoQuery = mGeoFire.queryAtLocation(new GeoLocation(28.6988839,77.1082443), 1);
        geoQuery.addGeoQueryEventListener(this);*//*
      Log.v(LOG_TAG, "List size"+mPlayerParcelables.size());
        Intent intent = new Intent(getActivity(),PlayersLocationActivity.class);
        intent.putParcelableArrayListExtra("mark_locations",mPlayerParcelables);

     //   intent.put
        startActivity(intent);

    }*/


    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        String emailId = Utility.decodeEmail(key);
        if (!Utility.decodeEmail(mUser.getEmail()).equals(emailId)) {
            playerFoundCount++;
            //  Log.d(LOG_TAG, String.format("Key %s entered the search area at [%f,%f]", emailId, location.latitude, location.longitude));
            Log.d(LOG_TAG, getString(R.string.user_found_latlng, emailId, location.latitude, location.longitude));
            // Log.v(LOG_TAG,"onKeyEntered :" +mUrlRef.child(key).getKey());
            //   mPlayerParcelable = new PlayerParcelable(Utility.decodeEmail(key),"",location.latitude,location.longitude);
            //   mPlayerParcelables.add(mPlayerParcelable);

    /*    Firebase urlRef = new Firebase(Constants.APP_URL_USERS);
        urlRef.child("key")*/
            ;
            findUserByEmailId(emailId);
        }
    }

    @Override
    public void onKeyExited(String key) {
        playerFoundCount--;
        // Log.d(LOG_TAG, String.format("Key %s is no longer in the search area", key));
        Log.d(LOG_TAG, getString(R.string.no_key_found, key));
    }

    @Override
    public void onGeoQueryReady() {
        Log.d(LOG_TAG, "All initial data has been loaded and events have been fired!");
        if (playerFoundCount <= 0) {
            //     Toast.makeText(getContext(), "No players found nearby", Toast.LENGTH_SHORT).show();
            Toast.makeText(getActivity(), getString(R.string.no_player_found), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onGeoQueryError(FirebaseError error) {
        //  Log.e(LOG_TAG, "There was an error with this query: " + error);
        Log.e(LOG_TAG, getString(R.string.geo_query_error) + error);
    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {
        //  Log.d(LOG_TAG, String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
        Log.d(LOG_TAG, getString(R.string.user_moved, key, location.latitude, location.longitude));
    }


    private void findUserByEmailId(String emailId) {
        Query queryRef = null;

        //  queryRef = mUrlRef.orderByChild("email").equalTo(emailId).limitToFirst(1);
        queryRef = mUrlRef.orderByChild(Constants.EMAIL).equalTo(emailId).limitToFirst(1);

        queryRef.addChildEventListener(this);

    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        User userObj = dataSnapshot.getValue(User.class);
        if (mSport != null && !mSport.isEmpty()) {
            if (!mSport.equalsIgnoreCase(userObj.getSport())) {
                playerFoundCount--;
                if (playerFoundCount <= 0) {
                    //  Toast.makeText(getContext(), "No players found nearby", Toast.LENGTH_SHORT).show();
                    Toast.makeText(getActivity(), getString(R.string.no_player_found), Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
        PlayerParcelable playerParcelable = new PlayerParcelable(userObj);
        mPlayerParcelables.add(playerParcelable);
        String time = userObj.getPlayingTime();
        markerOptions = new MarkerOptions()
                .position(new LatLng(userObj.getLatitude(), userObj.getLongitude()))
                .title(userObj.getName())
                .snippet(time);
        markerMap.put(userObj.getEmail(), markerOptions);
        mMap.addMarker(markerOptions);
        userLatLngMap.put(new LatLng(userObj.getLatitude(), userObj.getLongitude()), playerParcelable);
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

  /*  @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        //  HashMap<String, Object> values = (HashMap<String, Object>) dataSnapshot.getValue();
        //   User userObj = Utility.retrieveUserObject(values);
        User userObj = dataSnapshot.getValue(User.class);
        if (mSport != null && !mSport.isEmpty()) {
            if (!mSport.equalsIgnoreCase(userObj.getSport())) {
                playerFoundCount--;
                if (playerFoundCount <= 0) {
                    Toast.makeText(getContext(), "No players found nearby", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
        PlayerParcelable playerParcelable = new PlayerParcelable(userObj);
        mPlayerParcelables.add(playerParcelable);
        String time = userObj.getPlayingTime();
        markerOptions = new MarkerOptions()
                .position(new LatLng(userObj.getLatitude(), userObj.getLongitude()))
                .title(userObj.getName())
                .snippet(time);
        markerMap.put(userObj.getEmail(), markerOptions);
        mMap.addMarker(markerOptions);
        userLatLngMap.put(new LatLng(userObj.getLatitude(), userObj.getLongitude()), playerParcelable);
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }


    public void onCancelled(FirebaseError firebaseError) {

    }
*/




     /*   CustomInfoWindowAdapter() {
            mWindow = getActivity().getLayoutInflater().inflate(R.layout.custom_info_window, null);
            mContents = getActivity().getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        }*/

    @Override
    public View getInfoWindow(Marker marker) {
        render(marker, mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
          /*  render(marker, mContents);
            return mContents;*/
        return null;
    }

    private void render(Marker marker, View view) {
        int badge = R.drawable.cricket;
        // Use the equals() method on a Marker to check for equals.  Do not use ==.

        ((ImageView) view.findViewById(R.id.badge)).setImageResource(badge);

        String title = marker.getTitle();
        TextView titleUi = ((TextView) view.findViewById(R.id.title));
        if (title != null) {
            // Spannable string allows us to edit the formatting of the text.
            SpannableString titleText = new SpannableString(title);
            titleText.setSpan(new ForegroundColorSpan(Color.RED), 0, titleText.length(), 0);
            titleUi.setText(titleText);
        } else {
            titleUi.setText("");
        }

        String snippet = marker.getSnippet();
        TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));
        SpannableString snippetText = new SpannableString(snippet);
        snippetText.setSpan(new ForegroundColorSpan(Color.MAGENTA), 0, snippet.length(), 0);
        snippetUi.setText(snippetText);
          /*  if (snippet != null && snippet.length() > 12) {
                SpannableString snippetText = new SpannableString(snippet);
                snippetText.setSpan(new ForegroundColorSpan(Color.MAGENTA), 0, 10, 0);
                snippetText.setSpan(new ForegroundColorSpan(Color.BLUE), 12, snippet.length(), 0);
                snippetUi.setText(snippetText);
            } else {
                snippetUi.setText("");
            }*/
    }


    @Override
    public boolean onMarkerClick(final Marker marker) {
        /*final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 1500;

        final Interpolator interpolator = new BounceInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = Math.max(
                        1 - interpolator.getInterpolation((float) elapsed / duration), 0);
                marker.setAnchor(0.5f, 1.0f + 2 * t);

                if (t > 0.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
        return false;
    }*/
        marker.showInfoWindow();
        return false;
    }

    @Override
    public void onInfoWindowClick(final Marker marker) {
        Log.e(LOG_TAG, "onInfoWindowClick");
        Snackbar snackbar = Snackbar
                //.make(frameLayout, "Invite " + marker.getTitle() + " to play", Snackbar.LENGTH_LONG)
                .make(frameLayout, getString(R.string.invite_user,marker.getTitle()), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.v(LOG_TAG, "onClick of onInfoWindowClick");
                        LatLng markerLatLng = marker.getPosition();
                        PlayerParcelable playerParcelable = userLatLngMap.get(markerLatLng);
                        User user = playerParcelable.getUser();
                       /* List<String> msg = new ArrayList<String>();
                        msg.add(user.getEmail() + " wants to play with you");*/
                        Log.v(LOG_TAG, "User's mail::" + user.getEmail());
                        ((Callback) getActivity()).onInviteClick(mUser,user);
                        //   mNotificationRef.child(Utility.encodeEmail(user.getEmail())).child("msg").setValue(msg);
                      /*  Intent intent = new Intent(getActivity(), NotificationService.class);
                        intent.putExtra(Constants.NOTIFY_USER, playerParcelable);
                        getActivity().startService(intent);*/
                    }
                });
        snackbar.show();
        //Toast.makeText(getContext(),"onInfoWindowClick",Toast.LENGTH_LONG).show();;
    }

    @Override
    public void onInfoWindowLongClick(final Marker marker) {
        Log.e(LOG_TAG, "onInfoWindowClick");
        Snackbar snackbar = Snackbar
                //  .make(frameLayout, "Invite " + marker.getTitle() + " to play", Snackbar.LENGTH_LONG)
                .make(frameLayout,  getString(R.string.invite_user,marker.getTitle()), Snackbar.LENGTH_LONG)
                //  .setAction("SEND NOTIFICATION", new View.OnClickListener() {
                .setAction(getString(R.string.ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LatLng markerLatLng = marker.getPosition();
                        PlayerParcelable playerParcelable = userLatLngMap.get(markerLatLng);
                        User user = playerParcelable.getUser();
                        Log.v(LOG_TAG, "User's mail::" + user.getEmail());
                        //   Log.e(LOG_TAG, "onClick of onInfoWindowClick");
                        ((Callback) getActivity()).onInviteClick(mUser,user);
                    }
                });
        snackbar.show();
    }
}

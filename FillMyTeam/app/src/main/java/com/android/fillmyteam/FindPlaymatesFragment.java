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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.ArcMenu;
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

import butterknife.BindView;
import butterknife.ButterKnife;


public class FindPlaymatesFragment extends Fragment implements GeoQueryEventListener, ChildEventListener, OnMapReadyCallback, OnMarkerClickListener,
        InfoWindowAdapter, OnInfoWindowClickListener, GoogleMap.OnInfoWindowLongClickListener{

    private static final int[] ITEM_DRAWABLES = { R.drawable.ic_basketball, R.drawable.ic_tennis,
            R.drawable.ic_football, R.drawable.ic_cricket, R.drawable.ic_badminton, R.drawable.ic_baseball };

    private static final String[] SPORTS = { Constants.BASKETBALL,Constants.TENNIS,
            Constants.FOOTBALL, Constants.CRICKET, Constants.BADMINTON, Constants.BASEBALL };
    private String mLocation;

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
    @BindView(R.id.map)
    FrameLayout frameLayout;
    private View mContents;
    int playerFoundCount;
    LatLng latLngCenter;

    boolean isImgClicked;
    boolean isPanelClicked;
    public static final String IMAGE_CLICK = "image_click";
    public static final String PANEL_CLICK = "panel_click";
    public static final String PLAYERS_NEARBY = "players_nearby";
    public static final String SELECTED_SPORT = "selected_sport";

    CameraPosition cameraPosition;
    Map<String, MarkerOptions> markerMap;
    boolean mToken;
    boolean mGooglePlayServicesAvbl;
    User mUser;

    private RadioGroup mOptions;
    String mSport;
    GeoQuery geoQuery;
    /* @BindView(R.id.search_sports_image)
     ImageView searchSportsImageView;*/
    MapFragment mapFragment;
    Spinner sportSpinner;
    /*@BindView(R.id.sports_panel)
    FrameLayout panelLayout;
    @BindView(R.id.badminton_image)
    ImageView badmintonImage;
    @BindView(R.id.football_image)
    ImageView footballImage;
    @BindView(R.id.tennis_image)
    ImageView tennisImage;
    @BindView(R.id.cricket_image)
    ImageView cricketImage;
    @BindView(R.id.hockey_image)
    ImageView hockeyImage;
    @BindView(R.id.table_tennis_image)
    ImageView tableTennisImage;
    @BindView(R.id.volleyball_image)
    ImageView volleyballImage;
    @BindView(R.id.rugby_image)
    ImageView rugbyImage;
    @BindView(R.id.baseball_image)
    ImageView baseballImage;
    @BindView(R.id.basketball_image)
    ImageView basketballImage;
*/

ImageView basketBallImageView;
    ImageView tennisImageView;
    ImageView footballImageView;
    ImageView cricketImageView;
    ImageView badmintonImageView;
    ImageView baseballImageView;


    public FindPlaymatesFragment() {
        // Required empty public constructor
    }


    public static FindPlaymatesFragment newInstance(User user) {
        FindPlaymatesFragment fragment = new FindPlaymatesFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.USER_DETAILS, user);
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
        mPlayerParcelables = new ArrayList<>();
        markerMap = new HashMap<>();
        if (getArguments() != null) {

            mUser = (User) getArguments().getSerializable(Constants.USER_DETAILS);

        }
        latLngCenter = new LatLng(mUser.getLatitude(), mUser.getLongitude());
        cameraPosition = CameraPosition.builder()
                .target(latLngCenter)
                .zoom(15)

                .build();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_find_playmates, container, false);

        FragmentManager fragmentManager = getChildFragmentManager();
        MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.map);

        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fragmentManager.beginTransaction().replace(R.id.map, mapFragment).commit();

        }
        mapFragment.getMapAsync(this);

        mWindow = getActivity().getLayoutInflater().inflate(R.layout.custom_info_window, null);
        mContents = getActivity().getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        ButterKnife.bind(this, view);
        ArcMenu arcMenu = (ArcMenu) view.findViewById(R.id.arc_menu);
        initArcMenu(arcMenu,ITEM_DRAWABLES);
       /* rightCenterButton = (FloatingActionButton) view.findViewWithTag("FAB");
        createFloatingMenu();*/
 /*       panelLayout.setOnClickListener(this);
        searchSportsImageView.setOnClickListener(this);
        tennisImage.setOnClickListener(this);
        footballImage.setOnClickListener(this);
        baseballImage.setOnClickListener(this);
        basketballImage.setOnClickListener(this);
        cricketImage.setOnClickListener(this);
        hockeyImage.setOnClickListener(this);
        rugbyImage.setOnClickListener(this);
        volleyballImage.setOnClickListener(this);
        tableTennisImage.setOnClickListener(this);
        badmintonImage.setOnClickListener(this);
*/

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(PANEL_CLICK) && (savedInstanceState.containsKey(IMAGE_CLICK))) {
                isPanelClicked = savedInstanceState.getBoolean(PANEL_CLICK);
                isImgClicked = savedInstanceState.getBoolean(IMAGE_CLICK);
              /*  searchSportsImageView.setVisibility(!isImgClicked ? View.VISIBLE : View.INVISIBLE);
                panelLayout.setVisibility(!isPanelClicked ? View.VISIBLE : View.INVISIBLE);*/
            }
            if (savedInstanceState.containsKey(SELECTED_SPORT)) {
                mSport = savedInstanceState.getString(SELECTED_SPORT);
                if (savedInstanceState.containsKey(PLAYERS_NEARBY)) {
                    mPlayerParcelables = savedInstanceState.getParcelableArrayList(PLAYERS_NEARBY);
                    User user = null;
                    for (PlayerParcelable playerParcelable : mPlayerParcelables) {
                        user = playerParcelable.getUser();
                       /* markerOptions = new MarkerOptions()
                                .position(new LatLng(user.getLatitude(), user.getLongitude()))
                                .title(user.getName())
                                .snippet(user.getPlayingTime());
                        mMap.addMarker(markerOptions);*/
                        userLatLngMap.put(new LatLng(user.getLatitude(), user.getLongitude()), playerParcelable);
                    }
                }
            }
        }
        return view;
    }

/*
    @Override
    public void onDetach() {
        super.onDetach();
        rightCenterButton.detach();
    }*/


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        geoQuery.removeAllListeners();
        mUrlRef.removeEventListener(this);
//        rightCenterButton.detach();
    }

  /* @Override
    public void onClick(View v) {
      String tag= (String)v.getTag();
        switch (tag)    {
            case Constants.BASKETBALL:
                Log.v(LOG_TAG, "basketball clicked");
                mSport = Constants.BASKETBALL;
                fireGeoQuery();
           //     searchFloatingMenu.close(true);
                break;
            case Constants.TENNIS:
                Log.v(LOG_TAG, "table_tennis_image clicked");
                mSport = Constants.TENNIS;
                fireGeoQuery();
             //   searchFloatingMenu.close(true);
                break;
            case Constants.FOOTBALL:
                Log.v(LOG_TAG, "football clicked");
                mSport = Constants.FOOTBALL;
                fireGeoQuery();
                break;
            case Constants.CRICKET:
                Log.v(LOG_TAG, "cricket clicked");
                mSport = Constants.CRICKET;
                fireGeoQuery();
            //    searchFloatingMenu.close(true);
                break;
            case Constants.BADMINTON:
                Log.v(LOG_TAG, "badminton clicked");
                mSport = Constants.BADMINTON;
                fireGeoQuery();
              //  searchFloatingMenu.close(true);
                break;
            case Constants.BASEBALL:
                Log.v(LOG_TAG, "baseball clicked");
                mSport = Constants.BASEBALL;
                fireGeoQuery();
            //    searchFloatingMenu.close(true);
                break;
        }
    }
*/
    private void initArcMenu(ArcMenu menu, int[] itemDrawables) {
        final int itemCount = itemDrawables.length;
        for (int i = 0; i < itemCount; i++) {
            ImageView item = new ImageView(getActivity());
            item.setImageResource(itemDrawables[i]);
            item.setTag(SPORTS[i]);
            final int position = i;
            menu.addItem(item, new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    String tag = (String)v.getTag();
                    mSport = tag;
                    fireGeoQuery();
               //     Toast.makeText(getActivity(), "position:" + position, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /*
    @OnClick({R.id.sports_panel, R.id.search_sports_image, R.id.basketball_image, R.id.football_image, R.id.table_tennis_image, R.id.tennis_image,
            R.id.hockey_image, R.id.cricket_image, R.id.rugby_image, R.id.volleyball_image, R.id.badminton_image, R.id.baseball_image})
    @Override
    public void onClick(View v) {

        Slide slide;
        switch (v.getId()) {

            case R.id.sports_panel:

                slide = new Slide(Gravity.RIGHT);

                searchSportsImageView.animate().translationX(10);
                slide.addTarget(R.id.search_sports_image);
                slide.setInterpolator(AnimationUtils.loadInterpolator(mContext, android.R.interpolator
                        .linear_out_slow_in));
                slide.setDuration(300);

                panelLayout.setVisibility(View.INVISIBLE);
                searchSportsImageView.setVisibility(View.VISIBLE);
                isImgClicked = false;
                isPanelClicked = true;
                break;
            case R.id.search_sports_image:
                slide = new Slide(Gravity.RIGHT);

                panelLayout.animate().translationX(10);
                slide.addTarget(R.id.sports_panel);
                slide.setInterpolator(AnimationUtils.loadInterpolator(mContext, android.R.interpolator
                        .linear_out_slow_in));
                slide.setDuration(300);
                panelLayout.setVisibility(View.VISIBLE);
                searchSportsImageView.setVisibility(View.INVISIBLE);
                isImgClicked = true;
                isPanelClicked = false;
                break;
            case R.id.basketball_image:
                Log.v(LOG_TAG, "basketball clicked");
                mSport = Constants.BASKETBALL;
                fireGeoQuery();
                break;
            case R.id.football_image:
                Log.v(LOG_TAG, "football clicked");
                mSport = Constants.FOOTBALL;
                fireGeoQuery();
                break;
            case R.id.table_tennis_image:
                Log.v(LOG_TAG, "table_tennis_image clicked");
                mSport = Constants.TABLE_TENNIS;
                fireGeoQuery();
                break;
            case R.id.tennis_image:
                Log.v(LOG_TAG, "tennis clicked");
                mSport = Constants.TENNIS;
                fireGeoQuery();
                break;
            case R.id.hockey_image:
                Log.v(LOG_TAG, "rugby_image hockey_image");
                mSport = Constants.HOCKEY;
                fireGeoQuery();
                break;
            case R.id.cricket_image:
                Log.v(LOG_TAG, "cricket clicked");
                mSport = Constants.CRICKET;
                fireGeoQuery();
                break;
            case R.id.rugby_image:
                Log.v(LOG_TAG, "rugby_image clicked");
                mSport = Constants.RUGBY;
                fireGeoQuery();
                break;
            case R.id.volleyball_image:
                Log.v(LOG_TAG, "volleyball clicked");
                mSport = Constants.VOLLEY_BALL;
                fireGeoQuery();
                break;
            case R.id.badminton_image:
                Log.v(LOG_TAG, "badminton clicked");
                mSport = Constants.BADMINTON;
                fireGeoQuery();
                break;
            case R.id.baseball_image:
                Log.v(LOG_TAG, "baseball clicked");
                mSport = Constants.BASEBALL;
                fireGeoQuery();
                break;
        }

    }
*/

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IMAGE_CLICK, isImgClicked);
        outState.putBoolean(PANEL_CLICK, isPanelClicked);
        outState.putParcelableArrayList(PLAYERS_NEARBY, mPlayerParcelables);
        outState.putString(SELECTED_SPORT, mSport);
    }




    private void fireGeoQuery() {
        if (geoQuery != null) {
            geoQuery.removeAllListeners();
        }

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

        mMap.setInfoWindowAdapter(this);
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnInfoWindowLongClickListener(this);
    }


    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        String emailId = Utility.decodeEmail(key);
        if (!Utility.decodeEmail(mUser.getEmail()).equals(emailId)) {
            playerFoundCount++;

            Log.d(LOG_TAG, getString(R.string.user_found_latlng, emailId, location.latitude, location.longitude));

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
            Toast.makeText(getActivity(), getString(R.string.no_player_found, mSport), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getActivity(), getString(R.string.no_player_found, mSport), Toast.LENGTH_SHORT).show();
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


    @Override
    public View getInfoWindow(Marker marker) {
        render(marker, mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
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
    }


    @Override
    public boolean onMarkerClick(final Marker marker) {

        marker.showInfoWindow();
        return false;
    }

    @Override
    public void onInfoWindowClick(final Marker marker) {
        Log.e(LOG_TAG, "onInfoWindowClick");
        Snackbar snackbar = Snackbar
                //.make(frameLayout, "Invite " + marker.getTitle() + " to play", Snackbar.LENGTH_LONG)
                .make(frameLayout, getString(R.string.invite_user, marker.getTitle()), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.v(LOG_TAG, "onClick of onInfoWindowClick");
                        LatLng markerLatLng = marker.getPosition();
                        PlayerParcelable playerParcelable = userLatLngMap.get(markerLatLng);
                        User user = playerParcelable.getUser();

                        Log.v(LOG_TAG, "User's mail::" + user.getEmail());
                        ((Callback) getActivity()).onInviteClick(mUser, user);

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
                .make(frameLayout, getString(R.string.invite_user, marker.getTitle()), Snackbar.LENGTH_LONG)
                //  .setAction("SEND NOTIFICATION", new View.OnClickListener() {
                .setAction(getString(R.string.ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LatLng markerLatLng = marker.getPosition();
                        PlayerParcelable playerParcelable = userLatLngMap.get(markerLatLng);
                        User user = playerParcelable.getUser();
                        Log.v(LOG_TAG, "User's mail::" + user.getEmail());
                        //   Log.e(LOG_TAG, "onClick of onInfoWindowClick");
                        ((Callback) getActivity()).onInviteClick(mUser, user);
                    }
                });
        snackbar.show();
    }
}

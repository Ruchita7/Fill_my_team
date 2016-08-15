package com.sample.android.fillmyteam;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.text.Layout;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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
import com.sample.android.fillmyteam.model.PlayerParcelable;
import com.sample.android.fillmyteam.model.User;
import com.sample.android.fillmyteam.ui.ArcMenu;
import com.sample.android.fillmyteam.util.Constants;
import com.sample.android.fillmyteam.util.Utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FindPlaymatesActivity extends BaseOptionsActivity implements GeoQueryEventListener, ChildEventListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
        GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnInfoWindowLongClickListener {


    private static final int[] ITEM_DRAWABLES = {R.drawable.ic_basketball, R.drawable.ic_tennis,
            R.drawable.ic_football, R.drawable.ic_cricket, R.drawable.ic_badminton, R.drawable.ic_baseball};

    private static final String[] SPORTS = {Constants.BASKETBALL, Constants.TENNIS,
            Constants.FOOTBALL, Constants.CRICKET, Constants.BADMINTON, Constants.BASEBALL};
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
    DatabaseReference mUrlRef;
    PlayerParcelable mPlayerParcelable;
    ArrayList<PlayerParcelable> mPlayerParcelables;
    private GoogleMap mMap;
    MarkerOptions markerOptions;
    Map<LatLng, PlayerParcelable> userLatLngMap;
    private View mWindow;
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
    String mSport = "";
    GeoQuery geoQuery;

    MapFragment mapFragment;
    Spinner sportSpinner;
    @BindView(R.id.map_empty)
    TextView emptyMap;
    ArcMenu arcMenu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_playmates);
      /*  Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/
        if (getIntent() != null) {
            mUser = (User) getIntent().getSerializableExtra(Constants.USER_DETAILS);
        }
        Firebase.setAndroidContext(this);
        mGeoFire = new GeoFire(new Firebase(Constants.APP_PLAYERS_NEAR_URL));
        mUrlRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(Constants.APP_URL_USERS);
        userLatLngMap = new HashMap<>();
        mPlayerParcelables = new ArrayList<>();
        markerMap = new HashMap<>();
        latLngCenter = new LatLng(mUser.getLatitude(), mUser.getLongitude());
        cameraPosition = CameraPosition.builder()
                .target(latLngCenter)
                .zoom(15)
                .build();

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);
        mContents = getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        ButterKnife.bind(this);
        arcMenu = (ArcMenu) findViewById(R.id.arc_menu);
        initArcMenu(arcMenu, ITEM_DRAWABLES);
        emptyMap.setVisibility(View.GONE);
        frameLayout.setVisibility(View.VISIBLE);
        arcMenu.setVisibility(View.VISIBLE);
        if (!Utility.checkNetworkState(this)) {
            emptyMap.setVisibility(View.VISIBLE);
            frameLayout.setVisibility(View.INVISIBLE);
            arcMenu.setVisibility(View.INVISIBLE);
        }

        displayShowCaseMenu();

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(PANEL_CLICK) && (savedInstanceState.containsKey(IMAGE_CLICK))) {
                isPanelClicked = savedInstanceState.getBoolean(PANEL_CLICK);
                isImgClicked = savedInstanceState.getBoolean(IMAGE_CLICK);
            }
            if (savedInstanceState.containsKey(SELECTED_SPORT)) {
                mSport = savedInstanceState.getString(SELECTED_SPORT);
                if (savedInstanceState.containsKey(PLAYERS_NEARBY)) {
                    mPlayerParcelables = savedInstanceState.getParcelableArrayList(PLAYERS_NEARBY);
                    User user = null;
                    for (PlayerParcelable playerParcelable : mPlayerParcelables) {
                        user = playerParcelable.getUser();
                        userLatLngMap.put(new LatLng(user.getLatitude(), user.getLongitude()), playerParcelable);
                    }
                }
            }
        }
    }

    /**
     * Create floating button menu - sub menu items
     *
     * @param menu
     * @param itemDrawables
     */
    private void initArcMenu(ArcMenu menu, int[] itemDrawables) {
        final int itemCount = itemDrawables.length;
        for (int i = 0; i < itemCount; i++) {
            ImageView item = new ImageView(this);
            item.setImageResource(itemDrawables[i]);
            item.setContentDescription(getString(R.string.sports_button, SPORTS[i]));
            item.setTag(SPORTS[i]);
            final int position = i;
            menu.addItem(item, new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    String tag = (String) v.getTag();
                    mSport = tag;
                    fireGeoQuery();

                }
            });
        }
    }

    private void fireGeoQuery() {
        if (geoQuery != null) {
            geoQuery.removeAllListeners();
        }

        clearMap();
        playerFoundCount = 0;
        geoQuery = mGeoFire.queryAtLocation(new GeoLocation(latLngCenter.latitude, latLngCenter.longitude), 1);
        geoQuery.addGeoQueryEventListener(this);
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


    private boolean checkReady() {
        if (mMap == null) {
            Toast.makeText(this, R.string.map_not_ready, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    private void displayShowCaseMenu() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        int menuHelpCount = sharedPreferences.getInt(Constants.ACCESS_COUNT, 0);
        if (menuHelpCount == 0) {
            ShowcaseView showcaseView = new ShowcaseView.Builder(this)
                    .withMaterialShowcase()
                    .setStyle(R.style.CustomShowcaseTheme)
                    .setTarget(new ViewTarget(arcMenu))
                    .hideOnTouchOutside()
                    .setContentTitle(getString(R.string.button_message))
                    .build();

            showcaseView.hideButton();
            showcaseView.setTitleTextAlignment(Layout.Alignment.ALIGN_CENTER);
            showcaseView.forceTextPosition(ShowcaseView.BELOW_SHOWCASE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        geoQuery.removeAllListeners();
        mUrlRef.removeEventListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IMAGE_CLICK, isImgClicked);
        outState.putBoolean(PANEL_CLICK, isPanelClicked);
        outState.putParcelableArrayList(PLAYERS_NEARBY, mPlayerParcelables);
        outState.putString(SELECTED_SPORT, mSport);
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

    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        String emailId = Utility.decodeEmail(key);
        if (!Utility.decodeEmail(mUser.getEmail()).equals(emailId)) {
            playerFoundCount++;

            findUserByEmailId(emailId);
        }
    }

    @Override
    public void onKeyExited(String key) {
        playerFoundCount--;

    }

    @Override
    public void onGeoQueryReady() {
        if (playerFoundCount <= 0) {
            Toast.makeText(this, getString(R.string.no_player_found, mSport), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onGeoQueryError(FirebaseError error) {
        Toast.makeText(this, getString(R.string.geo_query_error), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {
        Toast.makeText(this, getString(R.string.user_moved), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(this, getString(R.string.no_player_found, mSport), Toast.LENGTH_SHORT).show();
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
                .snippet(time + "-" + userObj.getSport());

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

    /**
     * Show player related info on click of marker
     *
     * @param marker
     * @param view
     */
    private void render(Marker marker, View view) {
        String titleText = marker.getTitle();
        String snippet = marker.getSnippet();
        String snippets[] = snippet.split("-");
        int badge = Utility.retrieveSportsIcon(snippets[1]);
        ((ImageView) view.findViewById(R.id.badge)).setImageResource(badge);


        TextView titleUi = ((TextView) view.findViewById(R.id.title));
        if (titleText != null) {
            // Spannable string allows us to edit the formatting of the text.
            SpannableString markerTitleText = new SpannableString(titleText);
            markerTitleText.setSpan(new ForegroundColorSpan(Color.BLACK), 0, titleText.length(), 0);
            titleUi.setText(titleText);
        } else {
            titleUi.setText("");
        }

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
        Snackbar snackbar = Snackbar
                .make(frameLayout, getString(R.string.invite_user, marker.getTitle()), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        LatLng markerLatLng = marker.getPosition();
                        PlayerParcelable playerParcelable = userLatLngMap.get(markerLatLng);
                        User user = playerParcelable.getUser();
                      //  InviteToPlayFragment fragment = InviteToPlayFragment.newInstance(mUser, mUser);
                        Intent intent = new Intent(FindPlaymatesActivity.this,InviteToPlayActivity.class);
                        intent.putExtra(InviteToPlayFragment.CURRENT_USER,mUser);
                        intent.putExtra(InviteToPlayFragment.TO_PLAY_WITH_USER,user);
                        startActivity(intent);
                       // ((Callback) getActivity()).onInviteClick(mUser, mUser);

                    }
                });
        snackbar.show();
    }

    @Override
    public void onInfoWindowLongClick(final Marker marker) {
        Snackbar snackbar = Snackbar
                .make(frameLayout, getString(R.string.invite_user, marker.getTitle()), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LatLng markerLatLng = marker.getPosition();
                        PlayerParcelable playerParcelable = userLatLngMap.get(markerLatLng);
                        User user = playerParcelable.getUser();
                        Intent intent = new Intent(FindPlaymatesActivity.this,InviteToPlayActivity.class);
                        intent.putExtra(InviteToPlayFragment.CURRENT_USER,mUser);
                        intent.putExtra(InviteToPlayFragment.TO_PLAY_WITH_USER,user);
                        startActivity(intent);

                    }
                });
        snackbar.show();
    }


}

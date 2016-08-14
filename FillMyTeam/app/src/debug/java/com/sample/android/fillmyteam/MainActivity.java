package com.sample.android.fillmyteam;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import com.sample.android.fillmyteam.api.Callback;
import com.sample.android.fillmyteam.model.User;
import com.sample.android.fillmyteam.sync.SportsSyncAdapter;
import com.sample.android.fillmyteam.ui.CircularImageTransform;
import com.sample.android.fillmyteam.ui.ViewTargets;
import com.sample.android.fillmyteam.util.Constants;
import com.sample.android.fillmyteam.util.Utility;
import com.facebook.stetho.Stetho;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.location.Geocoder;
import android.location.Location;
import android.widget.Toast;
import android.support.v7.app.AlertDialog;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author Ruchita_Maheshwary
 *         Main Activity launched after login. It will be first screen if the user is already logged in
 */
public class MainActivity extends AppCompatActivity
        implements Callback, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, LocationListener {
    //NavigationView.OnNavigationItemSelectedListener,
    public static final String SENT_TOKEN_TO_SERVER = "SENT_TOKEN_TO_SERVER";
    String mAddressOutput = "";
    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    User mUser;
    public final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    boolean isDrawerLocked;
    View navigationHeader;
    double mLongitude;
    double mLatitude;
    Location mLastLocation;
    Activity mActivity;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    /*    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/
        Utility.hideSoftKeyboard(this);
      /*  final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);*/
        mActivity = this;
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        //When User is first time logging in, updated SharedPreferences with his credentials and load his details in navigation drawer

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        checkLocationSettings();
        if (!Utility.checkNetworkState(this)) {
            DialogFragment dialogFragment = new NetworkConnectionDialogFragment();
            dialogFragment.show(getFragmentManager(), getString(R.string.login_network_unavailable));
        }
        if (getIntent().hasExtra(Constants.USER_CREDENTIALS)) {

            mUser = (User) getIntent().getSerializableExtra(Constants.USER_CREDENTIALS);
            if (mLatitude != 0 && mLongitude != 0) {
                mUser.setLatitude(mLatitude);
                mUser.setLongitude(mLongitude);
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(Constants.EMAIL, mUser.getEmail());
            editor.commit();
            try {
                ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                String userJson = ow.writeValueAsString(mUser);
                editor.putString(Constants.USER_INFO, userJson);
                editor.commit();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        } else if (getIntent().hasExtra(Constants.LOGGED_IN_USER_EMAIL)) {            //User is already logged in
            String email = getIntent().getStringExtra(Constants.LOGGED_IN_USER_EMAIL);

            String userJson = sharedPreferences.getString(Constants.USER_INFO, "");
            if (userJson != null && !userJson.isEmpty()) {
                try {
                    mUser = new ObjectMapper().readValue(userJson, User.class);
                    if (mLatitude != 0 && mLongitude != 0) {
                        mUser.setLatitude(mLatitude);
                        mUser.setLongitude(mLongitude);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.IS_USER_LOGGED_IN, true);
        editor.commit();
        SportsSyncAdapter.initializeSyncAdapter(this);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(),mUser);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(mViewPager);

   /*     NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationHeader = navigationView.inflateHeaderView(R.layout.nav_header_main);
        updateNavigationViewHeader();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                Utility.hideSoftKeyboard(mActivity);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();*/
        int menuHelpCount = sharedPreferences.getInt(Constants.ACCESS_COUNT, 0);
 /*       if(menuHelpCount==0) {
            try {
                ViewTarget navigationButtonViewTarget = ViewTargets.navigationButtonViewTarget(toolbar);
                new ShowcaseView.Builder(this)
                        .withMaterialShowcase()
                        .setTarget(navigationButtonViewTarget)
                        .setStyle(R.style.CustomShowcaseTheme2)
                        .setContentText(getString(R.string.navigation_message))
                        .build();
            } catch (ViewTargets.MissingViewException e) {
                e.printStackTrace();
            }
            editor = sharedPreferences.edit();
            editor.putInt(Constants.ACCESS_COUNT, ++menuHelpCount);
            editor.commit();
        }*/

        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(
                                Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(
                                Stetho.defaultInspectorModulesProvider(this))
                        .build());

        /*if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, MatchesFragment.newInstance(mUser))
                    .commit();
        }
*/
    }


    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        String userJson = sharedPreferences.getString(Constants.USER_INFO, null);
        if (userJson != null) {
            if (userJson != null && !userJson.isEmpty()) {
                try {
                    mUser = new ObjectMapper().readValue(userJson, User.class);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //  updateNavigationViewHeader();
        }
        //Log.v(LOG_TAG, "mUser" + sharedPreferences.getString(Constants.USER_INFO, null));
    }

    /**
     * Update navigation drawer header
     */

/*    private void updateNavigationViewHeader() {
        if (mUser != null) {
            TextView userTextView = (TextView) navigationHeader.findViewById(R.id.userNameTextView);
            TextView emailTextView = (TextView) navigationHeader.findViewById(R.id.emailTextView);
            ImageView userPhotoImageView = (ImageView) navigationHeader.findViewById(R.id.profileImageView);
            userTextView.setText(mUser.getName());
            emailTextView.setText(mUser.getEmail());
            if (mUser.getPhotoUrl() != null && !mUser.getPhotoUrl().isEmpty()) {
                Picasso.with(this).load(mUser.getPhotoUrl()).transform(new CircularImageTransform()).into(userPhotoImageView);
            }
        }

    }*/
    private boolean isTablet() {
        return (getApplicationContext().getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

  /*  @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() < 1) {
            finish();
        }

        //Set correct title for back navigation
        if (getFragmentManager().getBackStackEntryCount() >= 2) {
            FragmentManager.BackStackEntry backStackFragment = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - 2);

            if (backStackFragment != null) {
                String className = backStackFragment.getName();
                if (className != null) {
                    setTitle(className);
                }
            } else {
                setTitle(getString(R.string.upcoming_matches));
            }
        } else {
            setTitle(getString(R.string.upcoming_matches));
        }
        MatchesFragment matchesFragment = (MatchesFragment) getFragmentManager().findFragmentByTag(MatchesFragment.class.getSimpleName());
        if (matchesFragment != null && matchesFragment.isVisible()) {
            finish();
            return;
        }

        //Redirect to upcoming matches fragment from learn to play and find playmates fragment
        SportsInfoFragment sportsInfoFragment = (SportsInfoFragment) getFragmentManager().findFragmentByTag(SportsInfoFragment.class.getSimpleName());
        FindPlaymatesFragment findPlaymatesFragment = (FindPlaymatesFragment) getFragmentManager().findFragmentByTag(FindPlaymatesFragment.class.getSimpleName());
        if (sportsInfoFragment != null && sportsInfoFragment.isVisible() || (findPlaymatesFragment != null && findPlaymatesFragment.isVisible())) {
            MatchesFragment fragment = (MatchesFragment) MatchesFragment.newInstance(mUser);
            getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment, fragment.getClass().getSimpleName()).commit();
            setTitle(getString(R.string.upcoming_matches));
        } else {
            super.onBackPressed();
        }
    }
*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.share_action) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Handle navigation view item clicks here to call corresponding fragments for each navigation item
     *
     * @param item
     * @return
     */
    @SuppressWarnings("StatementWithEmptyBody")
 /*   @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        Class fragmentClass = null;
        Utility.hideSoftKeyboard(this);
        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = null;
        FragmentTransaction ft = fragmentManager.beginTransaction();

        if (!Utility.checkNetworkState(this)) {
            DialogFragment dialogFragment = new NetworkConnectionDialogFragment();
            dialogFragment.show(getFragmentManager(), getString(R.string.login_network_unavailable));
        }

        switch (id) {

            case R.id.learn_play:
                fragment = (SportsInfoFragment) SportsInfoFragment.newInstance(mUser.getLatitude(), mUser.getLongitude());
                break;
            case R.id.find_playmates:
                checkLocationSettings();
                mUser.setLongitude(mLongitude);
                mUser.setLongitude(mLongitude);
                fragment = (FindPlaymatesFragment) FindPlaymatesFragment.newInstance(mUser);
                break;
            case R.id.edit_profile:
                fragment = (EditProfileFragment) EditProfileFragment.newInstance(mUser);
                break;
            case R.id.upcoming_matches:
                fragment = (MatchesFragment) MatchesFragment.newInstance(mUser);
                break;
            case R.id.sports_store_locator:
                fragment = (SportsStoreLocatorFragment) SportsStoreLocatorFragment.newInstance(mUser.getLatitude(), mUser.getLongitude());
                break;

            case R.id.user_settings:
                fragment = (SettingsFragment) SettingsFragment.newInstance();
                break;

            case R.id.logout:
                logoutUser();
                break;
        }

        if (fragment != null) {
            ft.replace(R.id.content_frame, fragment, fragment.getClass().getSimpleName())
                    .addToBackStack(Utility.getTitle(this, fragment.getClass().getSimpleName()))
                    .commit();


        }

        item.setChecked(true);

        setTitle(item.getTitle());
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
*/

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void logoutUser() {

        FirebaseAuth.getInstance().signOut();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.IS_USER_LOGGED_IN, false);
        editor.putString(Constants.EMAIL, "");
        editor.putString(Constants.USER_INFO, "");
        editor.commit();
        Intent intent = new Intent(getApplicationContext(), GoogleSignInActivity.class);
        intent.putExtra(Constants.LOGOUT, true);
        startActivity(intent);
    }


    /**
     * Called when user needs to be invited to play from FindPlaymatesFragment
     *
     * @param currentUser
     * @param playWithUser
     */
    @Override
    public void onInviteClick(User currentUser, User playWithUser) {
        InviteToPlayFragment fragment = InviteToPlayFragment.newInstance(currentUser, playWithUser);
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    @Override
    public void onItemSelected(String sportId, SportsInfoAdapter.InfoViewHolder vh) {
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GoogleSignInActivity.REQUEST_CHECK_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    // Log.i(TAG, "User agreed to make required location settings changes.");
                    startLocationUpdates();
                    break;
                case Activity.RESULT_CANCELED:
                    //  Log.i(TAG, "User chose not to make required location settings changes.");
                    break;
            }
        }
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                mLocationRequest,
                this
        );

    }

    /**
     * Prompt user to enable GPS and Location Services
     */
    public void checkLocationSettings() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(30 * 1000);
        mLocationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        startLocationUpdates();

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    MainActivity.this, GoogleSignInActivity.REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {

            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            if (mLastLocation != null) {
                // Determine whether a Geocoder is available.
                mLatitude = mLastLocation.getLatitude();
                mLongitude = mLastLocation.getLongitude();
                if (!Geocoder.isPresent()) {
                    Toast.makeText(this, getString(R.string.no_geocoder),
                            Toast.LENGTH_LONG).show();
                    return;
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class NetworkConnectionDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.no_network_header);
            builder.setMessage(R.string.no_network)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            NetworkConnectionDialogFragment.this.getDialog().cancel();
                        }
                    });

            // Create the AlertDialog object and return it
            return builder.create();
        }
    }
}
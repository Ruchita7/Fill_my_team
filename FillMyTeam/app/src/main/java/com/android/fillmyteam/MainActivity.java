package com.android.fillmyteam;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.fillmyteam.util.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeStandalonePlayer;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        SportsInfoFragment.Callback {

    public static final String SENT_TOKEN_TO_SERVER = "SENT_TOKEN_TO_SERVER";
    Location mLastLocation;

    // AddressResultReceiver mResultReceiver;
    GoogleApiClient mGoogleApiClient;
    String mAddressOutput = "";
    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    double mLongitude;
    double mLatitude;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // editor.putString("email", "ruchita,maheshwary@gmail,com");
        //  editor.putString("email", "poomah29@gmail,com");
        editor.putString("email", "ruchita,maheshwary@gmail,com");
        editor.commit();
        //mResultReceiver = new AddressResultReceiver(new Handler());

        //   mResultReceiver.setReceiver(this);
      /*  FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
*/
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerClosed(View view) {
                // getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                //    getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //   setupDrawerContent(navigationView);
        //   new GcmRegistrationAsyncTask(this).execute();

    /*    if (checkPlayServices()) {
      //      SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

            boolean sentToken = sharedPreferences.getBoolean(SENT_TOKEN_TO_SERVER, false);
            if (!sentToken) {
                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            }
        }*/

     /*   FirebaseOptions options = null;

        try {
            options = new FirebaseOptions.Builder()
                    .setServiceAccount(new FileInputStream("/Development/Notifications/My-Notification_Server-serviceAccountCredentials.json"))
                    .setDatabaseUrl("https://my-notification-server.firebaseio.com/")
                    .build();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/

    }

 /*   private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }
*/


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        // Fragment fragment = null;
        Class fragmentClass = null;
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = null;
       /* if (id == R.id.learn_play) {
            //   fragmentClass = SportsInfoFragment.class;
            SportsInfoFragment sportsInfoFragment = SportsInfoFragment.newInstance(mLatitude, mLongitude);
            fragmentManager.beginTransaction().replace(R.id.content_frame, sportsInfoFragment).commit();
        } else if (id == R.id.find_playmates) {
            //   fragmentClass = FindPlaymatesFragment.class;
            FindPlaymatesFragment findPlaymatesFragment = FindPlaymatesFragment.newInstance(mLatitude, mLongitude);
            fragmentManager.beginTransaction().replace(R.id.content_frame, findPlaymatesFragment).commit();
        } else if (id == R.id.edit_profile) {
            EditProfileFragment editProfileFragmentFragment = EditProfileFragment.newInstance(mLatitude, mLongitude);
            fragmentManager.beginTransaction().replace(R.id.content_frame, editProfileFragmentFragment).commit();
        }*/

        switch (id) {

            case R.id.learn_play:
                fragment = (SportsInfoFragment) SportsInfoFragment.newInstance(mLatitude, mLongitude);
                ;
                break;
            case R.id.find_playmates:
                fragment = (FindPlaymatesFragment) FindPlaymatesFragment.newInstance(mLatitude, mLongitude);
                break;
            case R.id.edit_profile:
                fragment = (EditProfileFragment) EditProfileFragment.newInstance(mLatitude, mLongitude);
                break;
            case R.id.sports_store_locator:
                fragment = (SportsStoreLocatorFragment) SportsStoreLocatorFragment.newInstance(mLatitude, mLongitude);
                break;
        }
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
     /*   try {
            fragment = (Fragment) fragmentClass.newInstance(mAddressOutput);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        // Insert the fragment by replacing any existing fragment
        //   FragmentManager fragmentManager = getSupportFragmentManager();
        //   fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        // Highlight the selected item has been done by NavigationView
        item.setChecked(true);
        // Set action bar title
        setTitle(item.getTitle());
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            if (mLastLocation != null) {
                // Determine whether a Geocoder is available.
                if (!Geocoder.isPresent()) {
                   /* Toast.makeText(this, R.string.no_geocoder_available,
                            Toast.LENGTH_LONG).show();*/
                    Toast.makeText(this, "no geocoder available",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                mLatitude = mLastLocation.getLatitude();
                mLongitude = mLastLocation.getLongitude();

                //    startIntentService();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

 /*   protected void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        startService(intent);
    }*/

    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

 /*   class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            //  displayAddressOutput();

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                // showToast(getString(R.string.address_found));
                Log.v(LOG_TAG, "address found" + mAddressOutput);
        *//*    if(mReceiver!=null) {
                mReceiver.onReceiveResult(resultCode, resultData);
            }*//*
            }
        }

   *//* @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        Log.d("Main Activity","received result from Service="+resultData.getString(Constants.RESULT_DATA_KEY));
    }*//*
    }*/

 /*   public boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.v(LOG_TAG, "This device is not supported");
                finish();
                ;
            }
            return false;
        }
        return true;
    }*/


    @Override
    public void onItemSelected(String sportId, SportsInfoAdapter.InfoViewHolder vh) {
        /*if (mTwoPane) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(DetailFragment.DETAIL_URI, dateUri);
            DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, detailFragment, DETAILFRAGMENT_TAG)
                    .commit();

        } else {*/

       /* Bundle args = new Bundle();
        args.putString("Id", sportId);
        SportsDetailFragment fragment = new SportsDetailFragment();
        fragment.setArguments(args);*/
        SportsDetailFragment fragment = SportsDetailFragment.newInstance(sportId);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                //     .addToBackStack(getResources().getString(R.string.book_detail))         //used string resource
                .commit();
       /* Intent intent = new Intent(this, SportsDetailActivity.class)
                .setData(dateUri);


        ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(this, new Pair<View, String>(vh.sportsImage, getString(R.string.detail_icon_transition_name)));
        ActivityCompat.startActivity(this, intent, activityOptions.toBundle());*/
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQ_START_STANDALONE_PLAYER && resultCode != RESULT_OK) {
            YouTubeInitializationResult errorReason =
                    YouTubeStandalonePlayer.getReturnedInitializationResult(data);
            if (errorReason.isUserRecoverableError()) {
                errorReason.getErrorDialog(this, 0).show();
            } else {
                String errorMessage =
                        String.format(getString(R.string.error_player), errorReason.toString());
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            }
        }
    }
}
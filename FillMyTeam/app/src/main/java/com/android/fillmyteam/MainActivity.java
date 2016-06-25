package com.android.fillmyteam;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.fillmyteam.api.Callback;
import com.android.fillmyteam.model.User;
import com.android.fillmyteam.util.Constants;
import com.android.fillmyteam.util.Utility;
import com.facebook.stetho.Stetho;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


//import com.google.api.client.json.gson.GsonFactory;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,Callback {

    public static final String SENT_TOKEN_TO_SERVER = "SENT_TOKEN_TO_SERVER";


    String mAddressOutput = "";
    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    User mUser;
    public final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    //  FirebaseAuth mAuth;
    boolean isDrawerLocked;
    View navigationHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationHeader = navigationView.inflateHeaderView(R.layout.nav_header_main);
        // mAuth = FirebaseAuth.getInstance();

        //   if (getIntent().hasExtra("User Credentials")) {
        if (getIntent().hasExtra(Constants.USER_CREDENTIALS)) {
            //  mUser = (User) getIntent().getSerializableExtra("User Credentials");
            mUser = (User) getIntent().getSerializableExtra(Constants.USER_CREDENTIALS);
            updateNavigationViewHeader();
            Log.v(LOG_TAG, mUser.getEmail() + "," + mUser.getName() + "," + mUser.getPhotoUrl());
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            //   editor.putString("email", mUser.getEmail());
            editor.putString(Constants.EMAIL, mUser.getEmail());
            editor.commit();
        } else if (getIntent().hasExtra(Constants.LOGGED_IN_USER_EMAIL)) {
            String email = getIntent().getStringExtra(Constants.LOGGED_IN_USER_EMAIL);
           final DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReferenceFromUrl((Constants.APP_URL_USERS) + "/" + Utility.encodeEmail(email));
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mUser = dataSnapshot.getValue(User.class);
                    updateNavigationViewHeader();
                    ref.removeEventListener(this);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }


        //  editor.putString("email", "poomah29@gmail,com");
        // editor.putString("email", "ruchita,maheshwary@gmail,com");


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
     /*   if (isTablet()) {
            drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
            drawer.setScrimColor(Color.TRANSPARENT);
            isDrawerLocked = true;
        }
*/
 /*       mUser = new User();
        mUser.setLongitude(28.7514586);
        mUser.setLatitude(77.0994467);
        mUser.setEmail("android.studio@android.com");
        mUser.setName("Android Studio");*/
     /*   ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerClosed(View view) {
                // getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                //    getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };*/
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();



        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(
                                Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(
                                Stetho.defaultInspectorModulesProvider(this))
                        .build());



    }


    private void updateNavigationViewHeader()   {
        if (mUser != null) {
            //  View navigationHeader = navigationView.inflateHeaderView(R.layout.nav_header_main);
            TextView userTextView = (TextView) navigationHeader.findViewById(R.id.userNameTextView);
            TextView emailTextView = (TextView) navigationHeader.findViewById(R.id.emailTextView);
            ImageView userPhotoImageView = (ImageView) navigationHeader.findViewById(R.id.profileImageView);
            userTextView.setText(mUser.getName());
            emailTextView.setText(mUser.getEmail());
            if (mUser.getPhotoUrl() != null && !mUser.getPhotoUrl().isEmpty()) {
                Picasso.with(this).load(mUser.getPhotoUrl()).into(userPhotoImageView);
            }
        }

    }

    private boolean isTablet() {
        return (getApplicationContext().getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

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

        switch (id) {

            case R.id.learn_play:
                //  fragment = (SportsInfoFragment) SportsInfoFragment.newInstance(mUser.getLatitude(), mUser.getLongitude());
                fragment = (SportsInfoFragment) SportsInfoFragment.newInstance(mUser.getLatitude(), mUser.getLongitude());
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
                break;
            case R.id.find_playmates:
                fragment = (FindPlaymatesFragment) FindPlaymatesFragment.newInstance(mUser);
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
                break;
            case R.id.edit_profile:
                fragment = (EditProfileFragment) EditProfileFragment.newInstance(mUser);
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
                break;
            case R.id.upcoming_matches :
                fragment = (MatchesFragment) MatchesFragment.newInstance(mUser);
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

                break;
            case R.id.sports_store_locator:
                fragment = (SportsStoreLocatorFragment) SportsStoreLocatorFragment.newInstance(mUser.getLatitude(), mUser.getLongitude());
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
                break;

            case R.id.user_settings:
                fragment = (SettingsFragment) SettingsFragment.newInstance();
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
                break;

            case R.id.logout:
                logoutUser();
                break;
        }

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
    protected void onStop() {
        super.onStop();

        //mGoogleApiClient.disconnect();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        mGoogleApiClient.connect();
    }

    private void logoutUser() {
     /*   mAuth.signOut();

        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                       // updateUI(null);
                        Intent intent = new Intent(getApplicationContext(),GoogleSignInActivity.class);
                        startActivity(intent);
                    }
                });*/
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getApplicationContext(), GoogleSignInActivity.class);
        intent.putExtra(Constants.LOGOUT,true);
        startActivity(intent);
    }




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

        } */


        SportsDetailFragment fragment = SportsDetailFragment.newInstance(sportId);
        // fragment.show(fragmentManager, "dialog");
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
    public void onInviteClick(User currentUser, User playWithUser) {
        InviteToPlayFragment fragment = InviteToPlayFragment.newInstance(currentUser,playWithUser);
        // fragment.show(fragmentManager, "dialog");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                //     .addToBackStack(getResources().getString(R.string.book_detail))         //used string resource
                .commit();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }


}
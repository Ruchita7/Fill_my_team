package com.android.fillmyteam;

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

import com.android.fillmyteam.api.Callback;
import com.android.fillmyteam.model.User;
import com.android.fillmyteam.sync.SportsSyncAdapter;
import com.android.fillmyteam.ui.CircularImageTransform;
import com.android.fillmyteam.util.Constants;
import com.android.fillmyteam.util.Utility;
import com.facebook.stetho.Stetho;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Ruchita_Maheshwary
 *         Main Activity launched after login. It will be first screen if the user is already logged in
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Callback {

    public static final String SENT_TOKEN_TO_SERVER = "SENT_TOKEN_TO_SERVER";
    String mAddressOutput = "";
    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    User mUser;
    public final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    boolean isDrawerLocked;
    View navigationHeader;


    Activity mActivity;
    List<WeakReference<Fragment>> fragList = new ArrayList<WeakReference<Fragment>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Utility.hideSoftKeyboard(this);
        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        mActivity = this;
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        //When User is first time logging in, updated SharedPreferences with his credentials and load his details in navigation drawer
        if (getIntent().hasExtra(Constants.USER_CREDENTIALS)) {

            mUser = (User) getIntent().getSerializableExtra(Constants.USER_CREDENTIALS);
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.IS_USER_LOGGED_IN, true);
        editor.commit();
        SportsSyncAdapter.initializeSyncAdapter(this);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
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
        toggle.syncState();

        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(
                                Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(
                                Stetho.defaultInspectorModulesProvider(this))
                        .build());

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, MatchesFragment.newInstance(mUser))
                    .commit();
        }

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
            updateNavigationViewHeader();
        }
        //Log.v(LOG_TAG, "mUser" + sharedPreferences.getString(Constants.USER_INFO, null));
    }

    /**
     * Update navigation drawer header
     */

    private void updateNavigationViewHeader() {
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

    }

    private boolean isTablet() {
        return (getApplicationContext().getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    @Override
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
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        Class fragmentClass = null;
        Utility.hideSoftKeyboard(this);
        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = null;
        FragmentTransaction ft = fragmentManager.beginTransaction();

        switch (id) {

            case R.id.learn_play:
                fragment = (SportsInfoFragment) SportsInfoFragment.newInstance(mUser.getLatitude(), mUser.getLongitude());
                break;
            case R.id.find_playmates:
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


}
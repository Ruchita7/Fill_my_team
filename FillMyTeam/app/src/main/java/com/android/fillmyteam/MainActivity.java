package com.android.fillmyteam;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
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
import com.android.fillmyteam.ui.CircularImageTransform;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationHeader = navigationView.inflateHeaderView(R.layout.nav_header_main);

        //When User is first time logging in, updated SharedPreferences with his credentials and load his details in navigation drawer
        if (getIntent().hasExtra(Constants.USER_CREDENTIALS)) {

            mUser = (User) getIntent().getSerializableExtra(Constants.USER_CREDENTIALS);
            updateNavigationViewHeader();
            Log.v(LOG_TAG, mUser.getEmail() + "," + mUser.getName() + "," + mUser.getPhotoUrl());
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(Constants.EMAIL, mUser.getEmail());
            editor.commit();
        } else if (getIntent().hasExtra(Constants.LOGGED_IN_USER_EMAIL)) {            //User is already logged in
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

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
            ft.replace(R.id.content_frame, fragment).commit();
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
        Intent intent = new Intent(getApplicationContext(), GoogleSignInActivity.class);
        intent.putExtra(Constants.LOGOUT, true);
        startActivity(intent);
    }


    /**
     * Called when SportsInfoFragment list item is clicked. This will then replace the fragment with SportsDetailFragment
     * @param sportId
     * @param vh
     */
   /* @Override
    public void onItemSelected(String sportId, SportsInfoAdapter.InfoViewHolder vh) {

        SportsDetailFragment fragment = SportsDetailFragment.newInstance(sportId);
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)

                .commit();
    }
*/

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
                .addToBackStack(null)
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
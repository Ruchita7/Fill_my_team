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
import android.util.Log;
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

    ImageView basketBallImageView;
    ImageView tennisImageView;
    ImageView footballImageView;
    ImageView cricketImageView;
    ImageView badmintonImageView;
    ImageView baseballImageView;
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

            Log.v(LOG_TAG, mUser.getEmail() + "," + mUser.getName() + "," + mUser.getPhotoUrl());

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
          /*  final DatabaseReference ref = FirebaseDatabase.getInstance()
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
            });*/
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
        //for tablet checking
       /* else {
            mUser = new User();
            mUser.setEmail("ruchita.maheshwary@gmail.com");
            mUser.setLatitude(28.6952431);
            mUser.setLongitude(77.1134005);
            mUser.setName("ruchita maheshwary");
            mUser.setPhotoUrl("https://lh6.googleusercontent.com/-32tmODRPtTw/AAAAAAAAAAI/AAAAAAAABEA/dISvm7M_sKE/s96-c/photo.jpg");
            mUser.setPlayingPlace("D-105, Tarun Enclave, Pitampura, New Delhi, Delhi 110034, India");
            mUser.setPlayingTime("4:30 PM");
            mUser.setSport("Volleyball");
        }*/
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationHeader = navigationView.inflateHeaderView(R.layout.nav_header_main);
        updateNavigationViewHeader();
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

    /* private boolean onBackPressed(FragmentManager fm) {
         if (fm != null) {
             if (fm.getBackStackEntryCount() > 0) {
                 fm.popBackStack();
                 return true;
             }

          //   List<Fragment> fragList = fm.getFragments();
             if (fragList != null && fragList.size() > 0) {
                 for (WeakReference<Fragment>  frag : fragList) {
                     Fragment fragment = frag.get();
                     if (fragment == null) {
                         continue;
                     }
                     if (fragment.isVisible()) {
                         if (onBackPressed(fragment.getChildFragmentManager())) {
                             return true;
                         }
                     }
                 }
             }
         }
         return false;
     }


     @Override
     public void onAttachFragment (Fragment fragment) {
         fragList.add(new WeakReference(fragment));
     }

     public List<Fragment> getActiveFragments() {
         ArrayList<Fragment> ret = new ArrayList<Fragment>();
         for(WeakReference<Fragment> ref : fragList) {
             Fragment f = ref.get();
             if(f != null) {
                 if(f.isVisible()) {
                     ret.add(f);
                 }
             }
         }
         return ret;
     }*/
    @Override
    public void onBackPressed() {
      /*  DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {

            FragmentManager.BackStackEntry backStackEntry = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - 1);
            String tag = backStackEntry.getName();
            Fragment fragment = getFragmentManager().findFragmentByTag(tag);
            if (fragment != null && fragment instanceof MatchesFragment) {
                finish();
            } else {
                try {
                    getFragmentManager().popBackStack();
                   // super.onBackPressed();
                } catch (NullPointerException e) {
                    e.printStackTrace();

                }
            }
        }*/
        if (getFragmentManager().getBackStackEntryCount() < 1) {
            finish();
        }
        if (getFragmentManager().getBackStackEntryCount() >= 2) {
            FragmentManager.BackStackEntry backStackFragment = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - 2);

            if (backStackFragment != null) {
                String className = backStackFragment.getName();
                if (className != null) {
                    setTitle(className);
                } /*else {
                    setTitle(getString(R.string.upcoming_matches));
                }*/
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


        SportsInfoFragment sportsInfoFragment = (SportsInfoFragment) getFragmentManager().findFragmentByTag(SportsInfoFragment.class.getSimpleName());
        FindPlaymatesFragment findPlaymatesFragment = (FindPlaymatesFragment) getFragmentManager().findFragmentByTag(FindPlaymatesFragment.class.getSimpleName());
        if (sportsInfoFragment != null && sportsInfoFragment.isVisible() || (findPlaymatesFragment != null && findPlaymatesFragment.isVisible())) {
            // finish();
            //  return;
            MatchesFragment fragment = (MatchesFragment) MatchesFragment.newInstance(mUser);
            //getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment, fragment.getClass().getSimpleName()).commit();
            getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment, fragment.getClass().getSimpleName()).commit();
            setTitle(getString(R.string.upcoming_matches));
        }


     /*   if (findPlaymatesFragment != null && findPlaymatesFragment.isVisible()) {
            // finish();
            //  return;
            MatchesFragment  fragment = (MatchesFragment) MatchesFragment.newInstance(mUser);
            getFragmentManager().beginTransaction().replace(R.id.content_frame,fragment,fragment.getClass().getSimpleName()).commit();
        }
*/
        else {
            super.onBackPressed();
        }
        // getFragmentManager().popBackStack();
                  /*  if (fragment != null && fragment instanceof MatchesFragment) {
                        finish();
                    } else {
                        MatchesFragment matchesFragment = MatchesFragment.newInstance(mUser);
                        getFragmentManager().beginTransaction().replace(R.id.content_frame, matchesFragment).addToBackStack(matchesFragment.getClass().getSimpleName()).commit();
                    }*/
                   /* FragmentManager.BackStackEntry backStackEntry = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount()-1);
            if(backStackEntry.getName())
            finish();*/
       /* FragmentManager fm = getFragmentManager();
        if (onBackPressed(fm)) {
            return;
        }
        super.onBackPressed();*/
       /* String tag = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount()-1).getName();

        if( null ==getFragmentManager().findFragmentByTag(tag) || !getFragmentManager().findFragmentByTag(tag).getChildFragmentManager().popBackStackImmediate()) {
            super.onBackPressed();
        }*/
    }

  /*  @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

            // Else, nothing in the direct fragment back stack
            else{
                // Let super handle the back press
                super.onBackPressed();
            }
        }
    */
    /* else {
            int backStackCount = getFragmentManager().getBackStackEntryCount();
            if (backStackCount > 0) {
                getFragmentManager().popBackStackImmediate();
               // super.onBackPressed();
            }
*/
  /*      FragmentManager fragmentManager = getFragmentManager();
        Log.v(LOG_TAG,"count"+fragmentManager.getBackStackEntryCount());
        int backStackCount = fragmentManager.getBackStackEntryCount();

        if(backStackCount==0)
        {
          //  finish();

            finish();
            return;
        }
        */
        /* Fragment fragment = (MatchesFragment) MatchesFragment.newInstance(mUser);
            String tag=fragment.getClass().getSimpleName();
            fragmentManager.beginTransaction().replace(R.id.content_frame,fragment,tag).addToBackStack(tag).commit();*/

        /*FragmentManager.BackStackEntry backStackFragment = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount());
        if (backStackFragment == null || backStackFragment instanceof MatchesFragment) {
            finish();
        }
    }*/


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
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

        // rightCenterButton.
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
                    // .addToBackStack(null)
                    //   .addToBackStack(fragment.getClass().getSimpleName())
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
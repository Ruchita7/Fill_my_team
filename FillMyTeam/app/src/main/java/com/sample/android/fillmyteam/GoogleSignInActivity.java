package com.sample.android.fillmyteam;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.client.Firebase;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sample.android.fillmyteam.model.User;
import com.sample.android.fillmyteam.ui.ScrimUtil;
import com.sample.android.fillmyteam.util.Constants;
import com.sample.android.fillmyteam.util.Utility;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Ruchita_Maheshwary
 *         This is the main launcher activity which logs in the user with Google  using Firebase-Google Authentication
 */
public class GoogleSignInActivity extends BaseActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        View.OnClickListener,LocationListener {

    private static final String LOG_TAG = GoogleSignInActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 9001;
    Location mLastLocation;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    DatabaseReference mUrlRef;
    private GoogleApiClient mGoogleApiClient;

    User mAuthenticatedUser;
    double mLongitude;
    double mLatitude;
    GeoFire mGeoFire;
    String mPlayingLocation = "";
    Typeface mSouthernAire;
    private LocationRequest mLocationRequest;

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mSouthernAire=  Typeface.createFromAsset(getAssets(),"SouthernAire_Personal_Use_Only.ttf");
        Firebase.setAndroidContext(this);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(LocationServices.API)
                .build();

        checkLocationSettings();
        boolean isLogout = false;
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        String userEmailId = sharedPreferences.getString(Constants.EMAIL, "");
        mUrlRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(Constants.APP_URL);
        mAuth = FirebaseAuth.getInstance();
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            View decorView = getWindow().getDecorView();    // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
        mGeoFire = new GeoFire(new Firebase(Constants.APP_PLAYERS_NEAR_URL));

        boolean isUserLoggedIn = sharedPreferences.getBoolean(Constants.IS_USER_LOGGED_IN, false);
        if (isUserLoggedIn) {       //user already logged in
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra(Constants.LOGGED_IN_USER_EMAIL, userEmailId);

            startActivity(intent);
            finish();
        }  else {
            mGoogleApiClient.connect();

            setContentView(R.layout.activity_google_sign_in);
            TextView appTitleTextView = (TextView)findViewById(R.id.app_title);
            appTitleTextView.setTypeface(mSouthernAire);

            View scrimView = findViewById(R.id.scrim_view);
            scrimView.setBackground(ScrimUtil.makeCubicGradientScrimDrawable(
                    0xaa000000, 8, Gravity.BOTTOM));

           findViewById(R.id.sign_in_button).setOnClickListener(this);

            initiateAuthentication(); // for new user or returning user
        }

    }



    /**
     * Prompt user to enable GPS and Location Services
     */
    public  void checkLocationSettings() {
        mLocationRequest  = LocationRequest.create();
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
                                    GoogleSignInActivity.this, REQUEST_CHECK_SETTINGS);
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
    public void onLocationChanged(Location location) {
        mLatitude=location.getLatitude();
        mLongitude=location.getLongitude();
        getLocation();
    }


  protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                mLocationRequest,
                this
        );

    }

    private void initiateAuthentication() {
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    //Log.d(LOG_TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    final String userEmailAddress = user.getEmail();

                    final DatabaseReference ref = mUrlRef.child("/" + Constants.LOCATION_USERS + "/" + Utility.encodeEmail(userEmailAddress));

                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            mAuthenticatedUser = dataSnapshot.getValue(User.class);
                            if (mAuthenticatedUser != null) {
                                //do nothing
                       /*         mAuthenticatedUser.setLongitude(mLongitude);
                                mAuthenticatedUser.setLatitude(mLatitude);
                                mAuthenticatedUser.setPlayingPlace(mPlayingLocation);
                                mAuthenticatedUser.setPhotoUrl(user.getPhotoUrl().toString());*/
                                ref.child(Constants.LATITUDE).setValue(mLatitude);
                                ref.child(Constants.LONGITUDE).setValue(mLongitude);
                                ref.child(Constants.PLAYING_PLACE).setValue(mPlayingLocation);
                                ref.child(Constants.PHOTO_URL_VAL).setValue(user.getPhotoUrl().toString());
                            } else {
                                HashMap<String, Object> userAndUidMapping = new HashMap<String, Object>();
                                GregorianCalendar gcalendar = new GregorianCalendar();
                                String playingTime = Utility.getCurrentTime(gcalendar);
                               //Log.v(LOG_TAG,"user details :"+user.getDisplayName()+" "+userEmailAddress+" "+mLatitude+" "+mLongitude+" "+playingTime+" "+user.getPhotoUrl()+" "+mPlayingLocation);
                                String photoUrl="";
                                if(user.getPhotoUrl()!=null)
                                {
                                    photoUrl= user.getPhotoUrl().toString();
                                }
                                mAuthenticatedUser = new User(user.getDisplayName(), userEmailAddress, "", mLatitude, mLongitude, playingTime,photoUrl , mPlayingLocation);
                                HashMap<String, Object> newUserMap = (HashMap<String, Object>)
                                        new ObjectMapper().convertValue(mAuthenticatedUser, Map.class);
                                userAndUidMapping.put("/" + Constants.LOCATION_USERS + "/" + Utility.encodeEmail(userEmailAddress),
                                        newUserMap);

                                mUrlRef.updateChildren(userAndUidMapping, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if (databaseError != null) {
                                                   Toast.makeText(getApplicationContext(),getString(R.string.api_not_connected),Toast.LENGTH_SHORT).show();
                                        } else {
                                            mGeoFire.setLocation(Utility.encodeEmail(mAuthenticatedUser.getEmail()), new GeoLocation(mAuthenticatedUser.getLatitude(), mAuthenticatedUser.getLongitude()));
                                        }
                                    }
                                });
                            }
                            ref.removeEventListener(this);
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(Constants.IS_USER_LOGGED_IN, true);
                            editor.commit();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.putExtra(Constants.USER_CREDENTIALS, mAuthenticatedUser);

                            startActivity(intent);

                            finish();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                          Toast.makeText(getApplicationContext(),getString(R.string.api_not_connected),Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    // User is signed out
                    //Log.d(LOG_TAG, "onAuthStateChanged:signed_out");
                }

                updateUI(user);

            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();


        if (mAuthListener != null) {
            mAuth.addAuthStateListener(mAuthListener);
        }


    }

    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);

        }
    }

    /**
     * Activity Result from signIn()
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                updateUI(null);
            }
        }
        else if(requestCode==REQUEST_CHECK_SETTINGS)    {
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


    /**
     * Authenticate with google
     *
     * @param acct
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        showProgressDialog();
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {

                            Toast.makeText(GoogleSignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        hideProgressDialog();
                        //    initiateAuthentication();
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Sign out method
     */
    private void signOut() {
        mAuth.signOut();    // Firebase sign out
        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        updateUI(null);
                        Toast.makeText(GoogleSignInActivity.this, "You have been successfully signed out.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void revokeAccess() {
        // Firebase sign out
        mAuth.signOut();

        // Google revoke access
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        updateUI(null);
                    }
                });
    }

    /**
     * @param user
     */
    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
    }

    // An unresolvable error has occurred and Google APIs (including Sign-In) will not be available.
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    /**
     * Handle button click listener
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.sign_in_button) {
            if (Utility.checkNetworkState(this)) {
                signIn();
            } else {
                Toast.makeText(this, getString(R.string.login_network_unavailable), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * @param bundle
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {

            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            if (mLastLocation != null) {
                // Determine whether a Geocoder is available.
                mLatitude = mLastLocation.getLatitude();
                mLongitude = mLastLocation.getLongitude();
                getLocation();
                if (!Geocoder.isPresent()) {
                    Toast.makeText(this, getString(R.string.no_geocoder),
                            Toast.LENGTH_LONG).show();
                    return;
                } else {
                    getLocation();
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void getLocation()    {
        try {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = geocoder.getFromLocation(mLatitude, mLongitude, 1);
        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        if (address != null) {
            mPlayingLocation = mPlayingLocation.concat(" ").concat(address);
        }
        String city = addresses.get(0).getLocality();
        if (city != null) {
            mPlayingLocation = mPlayingLocation.concat(" ").concat(city);
        }
        String state = addresses.get(0).getAdminArea();
        if (state != null) {
            mPlayingLocation = mPlayingLocation.concat(" ").concat(state);
        }
        String country = addresses.get(0).getCountryName();
        if (country != null) {
            mPlayingLocation = mPlayingLocation.concat(" ").concat(country);
        }
        String postalCode = addresses.get(0).getPostalCode();
        if (postalCode != null) {
            mPlayingLocation = mPlayingLocation.concat(" ").concat(postalCode);
        }

    }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }


}
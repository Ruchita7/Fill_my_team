package com.android.fillmyteam.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;

import com.android.fillmyteam.R;
import com.android.fillmyteam.data.PlayerMatchesColumns;
import com.android.fillmyteam.data.SportsProvider;
import com.android.fillmyteam.model.Match;
import com.android.fillmyteam.util.Constants;
import com.android.fillmyteam.util.Utility;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

/**
 * Upcoming matches sync adapter
 */
public class SportsSyncAdapter extends AbstractThreadedSyncAdapter {


    // Interval at which to sync with the weather, in milliseconds.
// 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    private static final int INDEX_MATCH_ID = 0;

    public static final String ACTION_DATA_UPDATED =
            "com.android.fillmyteam.ACTION_DATA_UPDATED";


    @IntDef({STATUS_OK, STATUS_SERVER_DOWN, MATCH_STATUS_SERVER_INVALID, STATUS_UNKNOWN, MATCH_STATUS_INVALID})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MatchStatus {
    }


    public static final int STATUS_OK = 0;
    public static final int STATUS_SERVER_DOWN = 1;
    public static final int MATCH_STATUS_SERVER_INVALID = 2;
    public static final int STATUS_UNKNOWN = 3;
    public static final int MATCH_STATUS_INVALID = 4;


    public static final String LOG_TAG = SportsSyncAdapter.class.getSimpleName();

    public SportsSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {


        Context context = getContext();
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        String email = sharedPreferences.getString(Constants.EMAIL, "");


        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String matchJsonStr = null;


        try {

            Uri.Builder uriBuilder = Uri.parse(Constants.MATCH_URL).buildUpon().appendEncodedPath(Utility.encodeEmail(email) + ".json");
            Uri builtUri = uriBuilder.build();
            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();


            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                setNetworkState(getContext(), STATUS_SERVER_DOWN);
                return;
            }
            matchJsonStr = buffer.toString();
            getContext().getContentResolver().delete(SportsProvider.UpcomingMatches.CONTENT_URI, null, null);
            getMatchDataFromJson(matchJsonStr);
        }catch (UnknownHostException e) {
            setNetworkState(getContext(), STATUS_SERVER_DOWN);

            e.printStackTrace();
        } catch (MalformedURLException e) {
           setNetworkState(getContext(), MATCH_STATUS_SERVER_INVALID);

            e.printStackTrace();

        } catch (IOException e) {

            setNetworkState(getContext(),STATUS_SERVER_DOWN);
            return;
        } catch (JSONException e) {
            setNetworkState(getContext(), MATCH_STATUS_SERVER_INVALID);

            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {

                }
            }
        }

        return;
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

                            /*
         * Add the account and account type, no password or user data
        * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private void getMatchDataFromJson(String matchJsonStr)
            throws JSONException {


        final String PLAYER_EMAIL = "playerEmail";
        final String PLAYING_DATE = "playingDate";

        // Location coordinate
        final String LATITUDE = "latitude";
        final String LONGITUDE = "longitude";
        final String PLAYING_PLACE = "playingPlace";
        final String PLAYING_TIME = "playingTime";
        final String PLAYING_WITH = "playingWith";
        final String SPORT = "sport";


        try {
            JSONObject matchJson = new JSONObject(matchJsonStr);
            if (matchJson==null) {

               setNetworkState(getContext(),MATCH_STATUS_INVALID);
                return;
            }
            Iterator<String> matchIterator = matchJson.keys();
            List<Match> matchList = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.ENGLISH);
            String key = null;
            JSONObject jsonObj;
            Calendar currentCal = Calendar.getInstance();

            String playingDate = null;
            String playingTime = null;

            GregorianCalendar gcalendar = new GregorianCalendar();
            GregorianCalendar playingCal = new GregorianCalendar();
            int currentDay,matchDay;
            int currentMonth,matchMonth;
            int currentYear,matchYear;
            int currentHour,matchHour;
            int currentMin,matchMin;

            currentDay=gcalendar.get(Calendar.DATE);
            currentMonth=gcalendar.get(Calendar.MONTH)+1;
            currentYear=gcalendar.get(Calendar.YEAR);
            currentHour=gcalendar.get(Calendar.HOUR);
            currentMin=gcalendar.get(Calendar.MINUTE);

            DateTime currentDateTime=new DateTime(currentYear,currentMonth,currentDay,currentHour,currentMin,0);
            DateTime matchDateTime=null;
            int result;
            while (matchIterator.hasNext()) {
                key = matchIterator.next();
                jsonObj = matchJson.getJSONObject(key);
                try {

                    playingDate = jsonObj.getString(PLAYING_DATE);
                    playingTime = jsonObj.getString(PLAYING_TIME);

                    playingCal.setTime(sdf.parse(playingDate + " " + playingTime));
                    long date = playingCal.getTimeInMillis();
                    matchDay=playingCal.get(Calendar.DATE);
                    matchMonth=playingCal.get(Calendar.MONTH)+1;
                    matchYear=playingCal.get(Calendar.YEAR);
                    matchHour=playingCal.get(Calendar.HOUR);
                    matchMin=playingCal.get(Calendar.MINUTE);
                    matchDateTime=new DateTime(matchYear,matchMonth,matchDay,matchHour,matchMin,0);

                    result = DateTimeComparator.getInstance().compare(matchDateTime, currentDateTime);
                    if(result >=0)  {
                        Match obj = new Match();
                        obj.setPlayerEmail(jsonObj.getString(PLAYER_EMAIL));

                        obj.setLatitude(jsonObj.getDouble(LATITUDE));
                        obj.setLongitude(jsonObj.getDouble(LONGITUDE));
                        obj.setPlayingPlace(jsonObj.getString(PLAYING_PLACE));
                        obj.setPlayingTime(date);
                        obj.setSport(jsonObj.getString(SPORT));
                        obj.setPlayingWith(jsonObj.getString(PLAYING_WITH));
                        matchList.add(obj);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            Vector<ContentValues> cVVector = new Vector<ContentValues>(matchList.size());
            for (Match matchObj : matchList) {
                ContentValues matchValues = new ContentValues();
                matchValues.put(PlayerMatchesColumns.LATITUDE, matchObj.getLatitude());
                matchValues.put(PlayerMatchesColumns.LONGITUDE, matchObj.getLongitude());
                matchValues.put(PlayerMatchesColumns.PLAYER_NAME, matchObj.getPlayingWith());
                matchValues.put(PlayerMatchesColumns.PLAYING_TIME, matchObj.getPlayingTime());
                matchValues.put(PlayerMatchesColumns.PLAYING_SPORT, matchObj.getSport());
                matchValues.put(PlayerMatchesColumns.PLAYING_PLACE, matchObj.getPlayingPlace());
                matchValues.put(PlayerMatchesColumns.PLAYER_EMAIL, matchObj.getPlayerEmail());
                cVVector.add(matchValues);
            }

            if (cVVector.size() > 0) {

                ContentValues[] contentValuesArr = new ContentValues[cVVector.size()];
                cVVector.toArray(contentValuesArr);


                getContext().getContentResolver().bulkInsert(SportsProvider.UpcomingMatches.CONTENT_URI, contentValuesArr);
                updateWidgets();
            }
            setNetworkState(getContext(), STATUS_OK);
        } catch (JSONException e) {

            e.printStackTrace();
            setNetworkState(getContext(), MATCH_STATUS_SERVER_INVALID);
        }

    }


    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }


    private static void onAccountCreated(Account newAccount, Context context) {

        SportsSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }


    private void updateWidgets() {
        Context context = getContext();
        // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }

    private void setNetworkState(Context context, int matchStatus) {

        String syncStatus = getContext().getString(R.string.network_status_key);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(syncStatus, matchStatus);
        editor.commit();
    }

}

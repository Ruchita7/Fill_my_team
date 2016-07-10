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
import android.util.Log;

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
 * Created by dgnc on 12/26/2015.
 */
public class SportsSyncAdapter extends AbstractThreadedSyncAdapter {

    public static final String LOCATION_QUERY_EXTRA = "lqe";
    // Interval at which to sync with the weather, in milliseconds.
// 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    private static final int INDEX_MATCH_ID = 0;
    // these indices must match the projection
  /*  private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_MAX_TEMP = 1;
    private static final int INDEX_MIN_TEMP = 2;
    private static final int INDEX_SHORT_DESC = 3;
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int WEATHER_NOTIFICATION_ID = 3004;*/
    public static final String ACTION_DATA_UPDATED =
            "com.example.android.weather.ACTION_DATA_UPDATED";

 /*   @IntDef({STATUS_OK, STATUS_SERVER_DOWN, STATUS_SERVER_INVALID, STATUS_UNKNOWN, STATUS_INVALID})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Status {
    }
    public static final int STATUS_OK = 0;
    public static final int STATUS_SERVER_DOWN = 1;
    public static final int STATUS_SERVER_INVALID = 2;
    public static final int STATUS_UNKNOWN = 3;
    public static final int STATUS_INVALID = 4;*/


    @IntDef({STATUS_OK, STATUS_SERVER_DOWN, MATCH_STATUS_SERVER_INVALID, STATUS_UNKNOWN, MATCH_STATUS_INVALID})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MatchStatus {
    }


    public static final int STATUS_OK = 0;
    public static final int STATUS_SERVER_DOWN = 1;
    public static final int MATCH_STATUS_SERVER_INVALID = 2;
    public static final int STATUS_UNKNOWN = 3;
    public static final int MATCH_STATUS_INVALID = 4;

/*    private static final String[] SPORTS_PROJECTION = new String[]{
            PlayerMatchesColumns._ID,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC
    };*/

    public static final String LOG_TAG = SportsSyncAdapter.class.getSimpleName();

    public SportsSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        Log.d(LOG_TAG, "In onPerformSync");
        //    String locationQuery = Utility.getPreferredLocation(getContext());

        Context context = getContext();
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        String email = sharedPreferences.getString(Constants.EMAIL, "");

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;


        try {


            Uri.Builder uriBuilder = Uri.parse(Constants.MATCH_URL).buildUpon().appendEncodedPath(Utility.encodeEmail(email) + ".json");

            Uri builtUri = uriBuilder.build();

            URL url = new URL(builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
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
            forecastJsonStr = buffer.toString();
            getContext().getContentResolver().delete(SportsProvider.UpcomingMatches.CONTENT_URI, null, null);
            getMatchDataFromJson(forecastJsonStr);
        }catch (UnknownHostException e) {
            setNetworkState(getContext(), STATUS_SERVER_DOWN);
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
        } catch (MalformedURLException e) {
           setNetworkState(getContext(), MATCH_STATUS_SERVER_INVALID);
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            setNetworkState(getContext(),STATUS_SERVER_DOWN);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return;
        } catch (JSONException e) {
            setNetworkState(getContext(), MATCH_STATUS_SERVER_INVALID);
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        // This will only happen if there was an error getting or parsing the forecast.
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
                        /*
             * If you don't set android:syncable="true" in
            * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
            */
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
        //    Calendar playingCal = Calendar.getInstance();
            GregorianCalendar gcalendar = new GregorianCalendar();
            GregorianCalendar playingCal = new GregorianCalendar();
            int currentDay,matchDay;
            int currentMonth,matchMonth;
            int currentYear,matchYear;
            int currentHour,matchHour;
            int currentMin,matchMin;
         /*   try {
                String currentDateTime = Utility.getCurrentDate(gcalendar) + " " + Utility.getCurrentTime(gcalendar);
                currentCal.setTime(sdf.parse(currentDateTime));
            } catch (ParseException e) {
                e.printStackTrace();
            }*/
            currentDay=gcalendar.get(Calendar.DATE);
            currentMonth=gcalendar.get(Calendar.MONTH)+1;
            currentYear=gcalendar.get(Calendar.YEAR);
            currentHour=gcalendar.get(Calendar.HOUR);
            currentMin=gcalendar.get(Calendar.MINUTE);

            DateTime currentDateTime=new DateTime(currentYear,currentMonth,currentDay,currentHour,currentMin,0);
            DateTime matchDateTime=null;
            int result;
            while (matchIterator.hasNext()) {
                //in future insert only upcoming matches only compared by date
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
                   // if (playingCal.after(currentCal)) {
                    result = DateTimeComparator.getInstance().compare(matchDateTime, currentDateTime);
                    if(result >=0)  {
                        Match obj = new Match();
                        obj.setPlayerEmail(jsonObj.getString(PLAYER_EMAIL));
                   //   obj.setPlayingDate(playingDate);
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
                ContentValues weatherValues = new ContentValues();

                weatherValues.put(PlayerMatchesColumns.LATITUDE, matchObj.getLatitude());
                weatherValues.put(PlayerMatchesColumns.LONGITUDE, matchObj.getLongitude());
                weatherValues.put(PlayerMatchesColumns.PLAYER_NAME, matchObj.getPlayingWith());
          //      weatherValues.put(PlayerMatchesColumns.PLAYING_DATE, matchObj.getPlayingDate());
                weatherValues.put(PlayerMatchesColumns.PLAYING_TIME, matchObj.getPlayingTime());
                weatherValues.put(PlayerMatchesColumns.PLAYING_SPORT, matchObj.getSport());
                weatherValues.put(PlayerMatchesColumns.PLAYING_PLACE, matchObj.getPlayingPlace());
                weatherValues.put(PlayerMatchesColumns.PLAYER_EMAIL, matchObj.getPlayerEmail());
                cVVector.add(weatherValues);
            }

             /* if (forecastJson.has(OWM_MESSAGE_CODE)) {
                int errorCode = forecastJson.getInt(OWM_MESSAGE_CODE);

                switch (errorCode) {
                    case HttpURLConnection.HTTP_OK:
                        break;
                    case HttpURLConnection.HTTP_NOT_FOUND:
                        setNetworkState(getContext(), LOCATION_STATUS_INVALID);
                        return;
                    default:
                        setNetworkState(getContext(), LOCATION_STATUS_SERVER_DOWN);
                        return;
                }
            }
*/
            if (cVVector.size() > 0) {

                ContentValues[] contentValuesArr = new ContentValues[cVVector.size()];
                cVVector.toArray(contentValuesArr);


                getContext().getContentResolver().bulkInsert(SportsProvider.UpcomingMatches.CONTENT_URI, contentValuesArr);
                updateWidgets();
            }


              /*
                //   String choice=Utility.isNotificationOn(getContext());
                if (Utility.isNotificationOn(getContext())) {
                    notifyWeather();
                }
                updateMuzei();

            }
            Log.d(LOG_TAG, "Sunshine Service Complete. " + cVVector.size() + " Inserted");
            setNetworkState(getContext(), LOCATION_STATUS_OK);
*/

            setNetworkState(getContext(), STATUS_OK);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            setNetworkState(getContext(), MATCH_STATUS_SERVER_INVALID);
        }

        // return null;
    }





    /*long addLocation(String locationSetting, String cityName, double lat, double lon) {
        // Students: First, check if the location with this city name exists in the db
        // If it exists, return the current ID
        // Otherwise, insert it using the content resolver and the base URI

        long locationId = 0;

        Cursor cursor = getContext().getContentResolver().query(WeatherContract.LocationEntry.CONTENT_URI,
                new String[]{WeatherContract.LocationEntry._ID},
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[]{locationSetting},
                null
        );

        if (cursor.moveToFirst()) {
            int locationIndex = cursor.getColumnIndex(WeatherContract.LocationEntry._ID);
            locationId = cursor.getLong(locationIndex);
        } else {
            ContentValues contentValues = new ContentValues();
            contentValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
            contentValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
            contentValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, lat);
            contentValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, lon);

            Uri uri = getContext().getContentResolver().insert(WeatherContract.LocationEntry.CONTENT_URI, contentValues);
            locationId = ContentUris.parseId(uri);
            //  getContext().getContentResolver().notifyChange(uri, null);

        }
        cursor.close();

        return locationId;

    }
*/

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
        /*
         * Since we've created an account
         */
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

    /*private void notifyWeather() {
        Log.v(LOG_TAG, "In notifyweather");
        Context context = getContext();
        //checking the last update and notify if it' the first of the day
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String lastNotificationKey = context.getString(R.string.pref_last_notification);

        long lastSync = prefs.getLong(lastNotificationKey, 0);

        if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {
            // Last sync was more than 1 day ago, let's send a notification with the weather.
            String locationQuery = Utility.getPreferredLocation(context);

            Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationQuery, System.currentTimeMillis());

            // we'll query our contentProvider, as always
            Cursor cursor = context.getContentResolver().query(weatherUri, NOTIFY_WEATHER_PROJECTION, null, null, null);

            if (cursor.moveToFirst()) {
                int weatherId = cursor.getInt(INDEX_WEATHER_ID);
                double high = cursor.getDouble(INDEX_MAX_TEMP);
                double low = cursor.getDouble(INDEX_MIN_TEMP);
                String desc = cursor.getString(INDEX_SHORT_DESC);

                int iconId = Utility.getIconResourceForWeatherCondition(weatherId);
                int artResourceId = Utility.getArtResourceForWeatherCondition(weatherId);
                String artUrl = Utility.getArtUrlForWeatherCondition(context, weatherId);

                Resources resources = context.getResources();

                int largeIconWidth = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                        ? resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_width)
                        : resources.getDimensionPixelSize(R.dimen.notification_large_icon_default);

                int largeIconHeight = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                        ? resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_height)
                        : resources.getDimensionPixelSize(R.dimen.notification_large_icon_default);

                Bitmap largeIcon;
                try {
                    largeIcon = Glide.with(context).load(artUrl).asBitmap().error(artResourceId).fitCenter().into(largeIconWidth, largeIconHeight).get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    largeIcon = BitmapFactory.decodeResource(resources, artResourceId);

                }


                String title = context.getString(R.string.app_name);

                // Define the text of the forecast.
                String contentText = String.format(context.getString(R.string.format_notification),
                        desc,
                        Utility.formatTemperature(context, high),
                        Utility.formatTemperature(context, low));

                //build your notification here.
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(getContext())
                                .setSmallIcon(iconId).setLargeIcon(largeIcon)
                                .setContentTitle(title)
                                .setContentText(contentText);
// Creates an explicit intent for an Activity in your app
                Intent resultIntent = new Intent(getContext(), MainActivity.class);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(getContext());
// Adds the back stack for the Intent (but not the Intent itself)
                stackBuilder.addParentStack(MainActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                mBuilder.setContentIntent(resultPendingIntent);
                NotificationManager mNotificationManager =
                        (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.

                mNotificationManager.notify(WEATHER_NOTIFICATION_ID, mBuilder.build());

                //refreshing last sync
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong(lastNotificationKey, System.currentTimeMillis());

                editor.commit();
            }
        }

    }
*/
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

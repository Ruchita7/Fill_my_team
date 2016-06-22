package com.android.fillmyteam.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.android.fillmyteam.MatchesFragment;
import com.android.fillmyteam.R;
import com.android.fillmyteam.data.PlayerMatchesColumns;
import com.android.fillmyteam.data.SportsProvider;
import com.android.fillmyteam.util.Utility;

/**
 * Created by dgnc on 6/19/2016.
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {

    public final String LOG_TAG = DetailWidgetRemoteViewsService.class.getSimpleName();
   /* private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
    };*/
    // these indices must match the projection
  /*  static final int INDEX_WEATHER_ID = 0;
    static final int INDEX_WEATHER_DATE = 1;
    static final int INDEX_WEATHER_CONDITION_ID = 2;
    static final int INDEX_WEATHER_DESC = 3;
    static final int INDEX_WEATHER_MAX_TEMP = 4;
    static final int INDEX_WEATHER_MIN_TEMP = 5;*/

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;
            public static final int detail_match_id = 0;


            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                //      String email
                //  String location = Utility.getPreferredLocation(DetailWidgetRemoteViewsService.this);
                Uri weatherForLocationUri = SportsProvider.UpcomingMatches.CONTENT_URI;
                data = getContentResolver().query(weatherForLocationUri,
                        null,
                        //  PlayerMatchesColumns.PLAYER_EMAIL+"=?",
                        null, null,
                        PlayerMatchesColumns.PLAYING_DATE + " ASC");
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_list_item);

                String playerName = data.getString(MatchesFragment.COL_PLAYER_NAME);
                String matchDate = data.getString(MatchesFragment.COL_PLAYING_DATE) + " " + data.getString(MatchesFragment.COL_PLAYING_TIME);
                String playingLocation = data.getString(MatchesFragment.COL_PLAYING_PLACE);
                String playingSport = data.getString(MatchesFragment.COL_PLAYING_SPORT);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    setRemoteContentDescription(views, playerName);
                }
                views.setTextViewText(R.id.widget_player, playerName);
                views.setTextViewText(R.id.widget_place, playingLocation);
                views.setImageViewResource(R.id.widget_sport_icon, Utility.retrieveSportsIcon(playingSport));
                views.setTextViewText(R.id.widget_time, matchDate);

                final Intent fillInIntent = new Intent();
               /* String locationSetting =
                        Utility.getPreferredLocation(DetailWidgetRemoteViewsService.this);
                Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                        locationSetting,
                        dateInMillis);*/
                Uri sportUri = SportsProvider.UpcomingMatches.CONTENT_URI;
                fillInIntent.setData(sportUri);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String description) {
                views.setContentDescription(R.id.widget, description);
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(detail_match_id);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}

package com.sample.android.fillmyteam.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sample.android.fillmyteam.MatchesFragment;
import com.sample.android.fillmyteam.R;
import com.sample.android.fillmyteam.data.PlayerMatchesColumns;
import com.sample.android.fillmyteam.data.SportsProvider;
import com.sample.android.fillmyteam.util.Constants;
import com.sample.android.fillmyteam.util.Utility;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Upcoming matches widget
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {

    public final String LOG_TAG = DetailWidgetRemoteViewsService.class.getSimpleName();

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

                final long identityToken = Binder.clearCallingIdentity();
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);
                String currentDate = day + " " + Utility.months[month] + " " + year;
                 Uri weatherForLocationUri = SportsProvider.UpcomingMatches.CONTENT_URI;
                String sortOrder= PlayerMatchesColumns.PLAYING_TIME+ Constants.ASC_ORDER;
                data = getContentResolver().query(weatherForLocationUri,
                        null,
                        null,
                        null,
                        sortOrder);
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
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.ENGLISH);
                String playerName = data.getString(MatchesFragment.COL_PLAYER_NAME);
                long matchDate=data.getLong(MatchesFragment.COL_PLAYING_TIME);
                GregorianCalendar gregorianCalendar = new GregorianCalendar();
                gregorianCalendar.setTimeInMillis(matchDate);

                String displayTime =   Utility.getCurrentDate(gregorianCalendar)+" "+Utility.getCurrentTime(gregorianCalendar);
                String playingLocation = data.getString(MatchesFragment.COL_PLAYING_PLACE);
                String playingSport = data.getString(MatchesFragment.COL_PLAYING_SPORT);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    setRemoteContentDescription(views, playerName);
                }
                views.setTextViewText(R.id.widget_player, playerName);
                views.setTextViewText(R.id.widget_place, playingLocation);
                views.setImageViewResource(R.id.widget_sport_icon, Utility.retrieveSportsIcon(playingSport));
                views.setTextViewText(R.id.widget_playing_sport,playingSport);
                views.setTextViewText(R.id.widget_time, displayTime);

                final Intent fillInIntent = new Intent();

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

package com.android.fillmyteam.widget;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.android.fillmyteam.R;
import com.android.fillmyteam.model.Match;
import com.android.fillmyteam.util.Constants;
import com.android.fillmyteam.util.Utility;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dgnc on 6/20/2016.
 */
public class MatchRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory, ValueEventListener {

    List<Match> matches = new ArrayList<Match>();
    Context mContext;
    Intent mIntent;
    DatabaseReference matchRef;

    public MatchRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mIntent = intent;
    }

    @Override
    public void onCreate() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(mContext);
        String email = sharedPreferences.getString(Constants.EMAIL, "");
        matchRef = FirebaseDatabase.getInstance().getReferenceFromUrl(Constants.PLAYERS_MATCHES + "/" + Utility.encodeEmail(email));
        matchRef.addValueEventListener(this);
    }

    @Override
    public void onDataSetChanged() {
        matchRef.addValueEventListener(this);
    }

    @Override
    public void onDestroy() {
        matchRef.removeEventListener(this);
    }

    @Override
    public int getCount() {
        return matches.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews views = new RemoteViews(mContext.getPackageName(),
                R.layout.widget_detail_list_item);
        Match scheduledMatch = matches.get(position);
        views.setTextViewText(R.id.widget_place, scheduledMatch.getPlayingPlace());
        views.setTextViewText(R.id.widget_player, scheduledMatch.getPlayingWith());
        views.setTextViewText(R.id.widget_time, scheduledMatch.getPlayingDate() + " " + scheduledMatch.getPlayingTime());
        views.setImageViewResource(R.id.widget_sport_icon, Utility.retrieveSportsIcon(scheduledMatch.getSport()));
        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        Map<String, Object> objectMap = (HashMap<String, Object>) dataSnapshot.getValue();
        for (Object obj : objectMap.values()) {
            if (obj instanceof Map) {
                Map<String, Object> mapObj = (Map<String, Object>) obj;
                Match match = new Match();
                match.setPlayingTime((String) mapObj.get(Constants.PLAY_TIME));
                match.setPlayingDate((String) mapObj.get(Constants.PLAY_DATE));
                match.setPlayingPlace((String) mapObj.get(Constants.PLAYING_PLACE));
                match.setLatitude((Double) mapObj.get(Constants.LATITUDE));
                match.setLongitude((Double) mapObj.get(Constants.LONGITUDE));
                match.setSport((String) mapObj.get(Constants.SPORT));
                match.setPlayingWith((String) mapObj.get(Constants.PLAYING_WITH));
                match.setPlayerEmail((String) mapObj.get(Constants.PLAYER_EMAIL));
                matches.add(match);

            }
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}

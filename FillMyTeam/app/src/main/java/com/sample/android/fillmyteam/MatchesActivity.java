package com.sample.android.fillmyteam;


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.sample.android.fillmyteam.data.PlayerMatchesColumns;
import com.sample.android.fillmyteam.data.SportsProvider;
import com.sample.android.fillmyteam.model.Match;
import com.sample.android.fillmyteam.model.User;
import com.sample.android.fillmyteam.sync.SportsSyncAdapter;
import com.sample.android.fillmyteam.util.Constants;
import com.sample.android.fillmyteam.util.Utility;

import java.util.List;

public class MatchesActivity extends BaseOptionsActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    User mUser;
    RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    DatabaseReference mRef;
    List<Match> mUserMatches;
    public static final String LOG_TAG = MatchesActivity.class.getSimpleName();
    MatchAdapter matchAdapter;
    private int mChoiceMode;
    public static final int MATCH_LOADER_ID = 0;

    private int mPosition = RecyclerView.NO_POSITION;
    public static final int COL_MATCH_ID = 0;
    public static final int COL_LATITUDE = 1;
    public static final int COL_LONGITUDE = 2;
    public static final int COL_PLAYER_EMAIL = 3;
    public static final int COL_PLAYING_TIME = 4;
    public static final int COL_PLAYING_PLACE = 5;

    public static final int COL_PLAYER_NAME = 6;
    public static final int COL_PLAYING_SPORT = 7;
    public static final String SELECTED_MATCH = "selected_match";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matches);
        if (getIntent() != null) {
            mUser = (User) getIntent().getSerializableExtra(Constants.USER_DETAILS);
        }
     /*  TypedArray a = obtainStyledAttributes(this.getTheme().obtainStyledAttributes(), R.styleable.SportInfoFragment,
                +0, 0);
        mChoiceMode = a.getInt(R.styleable.MatchFragment_android_choiceMode, AbsListView.CHOICE_MODE_NONE);
        a.recycle();*/


        getSupportLoaderManager().initLoader(MATCH_LOADER_ID, null, this);
        TextView emptyTextView = (TextView) findViewById(R.id.recyclerview_empty_matches);
        SportsSyncAdapter.syncImmediately(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.matches_recycler_view);
        matchAdapter = new MatchAdapter(this, mUserMatches, new MatchAdapter.MatchAdapterOnClickHandler() {
            @Override
            public void itemClick(String location, MatchAdapter.ViewHolder viewHolder) {
                mPosition = viewHolder.getAdapterPosition();
                launchMap(location);
            }
        }, emptyTextView, mChoiceMode);
        mLayoutManager = new GridLayoutManager(this, 1);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(matchAdapter);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SELECTED_MATCH)) {

                mPosition = savedInstanceState.getInt(SELECTED_MATCH);
            }
            matchAdapter.onRestoreInstanceState(savedInstanceState);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        matchAdapter.swapCursor(null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        matchAdapter.swapCursor(data);
        updateEmptyView();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = PlayerMatchesColumns.PLAYING_TIME + Constants.ASC_ORDER;
        return new CursorLoader(this,
                SportsProvider.UpcomingMatches.CONTENT_URI,
                null,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        if (mPosition != RecyclerView.NO_POSITION) {
            outState.putInt(SELECTED_MATCH, mPosition);
            matchAdapter.onSaveInstanceState(outState);
        }

        super.onSaveInstanceState(outState);
    }

    public void launchMap(String matchLocation) {
        if (!Utility.checkNetworkState(this)) {
            Toast.makeText(this, R.string.no_network, Toast.LENGTH_SHORT).show();
        } else {
            String geoLocation = getString(R.string.geo_location, matchLocation);
            Uri geoIntentUri = Uri.parse(geoLocation);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, geoIntentUri);
            mapIntent.setPackage(Constants.GOOGLE_MAPS_PACKAGE);
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            }
        }
    }

    private void updateEmptyView() {
        if (matchAdapter.getItemCount() == 0) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            @SportsSyncAdapter.MatchStatus int state = Utility.getNetworkState(this);
            TextView textView = (TextView) findViewById(R.id.recyclerview_empty_matches);
            if (null != textView) {
                int message = R.string.list_unavailable;
                switch (state) {
                    case SportsSyncAdapter.STATUS_SERVER_DOWN:
                        message = R.string.empty_list_server_down;
                        break;
                    case SportsSyncAdapter.MATCH_STATUS_SERVER_INVALID:
                        message = R.string.empty_match_list_server_error;
                        break;
                    case SportsSyncAdapter.MATCH_STATUS_INVALID:
                        message = R.string.match_invalid_request_error;
                    default:
                        if (!Utility.checkNetworkState(this)) {
                            message = R.string.network_unavailable_match;
                        }
                        break;
                }
                textView.setText(message);
            }
        }
    }
}

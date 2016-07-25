package com.sample.android.fillmyteam;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Upcoming matches fragment
 * @author Ruchita_Maheshwary
 *
 */
public class MatchesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    User mUser;
    RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    DatabaseReference mRef;
    List<Match> mUserMatches;
    public static final String LOG_TAG = MatchesFragment.class.getSimpleName();
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
    public  static final String SELECTED_MATCH="selected_match";

    public static MatchesFragment newInstance(User user) {
        MatchesFragment fragment = new MatchesFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.USER_DETAILS, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUser = (User) getArguments().getSerializable(Constants.USER_DETAILS);
        }
    }

    @Override
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(activity, attrs, savedInstanceState);
        TypedArray a = activity.obtainStyledAttributes(attrs, R.styleable.SportInfoFragment,
                +0, 0);
        mChoiceMode = a.getInt(R.styleable.MatchFragment_android_choiceMode, AbsListView.CHOICE_MODE_NONE);
        a.recycle();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(MATCH_LOADER_ID, null, this);

        super.onActivityCreated(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mUserMatches = new ArrayList<>();
        View view = inflater.inflate(R.layout.match_fragment, container, false);
        TextView emptyTextView = (TextView) view.findViewById(R.id.recyclerview_empty_matches);
        SportsSyncAdapter.syncImmediately(getActivity());

        mRecyclerView = (RecyclerView) view.findViewById(R.id.matches_recycler_view);
        matchAdapter = new MatchAdapter(getActivity(), mUserMatches, new MatchAdapter.MatchAdapterOnClickHandler() {
            @Override
            public void itemClick(String location, MatchAdapter.ViewHolder viewHolder) {
                mPosition = viewHolder.getAdapterPosition();
               launchMap(location);
            }
        }, emptyTextView, mChoiceMode);
        mLayoutManager = new GridLayoutManager(getActivity(), 1);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(matchAdapter);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SELECTED_MATCH)) {

                mPosition = savedInstanceState.getInt(SELECTED_MATCH);
            }
            matchAdapter.onRestoreInstanceState(savedInstanceState);
        }
        return view;
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
        return new CursorLoader(getActivity(),
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
        if (!Utility.checkNetworkState(getActivity())) {
            Toast.makeText(getActivity(),R.string.no_network,Toast.LENGTH_SHORT).show();
        } else {
            String geoLocation = getString(R.string.geo_location, matchLocation);
            Uri geoIntentUri = Uri.parse(geoLocation);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, geoIntentUri);
            mapIntent.setPackage(Constants.GOOGLE_MAPS_PACKAGE);
            if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                getActivity().startActivity(mapIntent);
            }
        }
    }

    private void updateEmptyView() {
        if (matchAdapter.getItemCount() == 0) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            @SportsSyncAdapter.MatchStatus int state = Utility.getNetworkState(getActivity());
            TextView textView = (TextView) getView().findViewById(R.id.recyclerview_empty_matches);
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
                        if (!Utility.checkNetworkState(getActivity())) {
                            message = R.string.network_unavailable_match;
                        }
                        break;
                }
                textView.setText(message);
            }
        }
    }
   }

package com.android.fillmyteam;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.fillmyteam.data.PlayerMatchesColumns;
import com.android.fillmyteam.data.SportsProvider;
import com.android.fillmyteam.model.Match;
import com.android.fillmyteam.model.User;
import com.android.fillmyteam.sync.SportsSyncAdapter;
import com.android.fillmyteam.util.Constants;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dgnc on 6/19/2016.
 */
public class MatchesFragment extends Fragment implements   LoaderManager.LoaderCallbacks<Cursor> {

    User mUser;
    RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    DatabaseReference mRef;
    List<Match> mUserMatches;
    public static final String LOG_TAG = MatchesFragment.class.getSimpleName();
    MatchAdapter matchAdapter;

    public static final int MATCH_LOADER_ID=0;


    public  static final int COL_MATCH_ID = 0;
    public  static final int COL_LATITUDE = 1;
    public  static final int COL_LONGITUDE = 2;
    public  static final int COL_PLAYER_EMAIL = 3;
    public  static final int COL_PLAYING_TIME = 4;
    public  static final int COL_PLAYING_PLACE = 5;
  //  public  static final int COL_PLAYING_TIME = 6;
    public  static final int COL_PLAYER_NAME= 6;
    public  static final int COL_PLAYING_SPORT= 7;

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
         /*   mRef = FirebaseDatabase.getInstance()
                    .getReferenceFromUrl(Constants.PLAYERS_MATCHES);
            mRef = mRef.child("/" + Utility.encodeEmail(mUser.getEmail()));*/
        }
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
    //    mRef.addValueEventListener(this);
        SportsSyncAdapter.syncImmediately(getActivity());
        mRecyclerView = (RecyclerView) view.findViewById(R.id.matches_recycler_view);
        matchAdapter = new MatchAdapter(getContext(), mUserMatches);
        mLayoutManager = new GridLayoutManager(getContext(), 1);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(matchAdapter);
        return view;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        matchAdapter.swapCursor(null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        matchAdapter.swapCursor(data);
      /*  if (mPosition != RecyclerView.NO_POSITION) {
            mRecyclerView.smoothScrollToPosition(mPosition);
        }*/
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder= PlayerMatchesColumns.PLAYING_TIME+Constants.ASC_ORDER;
        return new CursorLoader(getActivity(),
                SportsProvider.UpcomingMatches.CONTENT_URI,
                null,
                null,
                null,
                sortOrder);
    }

    /* @Override
    public void onCancelled(DatabaseError databaseError) {

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
                mUserMatches.add(match);
            }
        }

        matchAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        if (mRef != null) {
            mRef.removeEventListener(this);
        }
        super.onDestroy();
    }*/
}

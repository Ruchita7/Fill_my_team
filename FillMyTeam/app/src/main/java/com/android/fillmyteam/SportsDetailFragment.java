package com.android.fillmyteam;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.fillmyteam.data.SportsColumns;
import com.android.fillmyteam.data.SportsProvider;
import com.android.fillmyteam.util.Constants;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A placeholder fragment containing a simple view.
 */
public class SportsDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>  {

    static final String DETAIL_TRANSITION_ANIMATION = "DTA";
    static final String DETAIL_URI = "URI";

    Uri mUri;
    public static final String LOG_TAG = SportsDetailFragment.class.getSimpleName();

    @BindView(R.id.sports_name_text)
    TextView mSportNameTextView;
    @BindView(R.id.objective_text)
    TextView mObjectiveTextView;
    @BindView(R.id.players_text)
    TextView mPlayersTextView;
    @BindView(R.id.rules_text)
    TextView mRulesTextView;
   @BindView(R.id.sport_poster)
   ImageView mSportsImageView;
   @BindView(R.id.playVideo)
   ImageView mVideoPlayImageView;

    String mVideoKey;
/*    @BindView(R.id.youtube_view)
    YouTubePlayerView youTubeView;*/


    String mSportId;
    public static final int DETAIL_LOADER = 0;

    public SportsDetailFragment() {
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public static SportsDetailFragment newInstance(String sportId) {
        SportsDetailFragment fragment = new SportsDetailFragment();
        Bundle args = new Bundle();
        args.putString("ID", sportId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (getArguments() != null) {
            mSportId = getArguments().getString("ID");
            Log.v(LOG_TAG, "sport id is ::" + mSportId);
        }


        View view = inflater.inflate(R.layout.fragment_sports_detail, container, false);
        mUri = SportsProvider.Sports.CONTENT_URI;
        ButterKnife.bind(this, view);
       /* TextView textView=(TextView)view.findViewById(R.id.txt1);
        textView.setText("in detail fragment with id"+sportId);*/
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
            String selectionClause= SportsColumns._ID+"=?";
            String[] selectionArgs = {""};
            selectionArgs[0] = mSportId;

            return new CursorLoader(getActivity(), mUri,
                    null,
                    selectionClause,
                    selectionArgs,
                    null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }
        if (data != null && data.moveToFirst()) {
            String name = data.getString(data.getColumnIndex(SportsColumns.SPORTS_NAME));

            String players = data.getString(data.getColumnIndex(SportsColumns.PLAYERS));
            String imageUrl = data.getString(data.getColumnIndex(SportsColumns.POSTER_IMAGE));
            String objective = data.getString(data.getColumnIndex(SportsColumns.OBJECTIVE));
            String rules = data.getString(data.getColumnIndex(SportsColumns.RULES));
            String videoUrl = data.getString(data.getColumnIndex(SportsColumns.VIDEO_URL));
            mVideoKey=videoUrl.substring(videoUrl.indexOf("=")+1,videoUrl.length());
            mObjectiveTextView.setText(objective);
            mPlayersTextView.setText(players);
            mRulesTextView.setText(rules);
            mSportNameTextView.setText(name);
        Picasso.with(getContext()).load(imageUrl).into(mSportsImageView);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @OnClick(R.id.playVideo)
    public void playVideo() {
       Intent intent = YouTubeStandalonePlayer.createVideoIntent(
                getActivity(), Constants.YOUTUBE_KEY, mVideoKey, 0, true, false);
        if (intent != null) {
            if (canResolveIntent(intent)) {
                startActivityForResult(intent, Constants.REQ_START_STANDALONE_PLAYER);
            } else {
                // Could not resolve the intent - must need to install or update the YouTube API service.
                YouTubeInitializationResult.SERVICE_MISSING
                        .getErrorDialog(getActivity(), Constants.REQ_RESOLVE_SERVICE_MISSING).show();
            }
        }
    }



    private boolean canResolveIntent(Intent intent) {
        List<ResolveInfo> resolveInfo = getActivity().getPackageManager().queryIntentActivities(intent, 0);
        return resolveInfo != null && !resolveInfo.isEmpty();
    }


}

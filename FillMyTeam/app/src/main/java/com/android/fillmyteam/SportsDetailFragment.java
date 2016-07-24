package com.android.fillmyteam;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.fillmyteam.data.SportsColumns;
import com.android.fillmyteam.data.SportsProvider;
import com.android.fillmyteam.model.SportParcelable;
import com.android.fillmyteam.util.Constants;
import com.android.fillmyteam.util.Utility;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Ruchita_Maheshwary
 *         Fragment for showing sports detail
 */
public class SportsDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    static final String DETAIL_TRANSITION_ANIMATION = "DTA";
    static final String DETAIL_URI = "URI";

    Uri mUri;
    public static final String LOG_TAG = SportsDetailFragment.class.getSimpleName();
    public static final String POSITION = "position";

   
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
    @BindView(R.id.video_thumbnail_imageView)
    ImageView mThumbnailImageView;
    String sportsName;
    String mVideoKey;
    Context mContext;
    CollapsingToolbarLayout collapsingToolbar;
    String mImageUrl;
    String mThumbnailUrl;
    String mSportId;
    public static final int DETAIL_LOADER = 0;
    ShareActionProvider mShareActionProvider;
    int mPosition;
    SportParcelable mSportParcelable;
    public static final String PLAYER_PARCELABLE = "player_object";

    public SportsDetailFragment() {
    }


    @Override
    public String toString() {
        return super.toString();
    }

    public static SportsDetailFragment newInstance(String sportId, int cursorPosition) {
        SportsDetailFragment fragment = new SportsDetailFragment();
        Bundle args = new Bundle();
        args.putString(Constants.SPORT_ID, sportId);
        args.putInt(POSITION, cursorPosition);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (getArguments() != null) {
            mSportId = getArguments().getString(Constants.SPORT_ID);
            //Log.v(LOG_TAG, "sport id is ::" + mSportId);
            mPosition = getArguments().getInt(POSITION);
        }

        mContext = getActivity();
        View view = inflater.inflate(R.layout.fragment_sports_detail, container, false);
           final Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar_layout);
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.ic_action_ic_arrow_back);
            toolbar.setNavigationContentDescription(getString(R.string.back_button));
            Menu menu = toolbar.getMenu();
            if (null != menu) menu.clear();
            toolbar.inflateMenu(R.menu.main);
            MenuItem menuItem = menu.findItem(R.id.share_action);

            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    getFragmentManager().popBackStackImmediate();
                }
            });
        }

        collapsingToolbar = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar);
        mUri = SportsProvider.Sports.CONTENT_URI;
        ButterKnife.bind(this, view);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(PLAYER_PARCELABLE)) {
                mSportParcelable = savedInstanceState.getParcelable(PLAYER_PARCELABLE);
            }
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        final ActionBar ab = ((MainActivity) getActivity()).getSupportActionBar();
        ab.hide();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
            String selectionClause = SportsColumns._ID + "=?";
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
            sportsName = data.getString(data.getColumnIndex(SportsColumns.SPORTS_NAME));
            if (collapsingToolbar != null) {
                collapsingToolbar.setTitle(sportsName);
            }
            String players = data.getString(data.getColumnIndex(SportsColumns.PLAYERS));
            mImageUrl = data.getString(data.getColumnIndex(SportsColumns.POSTER_IMAGE));
            String objective = data.getString(data.getColumnIndex(SportsColumns.OBJECTIVE));
            String rules = data.getString(data.getColumnIndex(SportsColumns.RULES));
            mVideoKey = data.getString(data.getColumnIndex(SportsColumns.VIDEO_URL));
            Uri.Builder builder = Uri.parse(Constants.IMAGE_THUMBNAIL).buildUpon().appendPath(mVideoKey).appendPath(Constants.DEFAULT_IMG);
            mThumbnailUrl = builder.toString();
            mSportParcelable = new SportParcelable(mSportId, sportsName, objective, players, rules, mThumbnailUrl, mImageUrl, mVideoKey);
            mObjectiveTextView.setText(objective);
            mPlayersTextView.setText(players);
            mRulesTextView.setText(rules);
            Picasso.with(getActivity()).load(mImageUrl).into(mSportsImageView);
            mSportsImageView.setContentDescription(sportsName);
            Picasso.with(getActivity()).load(mThumbnailUrl).into(mThumbnailImageView, new Callback() {
                @Override
                public void onSuccess() {
                    if (Utility.checkNetworkState(getActivity())) {
                        mVideoPlayImageView.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onError() {

                }
            });

            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createSharedIntent());
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private Intent createSharedIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.learn_to_play, sportsName));
        String videoUrl = Uri.parse(Constants.YOUTUBE_URL).buildUpon().appendQueryParameter(Constants.VIDEO_REF, mVideoKey).build().toString();
        String intentText = getString(R.string.learn_to_play, sportsName) + "\n" + mImageUrl + "\n\n" +
                mPlayersTextView.getText() + "\n\n" + videoUrl + "\n\n" + mObjectiveTextView.getText() + "\n\n" +
                mRulesTextView.getText();
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                intentText);
        return shareIntent;
    }


    @OnClick(R.id.video_thumbnail_imageView)
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

    @Override
    public void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Log.v(LOG_TAG, "In on activity result");
        if (requestCode == Constants.REQ_START_STANDALONE_PLAYER && resultCode != Activity.RESULT_OK) {
            YouTubeInitializationResult errorReason =
                    YouTubeStandalonePlayer.getReturnedInitializationResult(data);
            if (errorReason.isUserRecoverableError()) {
                errorReason.getErrorDialog(getActivity(), 0).show();
            } else {
                String errorMessage =
                        String.format(getString(R.string.error_player), errorReason.toString());
                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
            }
        }
    }


    private boolean canResolveIntent(Intent intent) {
        List<ResolveInfo> resolveInfo = getActivity().getPackageManager().queryIntentActivities(intent, 0);
        return resolveInfo != null && !resolveInfo.isEmpty();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        if (mSportParcelable != null) {
            outState.putParcelable(PLAYER_PARCELABLE, mSportParcelable);
        }
        super.onSaveInstanceState(outState);
    }
}

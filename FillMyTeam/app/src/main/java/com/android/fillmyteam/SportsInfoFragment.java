package com.android.fillmyteam;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.TransitionInflater;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.fillmyteam.api.RestService;
import com.android.fillmyteam.data.SportsColumns;
import com.android.fillmyteam.data.SportsProvider;
import com.android.fillmyteam.model.SportParcelable;
import com.android.fillmyteam.model.SportsResult;
import com.android.fillmyteam.util.Constants;
import com.android.fillmyteam.util.Utility;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;


/**
 * @author Ruchita_Maheshwary
 *         This Fragment provides listing of Sports
 */
public class SportsInfoFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mLocation;
    private String mParam2;
    double mLatitude;
    double mLongitude;

    private RecyclerView mRecyclerView;
    SportsInfoAdapter mAdapter;
   
    private StaggeredGridLayoutManager mLayoutManager;

    static final int COL_SPORT_ID = 0;
    static final int COL_SPORT_NAME = 1;
    static final int COL_SPORT_OBJECTIVE = 2;
    static final int COL_SPORT_PLAYERS = 3;
    static final int COL_SPORT_RULES = 4;
    static final int COL_SPORT_THUMBNAIL = 5;
    static final int COL_SPORT_POSTER_IMAGE = 6;
    static final int COL_SPORT_VIDEO_URL = 7;

    private static final int MY_SPORTS_LOADER_ID = 0;
    public static final String LOG_TAG = SportsInfoFragment.class.getSimpleName();
    private boolean mAutoSelectView;
    private int mChoiceMode;
    private int mPosition = RecyclerView.NO_POSITION;
    private static final String SELECTED_KEY = "selected_position";

    ProgressBar mProgressBar;
    SportsInfoFragment mFragment;

    public SportsInfoFragment() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SportsInfoFragment.
     */
    public static SportsInfoFragment newInstance(double latitude, double longitude) {
        SportsInfoFragment fragment = new SportsInfoFragment();
        Bundle args = new Bundle();
        args.putDouble(Constants.LATITUDE, latitude);
        args.putDouble(Constants.LONGITUDE, longitude);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLatitude = getArguments().getDouble(Constants.LATITUDE);
            mLongitude = getArguments().getDouble(Constants.LONGITUDE);
        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(MY_SPORTS_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(activity, attrs, savedInstanceState);
        TypedArray a = activity.obtainStyledAttributes(attrs, R.styleable.SportInfoFragment,
                +0, 0);
        mChoiceMode = a.getInt(R.styleable.SportInfoFragment_android_choiceMode, AbsListView.CHOICE_MODE_NONE);
        mAutoSelectView = a.getBoolean(R.styleable.SportInfoFragment_autoSelectView, false);
        a.recycle();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sports_info, container, false);
        int columnCount = getResources().getInteger(R.integer.column_count);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.sports_info_recycler_view);

        mLayoutManager = new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        TextView textView = (TextView) view.findViewById(R.id.recyclerview_sports_empty);
        mProgressBar = (ProgressBar) view.findViewById(R.id.loading_indicator);
        mProgressBar.setVisibility(View.VISIBLE);
        mFragment = this;
        mAdapter = new SportsInfoAdapter(getActivity(), new SportsInfoAdapter.SportsAdapterOnClickHandler() {
            @Override
            public void itemClick(String sportId, SportsInfoAdapter.InfoViewHolder viewHolder) {
                mPosition = viewHolder.getAdapterPosition();
                Fragment nextFrag = (SportsDetailFragment) SportsDetailFragment.newInstance(sportId, mPosition);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
   
                    setExitTransition(new Fade());

                    nextFrag.setEnterTransition(new Explode());
                    nextFrag.setSharedElementEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.shared_photo));
                    setSharedElementReturnTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.shared_photo));
                    setReenterTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.grid_reenter));
                }
                getFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, nextFrag)
                        .addToBackStack(nextFrag.getClass().getSimpleName())
                        .addSharedElement(viewHolder.sportsImage, viewHolder.sportsImage.getTransitionName())
                        .commit();

            }
        }, textView, mProgressBar, mChoiceMode);
        mRecyclerView.setAdapter(mAdapter);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SELECTED_KEY)) {
                mPosition = savedInstanceState.getInt(SELECTED_KEY);
            }
            mAdapter.onRestoreInstanceState(savedInstanceState);
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        final ActionBar ab = ((MainActivity) getActivity()).getSupportActionBar();
        ab.show();
   
    }



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String sortOrder = SportsColumns._ID + Constants.ASC_ORDER;
        return new CursorLoader(getActivity(),
                SportsProvider.Sports.CONTENT_URI,
                null,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() == 0) {
        /*    SportsAsyncTask asyncTask = new SportsAsyncTask(getActivity(), mFragment);
            asyncTask.execute();*/
            retrieveSportsList();
        } else {
            if (!Utility.checkNetworkState(getActivity())) {
                updateEmptyView();
                return;
            }
            mAdapter.swapCursor(data);
        }
    }

    private void retrieveSportsList()   {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.SPORTS_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RestService service =  retrofit.create(RestService.class);

        Call<SportsResult> call = service.retrieveSportsDetails("14b15");
            call.enqueue(new Callback<SportsResult>() {
                @Override
                public void onResponse(Response<SportsResult> response, Retrofit retrofit) {
                    SportsResult sportsList = response.body();
                    String  sportsKey = getActivity().getString(R.string.sports_detail);
                   // List<SportParcelable> sportsList = response.body();
                    if(sportsList==null)  {
                        ResponseBody responseErrBody = response.errorBody();
                      //  response.code()

                        if (responseErrBody != null) {

                            try {
                                Utility.setNetworkState(getActivity(), response.code(), sportsKey);
                                String str = responseErrBody.string();
                            } catch (IOException e) {
                                Log.e(LOG_TAG, e.getMessage());
                                return;
                            }
                        }
                    }
                 List<SportParcelable> sportParcelables =sportsList.getList();
                    insertValues(sportParcelables);
                }


                @Override
                public void onFailure(Throwable t) {
                    Log.e(LOG_TAG, t.getMessage());
                }
            });

         //   Response<ResponseBody> response = service.retrieveSportsDetails("432j9").execute();
        }

    private void insertValues(List<SportParcelable> sportParcelables) {
        for(SportParcelable sportParcelable : sportParcelables) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(SportsColumns.SPORTS_NAME, sportParcelable.getName());
            contentValues.put(SportsColumns.OBJECTIVE, sportParcelable.getObjective());
            contentValues.put(SportsColumns.PLAYERS, sportParcelable.getPlayers());
            contentValues.put(SportsColumns.RULES, sportParcelable.getRules());
            contentValues.put(SportsColumns.THUMBNAIL, sportParcelable.getThumbnail());
            contentValues.put(SportsColumns.POSTER_IMAGE, sportParcelable.getImage());
            contentValues.put(SportsColumns.VIDEO_URL, sportParcelable.getVideo_reference());
            getActivity().getContentResolver().insert(SportsProvider.Sports.CONTENT_URI, contentValues);
        }
    }

    public void updateEmptyView() {
        mProgressBar.setVisibility(View.INVISIBLE);
        String sportsKey = getActivity().getString(R.string.sports_detail);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int state = Utility.getNetworkState(getActivity(), sportsKey);
        TextView textView = (TextView) getView().findViewById(R.id.recyclerview_sports_empty);
        textView.setVisibility(View.VISIBLE);
        if (null != textView) {
            int message = R.string.sports_list_unavailable;
            switch (state) {
               /* case SportsAsyncTask.SPORTS_STATUS_SERVER_DOWN:
                    message = R.string.empty_sports_list_server_down;
                    break;
                case SportsAsyncTask.SPORTS_INFO_STATUS_SERVER_INVALID:
                    message = R.string.empty_sports_list_server_error;
                    break;
                case SportsAsyncTask.SPORTS_INFO_STATUS_INVALID:
                    message = R.string.invalid_information_error;*/
                 case HttpURLConnection.HTTP_BAD_REQUEST:
                    message = R.string.empty_sports_list_server_down;
                    break;
                case HttpURLConnection.HTTP_INTERNAL_ERROR:
                    message = R.string.empty_sports_list_server_error;
                    break;
                case HttpURLConnection.HTTP_NO_CONTENT :
                    message = R.string.invalid_information_error;
                default:
                    if (!Utility.checkNetworkState(getActivity())) {
                        message = R.string.network_unavailable;
                    }
                    break;
            }
            textView.setText(message);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        if (mPosition != RecyclerView.NO_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
            mAdapter.onSaveInstanceState(outState);
        }

        super.onSaveInstanceState(outState);
    }

}



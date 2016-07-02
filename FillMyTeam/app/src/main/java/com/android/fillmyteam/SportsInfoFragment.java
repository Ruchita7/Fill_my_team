package com.android.fillmyteam;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.fillmyteam.data.SportsColumns;
import com.android.fillmyteam.data.SportsProvider;
import com.android.fillmyteam.util.Constants;


/**
 * @author Ruchita_Maheshwary
 * This Fragment provides listing of Sports
 *
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
    private RecyclerView.LayoutManager mLayoutManager;

    static final int COL_SPORT_ID = 0;
    static final int COL_SPORT_NAME = 1;
    static final int COL_SPORT_OBJECTIVE = 2;
    static final int COL_SPORT_PLAYERS = 3;
    static final int COL_SPORT_RULES = 4;
    static final int COL_SPORT_THUMBNAIL = 5;
    static final int COL_SPORT_POSTER_IMAGE = 6;
    static final int COL_SPORT_VIDEO_URL= 7;

    private static final int MY_SPORTS_LOADER_ID = 0;
    public static final String LOG_TAG=SportsInfoFragment.class.getSimpleName();
    private boolean mAutoSelectView;
    private int mChoiceMode;
    private int mPosition = RecyclerView.NO_POSITION;

    ProgressBar mProgressBar;

    public SportsInfoFragment() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SportsInfoFragment.
     */
    public static SportsInfoFragment newInstance(double latitude,double longitude)  {
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

        View view= inflater.inflate(R.layout.fragment_sports_info, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.sports_info_recycler_view);

        mLayoutManager = new GridLayoutManager(getActivity(),1);
        mRecyclerView.setLayoutManager(mLayoutManager);
        TextView textView = (TextView) view.findViewById(R.id.recyclerview_forecast_empty);
        mProgressBar = (ProgressBar) view.findViewById(R.id.loading_indicator);
        mProgressBar.setVisibility(View.VISIBLE);

        mAdapter = new SportsInfoAdapter(getActivity(), new SportsInfoAdapter.SportsAdapterOnClickHandler() {
            @Override
            public void itemClick(String sportId, SportsInfoAdapter.InfoViewHolder viewHolder) {
                mPosition = viewHolder.getAdapterPosition();
                Fragment nextFrag = (SportsDetailFragment) SportsDetailFragment.newInstance(sportId,mPosition);
                getFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, nextFrag)
                        .addToBackStack(null)
                        .commit();

            }
        }, textView, mProgressBar,mChoiceMode);
        mRecyclerView.setAdapter(mAdapter);
      /*  if(getActivity().getIntent()!=null)
        {
            if(getActivity().getIntent().hasExtra(SportsDetailFragment.POSITION))
            {
                int position = getActivity().getIntent().getIntExtra(SportsDetailFragment.POSITION,RecyclerView.NO_POSITION);
                mRecyclerView.scrollToPosition(position);
            }
        }*/
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

        String sortOrder= SportsColumns._ID+Constants.ASC_ORDER;
        return new CursorLoader(getActivity(),
                SportsProvider.Sports.CONTENT_URI,
                null,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.getCount()==0)
        {
            SportsAsyncTask asyncTask =new SportsAsyncTask(getActivity());
            asyncTask.execute();
        }
        else
        {
            mAdapter.swapCursor(data);
        }
     //   mProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}



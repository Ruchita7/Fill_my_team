package com.android.fillmyteam;

import android.app.Activity;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;

import com.android.fillmyteam.data.SportsColumns;
import com.android.fillmyteam.data.SportsProvider;
import com.android.fillmyteam.util.Constants;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SportsInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SportsInfoFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
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
    public SportsInfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *

     * @return A new instance of fragment SportsInfoFragment.
     */
/*    // TODO: Rename and change types and number of parameters
    public static SportsInfoFragment newInstance(String param1, String param2) {
        SportsInfoFragment fragment = new SportsInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }*/

    public static SportsInfoFragment newInstance(double latitude,double longitude)  {
        SportsInfoFragment fragment = new SportsInfoFragment();
        Bundle args = new Bundle();
        /*args.putDouble(ARG_PARAM1, latitude);
        args.putDouble(ARG_PARAM2, longitude); */
        args.putDouble(Constants.LATITUDE, latitude);
        args.putDouble(Constants.LONGITUDE, longitude);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
         /*   mLatitude = getArguments().getDouble(ARG_PARAM1);
            mLongitude = getArguments().getDouble(ARG_PARAM2);  */
            mLatitude = getArguments().getDouble(Constants.LATITUDE);
            mLongitude = getArguments().getDouble(Constants.LONGITUDE);
         //   mParam2 = getArguments().getString(ARG_PARAM2);
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
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_sports_info, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.sports_info_recycler_view);

        mLayoutManager = new GridLayoutManager(getContext(),1);
        mRecyclerView.setLayoutManager(mLayoutManager);
        TextView textView = (TextView) view.findViewById(R.id.recyclerview_forecast_empty);
        // specify an adapter (see also next example)
        mAdapter = new SportsInfoAdapter(getActivity(), new SportsInfoAdapter.SportsAdapterOnClickHandler() {
            @Override
            public void itemClick(String sportId, SportsInfoAdapter.InfoViewHolder viewHolder) {
         //       String locationSetting = Utility.getPreferredLocation(getActivity());


               // ((Callback) getActivity()).onItemSelected(SportsProvider.buildUri(sportId), viewHolder);
                ((Callback) getActivity()).onItemSelected(sportId, viewHolder);
                mPosition = viewHolder.getAdapterPosition();
            }
        }, textView, mChoiceMode);
        mRecyclerView.setAdapter(mAdapter);

/*
        Button button = (Button)view.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SportsAsyncTask asyncTask =new SportsAsyncTask(getContext());
                asyncTask.execute();
            }
        });
*/

        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        //String sortOrder= SportsColumns._ID+" ASC";
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
            SportsAsyncTask asyncTask =new SportsAsyncTask(getContext());
            asyncTask.execute();
          //  getLoaderManager().restartLoader(MY_SPORTS_LOADER_ID, null, this);
        }
        else
        {
            mAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(String id, SportsInfoAdapter.InfoViewHolder vh);
    }

}



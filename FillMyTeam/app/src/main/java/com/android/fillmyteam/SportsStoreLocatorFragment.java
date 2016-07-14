package com.android.fillmyteam;


import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.fillmyteam.api.StoreDataReceivedListener;
import com.android.fillmyteam.model.StoreLocatorParcelable;
import com.android.fillmyteam.util.Constants;
import com.android.fillmyteam.util.Utility;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;


/**
 * Fragment for finding nearby sports stores
 * @author Ruchita_Maheshwary
 *
 */
public class SportsStoreLocatorFragment extends Fragment implements StoreDataReceivedListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, AdapterView.OnItemClickListener {

    double mLatitude;
    double mLongitude;

    GoogleApiClient mGoogleApiClient;
    AutoCompleteTextView mAutocompleteView;
    private PlaceAutocompleteAdapter mAdapter;
    private TextView mPlaceDetailsText;
    SportsStoreLocatorFragment mFragment;
    
    String mPlaceId;
    ListView mListView;
    private RecyclerView.Adapter mRecyclerViewAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    List<StoreLocatorParcelable> mStoreLocatorParcelables;
    StoreLocatorAdapter mStoreLocatorAdapter;
    public static final String LOG_TAG = SportsStoreLocatorFragment.class.getSimpleName();
    public static final String SEARCHED_STORE = "searched_store";
    public static final String SELECTED_ITEM = "selected_item";
    int mIndex;
    TextView emptyList;


    public SportsStoreLocatorFragment() {
        // Required empty public constructor
    }

     public static SportsStoreLocatorFragment newInstance(double latitude, double longitude) {
        SportsStoreLocatorFragment fragment = new SportsStoreLocatorFragment();
        Bundle args = new Bundle();
        args.putDouble(Constants.LATITUDE, latitude);
        args.putDouble(Constants.LONGITUDE, longitude);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Places.GEO_DATA_API)
                .build();
 
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (getArguments() != null) {
            mLatitude = getArguments().getDouble(Constants.LATITUDE);
            mLongitude = getArguments().getDouble(Constants.LONGITUDE);
        }
        mFragment = this;
        View view = inflater.inflate(R.layout.fragment_sports_store_locator, container, false);
        mPlaceDetailsText = (TextView) view.findViewById(R.id.place_details);
        mAutocompleteView = (AutoCompleteTextView) view.
                findViewById(R.id.autocomplete_places);

        emptyList = (TextView) view.findViewById(R.id.listview_store_empty);
        mListView = (ListView) view.findViewById(R.id.store_locator_list_view);
        ImageView imageView = (ImageView) view.findViewById(R.id.powered_by_google);

        if (!Utility.checkNetworkState(getActivity())) {
            mAutocompleteView.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.INVISIBLE);
            mListView.setVisibility(View.INVISIBLE);
            emptyList.setVisibility(View.VISIBLE);
        } else {
            mAutocompleteView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    //Log.v(LOG_TAG, "in onEditorAction");
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        Utility.hideSoftKeyboard(getActivity());
                        //   mAutocompleteView.setOnItemClickListener(mAutocompleteClickListener);
                        return true;
                    }
                    return false;
                }
            });

            mAutocompleteView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    //Log.v(LOG_TAG, "in onItemSelected");
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    //Log.v(LOG_TAG, "in onNothingSelected");
                }
            });
            // Register a listener that receives callbacks when a suggestion has been selected
            mAutocompleteView.setOnItemClickListener(mAutocompleteClickListener);

            // Set up the adapter that will retrieve suggestions from the Places Geo Data API that cover
            // the entire world.
            mAdapter = new PlaceAutocompleteAdapter(getActivity(), mGoogleApiClient,
                    null, mFragment);
            mAutocompleteView.setAdapter(mAdapter);

            mLayoutManager = new LinearLayoutManager(getActivity());
            mStoreLocatorParcelables = new ArrayList<StoreLocatorParcelable>();
            mListView.setOnItemClickListener(this);
        }

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SEARCHED_STORE)) {
                mPlaceId = savedInstanceState.getString(SEARCHED_STORE);
                if (mPlaceId != null) {
                    PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                            .getPlaceById(mGoogleApiClient, mPlaceId);
                    placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
                    if (savedInstanceState.containsKey(SELECTED_ITEM)) {
                        mIndex = savedInstanceState.getInt(SELECTED_ITEM);
                     }
                }
            }
        }
        return view;
    }

    @Override
    public void retrieveStoresList(List<StoreLocatorParcelable> storeLocatorParcelables, int status) {
        //Log.v(LOG_TAG, "sports list size" + storeLocatorParcelables.size());
        mStoreLocatorParcelables = storeLocatorParcelables;
        mStoreLocatorAdapter = new StoreLocatorAdapter(getActivity(), 0, mStoreLocatorParcelables);
        mListView.setAdapter(mStoreLocatorAdapter);
        mStoreLocatorAdapter.notifyDataSetChanged();

        if (mStoreLocatorAdapter != null && mStoreLocatorAdapter.getCount() != 0) {
            if (mIndex != 0) {
                View v = mListView.getChildAt(0);
                int top = (v == null) ? 0 : (v.getTop() - mListView.getPaddingTop());
                mListView.setSelectionFromTop(mIndex, top);
            }
        } else {
            updateEmptyView(status);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }



     /**
     * Listener that handles selections from suggestions from the AutoCompleteTextView that
     * displays Place suggestions.
     * Gets the place id of the selected item and issues a request to the Places Geo Data API
     * to retrieve more details about the place.
     *
     * @see com.google.android.gms.location.places.GeoDataApi#getPlaceById(GoogleApiClient,
     * String...)
     */
    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a AutocompletePrediction from which we
             read the place ID and title.
              */
            //Log.v(LOG_TAG, "in onItemClick");
            emptyList.setVisibility(View.GONE);
            final AutocompletePrediction item = mAdapter.getItem(position);
            mPlaceId = item.getPlaceId();
            final CharSequence primaryText = item.getPrimaryText(null);

            //Log.i(LOG_TAG, "Autocomplete item selected: " + primaryText);
            Utility.hideSoftKeyboard(getActivity());

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
             details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, mPlaceId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

            //Log.i(LOG_TAG, "Called getPlaceById to get Place details for " + mPlaceId);
        }
    };

    /**
     * Callback for results from a Places Geo Data API query that shows the first place result in
     * the details view on screen.
     */
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            //Log.v(LOG_TAG, "in on result");
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                //Log.e(LOG_TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }
            // Get the Place object from the buffer.
            emptyList.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
            final Place place = places.get(0);
            LatLng placeLatLng = place.getLatLng();
            String placeName = place.getName().toString();
            String latitude = placeLatLng.latitude + "," + placeLatLng.longitude;
            StoreLocatorAsyncTask storeLocatorAsyncTask = new StoreLocatorAsyncTask(getActivity(), mFragment);
            storeLocatorAsyncTask.execute(latitude);
            //Log.i(LOG_TAG, "Place details received: " + place.getName());

            places.release();
        }
    };

/*    private static Spanned formatPlaceDetails(Resources res, CharSequence name, String id,
                                              CharSequence address, CharSequence phoneNumber, Uri websiteUri) {
        Log.e(LOG_TAG, res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));
        return Html.fromHtml(res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));

    }*/

    /**
     * Called when the Activity could not connect to Google Play services and the auto manager
     * could resolve the error automatically.
     * In this case the API is not available and notify the user.
     *
     * @param connectionResult can be inspected to determine the cause of the failure
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

      //  Log.e(LOG_TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
        Toast.makeText(getActivity(),
                getString(R.string.google_api_client_connect_error, connectionResult.getErrorCode()),
                Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onConnectionSuspended(int i) {
       // Log.e(LOG_TAG, "onConnectionSuspended: ConnectionResult.getErrorCode() = " + i);
        Toast.makeText(getActivity(),
                getString(R.string.google_api_client_connect_error),
                Toast.LENGTH_SHORT).show();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        StoreLocatorParcelable storeLocatorParcelable = (StoreLocatorParcelable) parent.getItemAtPosition(position);
        final double latitude = storeLocatorParcelable.getLatitude();
        final double longitude = storeLocatorParcelable.getLongitude();
        final String storeName = storeLocatorParcelable.getName();
        final String address = storeLocatorParcelable.getAddress();
        String geoLocation = getString(R.string.geo_location, storeName + address);

        Uri geoIntentUri = Uri.parse(geoLocation);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, geoIntentUri);
        mapIntent.setPackage(Constants.GOOGLE_MAPS_PACKAGE);
        if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            getActivity().startActivity(mapIntent);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mPlaceId != null) {
            outState.putString(SEARCHED_STORE, mPlaceId);
            mIndex = mListView.getFirstVisiblePosition();

            outState.putInt(SELECTED_ITEM, mIndex);
        }
    }

    public void updateEmptyView(int statusCode) {
        int message = 0;
        switch (statusCode) {
            case CommonStatusCodes.API_NOT_CONNECTED:
                message = R.string.api_not_connected;
                break;
            case CommonStatusCodes.CANCELED:
            case CommonStatusCodes.ERROR:
                message = R.string.location_error;
                break;

            case CommonStatusCodes.NETWORK_ERROR:
                message = R.string.location_data_unavailable;
                break;
            case CommonStatusCodes.TIMEOUT:
                message = R.string.timeout;
                break;

            case PlaceAutocompleteAdapter.NO_RESULTS_FOUND:
                message = R.string.no_location_found;
                break;

            case StoreLocatorAsyncTask.STORE_STATUS_INVALID:
                message = R.string.invalid_request_error;
                break;


            case StoreLocatorAsyncTask.STORE_STATUS_SERVER_DOWN:
                message = R.string.empty_store_list_server_down;
                break;

            case StoreLocatorAsyncTask.STORE_STATUS_SERVER_INVALID:
                message = R.string.empty_store_list_server_error;
                break;
            default:
                if (!Utility.checkNetworkState(getActivity())) {
                    message = R.string.store_network_unavailable;
                }

        }
        if (message != 0) {
            emptyList.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.INVISIBLE);
            emptyList.setText(message);
        }
    }
}

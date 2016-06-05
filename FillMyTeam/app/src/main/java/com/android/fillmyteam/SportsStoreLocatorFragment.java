package com.android.fillmyteam;


import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.fillmyteam.model.StoreLocatorParcelable;
import com.android.fillmyteam.util.Constants;
import com.android.fillmyteam.api.StoreDataReceivedListener;
import com.google.android.gms.common.ConnectionResult;
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
 * A simple {@link Fragment} subclass.
 */
public class SportsStoreLocatorFragment extends Fragment implements StoreDataReceivedListener,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    double mLatitude;
    double mLongitude;

    GoogleApiClient mGoogleApiClient;
    AutoCompleteTextView mAutocompleteView;
    private PlaceAutocompleteAdapter mAdapter;
    private TextView mPlaceDetailsText;
    SportsStoreLocatorFragment mFragment;
   // private TextView mPlaceDetailsAttribution;

    ListView mListView;
    private RecyclerView.Adapter mRecyclerViewAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    List<StoreLocatorParcelable> mStoreLocatorParcelables;
    StoreLocatorAdapter mStoreLocatorAdapter;
    public static final String LOG_TAG = SportsStoreLocatorFragment.class.getSimpleName();

    public SportsStoreLocatorFragment() {
        // Required empty public constructor
    }

 /*   public void setStoreLocatorParcelables(List<StoreLocatorParcelable> storeLocatorParcelables) {
        this.storeLocatorParcelables = storeLocatorParcelables;
    }*/

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
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(Places.GEO_DATA_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
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
        View view = inflater.inflate(R.layout.fragment_sports_store_locator, container, false);
        mPlaceDetailsText = (TextView) view.findViewById(R.id.place_details);
     //   mPlaceDetailsAttribution = (TextView) view.findViewById(R.id.place_attribution);
        mAutocompleteView = (AutoCompleteTextView) view.
                findViewById(R.id.autocomplete_places);

        // Register a listener that receives callbacks when a suggestion has been selected
        mAutocompleteView.setOnItemClickListener(mAutocompleteClickListener);

        // Set up the adapter that will retrieve suggestions from the Places Geo Data API that cover
        // the entire world.
        mAdapter = new PlaceAutocompleteAdapter(getContext(), mGoogleApiClient,
                null);
        mAutocompleteView.setAdapter(mAdapter);
        mListView = (ListView) view.findViewById(R.id.store_locator_list_view);
    //    mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mStoreLocatorParcelables=new ArrayList<StoreLocatorParcelable>();
      //  mRecyclerView.setLayoutManager(mLayoutManager);

        mFragment=this;
        return view;
    }

    @Override
    public void retrieveStoresList(List<StoreLocatorParcelable> storeLocatorParcelables) {
        Log.v(LOG_TAG,"sports list size"+storeLocatorParcelables.size());
        mStoreLocatorParcelables=storeLocatorParcelables;
        mStoreLocatorAdapter = new StoreLocatorAdapter(getContext(),0,mStoreLocatorParcelables);
        mListView.setAdapter(mStoreLocatorAdapter);
        mStoreLocatorAdapter.notifyDataSetChanged();


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
     * @see com.google.android.gms.location.places.GeoDataApi#getPlaceById(com.google.android.gms.common.api.GoogleApiClient,
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
            final AutocompletePrediction item = mAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            final CharSequence primaryText = item.getPrimaryText(null);

            Log.i(LOG_TAG, "Autocomplete item selected: " + primaryText);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
             details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

            Toast.makeText(getActivity().getApplicationContext(), "Clicked: " + primaryText,
                    Toast.LENGTH_SHORT).show();
            Log.i(LOG_TAG, "Called getPlaceById to get Place details for " + placeId);
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
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                Log.e(LOG_TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }
            // Get the Place object from the buffer.
            final Place place = places.get(0);
            LatLng placeLatLng = place.getLatLng();
            String placeName = place.getName().toString();
            String latitude=placeLatLng.latitude+","+placeLatLng.longitude;
           /* Uri.Builder builder = Uri.parse(Constants.STORE_LOCATOR_BASE_URL).buildUpon().appendQueryParameter(Constants.QUERY, getString(R.string.store_locator_query, placeName)).appendQueryParameter(Constants.LOCATION_KEY, getString(R.string.map_key));
           Uri uri= builder.build();

            Log.v(LOG_TAG,"Uri is::"+uri.toString());*/
            StoreLocatorAsyncTask storeLocatorAsyncTask = new StoreLocatorAsyncTask(getContext(),mFragment);
          //  storeLocatorAsyncTask.setStoreDataReceivedListener(SportsStoreLocatorFragment.this);
            storeLocatorAsyncTask.execute(latitude);
   //         Log.v(LOG_TAG,"Store locator list size"+storeLocatorParcelables);
            // Format details of the place for display and show it in a TextView.
         /*   mPlaceDetailsText.setText(formatPlaceDetails(getResources(), place.getName(),
                    place.getId(), place.getAddress(), place.getPhoneNumber(),
                    place.getWebsiteUri()));

            // Display the third party attributions if set.
            final CharSequence thirdPartyAttribution = places.getAttributions();
            if (thirdPartyAttribution == null) {
                mPlaceDetailsAttribution.setVisibility(View.GONE);
            } else {
                mPlaceDetailsAttribution.setVisibility(View.VISIBLE);
                mPlaceDetailsAttribution.setText(Html.fromHtml(thirdPartyAttribution.toString()));
            }*/


            Log.i(LOG_TAG, "Place details received: " + place.getName());

            places.release();
        }
    };

    private static Spanned formatPlaceDetails(Resources res, CharSequence name, String id,
                                              CharSequence address, CharSequence phoneNumber, Uri websiteUri) {
        Log.e(LOG_TAG, res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));
        return Html.fromHtml(res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));

    }

    /**
     * Called when the Activity could not connect to Google Play services and the auto manager
     * could resolve the error automatically.
     * In this case the API is not available and notify the user.
     *
     * @param connectionResult can be inspected to determine the cause of the failure
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.e(LOG_TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());

        // TODO(Developer): Check error code and notify the user of error state and resolution.
        Toast.makeText(getContext(),
                "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }


}

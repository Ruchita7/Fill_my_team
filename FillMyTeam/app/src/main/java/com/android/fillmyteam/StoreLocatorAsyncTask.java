package com.android.fillmyteam;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.android.fillmyteam.api.StoreDataReceivedListener;
import com.android.fillmyteam.model.StoreLocatorParcelable;
import com.android.fillmyteam.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Asynctask for sports store uri to json parsing
 * @author Ruchita_Maheshwary
 *
 */
public class StoreLocatorAsyncTask extends AsyncTask<String, Void, List<StoreLocatorParcelable>> {

    Context mContext;
    private StoreDataReceivedListener mStoreDataReceivedListener;

    SportsStoreLocatorFragment mFragment;
    public static final String LOG_TAG = StoreLocatorAsyncTask.class.getSimpleName();
    List<StoreLocatorParcelable> mStoreLocatorParcelables;
    int mStatus;
    public static final int STORE_STATUS_SERVER_DOWN = 1;
    public static final int STORE_STATUS_SERVER_INVALID = 2;
    public static final int STORE_STATUS_INVALID = 4;

    public StoreLocatorAsyncTask(Context context, SportsStoreLocatorFragment fragment) {
        super();
        mContext = context;
        mFragment = fragment;
    }


    @Override
    protected List<StoreLocatorParcelable> doInBackground(String... params) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String storeLocatorData = null;
      
        String location = params[0];
        try {
            mStoreLocatorParcelables = new ArrayList<>();
            Uri.Builder builder = Uri.parse(Constants.STORE_LOCATOR_BASE_URL).buildUpon().
                    appendQueryParameter(Constants.QUERY, mContext.getString(R.string.sport_goods_query)).
                    appendQueryParameter(Constants.LOCATION, location).
                    appendQueryParameter(Constants.RADIUS, Constants.TEN_KM_RADIUS).
                            appendQueryParameter(Constants.LOCATION_KEY, Constants.GOOGLE_MAPS_KEY);
            Uri builtUri = builder.build();
            //Log.v(LOG_TAG, "Uri is::" + builtUri.toString());
            URL url = new URL(builtUri.toString());

            //Log.v(LOG_TAG, "Built URI " + builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(Constants.GET_REQUEST);
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                mStatus = STORE_STATUS_SERVER_DOWN;
                return null;
            }
            storeLocatorData = buffer.toString();

            //Log.v(LOG_TAG, "Store Locator string: " + storeLocatorData);
            retrieveStoreLocator(storeLocatorData);
        } catch (UnknownHostException e) {
            mStatus = STORE_STATUS_SERVER_DOWN;
            //Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();

        } catch (MalformedURLException e) {
            mStatus = STORE_STATUS_SERVER_INVALID;
            //Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
        } catch (JSONException e) {
            mStatus = STORE_STATUS_INVALID;
            //Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } catch (IOException e) {
            //Log.e(LOG_TAG, e.getMessage());
            mStatus = STORE_STATUS_SERVER_DOWN;
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    //Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

              return null;
      
    }


    private void retrieveStoreLocator(String locationData) throws JSONException {
        String address;
        double latitude;
        double longitude;
        String name;

        StoreLocatorParcelable storeLocatorParcelable;
        JSONObject jsonObject;
        try {
            JSONObject storeJson = new JSONObject(locationData);
            if (!storeJson.has(Constants.RESULTS)) {

                mStatus = STORE_STATUS_INVALID;
                return;
            }
            JSONArray storeJsonArray = storeJson.getJSONArray(Constants.RESULTS);
            JSONObject latLng;
            JSONObject geometry;
            String photoReference = null;
            JSONArray photoJsonArray;
            JSONObject photoJson;
            for (int i = 0; i < storeJsonArray.length(); i++) {
                jsonObject = storeJsonArray.getJSONObject(i);
                geometry = jsonObject.getJSONObject(Constants.GEOMETRY);
                latLng = geometry.getJSONObject(Constants.LOCATION);
                address = jsonObject.getString(Constants.FORMATTED_ADDRESS);
                latitude = latLng.getDouble(Constants.LAT);
                longitude = latLng.getDouble(Constants.LNG);
                name = jsonObject.getString(Constants.PLACE_NAME);
                photoReference = null;
                if (jsonObject.has(Constants.PHOTOS)) {
                    photoJsonArray = jsonObject.getJSONArray(Constants.PHOTOS);
                    for (int j = 0; j < photoJsonArray.length(); j++) {
                        photoJson = photoJsonArray.getJSONObject(j);
                        if (photoJson.get(Constants.PHOTO_REFERENCE) != null) {
                            photoReference = photoJson.getString(Constants.PHOTO_REFERENCE);
                            //Log.v(LOG_TAG, "photo url " + photoReference);
                            break;
                        }
                    }
                }

                storeLocatorParcelable = new StoreLocatorParcelable(name, address, latitude, longitude, photoReference);
                mStoreLocatorParcelables.add(storeLocatorParcelable);
            }
            //Log.v(LOG_TAG, "List size" + mStoreLocatorParcelables.size());
        } catch (JSONException e) {
            e.printStackTrace();
            mStatus = STORE_STATUS_SERVER_INVALID;
        }
    }

    @Override
    protected void onPostExecute(List<StoreLocatorParcelable> storeLocators) {
        super.onPostExecute(storeLocators);
        storeLocators = mStoreLocatorParcelables;
        mFragment.retrieveStoresList(storeLocators, mStatus);

      
    }

    public void setStoreDataReceivedListener(StoreDataReceivedListener listener) {
        mStoreDataReceivedListener = listener;
    }
}

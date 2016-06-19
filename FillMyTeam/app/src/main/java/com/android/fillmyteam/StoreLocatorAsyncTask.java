package com.android.fillmyteam;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.android.fillmyteam.model.StoreLocatorParcelable;
import com.android.fillmyteam.util.Constants;
import com.android.fillmyteam.api.StoreDataReceivedListener;

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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dgnc on 5/29/2016.
 */
public class StoreLocatorAsyncTask extends AsyncTask<String, Void, List<StoreLocatorParcelable>> {

    Context mContext;
    private StoreDataReceivedListener mStoreDataReceivedListener;

    SportsStoreLocatorFragment mFragment;
    public static final String LOG_TAG = StoreLocatorAsyncTask.class.getSimpleName();
    List<StoreLocatorParcelable> mStoreLocatorParcelables;

    public StoreLocatorAsyncTask(Context context,SportsStoreLocatorFragment fragment) {
        super();
        mContext = context;
        mFragment=fragment;
    }


    @Override
    protected List<StoreLocatorParcelable> doInBackground(String... params) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String storeLocatorData = null;
        //   Uri builtUri = Uri.parse(Constants.SPORTS_URL);
        String location = params[0];
        try {
            mStoreLocatorParcelables = new ArrayList<>();
            Uri.Builder builder = Uri.parse(Constants.STORE_LOCATOR_BASE_URL).buildUpon().
                    appendQueryParameter(Constants.QUERY, mContext.getString(R.string.sport_goods_query)).
                    appendQueryParameter(Constants.LOCATION,location).
                    appendQueryParameter(Constants.RADIUS,Constants.TEN_KM_RADIUS).
                    appendQueryParameter(Constants.LOCATION_KEY, mContext.getString(R.string.map_key));
            Uri builtUri = builder.build();
            Log.v(LOG_TAG,"Uri is::"+builtUri.toString());
            URL url = new URL(builtUri.toString());

            Log.v(LOG_TAG, "Built URI " + builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
      //      urlConnection.setRequestMethod("GET");
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
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            storeLocatorData = buffer.toString();

            Log.v(LOG_TAG, "Store Locator string: " + storeLocatorData);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try {
            retrieveStoreLocator(storeLocatorData);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
        // return new StoreLocator[0];
    }



    private void retrieveStoreLocator(String locationData)      throws JSONException  {
        String address;
        double latitude;
        double longitude;
        String name;

        StoreLocatorParcelable storeLocatorParcelable;
        JSONObject jsonObject;
        try {
            JSONObject storeJson = new JSONObject(locationData);
         //   JSONArray storeJsonArray = storeJson.getJSONArray("results");
            JSONArray storeJsonArray = storeJson.getJSONArray(Constants.RESULTS);
            JSONObject latLng;
            JSONObject geometry;
            for(int i=0;i<storeJsonArray.length();i++) {
                jsonObject =storeJsonArray.getJSONObject(i);
                /*geometry=jsonObject.getJSONObject("geometry");
                latLng= geometry.getJSONObject("location");*/
                geometry=jsonObject.getJSONObject(Constants.GEOMETRY);
                latLng= geometry.getJSONObject(Constants.LOCATION);
             //   latLngArray=geometry.getJSONArray("location");
            /*    address=jsonObject.getString("formatted_address");
                latitude=latLng.getDouble("lat");
                longitude=latLng.getDouble("lng");
                name=jsonObject.getString("name");     */
                address=jsonObject.getString(Constants.FORMATTED_ADDRESS);
                latitude=latLng.getDouble(Constants.LAT);
                longitude=latLng.getDouble(Constants.LNG);
                name=jsonObject.getString(Constants.PLACE_NAME);
                storeLocatorParcelable = new StoreLocatorParcelable(name,address,latitude,longitude);
                mStoreLocatorParcelables.add(storeLocatorParcelable);
            }
            Log.v(LOG_TAG,"List size"+mStoreLocatorParcelables.size());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostExecute(List<StoreLocatorParcelable> storeLocators) {
        super.onPostExecute(storeLocators);
        storeLocators=mStoreLocatorParcelables;
      //  mStoreDataReceivedListener.retrieveStoresList(storeLocators);
        mFragment.retrieveStoresList(storeLocators);

        //mContext.setStoreLocatorParcelables(storeLocators);
      /*  storeLocators= (StoreLocatorParcelable[]) mStoreLocatorParcelables.toArray();
        Log.v(LOG_TAG,"List size"+storeLocators.length);*/
    }

    public  void setStoreDataReceivedListener(StoreDataReceivedListener listener)   {
        mStoreDataReceivedListener=listener;
    }
}

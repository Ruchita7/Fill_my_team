package com.android.fillmyteam;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.android.fillmyteam.data.SportsColumns;
import com.android.fillmyteam.data.SportsProvider;
import com.android.fillmyteam.model.SportParcelable;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dgnc on 5/22/2016.
 */
public class SportsAsyncTask extends AsyncTask<Void, Void, Void> {

    final String LOG_TAG = SportsAsyncTask.class.getSimpleName();
    Context mContext;

    public SportsAsyncTask(Context context) {
        mContext=context;
    }
    List<SportParcelable> mSportParcelables;

    @Override
    protected Void doInBackground(Void... params) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String sportsData=null;
        Uri builtUri = Uri.parse(Constants.SPORTS_URL);
        try {
            URL url = new URL(builtUri.toString());
            mSportParcelables = new ArrayList<>();
            Log.v(LOG_TAG, "Built URI " + builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
           // urlConnection.setRequestMethod("GET");
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
            sportsData = buffer.toString();

            Log.v(LOG_TAG, "Sports string: " + sportsData);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
        }
        finally {
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
             retrieveSports(sportsData);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    public void retrieveSports(String sportsData)     throws JSONException {
        String sportName;
        String objective;
        String players;
        String thumbnail;
        String image;
        String rules;
        String video;
        SportParcelable sportParcelable;
        JSONObject jsonObject;
        try {
            JSONObject sportsJson = new JSONObject(sportsData);
            JSONArray sportsJsonArray = sportsJson.getJSONObject(Constants.SPORT).getJSONArray(Constants.LIST);
            for(int i=0;i<sportsJsonArray.length();i++) {
                jsonObject =sportsJsonArray.getJSONObject(i);
               /* sportName=jsonObject.getString("name");
                objective=jsonObject.getString("objective");
                players=jsonObject.getString("players");
                rules=jsonObject.getString("rules");
                thumbnail=jsonObject.getString("thumbnail");
                image=jsonObject.getString("image");
                video=jsonObject.getString("video");*/
                sportName=jsonObject.getString(Constants.SPORTS_NAME);
                objective=jsonObject.getString(Constants.OBJECTIVE);
                players=jsonObject.getString(Constants.PLAYERS);
                rules=jsonObject.getString(Constants.RULES);
                thumbnail=jsonObject.getString(Constants.THUMBNAIL);
                image=jsonObject.getString(Constants.IMAGE);
                video=jsonObject.getString(Constants.VIDEO);
                sportParcelable = new SportParcelable("",sportName,objective,players,rules,thumbnail,image,video);
                mSportParcelables.add(sportParcelable);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        for(SportParcelable sportParcelable : mSportParcelables)    {
            insertValues(sportParcelable);
        }
    }

    private void insertValues(SportParcelable sportParcelable)  {
        ContentValues contentValues=new ContentValues();
        contentValues.put(SportsColumns.SPORTS_NAME,sportParcelable.getSportsName());
        contentValues.put(SportsColumns.OBJECTIVE,sportParcelable.getObjective());
        contentValues.put(SportsColumns.PLAYERS,sportParcelable.getPlayers());
        contentValues.put(SportsColumns.RULES,sportParcelable.getRules());
        contentValues.put(SportsColumns.THUMBNAIL,sportParcelable.getThumbnail());
        contentValues.put(SportsColumns.POSTER_IMAGE,sportParcelable.getPosterImage());
        contentValues.put(SportsColumns.VIDEO_URL,sportParcelable.getVideoUrl());
        mContext.getContentResolver().insert(SportsProvider.Sports.CONTENT_URI,contentValues);
    }

}

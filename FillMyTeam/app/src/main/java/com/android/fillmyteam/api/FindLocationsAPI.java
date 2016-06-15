package com.android.fillmyteam.api;

import com.android.fillmyteam.model.LocationResponse;
import com.android.fillmyteam.util.Constants;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by dgnc on 6/5/2016.
 */

public interface FindLocationsAPI {
    @GET("textsearch/json")
    Call<LocationResponse> retrieveParksNearMe(@Query(Constants.LOCATION) String location, @Query(Constants.TYPE) String type, @Query(Constants.RADIUS) String radius, @Query(Constants.KEY) String key );

}

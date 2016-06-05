package com.android.fillmyteam.api;

import com.android.fillmyteam.model.LocationResponse;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by dgnc on 6/5/2016.
 */

public interface FindLocationsAPI {
    @GET("textsearch/json")
    Call<LocationResponse> retrieveParksNearMe(@Query("location") String location, @Query("type") String type, @Query("radius") String radius, @Query("key") String key );

}

package com.android.fillmyteam.api;

import com.android.fillmyteam.model.LocationResponse;
import com.android.fillmyteam.model.SportsResult;
import com.android.fillmyteam.util.Constants;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * @author Ruchita_Maheshwary
 */
public interface RestService {

    @GET("{key}")
    Call<SportsResult> retrieveSportsDetails(@Path("key") String urlKey);

    @GET("json")
    Call<LocationResponse> retrieveSportsStores(@Query(Constants.QUERY) String query, @Query(Constants.LOCATION_KEY) String mapsKey);
}

package com.android.fillmyteam.api;

import com.android.fillmyteam.model.SportsResult;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * @author Ruchita_Maheshwary
 */
public interface RestService {

    @GET("{key}")
    Call<SportsResult> retrieveSportsDetails(@Path("key") String urlKey);
}

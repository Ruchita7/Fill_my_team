package com.android.fillmyteam.api;

import com.android.fillmyteam.model.StoreLocatorParcelable;

import java.util.List;

/**
 * Interface for returning sports store
 * @author Ruchita_Maheshwary
 *
 */
public interface StoreDataReceivedListener {
    public void retrieveStoresList(List<StoreLocatorParcelable> storeLocatorParcelables, int status);
}

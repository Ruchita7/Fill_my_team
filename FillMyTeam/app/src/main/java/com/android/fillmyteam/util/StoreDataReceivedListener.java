package com.android.fillmyteam.util;

import com.android.fillmyteam.model.StoreLocatorParcelable;

import java.util.List;

/**
 * Created by dgnc on 5/30/2016.
 */
public interface StoreDataReceivedListener {
    public void retrieveStoresList(List<StoreLocatorParcelable> storeLocatorParcelables);
}

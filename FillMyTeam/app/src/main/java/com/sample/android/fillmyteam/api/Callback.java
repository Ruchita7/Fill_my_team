package com.sample.android.fillmyteam.api;

import com.sample.android.fillmyteam.SportsInfoAdapter;
import com.sample.android.fillmyteam.model.User;

/**
 * Callback interface
 * @author Ruchita_Maheshwary
 *
 */
public interface Callback {

     // DetailFragmentCallback for when an item has been selected.

    public void onItemSelected(String id, SportsInfoAdapter.InfoViewHolder vh);

    //FindPlaymatesFragment callback to invite players
    public void onInviteClick(User currentUser, User playWithUser);
}
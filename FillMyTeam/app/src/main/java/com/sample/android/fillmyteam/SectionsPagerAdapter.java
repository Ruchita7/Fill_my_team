package com.sample.android.fillmyteam;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.sample.android.fillmyteam.model.User;

/**
 * Created by dgnc on 8/13/2016.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {
    private String tabTitles[] = new String[]{"Play", "Profile"};
    User mUser;

    public SectionsPagerAdapter(FragmentManager fm, User user) {
        super(fm);
        mUser = user;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        //    return PlaceholderFragment.newInstance(position + 1);
        if (position == 0) {
            return PlayFragment.newInstance(position + 1, mUser);
        } else {
            return ProfileFragment.newInstance(position + 1, mUser);
        }
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        /*    switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
            }*
            return null;*/
        return tabTitles[position];
    }
}
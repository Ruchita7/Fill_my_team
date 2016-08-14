package com.sample.android.fillmyteam;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sample.android.fillmyteam.model.User;
import com.sample.android.fillmyteam.util.Constants;

/**
 * Created by dgnc on 8/13/2016.
 */
public class ProfileFragment extends android.support.v4.app.Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static User mUser;
    public ProfileFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ProfileFragment newInstance(int sectionNumber, User user) {
        ProfileFragment fragment = new ProfileFragment();
        mUser=user;
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        args.putSerializable(Constants.USER_DETAILS, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
      /*  TextView textView = (TextView) rootView.findViewById(R.id.label2);
        textView.setText(getString(R.string.profile_format, getArguments().getInt(ARG_SECTION_NUMBER)));
*/
        return rootView;
    }
}

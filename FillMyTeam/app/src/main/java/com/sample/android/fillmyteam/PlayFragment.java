package com.sample.android.fillmyteam;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.sample.android.fillmyteam.model.User;
import com.sample.android.fillmyteam.ui.GridItemDecoration;
import com.sample.android.fillmyteam.util.Constants;

import java.util.Arrays;
import java.util.List;

/**
 * Created by dgnc on 8/13/2016.
 */
public class PlayFragment extends android.support.v4.app.Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
     User mUser;
    private static final String ARG_SECTION_NUMBER = "section_number";
    List<String> mPlayItemsListMenu;
    public PlayFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlayFragment newInstance(int sectionNumber,User user) {
        PlayFragment fragment = new PlayFragment();
      //  mUser = user;
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        args.putSerializable(Constants.USER_DETAILS, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_play, container, false);
        if(getArguments()!=null)    {
            if(getArguments().containsKey(Constants.USER_DETAILS))    {
                    mUser = (User)getArguments().get(Constants.USER_DETAILS);
            }
        }
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.play_recycler_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        recyclerView.setLayoutManager(gridLayoutManager);
        final String[] playItems = {"Teammates\n in my Area", "Upcoming\nMatches", "Store\nLocator", "Learn to\nPlay"};
        mPlayItemsListMenu = Arrays.asList(playItems);
        PlayTabAdapter playTabAdapter = new PlayTabAdapter(getActivity(), mPlayItemsListMenu, new PlayTabAdapter.PlayAdapterOnClickHandler() {
            @Override
            public void itemClick(int position, PlayTabAdapter.PlayViewHolder viewHolder) {
                Toast.makeText(getActivity(),mPlayItemsListMenu.get(position),Toast.LENGTH_SHORT).show();
                if(position==3)    {
                   // SportsInfoFragment.newInstance(mUser.getLatitude(),mUser.getLongitude());
                    Intent intent = new Intent(getContext(),SportsInfoActivity.class);
                    //intent.putExtra()
                    startActivity(intent);
                }
            }
        });
        recyclerView.setAdapter(playTabAdapter);
        /*recyclerView.addItemDecoration(new GridItemDecoration(
                getActivity(),GridItemDecoration.GRID));*/
        recyclerView.addItemDecoration(new GridItemDecoration(
                getActivity(),R.dimen.list_margin));

        // int view_height=screen_height/number of rows;   //height for imageview

       /* TextView textView = (TextView) rootView.findViewById(R.id.section_label);
        textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));*/

     /*   Button btn1 = (Button)rootView.findViewById(R.id.find_playmates);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),FindPlaymatesActivity.class);
                startActivity(intent);
            }
        });

        Button btn2 = (Button)rootView.findViewById(R.id.upcoming_matches);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),UpcomingMatchesActivity.class);
                startActivity(intent);
            }
        });
*/

        return rootView;
    }
}
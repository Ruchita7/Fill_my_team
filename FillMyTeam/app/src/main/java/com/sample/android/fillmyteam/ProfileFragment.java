package com.sample.android.fillmyteam;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.sample.android.fillmyteam.model.User;
import com.sample.android.fillmyteam.ui.BlurTransformation;
import com.sample.android.fillmyteam.ui.CircularImageTransform;
import com.sample.android.fillmyteam.util.Constants;
import com.squareup.picasso.Picasso;

/**
 * Created by dgnc on 8/13/2016.
 */
public class ProfileFragment extends android.support.v4.app.Fragment implements AdapterView.OnItemClickListener {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private User mUser;

    String[] listItems;
    // List<String> listItems;
    private static int IMAGE_DIMENS = 200;

    public ProfileFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ProfileFragment newInstance(int sectionNumber, User user) {
        ProfileFragment fragment = new ProfileFragment();

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
     /*   listItems = new ArrayList<>();
        listItems.add(getString(R.string.settings));
     */
        listItems = new String[]{"Manage Settings"};
        if (getArguments() != null) {
            if (getArguments().containsKey(Constants.USER_DETAILS)) {
                mUser = (User) getArguments().get(Constants.USER_DETAILS);
            }
        }
        if (mUser != null) {
            TextView userTextView = (TextView) rootView.findViewById(R.id.userNameTextView);
            //     TextView emailTextView = (TextView) navigationHeader.findViewById(R.id.emailTextView);
            ImageView userPhotoImageView = (ImageView) rootView.findViewById(R.id.profileImageView);
            ImageView blurPhotoImageView = (ImageView) rootView.findViewById(R.id.blur_profile_image_view);
            userTextView.setText(mUser.getName());
            // emailTextView.setText(mUser.getEmail());
            if (mUser.getPhotoUrl() != null && !mUser.getPhotoUrl().isEmpty()) {
                Picasso.with(getActivity()).load(mUser.getPhotoUrl()).resize(IMAGE_DIMENS, IMAGE_DIMENS).transform(new CircularImageTransform()).into(userPhotoImageView);
                Picasso.with(getActivity()).load(mUser.getPhotoUrl()).transform(new BlurTransformation(getActivity())).into(blurPhotoImageView);

            }

            ImageView imageView = (ImageView) rootView.findViewById(R.id.edit_profile_button);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), EditProfileActivity.class);
                    intent.putExtra(Constants.USER_DETAILS, mUser);
                    startActivity(intent);
                }
            });
        }
        ListView listView = (ListView) rootView.findViewById(R.id.profile_list_view);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, listItems);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            Intent intent = new Intent(getContext(), SettingsActivity.class);
            startActivity(intent);
        }
    }

  /*  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sports_info_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.share_action) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/

}

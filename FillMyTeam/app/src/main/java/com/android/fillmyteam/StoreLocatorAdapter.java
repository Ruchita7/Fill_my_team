package com.android.fillmyteam;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.fillmyteam.model.StoreLocatorParcelable;
import com.android.fillmyteam.util.Constants;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by dgnc on 5/31/2016.
 */
public class StoreLocatorAdapter extends ArrayAdapter<StoreLocatorParcelable>  {

    List<StoreLocatorParcelable> mStoreLocatorParcelables;
    Context mContext;

    public StoreLocatorAdapter(Context context, int resource, List<StoreLocatorParcelable> mStoreLocatorParcelables) {
        super(context, R.layout.store_locator_list_item, mStoreLocatorParcelables);
        this.mStoreLocatorParcelables = mStoreLocatorParcelables;
        this.mContext = context;
    }

    public static class ViewHolder {
        // each data item is just a string in this case
        public TextView addressTextView;
        public TextView nameTextView;
        ImageView locatorImageView;
        ImageView storeImageView;

        public ViewHolder() {
        }

        public ViewHolder(View v) {
            //  super(v);
            addressTextView = (TextView) v.findViewById(R.id.address);
            nameTextView = (TextView) v.findViewById(R.id.name);
           // locatorImageView = (ImageView) v.findViewById(R.id.locator_imageView);
            storeImageView=(ImageView) v.findViewById(R.id.store_image_view);
        }
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        StoreLocatorParcelable storeLocatorParcelable = mStoreLocatorParcelables.get(position);
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.store_locator_list_item, parent, false);
            viewHolder.addressTextView = (TextView) convertView.findViewById(R.id.address);
            viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.name);
        //    viewHolder.locatorImageView = (ImageView) convertView.findViewById(R.id.locator_imageView);
            viewHolder.storeImageView=(ImageView) convertView.findViewById(R.id.store_image_view);
            // viewHolder.home = (TextView) convertView.findViewById(R.id.tvHome);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.addressTextView.setText(storeLocatorParcelable.getAddress());
        viewHolder.nameTextView.setText(storeLocatorParcelable.getName());

        Uri.Builder builder = Uri.parse(Constants.PHOTO_URL).buildUpon().
                appendQueryParameter(Constants.MAX_WIDTH, Constants.WIDTH_VALUE).
                appendQueryParameter(Constants.REFERENCE_ID, storeLocatorParcelable.getPhotoReference()).
              //  appendQueryParameter(Constants.KEY, mContext.getString(R.string.map_key));
                      appendQueryParameter(Constants.KEY,Constants.GOOGLE_MAPS_KEY);
        String photoUrl = builder.build().toString();
        if(photoUrl!=null &&!photoUrl.isEmpty())
        {
            viewHolder.storeImageView.setVisibility(View.VISIBLE);
            Picasso.with(mContext).load(photoUrl).into(viewHolder.storeImageView);
        }
       /* final double latitude = storeLocatorParcelable.getLatitude();
        final double longitude = storeLocatorParcelable.getLongitude();
        final String storeName = storeLocatorParcelable.getName();
        final String address = storeLocatorParcelable.getAddress();
*/
      /*  viewHolder.locatorImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick(v, latitude, longitude, storeName, address);
            }
        });*/
        return convertView;
    }


   /* public void onItemClick(View view, double latitude, double longitude, String storeName, String address) {

        //launch map


        //directions
        String geoLocation = "google.navigation:" + "q=" + storeName + address;

        Uri geoIntentUri = Uri.parse(geoLocation);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, geoIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(mContext.getPackageManager()) != null) {
            mContext.startActivity(mapIntent);
        }
    }*/

}

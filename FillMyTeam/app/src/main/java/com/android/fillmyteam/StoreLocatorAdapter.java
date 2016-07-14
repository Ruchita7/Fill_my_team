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
 * Adapter for sports store
 * @author Ruchita_Maheshwary
 *
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
            viewHolder.storeImageView=(ImageView) convertView.findViewById(R.id.store_image_view);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.addressTextView.setText(storeLocatorParcelable.getAddress());
        viewHolder.nameTextView.setText(storeLocatorParcelable.getName());

        Uri.Builder builder = Uri.parse(Constants.PHOTO_URL).buildUpon().
                appendQueryParameter(Constants.MAX_WIDTH, Constants.WIDTH_VALUE).
                appendQueryParameter(Constants.REFERENCE_ID, storeLocatorParcelable.getPhotoReference()).
                      appendQueryParameter(Constants.KEY,Constants.GOOGLE_MAPS_KEY);
        String photoUrl = builder.build().toString();
        if(photoUrl!=null &&!photoUrl.isEmpty())
        {
            viewHolder.storeImageView.setVisibility(View.VISIBLE);
            Picasso.with(mContext).load(photoUrl).into(viewHolder.storeImageView);
        }
         return convertView;
    }


   
}

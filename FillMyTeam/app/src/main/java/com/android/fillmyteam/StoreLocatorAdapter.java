package com.android.fillmyteam;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.fillmyteam.model.StoreLocatorParcelable;
import com.android.fillmyteam.util.Constants;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by dgnc on 5/31/2016.
 */
public class StoreLocatorAdapter extends RecyclerView.Adapter<StoreLocatorAdapter.StoreViewHolder> {

    List<StoreLocatorParcelable> mStoreLocatorParcelables;
    Context mContext;
    final StoreLocatorAdapterOnClickHandler mClickHandler;
    final View mEmptyView;
    ItemChoiceManager mIcm;

    public StoreLocatorAdapter(Context context, StoreLocatorAdapterOnClickHandler adapterClickHandler, List<StoreLocatorParcelable> storeLocatorParcelables, View emptyView, int choiceMode) {
        mContext = context;
        mClickHandler = adapterClickHandler;
        mEmptyView = emptyView;
        mStoreLocatorParcelables = storeLocatorParcelables;
        mIcm = new ItemChoiceManager(this);
        mIcm.setChoiceMode(choiceMode);
    }


    @Override
    public StoreViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.store_locator_list_item, parent, false);
        view.setFocusable(true);
        return new StoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StoreViewHolder holder, int position) {
        StoreLocatorParcelable storeLocatorParcelable = mStoreLocatorParcelables.get(position);

        holder.addressTextView.setText(storeLocatorParcelable.getAddress());
        holder.nameTextView.setText(storeLocatorParcelable.getName());

        Uri.Builder builder = Uri.parse(Constants.PHOTO_URL).buildUpon().
                appendQueryParameter(Constants.MAX_WIDTH, Constants.WIDTH_VALUE).
                appendQueryParameter(Constants.REFERENCE_ID, storeLocatorParcelable.getPhotoReference()).
                appendQueryParameter(Constants.KEY, mContext.getString(R.string.map_key));
        String photoUrl = builder.build().toString();
        Log.v(StoreLocatorAdapter.class.getSimpleName(),"photo url "+photoUrl);
        Picasso.with(mContext).load(photoUrl).into(holder.storeImageView);


        // ViewCompat.setTransitionName(holder.sportsImage, Constants.ICON_VIEW + position);
        mIcm.onBindViewHolder(holder, position);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mIcm.onRestoreInstanceState(savedInstanceState);
    }

    public void onSaveInstanceState(Bundle outState) {
        mIcm.onSaveInstanceState(outState);
    }


    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        if (null == mStoreLocatorParcelables) return 0;
        return mStoreLocatorParcelables.size();
    }

    public class StoreViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView addressTextView;
        public TextView nameTextView;
        ImageView locatorImageView;
        ImageView storeImageView;

        public StoreViewHolder(View v) {
             super(v);
            addressTextView = (TextView) v.findViewById(R.id.address);
            nameTextView = (TextView) v.findViewById(R.id.name);
            locatorImageView = (ImageView) v.findViewById(R.id.locator_imageView);
            storeImageView = (ImageView) v.findViewById(R.id.store_image_view);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            mClickHandler.itemClick(position, this);
            mIcm.onClick(this);
        }
    }


    public void selectView(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof RecyclerView.ViewHolder) {
            StoreViewHolder svh = (StoreViewHolder) viewHolder;
            svh.onClick(svh.itemView);
        }
    }


    public static interface StoreLocatorAdapterOnClickHandler {
        public void itemClick(int position,StoreViewHolder svh);
    }


}

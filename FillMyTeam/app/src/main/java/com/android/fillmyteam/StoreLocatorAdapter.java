package com.android.fillmyteam;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.fillmyteam.api.ExpandableLayout;
import com.android.fillmyteam.model.StoreLocatorParcelable;
import com.android.fillmyteam.ui.ExpandableLayoutListenerAdapter;
import com.android.fillmyteam.ui.ExpandableLinearLayout;
import com.android.fillmyteam.util.Constants;
import com.android.fillmyteam.util.Utility;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Adapter for sports store
 *
 * @author Ruchita_Maheshwary
 */
public class StoreLocatorAdapter extends RecyclerView.Adapter<StoreLocatorAdapter.ViewHolder> {

    List<StoreLocatorParcelable> mStoreLocatorParcelables;
    Context mContext;
    StoreAdapterOnClickHandler mClickHandler;
    private SparseBooleanArray expandState = new SparseBooleanArray();

    public StoreLocatorAdapter(Context context, List<StoreLocatorParcelable> storeLocatorParcelables) {//, StoreAdapterOnClickHandler clickHandler) {
        //    super(context, R.layout.store_locator_list_item, mStoreLocatorParcelables);
        this.mStoreLocatorParcelables = storeLocatorParcelables;
        //  mClickHandler = clickHandler;
        this.mContext = context;
        for (int i = 0; i < storeLocatorParcelables.size(); i++) {
            expandState.append(i, false);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.store_locator_list_item, parent, false);
        view.setFocusable(true);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder,final int position) {
        StoreLocatorParcelable storeLocatorParcelable = mStoreLocatorParcelables.get(position);
        holder.addressTextView.setText(storeLocatorParcelable.getAddress());
        holder.nameTextView.setText(storeLocatorParcelable.getName());
       // holder.storeNameTextView.setText(storeLocatorParcelable.getName());

        Uri.Builder builder = Uri.parse(Constants.PHOTO_URL).buildUpon().
                appendQueryParameter(Constants.MAX_WIDTH, Constants.WIDTH_VALUE).
                appendQueryParameter(Constants.REFERENCE_ID, storeLocatorParcelable.getPhotoReference()).
                appendQueryParameter(Constants.KEY, Constants.GOOGLE_MAPS_KEY);
        String photoUrl = builder.build().toString();
        if (photoUrl != null && !photoUrl.isEmpty()) {
            holder.storeImageView.setVisibility(View.VISIBLE);
            Picasso.with(mContext).load(photoUrl).into(holder.storeImageView);
        }
        holder.expandableLayout.setInterpolator(Utility.createInterpolator(Constants.ACCELERATE_INTERPOLATOR));
        holder.expandableLayout.setExpanded(expandState.get(position));
        holder.expandableLayout.setListener(new ExpandableLayoutListenerAdapter() {
            @Override
            public void onPreOpen() {
                createRotateAnimator(holder.buttonLayout, 0f, 180f).start();
                expandState.put(position, true);
            }

            @Override
            public void onPreClose() {
                createRotateAnimator(holder.buttonLayout, 180f, 0f).start();
                expandState.put(position, false);
            }
        });

        holder.buttonLayout.setRotation(expandState.get(position) ? 180f : 0f);
        holder.buttonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                onClickButton(holder.expandableLayout);
            }
        });
        holder.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StoreLocatorParcelable locatorParcelable =mStoreLocatorParcelables.get(position);
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, locatorParcelable.getName()+"\n"+locatorParcelable.getAddress());
                mContext.startActivity(sendIntent);
            }
        });

        holder.directionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StoreLocatorParcelable locatorParcelable =mStoreLocatorParcelables.get(position);
                String geoLocation = mContext.getString(R.string.geo_location, locatorParcelable.getName() + locatorParcelable.getAddress());

                Uri geoIntentUri = Uri.parse(geoLocation);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, geoIntentUri);
                mapIntent.setPackage(Constants.GOOGLE_MAPS_PACKAGE);
                if (mapIntent.resolveActivity(mContext.getPackageManager()) != null) {
                    mContext.startActivity(mapIntent);
                }
            }
        });
    }

    private void onClickButton(final ExpandableLayout expandableLayout) {
        expandableLayout.toggle();
    }
    @Override
    public int getItemCount() {
        if (null == mStoreLocatorParcelables) return 0;
        return mStoreLocatorParcelables.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder  {
        // each data item is just a string in this case
         TextView addressTextView;
         TextView nameTextView;
         RelativeLayout buttonLayout;
      //  ImageView locatorImageView;
        ImageView storeImageView;
      //   TextView storeNameTextView;
        Button directionsButton;
        Button shareButton;
         ExpandableLinearLayout expandableLayout;

        public ViewHolder(View v) {
            super(v);
            addressTextView = (TextView) v.findViewById(R.id.address);
            nameTextView = (TextView) v.findViewById(R.id.name);
            buttonLayout = (RelativeLayout) v.findViewById(R.id.button);
          //  storeNameTextView = (TextView) v.findViewById(R.id.store_name);
            storeImageView = (ImageView) v.findViewById(R.id.store_image_view);
            expandableLayout = (ExpandableLinearLayout) v.findViewById(R.id.expandableLayout);
            directionsButton = (Button)v.findViewById(R.id.directions_button);
            shareButton = (Button)v.findViewById(R.id.share_button);
          //  v.setOnClickListener(this);
        }

   /*     @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            //  mCursor.moveToPosition(position);
            *//*StoreLocatorParcelable storeLocatorParcelable = mStoreLocatorParcelables.get(position);
            mClickHandler.itemClick(storeLocatorParcelable, this);*//*
            StoreLocatorParcelable storeLocatorParcelable = mStoreLocatorParcelables.get(position);
            BottomSheetDialogFragment bottomSheetDialogFragment = new BottomsheetDialog();
            Bundle bundle = new Bundle();
            bundle.putParcelable("store", storeLocatorParcelable);
            bottomSheetDialogFragment.setArguments(bundle);
            bottomSheetDialogFragment.show(((FragmentActivity) mContext).getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
        }*/
    }

    public ObjectAnimator createRotateAnimator(final View target, final float from, final float to) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(target, "rotation", from, to);
        animator.setDuration(300);
        animator.setInterpolator(Utility.createInterpolator(Constants.LINEAR_INTERPOLATOR));
        return animator;
    }
    public static interface StoreAdapterOnClickHandler {
        public void itemClick(StoreLocatorParcelable storeLocatorParcelable, ViewHolder viewHolder);
    }
}

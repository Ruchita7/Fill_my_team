package com.android.fillmyteam;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.fillmyteam.data.SportsColumns;
import com.android.fillmyteam.ui.AspectRatioImageViewer;
import com.android.fillmyteam.util.Constants;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 *
 * @author Ruchita_Maheshwary
 * Adapter for SportsInfoFragment
 *
 */
public class SportsInfoAdapter extends RecyclerView.Adapter<SportsInfoAdapter.InfoViewHolder> {

    private Cursor mCursor;
    Context mContext;
    ItemChoiceManager mIcm;
    final SportsAdapterOnClickHandler mClickHandler;
    final View mEmptyView;
    ProgressBar mProgressBar;

    public SportsInfoAdapter(Context context,SportsAdapterOnClickHandler adapterClickHandler,View emptyView,ProgressBar progressBar, int choiceMode)  {
        mContext=context;
        mClickHandler=adapterClickHandler;
        mEmptyView=emptyView;
        mIcm = new ItemChoiceManager(this);
        mIcm.setChoiceMode(choiceMode);
        mProgressBar=progressBar;

    }

    @Override
    public InfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.sports_list_item, parent, false);
        view.setFocusable(true);
        return new InfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(InfoViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        String url = mCursor.getString(SportsInfoFragment.COL_SPORT_POSTER_IMAGE);
        Picasso.with(mContext).load(url).into(holder.sportsImage, new Callback() {
            @Override
            public void onSuccess() {
                if(mProgressBar!=null)  {
                    mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError() {

            }
        }

        );
        holder.sportsName.setText(mCursor.getString(SportsInfoFragment.COL_SPORT_NAME));
        holder.sportsImage.setTransitionName("iconView"+position);
        //   ViewCompat.setTransitionName(holder.sportsImage, "iconView" + position);
        ViewCompat.setTransitionName(holder.sportsImage, Constants.ICON_VIEW + position);
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
        if ( null == mCursor ) return 0;
        return mCursor.getCount();
    }

    public class InfoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

     
        public final AspectRatioImageViewer sportsImage;
        public final TextView sportsName;

        public InfoViewHolder(View itemView) {
            super(itemView);
           sportsImage = (AspectRatioImageViewer) itemView.findViewById(R.id.sports_image);
            sportsName = (TextView) itemView.findViewById(R.id.sport_name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            mCursor.moveToPosition(position);
            int sportId = mCursor.getColumnIndex(SportsColumns._ID);
            mClickHandler.itemClick(mCursor.getString(sportId),this);
            mIcm.onClick(this);
        }
    }


    public void selectView(RecyclerView.ViewHolder viewHolder) {
        if ( viewHolder instanceof InfoViewHolder ) {
            InfoViewHolder vfh = (InfoViewHolder)viewHolder;
            vfh.onClick(vfh.itemView);
        }
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    public static  interface SportsAdapterOnClickHandler    {
        public void itemClick(String id, InfoViewHolder viewHolder);
    }

}

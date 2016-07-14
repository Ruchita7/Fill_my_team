package com.android.fillmyteam;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.fillmyteam.data.PlayerMatchesColumns;
import com.android.fillmyteam.model.Match;
import com.android.fillmyteam.util.Utility;

import java.util.GregorianCalendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter for handling upcoming matches
 * @author Ruchita_Maheshwary
 *
 */
public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.ViewHolder> {
    List<Match> matchesList;
    Context mContext;
    Cursor mCursor;
    ItemChoiceManager mIcm;
    final MatchAdapterOnClickHandler mClickHandler;
    final View mEmptyView;
    int mChoiceMode;

    public MatchAdapter(Context context, List<Match> matches,MatchAdapterOnClickHandler adapterClickHandler,View emptyView,int choiceMode) {
        matchesList = matches;
        mContext = context;
        mEmptyView=emptyView;
        mClickHandler=adapterClickHandler;
        mIcm = new ItemChoiceManager(this);
        mIcm.setChoiceMode(choiceMode);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener  {
        @BindView(R.id.playing_time)
        TextView playingTime;
        @BindView(R.id.playing_place)
        TextView playingPlace;
        @BindView(R.id.playing_with)
        TextView player;
        @BindView(R.id.game_image_view)
        ImageView sport;
        @BindView(R.id.playing_sport)
        TextView playingSport;

        @BindView(R.id.playing_with_label)
        TextView playingWihLabel;

        @BindView(R.id.playing_on_label)
        TextView playingOnLabel;

        @BindView(R.id.playing_where_label)
        TextView playingWhereLabel;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            mCursor.moveToPosition(position);
            int location = mCursor.getColumnIndex(PlayerMatchesColumns.PLAYING_PLACE);
            mClickHandler.itemClick(mCursor.getString(location),this);

            mIcm.onClick(this);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        holder.player.setText(mCursor.getString(MatchesFragment.COL_PLAYER_NAME));
        long time = mCursor.getLong(MatchesFragment.COL_PLAYING_TIME);
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTimeInMillis(time);
        String displayTime = Utility.getCurrentDate(gregorianCalendar) + " " + Utility.getCurrentTime(gregorianCalendar);
        holder.playingTime.setText(displayTime);
        holder.playingPlace.setText(mCursor.getString(MatchesFragment.COL_PLAYING_PLACE));
        String sport = mCursor.getString(MatchesFragment.COL_PLAYING_SPORT);
        int sportDrawable = Utility.retrieveSportsIcon(sport);
        if (sportDrawable != 0) {
            holder.sport.setImageDrawable(mContext.getDrawable(sportDrawable));
            holder.sport.setContentDescription(mContext.getString(R.string.play_sport,sport));
        }
        holder.playingSport.setText(sport);
        mIcm.onBindViewHolder(holder, position);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mIcm.onRestoreInstanceState(savedInstanceState);
    }

    public void selectView(RecyclerView.ViewHolder viewHolder) {
        if ( viewHolder instanceof ViewHolder ) {
            ViewHolder vh = (ViewHolder)viewHolder;
            vh.onClick(vh.itemView);
        }
    }


    public void onSaveInstanceState(Bundle outState) {
        mIcm.onSaveInstanceState(outState);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.match_detail_list_item, parent, false);
        view.setFocusable(true);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
           mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public static  interface MatchAdapterOnClickHandler    {
        public void itemClick(String location, ViewHolder viewHolder);
    }

}

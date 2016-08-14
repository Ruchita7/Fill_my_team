package com.sample.android.fillmyteam;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sample.android.fillmyteam.util.Utility;

import java.util.List;

/**
 * Created by dgnc on 8/14/2016.
 */
public class PlayTabAdapter extends RecyclerView.Adapter<PlayTabAdapter.PlayViewHolder> {

    Context mContext;
    List<String> mPlayItems;
    PlayAdapterOnClickHandler mPlayItemClickHandler;

    public PlayTabAdapter(Context context, List<String> playItems,PlayAdapterOnClickHandler playItemClickHandler) {
        mContext = context;
        mPlayItems = playItems;
        mPlayItemClickHandler=playItemClickHandler;
    }


    @Override
    public PlayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.play_tab_list_item, parent, false);
        view.setFocusable(true);
        return new PlayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PlayViewHolder holder, int position) {
        String item = mPlayItems.get(position);
        holder.textView.setText(item);
        int iconRes = Utility.retrieveMenuIcon(position);
        holder.imageView.setImageResource(iconRes);
    }

    @Override
    public int getItemCount() {
        return mPlayItems.size();
    }


    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public class PlayViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView textView;
        ImageView imageView;

        public PlayViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.item_name);
            imageView = (ImageView) itemView.findViewById(R.id.item_icon);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            mPlayItemClickHandler.itemClick(position,this);
        }
    }

    public static interface PlayAdapterOnClickHandler {
        public void itemClick(int position, PlayViewHolder viewHolder);
    }
}

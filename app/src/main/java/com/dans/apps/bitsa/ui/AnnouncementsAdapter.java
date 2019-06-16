package com.dans.apps.bitsa.ui;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;


import com.dans.apps.bitsa.Constants;
import com.dans.apps.bitsa.R;
import com.dans.apps.bitsa.callbacks.FragmentAdapterInteractionListener;
import com.dans.apps.bitsa.model.Announcement;
import com.dans.apps.bitsa.model.User;
import com.dans.apps.bitsa.utils.LogUtils;
import com.dans.apps.bitsa.utils.UiUtils;
import com.dans.apps.bitsa.widget.ExpandableTextView;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by duncan on 12/21/17.
 */

public class AnnouncementsAdapter extends
        RecyclerView.Adapter<AnnouncementsAdapter.AnnouncementRequestViewHolder> {
    private List<Announcement> items;
    private final Activity host;
    private final LayoutInflater layoutInflater;
    private final FragmentAdapterInteractionListener listener;
    private BaseFragment baseFragment;

    AnnouncementsAdapter(Activity context,BaseFragment fragment, FragmentAdapterInteractionListener listener) {
        this.layoutInflater = LayoutInflater.from(context);
        items = new ArrayList<>();
        this.host = context;
        this.listener= listener;
        this.baseFragment = fragment;
    }


    @Override
    public AnnouncementRequestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final AnnouncementRequestViewHolder holder =
                new AnnouncementRequestViewHolder(layoutInflater.inflate(R.layout.announcement_list_item, parent,
                        false));

        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Announcement announcement = items.get(holder.getAdapterPosition());
                PopupMenu menu = new PopupMenu(host, v);
                menu.getMenuInflater().inflate(R.menu.announcement_item_menu, menu.getMenu());
                User user = baseFragment.getUser();
                if(user!=null) {

                    if (user.getType() != Constants.USER_TYPE.CLUB_OFFICIAL) {
                        menu.getMenu().findItem(R.id.delete).setVisible(false);
                    }
                }else{
                    menu.getMenu().findItem(R.id.delete).setVisible(false);
                }
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.share:{
                                String content = announcement.getTitle() + "\n" + announcement.getMessage();
                                UiUtils.shareText("Announcement", content, host);
                                break;
                            }
                            case R.id.delete:{
                                listener.onDelete(announcement);
                                break;
                            }
                        }

                        return false;
                    }
                });
                menu.show();
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.body.toggle();
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(AnnouncementRequestViewHolder holder, int position) {
        Announcement announcement = items.get(position);
        holder.title.setText(announcement.getTitle());
        holder.createdAt.setText(announcement.getDate().toString());
        holder.from.setText(announcement.getFrom());
        holder.body.setText(announcement.getMessage());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    void addAnnouncements(List<Announcement> requests) {
        items.clear();
        items.addAll(requests);
        notifyDataSetChanged();
    }

    public List<Announcement> getItems() {
        return items;
    }

    public void clear() {
        if(items!=null){
            items.clear();
        }
        notifyDataSetChanged();
    }

    static class AnnouncementRequestViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ExpandableTextView body;
        TextView createdAt;
        TextView from;
        ImageView more;

        AnnouncementRequestViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            body = itemView.findViewById(R.id.body);
            createdAt = itemView.findViewById(R.id.createdAt);
            more = itemView.findViewById(R.id.more);
            from = itemView.findViewById(R.id.from);
            body.setInterpolator(new OvershootInterpolator());
        }
    }

}

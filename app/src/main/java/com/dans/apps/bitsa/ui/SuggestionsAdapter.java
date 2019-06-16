package com.dans.apps.bitsa.ui;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.dans.apps.bitsa.Constants;
import com.dans.apps.bitsa.R;
import com.dans.apps.bitsa.callbacks.FragmentAdapterInteractionListener;
import com.dans.apps.bitsa.model.Announcement;
import com.dans.apps.bitsa.model.Suggestion;
import com.dans.apps.bitsa.model.User;
import com.dans.apps.bitsa.utils.UiUtils;
import com.dans.apps.bitsa.widget.ExpandableTextView;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by duncan on 12/21/17.
 */

public class SuggestionsAdapter extends
        RecyclerView.Adapter<SuggestionsAdapter.SuggestionsViewHolder> {
    private List<Suggestion> items;
    private final Activity host;
    private final LayoutInflater layoutInflater;
    private final FragmentAdapterInteractionListener listener;
    private BaseFragment basefragment;

    SuggestionsAdapter(Activity context,BaseFragment fragment,FragmentAdapterInteractionListener listener) {
        this.layoutInflater = LayoutInflater.from(context);
        items = new ArrayList<>();
        this.host = context;
        this.basefragment = fragment;
        this.listener= listener;
    }


    @Override
    public SuggestionsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final SuggestionsViewHolder holder =
                new SuggestionsViewHolder(layoutInflater.inflate(R.layout.suggestion_list_item, parent,
                        false));

        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Suggestion suggestion = items.get(holder.getAdapterPosition());
                PopupMenu menu = new PopupMenu(host, v);
                menu.getMenuInflater().inflate(R.menu.announcement_item_menu, menu.getMenu());
                User user = basefragment.getUser();
                if(user!=null) {
                    if (suggestion.getSenderEmail().equals(user.getEmail())) {
                        menu.getMenu().findItem(R.id.delete).setVisible(true);
                    } else {
                        if (user.getType() != Constants.USER_TYPE.CLUB_OFFICIAL) {
                            menu.getMenu().findItem(R.id.delete).setVisible(false);
                        }
                    }
                }else{
                    menu.getMenu().findItem(R.id.delete).setVisible(false);
                }

                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.share:{
                                String content = suggestion.getTitle() + "\n" + suggestion.getBody();
                                UiUtils.shareText("Announcement", content, host);
                                break;
                            }
                            case R.id.delete:{
                                listener.onDelete(suggestion);
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
    public void onBindViewHolder(SuggestionsViewHolder holder, int position) {
        Suggestion suggestion = items.get(position);
        holder.title.setText(suggestion.getTitle());
        holder.from.setText(suggestion.getFrom());
        holder.body.setText(suggestion.getBody());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addSuggestions(List<Suggestion> suggestions) {
        items.clear();
        items.addAll(suggestions);
        notifyDataSetChanged();
    }

    public List<Suggestion> getItems() {
        return items;
    }

    public void clear() {
        if(items!=null){
            items.clear();
        }
        notifyDataSetChanged();
    }

    static class SuggestionsViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ExpandableTextView body;
        TextView from;
        ImageView more;

        SuggestionsViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            body = itemView.findViewById(R.id.body);
            from = itemView.findViewById(R.id.from);
            more = itemView.findViewById(R.id.more);
            body.setInterpolator(new OvershootInterpolator());
        }
    }

}

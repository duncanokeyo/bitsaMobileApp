package com.dans.apps.bitsa.ui;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.dans.apps.bitsa.Constants;
import com.dans.apps.bitsa.R;
import com.dans.apps.bitsa.callbacks.FragmentAdapterInteractionListener;
import com.dans.apps.bitsa.model.Announcement;
import com.dans.apps.bitsa.model.User;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by duncan on 12/21/17.
 */

public class StudentsAdapter extends
        RecyclerView.Adapter<StudentsAdapter.UserViewHolder> {
    private List<User> items;
    private final Activity host;
    private final LayoutInflater layoutInflater;
    private final FragmentAdapterInteractionListener listener;
    private BaseFragment baseFragment;
    private StudentsFragment studentsFragment;
    private final int lightOrange;
    StudentsAdapter(Activity context, BaseFragment fragment,StudentsFragment studentsFragment,
                    FragmentAdapterInteractionListener listener) {
        this.layoutInflater = LayoutInflater.from(context);
        items = new ArrayList<>();
        this.host = context;
        this.listener= listener;
        lightOrange = host.getResources().getColor(android.R.color.holo_orange_light);
        this.baseFragment = fragment;
        this.studentsFragment = studentsFragment;
    }


    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final UserViewHolder holder =
                new UserViewHolder(layoutInflater.inflate(R.layout.student_list_item, parent,
                        false));

        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final User student = items.get(holder.getAdapterPosition());
                PopupMenu menu = new PopupMenu(host, v);
                menu.getMenuInflater().inflate(R.menu.student_item_menu, menu.getMenu());
                final User user = baseFragment.getUser();

                if(user!=null) {
                    if (user.getType() != Constants.USER_TYPE.CLUB_OFFICIAL) { //club officials cannot delete students,but can add,the only one who can delete if super user or HOD
                        menu.getMenu().findItem(R.id.delete).setVisible(false);
                    }
                }else{
                    menu.getMenu().findItem(R.id.delete).setVisible(false);
                }
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.update:{
                                listener.onUpdate(student);
                                break;
                            }
                            case R.id.delete:{
                                listener.onDelete(student);
                                break;
                            }
                        }

                        return false;
                    }
                });
                menu.show();
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        User user = items.get(position);
        holder.name.setText(user.getName());
        holder.schoolID.setText(user.getSchoolID());
        holder.major.setText(user.getMajor());

        if(user.getType() == Constants.USER_TYPE.CLUB_OFFICIAL){
            holder.info.setVisibility(View.VISIBLE);
            setRole(holder.info,user);
        }else{
            holder.info.setVisibility(View.GONE);
        }
    }

    private void setRole(TextView view, User user){
        String role = studentsFragment.getRole(user.getRole());
        view.setBackgroundTintList(ColorStateList.valueOf(lightOrange));
        view.setText("Role: "+role);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    void addItems(List<User> users) {
        items.clear();
        items.addAll(users);
        notifyDataSetChanged();
    }

    public List<User> getItems() {
        return items;
    }

    public void clear() {
        if(items!=null){
            items.clear();
        }
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView schoolID;
        TextView major;
        ImageView more;
        TextView info;

        UserViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            schoolID = itemView.findViewById(R.id.school_id);
            major = itemView.findViewById(R.id.major);
            more = itemView.findViewById(R.id.more);
            info = itemView.findViewById(R.id.info);
        }
    }

}

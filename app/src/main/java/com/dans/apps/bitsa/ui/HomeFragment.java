package com.dans.apps.bitsa.ui;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dans.apps.bitsa.Constants;
import com.dans.apps.bitsa.NewAnnouncementActivity;
import com.dans.apps.bitsa.R;
import com.dans.apps.bitsa.callbacks.FragmentAdapterInteractionListener;
import com.dans.apps.bitsa.model.Announcement;
import com.dans.apps.bitsa.model.Entity;
import com.dans.apps.bitsa.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends BaseFragment
        implements FragmentAdapterInteractionListener {

    public HomeFragment() { }

    AnnouncementsAdapter adapter;
    DatabaseReference announcementReference;
    User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.base_layout, container, false);
        initViews(view);
    //    refreshLayout.setRefreshing(true);
        message.setText(R.string.no_notifications);
        adapter = new AnnouncementsAdapter(getActivity(),this,this);
        list.setAdapter(adapter);

        shouldHideAddButton(getUser());
        return view;
    }


    private void fetchMessages(){
        announcementReference = FirebaseDatabase.getInstance().
                getReference().child(Constants.PATHS.announcements);
        announcementReference.addValueEventListener(this);
    }


    @Override
    public void onStart() {
        super.onStart();
        fetchMessages();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(announcementReference!=null){
            announcementReference.removeEventListener(this);
        }
    }


    @Override
    void onRefresh() {
        fetchMessages();
    }

    @Override
    void onFirebaseDataChange(@NonNull DataSnapshot dataSnapshot) {
        List<Announcement> announcements = new ArrayList<>();
        for(DataSnapshot snapshot:dataSnapshot.getChildren()){
            Announcement announcement = snapshot.getValue(Announcement.class);
            announcement.setId(snapshot.getKey());
            announcements.add(announcement);
        }
        adapter.addAnnouncements(announcements);
    }

    @Override
    void showOnEmptyContentMessage() {
        adapter.clear();
        message.setText(R.string.no_notifications);
    }

    @Override
    void showOnDatabaseErrorOccured(@NonNull DatabaseError databaseError) {
        message.setText(R.string.error_occured);
    }


    @Override
    void onAddClicked() {
        Intent intent = new Intent(getActivity(),NewAnnouncementActivity.class);
        startActivity(intent);
    }

    @Override
    void reloadAdapters() {
        if(adapter!=null){
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    void onShowHideAddButton() {}


    @Override
    public void viewContent(Entity entity) {

    }

    @Override
    public void onDelete(Entity entity) {
        Announcement announcement = (Announcement) entity;
        FirebaseDatabase.getInstance().getReference().child(Constants.PATHS.announcements).
                child(announcement.getId()).
                removeValue().addOnFailureListener(getActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(),R.string.error_occured,Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onUpdate(Entity entity) {

    }
}

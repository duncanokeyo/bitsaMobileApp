package com.dans.apps.bitsa.ui;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Toast;

import com.dans.apps.bitsa.Constants;
import com.dans.apps.bitsa.NewAnnouncementActivity;
import com.dans.apps.bitsa.NewSuggestionActivity;
import com.dans.apps.bitsa.R;
import com.dans.apps.bitsa.callbacks.FragmentAdapterInteractionListener;
import com.dans.apps.bitsa.model.Announcement;
import com.dans.apps.bitsa.model.Entity;
import com.dans.apps.bitsa.model.Suggestion;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class SuggestionFragment extends BaseFragment implements FragmentAdapterInteractionListener {

    public SuggestionFragment() { }

    SuggestionsAdapter adapter;
    DatabaseReference suggestionReference;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.base_layout, container, false);
        initViews(view);
        //    refreshLayout.setRefreshing(true);
        message.setText(R.string.no_suggestions);
        adapter = new SuggestionsAdapter(getActivity(),this,this);
        list.setAdapter(adapter);
        add.setVisibility(View.VISIBLE);
        return view;
    }


    private void fetchSuggestions(){
        suggestionReference = FirebaseDatabase.getInstance().
                getReference().child(Constants.PATHS.suggestions);
        suggestionReference.addValueEventListener(this);
    }


    @Override
    public void onStart() {
        super.onStart();
        fetchSuggestions();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(suggestionReference!=null){
            suggestionReference.removeEventListener(this);
        }
    }


    @Override
    void onRefresh() {
        fetchSuggestions();
    }

    @Override
    void onFirebaseDataChange(@NonNull DataSnapshot dataSnapshot) {
        List<Suggestion> suggestions = new ArrayList<>();
        for(DataSnapshot snapshot:dataSnapshot.getChildren()){
            Suggestion suggestion = snapshot.getValue(Suggestion.class);
            suggestion.setId(snapshot.getKey());
            suggestions.add(suggestion);
        }
        adapter.addSuggestions(suggestions);
    }

    @Override
    void showOnEmptyContentMessage() {
        adapter.clear();
        message.setText(R.string.no_suggestions);
    }

    @Override
    void showOnDatabaseErrorOccured(@NonNull DatabaseError databaseError) {
        message.setText(R.string.error_occured);
    }

    @Override
    void onAddClicked() {
        Intent intent = new Intent(getActivity(),NewSuggestionActivity.class);
        startActivity(intent);
    }

    @Override
    void reloadAdapters() {
        if(adapter!=null){
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    void onShowHideAddButton() {
        add.setVisibility(View.VISIBLE);
    }


    @Override
    public void viewContent(Entity entity) {

    }

    @Override
    public void onDelete(Entity entity) {
        Suggestion suggestion = (Suggestion) entity;
        FirebaseDatabase.getInstance().getReference().child(Constants.PATHS.suggestions).
                child(suggestion.getId()).
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

package com.dans.apps.bitsa.ui;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dans.apps.bitsa.Constants;
import com.dans.apps.bitsa.R;
import com.dans.apps.bitsa.StudentActivity;
import com.dans.apps.bitsa.callbacks.FragmentAdapterInteractionListener;
import com.dans.apps.bitsa.model.Announcement;
import com.dans.apps.bitsa.model.Entity;
import com.dans.apps.bitsa.model.Role;
import com.dans.apps.bitsa.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class StudentsFragment extends BaseFragment implements FragmentAdapterInteractionListener {
    String TAG = "StudentsFragment";

    public StudentsFragment() {}

    StudentsAdapter adapter;
    DatabaseReference reference;
    DatabaseReference roleReference;

    ArrayList<Role>roles = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.base_layout, container, false);
        initViews(view);
        adapter = new StudentsAdapter(getActivity(),this,this,this);
        list.setAdapter(adapter);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        fetchRoles();

    }


    private ValueEventListener roleListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            roles.clear();
            for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                Role role = snapshot.getValue(Role.class);
                roles.add(role);
            }

            fetchStudents();
        }
        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {}
    };


    @Override
    public void onStop() {
        super.onStop();
        if(reference!=null){
            reference.removeEventListener(this);
        }
        if(roleReference!=null){
            roleReference.removeEventListener(roleListener);
        }
    }

    @Override
    void onFirebaseDataChange(@NonNull DataSnapshot dataSnapshot) {
        List<User> users = new ArrayList<>();
        for(DataSnapshot snapshot:dataSnapshot.getChildren()){
            User user = snapshot.getValue(User.class);
            user.setId(snapshot.getKey());
            users.add(user);
        }
        adapter.addItems(users);
    }

    private void fetchStudents(){
        reference = FirebaseDatabase.getInstance().getReference().child(Constants.PATHS.users);
        reference.orderByChild("type").startAt(Constants.USER_TYPE.STUDENT);
        reference.addValueEventListener(this);
    }
    private void fetchRoles(){
        roleReference = FirebaseDatabase.getInstance().getReference(Constants.PATHS.roles);
        roleReference.addValueEventListener(roleListener);
    }

    @Override
    void showOnEmptyContentMessage() {
        adapter.clear();
        message.setText(R.string.no_students);
    }

    @Override
    void showOnDatabaseErrorOccured(@NonNull DatabaseError databaseError) {
        message.setText(R.string.error_occured);
    }




    @Override
    void onRefresh() {
        fetchStudents();
    }

    @Override
    public void viewContent(Entity entity) {}

    @Override
    public void onDelete(Entity entity) {
        User user = (User) entity;
        reference.child(user.getId()).removeValue();
    }

    @Override
    void onAddClicked() {
        Intent intent = new Intent(getActivity(), StudentActivity.class);
        intent.putExtra(StudentActivity.KEY_ACTION,StudentActivity.ACTION_ADD);
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
        int userType = getUser().getType();
        if(userType == Constants.USER_TYPE.CLUB_OFFICIAL
                || userType == Constants.USER_TYPE.SUPER_ADMIN){
            add.setVisibility(View.VISIBLE);
        }else{
            add.setVisibility(View.GONE);
        }
    }

    @Override
    public void onUpdate(Entity entity) {
        User user = (User) entity;
        Intent intent = new Intent(getActivity(), StudentActivity.class);
        intent.putExtra(StudentActivity.KEY_ACTION,StudentActivity.ACTION_UPDATE);
        intent.putExtra(StudentActivity.KEY_ID,user.getId());
        startActivity(intent);
    }
    String getRole(int code) {
        for(Role role:roles){
            if(role.getCode() == code){
                return role.getTitle();
            }
        }
        return null;
    }
}

package com.dans.apps.bitsa.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dans.apps.bitsa.Constants;
import com.dans.apps.bitsa.ProjectActivity;
import com.dans.apps.bitsa.R;
import com.dans.apps.bitsa.callbacks.FragmentAdapterInteractionListener;
import com.dans.apps.bitsa.model.Entity;
import com.dans.apps.bitsa.model.Project;
import com.dans.apps.bitsa.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

public class SeniorYearProjectsFragment extends BaseFragment implements FragmentAdapterInteractionListener {

    public SeniorYearProjectsFragment() { }

    ProjectsAdapter adapter;
    DatabaseReference seniorYearProjectsReference;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.base_layout, container, false);
        initViews(view);
        //    refreshLayout.setRefreshing(true);
        ///message.setText(R.string.no_notifications);
        adapter = new ProjectsAdapter(getActivity(),this,this);
        list.setAdapter(adapter);
        return view;
    }


    private void fetchContent(){
        seniorYearProjectsReference = FirebaseDatabase.getInstance().
                getReference().child(Constants.PATHS.seniorYearProjects);
        seniorYearProjectsReference.addValueEventListener(this);
    }


    @Override
    public void onStart() {
        super.onStart();
        fetchContent();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(seniorYearProjectsReference !=null){
            seniorYearProjectsReference.removeEventListener(this);
        }
    }


    @Override
    void onRefresh() {
        fetchContent();
    }

    @Override
    void onFirebaseDataChange(@NonNull DataSnapshot dataSnapshot) {
        List<Project> projects = new ArrayList<>();
        for(DataSnapshot snapshot:dataSnapshot.getChildren()){
            Project project = snapshot.getValue(Project.class);
            project.setId(snapshot.getKey());
            projects.add(project);
        }
        adapter.addProjects(projects);
    }

    @Override
    void showOnEmptyContentMessage() {
        adapter.clear();
        message.setText(R.string.no_senior_year_projects);
    }

    @Override
    void showOnDatabaseErrorOccured(@NonNull DatabaseError databaseError) {
        message.setText(R.string.error_occured);
    }

    @Override
    void onAddClicked() {
        Intent intent = new Intent(getActivity(),ProjectActivity.class);
        intent.putExtra(ProjectActivity.KEY_ACTION,ProjectActivity.ACTION_ADD);
        intent.putExtra(ProjectActivity.KEY_PROJECT_TYPE,Constants.PROJECT_TYPES.SENIOR_PROJECTS);
        startActivity(intent);
    }

    @Override
    void reloadAdapters() {
        if(adapter!=null){
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    void onShowHideAddButton() { }




    @Override
    public void viewContent(Entity entity) {
        Project project = (Project) entity;
        if(((Project) entity).getProjectMembers().isEmpty()){
            Toast.makeText(getActivity(),R.string.no_project_members,Toast.LENGTH_SHORT).show();
            return;
        }
        ProjectMemberListFragment fragment = ProjectMemberListFragment.newInstance(project.getProjectMembers());
        FragmentManager manager = getFragmentManager();
        fragment.showNow(manager,"project_member_list_fragment");
    }

    @Override
    public void onDelete(Entity entity) {
        Project project = (Project) entity;
        FirebaseDatabase.getInstance().getReference().child(Constants.PATHS.seniorYearProjects).
                child(project.getId()).
                removeValue().addOnFailureListener(getActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(),R.string.error_occured,Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onUpdate(Entity entity) {
        Project project = (Project) entity;
        Intent intent = new Intent(getActivity(),ProjectActivity.class);
        intent.putExtra(ProjectActivity.KEY_ACTION,ProjectActivity.ACTION_UPDATE);
        intent.putExtra(ProjectActivity.KEY_PROJECT_TYPE,Constants.PROJECT_TYPES.SENIOR_PROJECTS);
        intent.putExtra(ProjectActivity.KEY_PROJECT_ID,project.getId());
        startActivity(intent);
    }
}

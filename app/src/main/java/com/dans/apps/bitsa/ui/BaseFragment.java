package com.dans.apps.bitsa.ui;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dans.apps.bitsa.Constants;
import com.dans.apps.bitsa.R;
import com.dans.apps.bitsa.model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public abstract class BaseFragment extends Fragment implements ValueEventListener {
    String TAG = "BaseFragment";

    TextView message;
    ProgressBar progressBar;
    RecyclerView list;
    SwipeRefreshLayout refreshLayout;
    LinearLayoutManager layoutManager;
    FloatingActionButton add;
    FirebaseUser user;
    User currentUser;


    public interface Callback{

        User getCurrentUser();
    }

    Callback callback;

    void initViews(@NonNull View view){
        message = view.findViewById(R.id.message);
        progressBar = view.findViewById(R.id.progress_bar);
        list = view.findViewById(R.id.list);
        refreshLayout = view.findViewById(R.id.swipe_refresh);
        add = view.findViewById(R.id.add);
        add.setVisibility(View.GONE);
        user = FirebaseAuth.getInstance().getCurrentUser();
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                BaseFragment.this.onRefresh();
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddClicked();
            }
        });

        message.setVisibility(View.GONE);
        layoutManager =
                new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        list.setLayoutManager(layoutManager);

        shouldHideAddButton(getUser());
    }

    public void onUserFetched(User currentUser){
        this.currentUser = currentUser;
        if(currentUser.getType() == Constants.USER_TYPE.CLUB_OFFICIAL
                || currentUser.getType() == Constants.USER_TYPE.SUPER_ADMIN){
            add.setVisibility(View.VISIBLE);
        }else{
            add.setVisibility(View.GONE);
        }
        onShowHideAddButton();
        reloadAdapters();
    }

    public User getUser() {
        if(currentUser!=null){
            return currentUser;
        }
        currentUser = callback.getCurrentUser();
        return currentUser;
    }

    void shouldHideAddButton(User user){
        if(user == null)return;
        if(currentUser.getType() == Constants.USER_TYPE.CLUB_OFFICIAL
                || currentUser.getType() == Constants.USER_TYPE.SUPER_ADMIN){
            add.setVisibility(View.VISIBLE);
        }else{
            add.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof Callback){
            callback = (Callback) context;
        }
    }


    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        Log.d(TAG,"data snapshot "+dataSnapshot.toString());
        progressBar.setVisibility(View.GONE);
        refreshLayout.setRefreshing(false);
        if(dataSnapshot.getValue()==null){
            message.setVisibility(View.VISIBLE);
            showOnEmptyContentMessage();
        }else{
            if(dataSnapshot.getChildrenCount()==0){
                onFirebaseDataChange(dataSnapshot);// show error
                message.setVisibility(View.VISIBLE);
                showOnEmptyContentMessage();
            }else{
                message.setVisibility(View.GONE);
                onFirebaseDataChange(dataSnapshot);
            }
        }

    }



    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {
        showOnDatabaseErrorOccured(databaseError);
    }

    abstract void onFirebaseDataChange(@NonNull DataSnapshot dataSnapshot);
    abstract void showOnEmptyContentMessage();
    abstract void showOnDatabaseErrorOccured(@NonNull DatabaseError databaseError);
    abstract void onAddClicked();
    abstract void reloadAdapters();
    abstract void onShowHideAddButton();
    abstract  void onRefresh();
}

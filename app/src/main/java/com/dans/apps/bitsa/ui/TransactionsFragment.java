package com.dans.apps.bitsa.ui;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dans.apps.bitsa.Constants;
import com.dans.apps.bitsa.R;
import com.dans.apps.bitsa.callbacks.FragmentAdapterInteractionListener;
import com.dans.apps.bitsa.model.Entity;
import com.dans.apps.bitsa.model.Transaction;
import com.dans.apps.bitsa.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class TransactionsFragment  extends BaseFragment implements FragmentAdapterInteractionListener {

    public TransactionsFragment() { }

    TransactionsAdapter adapter;
    DatabaseReference reference;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.base_layout, container, false);
        initViews(view);
        add.setVisibility(View.GONE);
        //    refreshLayout.setRefreshing(true);
        message.setText(R.string.no_transactions);
        adapter = new TransactionsAdapter(getActivity());

        list.setAdapter(adapter);
        return view;
    }


    private void fetchMessages(){
        reference = FirebaseDatabase.getInstance().
                getReference().child(Constants.PATHS.transactions);
        reference.orderByChild("email").equalTo(user.getEmail());
        reference.addValueEventListener(this);
    }


    @Override
    public void onStart() {
        super.onStart();
        fetchMessages();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(reference !=null){
            reference.removeEventListener(this);
        }
    }


    @Override
    void onRefresh() {
        fetchMessages();
    }

    @Override
    void onFirebaseDataChange(@NonNull DataSnapshot dataSnapshot) {
        List<Transaction> transactions = new ArrayList<>();
        for(DataSnapshot snapshot:dataSnapshot.getChildren()){
            Transaction transaction = snapshot.getValue(Transaction.class);
            transaction.setId(snapshot.getKey());
            transactions.add(transaction);
        }
        adapter.addTransactions(transactions);
    }

    @Override
    void showOnEmptyContentMessage() {
        adapter.clear();
        message.setText(R.string.no_transactions);
    }

    @Override
    void showOnDatabaseErrorOccured(@NonNull DatabaseError databaseError) {
        message.setText(R.string.error_occured);
    }

    @Override
    void onAddClicked() {}

    @Override
    void reloadAdapters() {

    }

    @Override
    void onShowHideAddButton() {

    }

    @Override
    public void viewContent(Entity entity) {}
    @Override
    public void onDelete(Entity entity) {}
    @Override
    public void onUpdate(Entity entity) {}
}

package com.dans.apps.bitsa.ui;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.dans.apps.bitsa.Constants;
import com.dans.apps.bitsa.R;
import com.dans.apps.bitsa.callbacks.FragmentAdapterInteractionListener;
import com.dans.apps.bitsa.model.Announcement;
import com.dans.apps.bitsa.model.Transaction;
import com.dans.apps.bitsa.utils.UiUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by duncan on 12/21/17.
 */

public class TransactionsAdapter extends
        RecyclerView.Adapter<TransactionsAdapter.TransactionsViewHolder> {
    private List<Transaction> items;
    private final Activity host;
    private final LayoutInflater layoutInflater;
    private final int lightOrange;
    private final int lightBlue;
    public TransactionsAdapter(Activity context) {
        this.layoutInflater = LayoutInflater.from(context);
        items = new ArrayList<>();
        this.host = context;
        lightOrange = host.getResources().getColor(android.R.color.holo_orange_light);
        lightBlue = host.getResources().getColor(android.R.color.holo_blue_light);
    }

    @Override
    public TransactionsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final TransactionsViewHolder holder =
                new TransactionsViewHolder(layoutInflater.inflate(R.layout.transaction_list_item, parent,
                        false));
        return holder;
    }

    @Override
    public void onBindViewHolder(TransactionsViewHolder holder, int position) {
        Transaction transaction = items.get(position);
        holder.reference.setText("Reference: "+transaction.getReferenceNumber());
        holder.date.setText(transaction.getPaymentDate());
        if(transaction.getType() == Constants.TRANSACTION_TYPE.CLUB_PAYMENT) {
            holder.info.setBackgroundTintList(ColorStateList.valueOf(lightBlue));
            holder.info.setText("Semester: " + transaction.getSemester());
        }else{
            holder.info.setBackgroundTintList(ColorStateList.valueOf(lightOrange));
            holder.info.setText(R.string.contribution);
        }
        holder.amount.setText("Amount: "+transaction.getAmount());

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addTransactions(List<Transaction> transactions) {
        items.clear();
        items.addAll(transactions);
        notifyDataSetChanged();
    }

    public List<Transaction> getItems() {
        return items;
    }

    public void clear() {
        if(items!=null){
            items.clear();
        }
        notifyDataSetChanged();
    }

    static class TransactionsViewHolder extends RecyclerView.ViewHolder {
        TextView date;
        TextView reference;
        TextView amount;
        TextView info;

        TransactionsViewHolder(View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date);
            reference = itemView.findViewById(R.id.reference);
            amount = itemView.findViewById(R.id.amount);
            info = itemView.findViewById(R.id.info);
        }
    }

}

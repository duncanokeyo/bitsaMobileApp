package com.dans.apps.bitsa.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dans.apps.bitsa.R;
import com.dans.apps.bitsa.model.Member;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ProjectMemberListFragment extends DialogFragment {
    String TAG = "ProjectMemberListFragment";
    static List<String>projectMembers = new ArrayList<>();


    public static ProjectMemberListFragment newInstance(List<String>members){
        projectMembers = members;
        return  new ProjectMemberListFragment();
    }

    RecyclerView list;
    LinearLayoutManager layoutManager;
    ProjectMemberViewAdapter adapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fragment_project_member_list,container,false);
        list = view.findViewById(R.id.list);

        layoutManager =
                new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        list.setLayoutManager(layoutManager);
        adapter = new ProjectMemberViewAdapter(getActivity());
        list.setAdapter(adapter);

        showMembers();

        return view;
    }

    private void showMembers() {
        adapter.addItems(projectMembers);
    }


    public static class ProjectMemberViewAdapter extends RecyclerView.Adapter<ProjectMemberViewAdapter.MemberViewHolder> {
        List<Member>items;
        Activity host;

        ProjectMemberViewAdapter(Activity host) {
            items = new ArrayList<>();
            this.host = host;
        }

        @Override
        public MemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.project_member_list_item, parent, false);
            final MemberViewHolder holder = new MemberViewHolder(view);

            //when the user clicks on the item. it will display the doctor details
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });
            holder.more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Member member = items.get(holder.getAdapterPosition());
                    PopupMenu menu = new PopupMenu(host,holder.more);
                    menu.getMenuInflater().inflate(R.menu.project_member_item,menu.getMenu());

                    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()){
                                case R.id.email:
                                    if(TextUtils.isEmpty(member.getEmail())){
                                        Toast.makeText(host,R.string.no_email_set,Toast.LENGTH_SHORT).show();
                                        return false;
                                    }

                                    try{
                                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                                                "mailto",member.getEmail(), null));
                                        host.startActivity(Intent.createChooser(emailIntent, "Send email..."));
                                    }catch (Exception e){
                                        Toast.makeText(host,R.string.email_send_failed,Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                case R.id.call:
                                    if(TextUtils.isEmpty(member.getPhoneNumber())){
                                        Toast.makeText(host,R.string.no_phone_number_set,Toast.LENGTH_SHORT).show();
                                        return false;
                                    }

                                    try {
                                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + member.getPhoneNumber()));
                                        host.startActivity(intent);
                                    }catch (Exception e){
                                        Toast.makeText(host,R.string.phone_call_failed,Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                            }
                            return true;
                        }
                    });
                    menu.show();
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(final MemberViewHolder holder, int position) {
            Member member = items.get(position);
            holder.name.setText(member.getName());
            holder.role.setText("Role: "+member.getRole());
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        void addItems(List<String> members) {
            items.clear();
            for(String string:members){

                Member member = new Member();
                member.setFields(string);
                Log.d("Projectmember","member-- >"+member.toString());
                items.add(member);
            }
            notifyDataSetChanged();
        }

        static class MemberViewHolder extends RecyclerView.ViewHolder {
            TextView name;
            TextView role;
            ImageView more;

            MemberViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.name);
                role = itemView.findViewById(R.id.role);
                more = itemView.findViewById(R.id.more);
            }


        }


    }


}

package com.dans.apps.bitsa.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.dans.apps.bitsa.Constants;
import com.dans.apps.bitsa.R;
import com.dans.apps.bitsa.callbacks.FragmentAdapterInteractionListener;
import com.dans.apps.bitsa.model.Announcement;
import com.dans.apps.bitsa.model.Project;
import com.dans.apps.bitsa.model.User;
import com.dans.apps.bitsa.utils.UiUtils;
import com.dans.apps.bitsa.widget.ExpandableTextView;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by duncan on 12/21/17.
 */

public class ProjectsAdapter extends
        RecyclerView.Adapter<ProjectsAdapter.ProjectsViewHolder> {

    private List<Project> items;
    private final Activity host;
    private final LayoutInflater layoutInflater;
    private final FragmentAdapterInteractionListener listener;
    private BaseFragment baseFragment;

    int ongoing;
    int complete;
    int failed;

    ProjectsAdapter(Activity context,BaseFragment fragment,FragmentAdapterInteractionListener listener) {
        this.layoutInflater = LayoutInflater.from(context);
        items = new ArrayList<>();
        this.host = context;
        this.listener= listener;
        this.baseFragment = fragment;
        ongoing = host.getResources().getColor(R.color.ongoing);
        complete = host.getResources().getColor(R.color.complete);
        failed = host.getResources().getColor(R.color.failed);
    }


    @Override
    public ProjectsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final ProjectsViewHolder holder =
                new ProjectsViewHolder(layoutInflater.inflate(R.layout.project_list_item, parent,
                        false));

        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Project project = items.get(holder.getAdapterPosition());
                PopupMenu menu = new PopupMenu(host, v);
                menu.getMenuInflater().inflate(R.menu.project_item_menu, menu.getMenu());
                User user = baseFragment.getUser();
                if(user!=null) {
                    if (project.getEmail().equals(user.getEmail())
                            || user.getType() == Constants.USER_TYPE.CLUB_OFFICIAL) {
                        menu.getMenu().findItem(R.id.update).setVisible(true);
                        menu.getMenu().findItem(R.id.delete).setVisible(true);
                    } else {
                        menu.getMenu().findItem(R.id.update).setVisible(false);
                        menu.getMenu().findItem(R.id.delete).setVisible(false);
                    }
                }else{
                    menu.getMenu().findItem(R.id.update).setVisible(false);
                    menu.getMenu().findItem(R.id.delete).setVisible(false);
                }

                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.share:{
                                String content = project.getTitle() + "\n" + project.getDescription();
                                UiUtils.shareText("Announcement", content, host);
                                break;
                            }
                            case R.id.view_project_members:{
                                listener.viewContent(project);
                                break;
                            }
                            case R.id.visit_webpage:{
                                if(project.getUrl()!=null){
                                    try{
                                        Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(project.getUrl()));
                                        host.startActivity(intent);
                                    }catch (Exception ignored){
                                        Toast.makeText(host,R.string.error_occured,Toast.LENGTH_SHORT).show();
                                    }
                                    return true;
                                }else{
                                    Toast.makeText(host,R.string.no_web_page,Toast.LENGTH_SHORT).show();
                                }
                                break;
                            }
                            case R.id.update:{
                                listener.onUpdate(project);
                                break;
                            }
                            case R.id.delete:{
                                listener.onDelete(project);
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
                holder.description.toggle();
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(ProjectsViewHolder holder, int position) {
        Project project = items.get(position);
        holder.title.setText(project.getTitle());
        holder.description.setText(project.getDescription());
        holder.duration.setText("From "+project.getStartDate()+" to "+project.getEndDate());
        holder.addedBy.setText("Added by "+project.getAddedBy());
        setProjectStatus(project,holder);
    }

    private void setProjectStatus(Project project, ProjectsViewHolder holder){

        if(project.getStatus() == Constants.PROJECT_STATUS.COMPLETE){
            holder.failedInfo.setVisibility(View.GONE);
            holder.status.setTextColor(complete);
            holder.status.setText(R.string.complete);
        }else if(project.getStatus() == Constants.PROJECT_STATUS.FAILED){
            holder.failedInfo.setVisibility(View.VISIBLE);
            holder.status.setTextColor(failed);
            holder.status.setText(R.string.failed);
        }else if(project.getStatus() == Constants.PROJECT_STATUS.ONGOING){
            holder.failedInfo.setVisibility(View.GONE);
            holder.status.setTextColor(ongoing);
            holder.status.setText(R.string.ongoing);
        }else if(project.getStatus() == Constants.PROJECT_STATUS.IDEA){
            holder.failedInfo.setVisibility(View.GONE);
            holder.status.setTextColor(ongoing);
            holder.status.setText(R.string.idea);
        }
    }



    @Override
    public int getItemCount() {
        return items.size();
    }


    public List<Project> getItems() {
        return items;
    }

    void clear() {
        if(items!=null){
            items.clear();
        }
        notifyDataSetChanged();
    }

    void addProjects(List<Project> projects) {
        items.clear();
        items.addAll(projects);
        notifyDataSetChanged();
    }

    static class ProjectsViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ExpandableTextView description;
        TextView duration;
        TextView status;
        ImageView failedInfo;
        TextView addedBy;
        ImageView more;

        ProjectsViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
            duration = itemView.findViewById(R.id.duration);
            more = itemView.findViewById(R.id.more);
            status = itemView.findViewById(R.id.status);

            failedInfo = itemView.findViewById(R.id.failed_info);
            description.setInterpolator(new OvershootInterpolator());
            addedBy = itemView.findViewById(R.id.added_by);
        }
    }

}

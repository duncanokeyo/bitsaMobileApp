package com.dans.apps.bitsa.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Project extends Entity {

    /**
     * the email of the person who added it
     */
    String email;
    /**
     * the person who added this project
     */
    String addedBy;
    /**
     * The title of the project
     */
    String title;
    /**
     * A little description about the project
     */
    String description;
    /**
     * Status of the project
     * <pre>
     * 5 for ongoing
     * 6 for completed
     * 7 for failed
     * </pre>
     */
    int status;

    /**
     * An external url where the project is hosted
     */
    String url;

    /**
     * list of project members
     * if the number of project members is equal to one, then he/she is the project owner
     */
    List<String> projectMembers=new ArrayList<>();

    /**
     * A brief reason why the project failed.
     */
    String failureReason;

    /**
     * the date this project started
     */
    String startDate;
    /**
     * the date this project is expected to finish
     */
    String endDate;

    Object addedAt;

    public Project() {
    }

    public Project(
                   String email,
                   String addedBy,
                   String title,
                   String description,
                   int status,
                   String url,
                   List<String> projectMembers,
                   String failureReason,
                   String startDate,
                   String endDate) {

        this.email = email;
        this.title = title;
        this.addedBy = addedBy;
        this.description = description;
        this.status = status;
        this.url = url;
        this.projectMembers = projectMembers;
        this.failureReason = failureReason;
        this.startDate = startDate;
        this.endDate = endDate;
        this.addedAt = ServerValue.TIMESTAMP;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getProjectMembers() {
        return projectMembers;
    }

    public void setProjectMembers(List<String> projectMembers) {
        this.projectMembers = projectMembers;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Object getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Object addedAt) {
        this.addedAt = addedAt;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    @Exclude
    public Date getAddedDate(){
        Timestamp tsp = new Timestamp((Long)addedAt);
        return new Date(tsp.getTime());
    }

    @Override
    public String toString() {
        return "Project{" +
                "addedBy='" + addedBy + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", url='" + url + '\'' +
                ", projectMembers=" + projectMembers +
                ", failureReason='" + failureReason + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        Project project = (Project) o;
        Collections.sort(projectMembers);
        Collections.sort(project.projectMembers);
        return status == project.status &&
                Objects.equals(title, project.title) &&
                Objects.equals(description, project.description) &&
                Objects.equals(url, project.url) &&
                Objects.equals(projectMembers, project.projectMembers) &&
                Objects.equals(failureReason, project.failureReason) &&
                Objects.equals(startDate, project.startDate) &&
                Objects.equals(endDate, project.endDate);
    }

    @Override
    public int hashCode() {

        return Objects.hash(title, description, status, url, projectMembers, failureReason, startDate, endDate);
    }

    public Map<String,Object> getUpdateFields(Project project) {
        Map<String,Object>update=new HashMap<>();

        Collections.sort(projectMembers);
        Collections.sort(project.projectMembers);

        if(!Objects.equals(title,project.title)) {
            update.put("title", title);
        }
        if(!Objects.equals(description,project.description)) {
            update.put("description", description);
        }
        if(!Objects.equals(status,project.status)) {
            update.put("status", status);
        }
        if(!Objects.equals(url,project.url)) {
            update.put("url", url);
        }
        if(!Objects.equals(projectMembers,project.projectMembers)) {
            update.put("projectMembers", projectMembers);
        }
        if(!Objects.equals(failureReason,project.failureReason)) {
            update.put("failureReason", failureReason);
        }
        if(!Objects.equals(startDate,project.startDate)) {
            update.put("startDate", startDate);
        }
        if(!Objects.equals(endDate,project.endDate)) {
            update.put("endDate", endDate);
        }
        return update;
    }
}

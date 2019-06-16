package com.dans.apps.bitsa.model;

import java.util.HashMap;
import java.util.Map;

public class Semester extends Entity{

    int number;
    int startYear;
    int endYear;

    public Semester() {
    }

    public Semester(int number, int startYear, int endYear) {
        this.number = number;
        this.startYear = startYear;
        this.endYear = endYear;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getStartYear() {
        return startYear;
    }

    public void setStartYear(int startYear) {
        this.startYear = startYear;
    }

    public int getEndYear() {
        return endYear;
    }

    public void setEndYear(int endYear) {
        this.endYear = endYear;
    }


    public String formulateSemester(){
        return startYear+"/"+endYear+"."+number;
    }

    public Map<String, Object> getUpdateParams(Semester updateSemester) {
        Map<String,Object>update = new HashMap<>();
        if(updateSemester.getNumber()!=number){
            update.put("number",updateSemester.getNumber());
        }
        if(updateSemester.getStartYear()!=startYear){
            update.put("startYear",updateSemester.getStartYear());
        }
        if(updateSemester.getEndYear()!=endYear){
            update.put("endYear",updateSemester.getEndYear());
        }
        return update;
    }
}

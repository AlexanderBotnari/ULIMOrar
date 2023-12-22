package com.example.ulimorar.entities;

import java.io.Serializable;
import java.util.List;

public class Group implements Serializable {

    private String groupName;
    private String groupSymbol;
    private List<Timetable> timetables;

    public Group() {
    }

    public Group(String groupName, String groupSymbol) {
        this.groupName = groupName;
        this.groupSymbol = groupSymbol;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupSymbol() {
        return groupSymbol;
    }

    public void setGroupSymbol(String groupSymbol) {
        this.groupSymbol = groupSymbol;
    }

    public List<Timetable> getTimetables() {
        return timetables;
    }

    public void setTimetables(List<Timetable> timetables) {
        this.timetables = timetables;
    }
}

package com.example.ulimorar.entities;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Group implements Serializable {

    private String id;
    private String groupName;
    private String groupSymbol;
    private Map<String, Timetable> timetables;

    public Group() {
    }

    public Group(String id, String groupName, String groupSymbol) {
        this.id = id;
        this.groupName = groupName;
        this.groupSymbol = groupSymbol;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Map<String, Timetable> getTimetables() {
        return timetables;
    }

    public void setTimetables(Map<String, Timetable> timetables) {
        this.timetables = timetables;
    }
}

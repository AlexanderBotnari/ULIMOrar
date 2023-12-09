package com.example.ulimorar.entities;

import java.util.List;

public class Group {

    private long id;
    private String groupName;
    private String groupSymbol;
    private List<Timetable> timetables;

    public Group() {
    }

    public Group(long id, String groupName, String groupSymbol, List<Timetable> timetables) {
        this.id = id;
        this.groupName = groupName;
        this.groupSymbol = groupSymbol;
        this.timetables = timetables;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public List<Timetable> getTimetables() {
        return timetables;
    }

    public void setTimetables(List<Timetable> timetables) {
        this.timetables = timetables;
    }
}

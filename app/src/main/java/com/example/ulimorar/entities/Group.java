package com.example.ulimorar.entities;

import java.util.List;

public class Group {

    private String groupName;
    private String groupSymbol;
    private List<Timetable> timetables;

    public Group() {
    }

    public Group(String groupName, String groupSymbol, List<Timetable> timetables) {
        this.groupName = groupName;
        this.groupSymbol = groupSymbol;
        this.timetables = timetables;
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

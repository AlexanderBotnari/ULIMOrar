package com.example.ulimorar.entities;

import java.util.List;

public class Chair {

    private String chairName;
    private List<Group> groups;

    public Chair() {
    }

    public Chair(String chairName, List<Group> groups) {
        this.chairName = chairName;
        this.groups = groups;
    }

    public String getChairName() {
        return chairName;
    }

    public void setChairName(String chairName) {
        this.chairName = chairName;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }
}

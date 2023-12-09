package com.example.ulimorar.entities;

import java.util.List;

public class Chair {

    private long id;
    private String chairName;
    private List<Group> groups;

    public Chair() {
    }

    public Chair(long id, String chairName, List<Group> groups) {
        this.id = id;
        this.chairName = chairName;
        this.groups = groups;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

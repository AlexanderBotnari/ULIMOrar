package com.example.ulimorar.entities;

import java.io.Serializable;
import java.util.List;

public class Chair implements Serializable {

    private String chairName;
    private String chairSymbol;
    private List<Group> groups;

    public Chair() {
    }

    public Chair(String chairName, String chairSymbol) {
        this.chairName = chairName;
        this.chairSymbol = chairSymbol;
    }

    public String getChairName() {
        return chairName;
    }

    public void setChairName(String chairName) {
        this.chairName = chairName;
    }

    public String getChairSymbol() {
        return chairSymbol;
    }

    public void setChairSymbol(String chairSymbol) {
        this.chairSymbol = chairSymbol;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }
}

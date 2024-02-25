package com.example.ulimorar.entities;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Chair implements Serializable {

    private String id;
    private String chairName;
    private String chairSymbol;
    private Map<String, Group> groups;

    public Chair() {
    }

    public Chair(String id, String chairName, String chairSymbol) {
        this.id = id;
        this.chairName = chairName;
        this.chairSymbol = chairSymbol;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Map<String, Group> getGroups() {
        return groups;
    }

    public void setGroups(Map<String, Group> groups) {
        this.groups = groups;
    }
}

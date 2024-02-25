package com.example.ulimorar.entities;

import java.io.Serializable;
import java.util.Date;

public class Timetable implements Serializable {

    private String id;
    private String timetableName;
    private String imageUrl;
    private Long updateTime;

    public Timetable() {
    }

    public Timetable(String id, String timetableName, Long updateTime) {
        this.id = id;
        this.timetableName = timetableName;
        this.updateTime = updateTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Timetable(String timetableName){
        this.timetableName = timetableName;
    }

    public String getTimetableName() {
        return timetableName;
    }

    public void setTimetableName(String timetableName) {
        this.timetableName = timetableName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }
}

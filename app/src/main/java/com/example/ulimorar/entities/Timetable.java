package com.example.ulimorar.entities;

public class Timetable {

    private String timetableName;
    private String imageUrl;
    private Long updateTime;

    public Timetable() {
    }

    public Timetable(String timetableName, String imageUrl) {
        this.timetableName = timetableName;
        this.imageUrl = imageUrl;
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

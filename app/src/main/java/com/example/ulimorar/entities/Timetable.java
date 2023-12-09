package com.example.ulimorar.entities;

public class Timetable {

    private long id;
    private String timetableName;
    private String imageUrl;
    private Long updateTime;

    public Timetable() {
    }

    public Timetable(long id, String timetableName, String imageUrl) {
        this.id = id;
        this.timetableName = timetableName;
        this.imageUrl = imageUrl;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

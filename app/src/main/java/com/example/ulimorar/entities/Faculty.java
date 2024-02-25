package com.example.ulimorar.entities;

import com.google.firebase.database.PropertyName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Faculty implements Serializable {

    private String id;
    private String facultyName;
    private String facultyDescription;
    private String facultyPosterPath;
    @PropertyName("chairs")
    private Map<String, Chair> chairs;

    public Faculty() {
    }

    public Faculty(String id, String facultyName, String facultyDescription) {
        this.id = id;
        this.facultyName = facultyName;
        this.facultyDescription = facultyDescription;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public String getFacultyDescription() {
        return facultyDescription;
    }

    public void setFacultyDescription(String facultyDescription) {
        this.facultyDescription = facultyDescription;
    }

    public String getFacultyPosterPath() {
        return facultyPosterPath;
    }

    public void setFacultyPosterPath(String facultyPosterPath) {
        this.facultyPosterPath = facultyPosterPath;
    }

    public Map<String, Chair> getChairs() {
        return chairs;
    }

    public void setChairs(Map<String, Chair> chairs) {
        this.chairs = chairs;
    }
}

package com.example.ulimorar.entities;

import java.io.Serializable;
import java.util.List;

public class Faculty implements Serializable {

    private String id;
    private String facultyName;
    private String facultyDescription;
    private String facultyPosterPath;
    private List<Chair> chairs;

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

    public List<Chair> getChairs() {
        return chairs;
    }

    public void setChairs(List<Chair> chairs) {
        this.chairs = chairs;
    }
}

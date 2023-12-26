package com.example.ulimorar.entities;

import com.example.ulimorar.entities.enums.UserRole;

public class User {

    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String idnp;
    private String role;
    private String password;

    public User() {
    }

    public User(String id, String firstName, String lastName, String email, String idnp, String role, String password) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.idnp = idnp;
        this.password = password;
        this.role = role;
    }

    public User(String userId, String firstName, String lastName, String email, String idnp) {
        this.id = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.idnp = idnp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIdnp() {
        return idnp;
    }

    public void setIdnp(String idnp) {
        this.idnp = idnp;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

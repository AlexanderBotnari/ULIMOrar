package com.example.ulimorar.entities;

import com.example.ulimorar.entities.enums.UserRole;
import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

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

    public User(String userId, String firstName, String lastName, String email, String idnp, String password) {
        this.id = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.idnp = idnp;
        this.password = password;
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

    @Exclude
    public LinkedHashMap<String, Object> toMap() {
       LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        if(firstName != null)
            result.put("firstName", firstName);
        if(lastName != null)
            result.put("lastName", lastName);
        if(email != null)
            result.put("email", email);
        if(idnp != null)
            result.put("idnp", idnp);
        if(role != null)
            result.put("role", role);
        if(password != null)
            result.put("password", password);

        return result;
    }
}

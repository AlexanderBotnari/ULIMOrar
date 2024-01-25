package com.example.ulimorar.repositories.interfaces;

import com.example.ulimorar.entities.User;

import java.util.List;

public interface UserRepository {

    public List<User> getUsers();

    public User getUserByEmail();
}

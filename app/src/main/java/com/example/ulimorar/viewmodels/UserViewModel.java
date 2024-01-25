package com.example.ulimorar.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.ulimorar.entities.User;
import com.example.ulimorar.repositories.UserRepositoryImpl;

import java.util.List;

public class UserViewModel extends AndroidViewModel {

    private UserRepositoryImpl userRepository;

    public UserViewModel(@NonNull Application application) {
        super(application);
    }

    public List<User> getUsers(){
        return userRepository.getUsers();
    }
}

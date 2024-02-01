package com.example.ulimorar.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ulimorar.entities.User;
import com.example.ulimorar.repositories.UserRepository;

import java.util.List;

public class UserViewModel extends ViewModel {

    private UserRepository userRepository;
    private MutableLiveData<List<User>> userListLiveData;

    private MutableLiveData<User> currentUserLiveData;

    public UserViewModel() {
        userRepository = new UserRepository();
        userListLiveData = userRepository.getUsersLiveData();
        currentUserLiveData = new MutableLiveData<>();
    }

    public void getUsers() {
        userRepository.getUsers();
    }

    public void getUserByEmail(String userEmail) {
        userRepository.getUserByEmail(userEmail);
    }

    // Getter for observing the list of users
    public LiveData<List<User>> getUsersLiveData() {
        return userListLiveData;
    }

    // Getter for observing the current user
    public LiveData<User> getCurrentUserLiveData() {
        return currentUserLiveData;
    }

    // Setter for updating the current user LiveData
    public void setCurrentUser(User user) {
        currentUserLiveData.setValue(user);
    }

}

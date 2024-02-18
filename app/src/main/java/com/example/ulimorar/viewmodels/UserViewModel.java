package com.example.ulimorar.viewmodels;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ulimorar.callbacks.EmailExistCallback;
import com.example.ulimorar.callbacks.PassportExistCallback;
import com.example.ulimorar.entities.User;
import com.example.ulimorar.fragments.DeleteBottomSheetFragment;
import com.example.ulimorar.repositories.UserRepository;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

public class UserViewModel extends ViewModel {

    private UserRepository userRepository;
    private MutableLiveData<List<User>> userListLiveData;

    public UserViewModel() {
        userRepository = new UserRepository();
        userListLiveData = userRepository.getUsersLiveData();
    }

    public void getUsers() {
        userRepository.getUsers();
    }

    public void addUser(View view, AlertDialog alertDialog, User userToAdd){
        userRepository.addUser(view, alertDialog, userToAdd);
    }

    public void editUser(View view, User newUser, User oldUser, AlertDialog alertDialog){
        userRepository.editUser(view, newUser, oldUser, alertDialog);
    }

    public void deleteUser(Context context, User userToDelete, Activity activity,
                           DeleteBottomSheetFragment bottomSheetFragment){
        userRepository.deleteUserByEmail(context, userToDelete, activity, bottomSheetFragment);
    }

    public void checkPassportExistence(String passportId, PassportExistCallback callback,
                                       TextInputLayout passportIdInputLayout, Activity activity){
        userRepository.checkPassportExistence(passportId, callback, passportIdInputLayout, activity);
    }

    public void checkEmailExistence(String email, EmailExistCallback callback){
        userRepository.checkEmailExistence(email, callback);
    }

    // Getter for observing the list of users
    public LiveData<List<User>> getUsersLiveData() {
        return userListLiveData;
    }

    public User getUserByEmail(String email){
        return userRepository.getUserByEmail(email);
    }

}

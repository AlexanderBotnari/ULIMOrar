package com.example.ulimorar.viewmodels;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ulimorar.R;
import com.example.ulimorar.activities.AccountActivity;
import com.example.ulimorar.callbacks.EmailExistCallback;
import com.example.ulimorar.callbacks.PassportExistCallback;
import com.example.ulimorar.entities.User;
import com.example.ulimorar.fragments.DeleteBottomSheetFragment;
import com.example.ulimorar.repositories.UserRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UserViewModel extends ViewModel {

    private UserRepository userRepository;
    private MutableLiveData<List<User>> userListLiveData;

    public UserViewModel() {
        userRepository = new UserRepository();
        userListLiveData = userRepository.getUsersLiveData();
    }

    public void loginUser(Activity activity, String email, String password){
        userRepository.loginUser(activity, email, password);
    }

    public void getUsers() {
        userRepository.getUsers();
    }

    public void addUser(View view, AlertDialog alertDialog, User userToAdd){
        userRepository.addUser(view, alertDialog, userToAdd);
    }

    public void registerUser(Activity activity, View view, AlertDialog alertDialog, User userToRegister){
        userRepository.registerUser(activity, view, alertDialog, userToRegister);
    }

    public void editUser(View view, User newUser, User oldUser, AlertDialog alertDialog){
        userRepository.editUser(view, newUser, oldUser, alertDialog);
    }

    public void updateFirstAndLastName(AccountActivity accountActivity, String userToUpdateId, User newUser){
        userRepository.updateFirstAndLastName(accountActivity, userToUpdateId, newUser);
    }

    public void updateEmail(Activity activity, User newUser, String userId){
        userRepository.updateEmail(activity, newUser, userId);
    }

    public void updatePassword(Activity activity, User newUser, String userId){
        userRepository.updatePassword(activity, newUser, userId);
    }

    public void deleteOldUserAndSignInWithNewEmail(Activity activity, User oldUser, User newUser){
        userRepository.deleteOldUserAndSignInWithNewEmail(activity, oldUser, newUser);
    }

    public void deleteOldUserAndSignInWithNewPassword(Activity activity, User oldUser, User newUser){
        userRepository.deleteOldUserAndSignInWithNewPassword(activity, oldUser, newUser);
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

    public MutableLiveData<User> getUserByEmailLiveData(String email) {
        return userRepository.getUserByEmailLiveData(email);
    }

    public FirebaseUser getLoggedInUser(){
        return userRepository.getLoggedInUser();
    }

}

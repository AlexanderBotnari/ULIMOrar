package com.example.ulimorar.repositories;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.ulimorar.R;
import com.example.ulimorar.activities.AccountActivity;
import com.example.ulimorar.activities.FacultyActivity;
import com.example.ulimorar.activities.LoginActivity;
import com.example.ulimorar.callbacks.EmailExistCallback;
import com.example.ulimorar.callbacks.PassportExistCallback;
import com.example.ulimorar.entities.User;
import com.example.ulimorar.fragments.DeleteBottomSheetFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private DatabaseReference usersDatabaseReference;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private MutableLiveData<List<User>> userListLiveData = new MutableLiveData<>();
    private User findedUserByEmail;

    private MutableLiveData<User> findedUserLiveData = new MutableLiveData<>();

    private PassportIdRepository passportIdRepository;

    public UserRepository() {
        passportIdRepository = new PassportIdRepository();
        // Initialize the Firebase Database reference
        usersDatabaseReference = FirebaseDatabase.getInstance().getReference("users");
    }

    public void loginUser(Activity activity, String email, String password){
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = auth.getCurrentUser();
                            activity.startActivity(new Intent(activity, FacultyActivity.class).putExtra("currentUserEmail", user.getEmail()));
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("FAILURE_TAG", "signInWithEmail:failure", task.getException());
                            Toast.makeText(activity, R.string.authentication_error,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void getUsers() {
        usersDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<User> userList = new ArrayList<>();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null && user.getEmail() != null && auth.getCurrentUser() != null) {
                        if (!user.getEmail().equals(auth.getCurrentUser().getEmail())) {
                            userList.add(user);
                        }
                    }
                }
                userListLiveData.postValue(userList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
                userListLiveData.postValue(null);
            }
        });
    }

    private void findUserByEmail(String userEmail) {
        Query query = usersDatabaseReference.orderByChild("email").equalTo(userEmail);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User found
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        // Access user data
                        User authenticatedUserFromDb = snapshot.getValue(User.class);
                        if (authenticatedUserFromDb != null) {
                            findedUserByEmail = authenticatedUserFromDb;
                            findedUserLiveData.postValue(authenticatedUserFromDb);
                        }
                    }
                } else {
                    // User not found
                    findedUserLiveData.postValue(null);
                    Log.d("User Data", "User not found for email: " + userEmail);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Error", "Failed to read user data.", databaseError.toException());
            }
        });
    }

    public void addUser(View view, AlertDialog alertDialog, User userToAdd) {
        findUserByEmail(auth.getCurrentUser().getEmail());

        String userId = usersDatabaseReference.push().getKey();

        User user = new User(userId, userToAdd.getFirstName(), userToAdd.getLastName(),
                userToAdd.getEmail(), userToAdd.getIdnp(), userToAdd.getRole(), userToAdd.getPassword());

        // Add the user to the database
        usersDatabaseReference.child(userId).setValue(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                passportIdRepository.checkPassportToRegisterExistence(user.getIdnp(), new PassportExistCallback() {
                    @Override
                    public void onResult(boolean exists) {
                        if (exists){
                            passportIdRepository.deletePassportId(view, user.getIdnp());
                        }
                    }
                });

                // If user added successfully, add the same user credentials to Firebase Authentication
                auth.createUserWithEmailAndPassword(userToAdd.getEmail(), userToAdd.getPassword())
                        .addOnCompleteListener(authTask -> {
                            if (authTask.isSuccessful()) {
                                Toast.makeText(view.getContext(), R.string.add_user_successful_message, Toast.LENGTH_SHORT).show();
                                auth.signInWithEmailAndPassword(findedUserByEmail.getEmail(), findedUserByEmail.getPassword());
                            } else {
                                Toast.makeText(view.getContext(), R.string.add_user_failure_message, Toast.LENGTH_SHORT).show();
                                Log.d("FailureAddUser", authTask.getException().getMessage());
                            }
                        });

                alertDialog.dismiss();
            } else {
                Toast.makeText(view.getContext(), R.string.add_user_failure_message, Toast.LENGTH_SHORT).show();
                Log.d("FailureAddUser", task.getException().getMessage());
            }
        });
    }

    public void registerUser(Activity activity, View view, AlertDialog alertDialog, User userToRegister){
        String userId = usersDatabaseReference.push().getKey();

        User user = new User(userId, userToRegister.getFirstName(), userToRegister.getLastName(),
                userToRegister.getEmail(), userToRegister.getIdnp(), userToRegister.getRole(), userToRegister.getPassword());

        // Add the user to the database
        usersDatabaseReference.child(userId).setValue(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // If user added successfully, add the same user credentials to Firebase Authentication
                auth.createUserWithEmailAndPassword(userToRegister.getEmail(), userToRegister.getPassword())
                        .addOnCompleteListener(authTask -> {
                            if (authTask.isSuccessful()) {
                                Toast.makeText(activity, R.string.successful_registration, Toast.LENGTH_SHORT).show();
                                activity.startActivity(new Intent(activity, LoginActivity.class));
                                activity.finish();
                                passportIdRepository.deletePassportId(view, userToRegister.getIdnp());
                                auth.signOut();
                            } else {
                                Toast.makeText(activity, R.string.registration_failure, Toast.LENGTH_SHORT).show();
                                Log.d("FailureAddUser", authTask.getException().getMessage());
                            }
                        });

                alertDialog.dismiss();
            } else {
                Toast.makeText(activity, R.string.registration_failure, Toast.LENGTH_SHORT).show();
                Log.d("FailureAddUser", task.getException().getMessage());
            }
        });
    }

    public void editUser(View view, User newUser, User oldUser, AlertDialog alertDialog){
        findUserByEmail(auth.getCurrentUser().getEmail());

        User userToUpdate = new User(newUser.getId(), newUser.getFirstName(),
                newUser.getLastName(), newUser.getEmail(), newUser.getIdnp(),
                newUser.getRole(), newUser.getPassword());

        usersDatabaseReference.child(userToUpdate.getId()).setValue(userToUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                auth.signInWithEmailAndPassword(oldUser.getEmail(), oldUser.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            FirebaseUser oldUser = auth.getCurrentUser();
                            oldUser.delete();
                            auth.createUserWithEmailAndPassword(newUser.getEmail(), newUser.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                                    Toast.makeText(view.getContext(), R.string.update_user_success_message, Toast.LENGTH_SHORT).show();
                                    auth.signInWithEmailAndPassword(findedUserByEmail.getEmail(), findedUserByEmail.getPassword());
                                }
                            });
                        }
                    }
                });
                alertDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Toast.makeText(view.getContext(), R.string.update_user_failure, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateFirstAndLastName(AccountActivity accountActivity, String userToUpdateId, User newUser){
        usersDatabaseReference.child(userToUpdateId).setValue(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Void> task) {
                    Toast.makeText(accountActivity, R.string.update_user_success_message, Toast.LENGTH_SHORT).show();
                    accountActivity.makeFieldsEditable(false);
                }
            });
    }

    public void updateEmail(Activity activity, User newUser, String userId){
        auth.createUserWithEmailAndPassword(newUser.getEmail(), newUser.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                usersDatabaseReference.child(userId).setValue(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        Toast.makeText(activity, R.string.update_user_success_message, Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(activity, R.string.update_user_failure, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Toast.makeText(activity, R.string.error_create_user_in_auth, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updatePassword(Activity activity, User newUser, String userId){
        auth.createUserWithEmailAndPassword(newUser.getEmail(), newUser.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                usersDatabaseReference.child(userId).setValue(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        Toast.makeText(activity, R.string.update_user_success_message, Toast.LENGTH_SHORT).show();
                        signInWithNewDataAndReload(activity, newUser);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(activity, R.string.update_user_failure, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Toast.makeText(activity, R.string.error_create_user_in_auth, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deleteOldUserAndSignInWithNewEmail(Activity activity, User oldUser, User newUser){
        auth.signInWithEmailAndPassword(oldUser.getEmail(), oldUser.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser oldUser = auth.getCurrentUser();
                    oldUser.delete();
                    signInWithNewDataAndReload(activity, newUser);
                }
            }
        });
    }

    public void deleteOldUserAndSignInWithNewPassword(Activity activity, User oldUser, User newUser){
        auth.signInWithEmailAndPassword(oldUser.getEmail(), oldUser.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = auth.getCurrentUser();
                    user.delete();
                    updatePassword(activity, newUser, oldUser.getId());
                }
            }
        });
    }

    private void signInWithNewDataAndReload(Activity activity, User newUser){
        auth.signInWithEmailAndPassword(newUser.getEmail(), newUser.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), activity.getText(R.string.user_data_changed), 3000);
                snackbar.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(4000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        FirebaseAuth.getInstance().signOut();
                        activity.startActivity(new Intent(activity, LoginActivity.class));
                        activity.finish();
                    }
                }).start();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Toast.makeText(activity, R.string.error_signin_with_new_credentials, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deleteUserByEmail(Context context, User userToDelete, Activity activity,
                                  DeleteBottomSheetFragment bottomSheetFragment) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        findUserByEmail(firebaseUser.getEmail());

        // Make sure the current user exists and is not the user you are trying to delete
        if (!firebaseUser.getEmail().equals(userToDelete.getEmail())) {
            // Sign in with the email address of the user you want to delete
            auth.signInWithEmailAndPassword(userToDelete.getEmail(), userToDelete.getPassword())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = task.getResult().getUser();
                            user.delete();

                            // Search the user in database by email address
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
                            Query query = databaseReference.orderByChild("email").equalTo(userToDelete.getEmail());
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        // Get the key of the user found and delete him from the database
                                        String userKey = snapshot.getKey();
                                        DatabaseReference userReference = databaseReference.child(userKey);
                                        userReference.removeValue()
                                                .addOnSuccessListener(aVoid -> {
                                                    // The user has been successfully deleted from the database
                                                    String message = activity.getText(R.string.user) + " " + userToDelete.getFirstName() + " " +
                                                            userToDelete.getLastName() + " " + activity.getText(R.string.delete_user_success);
                                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                                                    auth.signInWithEmailAndPassword(findedUserByEmail.getEmail(), findedUserByEmail.getPassword());
                                                    bottomSheetFragment.dismiss();
                                                })
                                                .addOnFailureListener(e -> {
                                                    // Error handling when deleting from the Realtime Database
                                                    Toast.makeText(context, R.string.delete_user_from_db_fail, Toast.LENGTH_LONG).show();
                                                });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Error handling when querying the database
                                    Toast.makeText(context, R.string.database_query_error, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            // Error handling when logging into user account
                            Toast.makeText(context, R.string.authentication_error, Toast.LENGTH_SHORT).show();
                            auth.signInWithEmailAndPassword(findedUserByEmail.getEmail(), findedUserByEmail.getPassword());
                        }
                    });
        } else {
            // Handling if the current user is null or is the current user
            Toast.makeText(context, R.string.delete_current_user_error, Toast.LENGTH_SHORT).show();
            auth.signInWithEmailAndPassword(findedUserByEmail.getEmail(), findedUserByEmail.getPassword());
        }
    }

    public void checkEmailExistence(String email, EmailExistCallback callback){
        usersDatabaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.onResult(snapshot.exists());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onResult(false);
            }
        });
    }

    public void checkPassportExistence(String passportId, PassportExistCallback callback,
                                       TextInputLayout passportIdInputLayout, Activity activity) {
        usersDatabaseReference.orderByChild("idnp").equalTo(passportId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean exists = snapshot.exists();
                if (exists) {
                    passportIdInputLayout.setError(activity.getString(R.string.passport_already_exists));
                }
                callback.onResult(exists);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onResult(false);
            }
        });
    }

    public MutableLiveData<List<User>> getUsersLiveData() {
        return userListLiveData;
    }

    public MutableLiveData<User> getUserByEmailLiveData(String email) {
        findUserByEmail(email);
        return findedUserLiveData;
    }

    public User getUserByEmail(String email){
        findUserByEmail(email);
        return findedUserByEmail;
    }

    public FirebaseUser getLoggedInUser(){
        return auth.getCurrentUser();
    }

}


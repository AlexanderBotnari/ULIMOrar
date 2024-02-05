package com.example.ulimorar.repositories;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.ulimorar.R;
import com.example.ulimorar.callbacks.EmailExistCallback;
import com.example.ulimorar.callbacks.PassportExistCallback;
import com.example.ulimorar.entities.User;
import com.example.ulimorar.fragments.DeleteBottomSheetFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
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
    private User currentUser;

    private PassportIdRepository passportIdRepository;

    public UserRepository() {
        passportIdRepository = new PassportIdRepository();
        // Initialize the Firebase Database reference
        usersDatabaseReference = FirebaseDatabase.getInstance().getReference("users");
        getUsers(); // Call this method to fetch initial data
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

    public void getUserByEmail(String userEmail) {
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
                            currentUser = authenticatedUserFromDb;
                        }
                    }
                } else {
                    // User not found
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
        getUserByEmail(auth.getCurrentUser().getEmail());

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
                                auth.signInWithEmailAndPassword(currentUser.getEmail(), currentUser.getPassword());
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

    public void editUser(View view, User newUser, User oldUser, AlertDialog alertDialog){
        getUserByEmail(auth.getCurrentUser().getEmail());

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
                                    auth.signInWithEmailAndPassword(currentUser.getEmail(), currentUser.getPassword());
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

    public void deleteUserByEmail(Context context, User userToDelete, Activity activity,
                                  DeleteBottomSheetFragment bottomSheetFragment) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        getUserByEmail(firebaseUser.getEmail());

        // Asigurați-vă că utilizatorul curent există și nu este utilizatorul pe care încercați să-l ștergeți
        if (!firebaseUser.getEmail().equals(userToDelete.getEmail())) {
            // Autentificați-vă cu adresa de e-mail a utilizatorului pe care doriți să-l ștergeți
            auth.signInWithEmailAndPassword(userToDelete.getEmail(), userToDelete.getPassword())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = task.getResult().getUser();
                            user.delete();

                            // Căutați utilizatorul în baza de date după adresa de e-mail
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
                            Query query = databaseReference.orderByChild("email").equalTo(userToDelete.getEmail());
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        // Obțineți cheia utilizatorului găsit și ștergeți-l din baza de date
                                        String userKey = snapshot.getKey();
                                        DatabaseReference userReference = databaseReference.child(userKey);
                                        userReference.removeValue()
                                                .addOnSuccessListener(aVoid -> {
                                                    // Utilizatorul a fost șters cu succes din baza de date
                                                    String message = activity.getText(R.string.user) + " " + userToDelete.getFirstName() + " " +
                                                            userToDelete.getLastName() + " " + activity.getText(R.string.delete_user_success);
                                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                                                    auth.signInWithEmailAndPassword(currentUser.getEmail(), currentUser.getPassword());
                                                    bottomSheetFragment.dismiss();
                                                })
                                                .addOnFailureListener(e -> {
                                                    // Tratarea erorii la ștergerea din Realtime Database
                                                    Toast.makeText(context, R.string.delete_user_from_db_fail, Toast.LENGTH_LONG).show();
                                                });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Tratarea erorii la interogarea bazei de date
                                    Toast.makeText(context, R.string.database_query_error, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            // Tratarea erorii la autentificarea în contul utilizatorului
                            Toast.makeText(context, R.string.authentication_error, Toast.LENGTH_SHORT).show();
                            auth.signInWithEmailAndPassword(currentUser.getEmail(), currentUser.getPassword());
                        }
                    });
        } else {
            // Tratarea cazului în care utilizatorul curent este null sau este utilizatorul curent
            Toast.makeText(context, R.string.delete_current_user_error, Toast.LENGTH_SHORT).show();
            auth.signInWithEmailAndPassword(currentUser.getEmail(), currentUser.getPassword());
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
}


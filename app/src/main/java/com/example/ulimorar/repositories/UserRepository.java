package com.example.ulimorar.repositories;


import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.ulimorar.entities.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private DatabaseReference databaseReference;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private MutableLiveData<List<User>> userListLiveData = new MutableLiveData<>();
    private User currentUser;

    public UserRepository() {
        // Initialize the Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        getUsers(); // Call this method to fetch initial data
    }

    public void getUsers() {
        databaseReference.addValueEventListener(new ValueEventListener() {
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
        Query query = databaseReference.orderByChild("email").equalTo(userEmail);

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

    public MutableLiveData<List<User>> getUsersLiveData() {
        return userListLiveData;
    }
}


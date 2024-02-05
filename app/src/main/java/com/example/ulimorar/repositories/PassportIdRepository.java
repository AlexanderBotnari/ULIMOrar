package com.example.ulimorar.repositories;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.ulimorar.R;
import com.example.ulimorar.activities.fragments.PassportIdsForUserRegistrationFragment;
import com.example.ulimorar.activities.fragments.RegisteredUsersFragment;
import com.example.ulimorar.adapters.PassportIdAdapter;
import com.example.ulimorar.callbacks.PassportExistCallback;
import com.example.ulimorar.entities.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PassportIdRepository {

    private DatabaseReference passportDatabaseReference;

    private MutableLiveData<List<String>> passportsListLiveData = new MutableLiveData<>();

    private FirebaseAuth auth = FirebaseAuth.getInstance();

    public PassportIdRepository() {
        passportDatabaseReference = FirebaseDatabase.getInstance().getReference("passportIds");
        getPassportIds();
    }

    public void getPassportIds() {
        passportDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> passportList = new ArrayList<>();
                for (DataSnapshot passportSnapshot : snapshot.getChildren()) {
                    String passport = passportSnapshot.getValue(String.class);
                    if (passport != null && auth.getCurrentUser() != null) {
                        passportList.add(passport);
                    }
                }
                passportsListLiveData.postValue(passportList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
                passportsListLiveData.postValue(null);
            }
        });
    }

    public void deletePassportId(View view, String passportId){
        passportDatabaseReference.child(passportId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    passportDatabaseReference.child(passportId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(view.getContext(), R.string.passport_is_removed, Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(view.getContext(), R.string.passport_remove_fail, Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    Toast.makeText(view.getContext(), R.string.passport_not_exists, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error if necessary
                Log.e("DatabaseError", "Error checking passport ID existence: " + databaseError.getMessage());
            }
        });
    }

    public void checkPassportToRegisterExistence(String passportId, PassportExistCallback callback){
        passportDatabaseReference.child(passportId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                callback.onResult(dataSnapshot.exists());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onResult(false);
                // Handle the error if necessary
                Log.e("DatabaseError", "Error checking passport ID existence: " + databaseError.getMessage());
            }
        });
    }
}

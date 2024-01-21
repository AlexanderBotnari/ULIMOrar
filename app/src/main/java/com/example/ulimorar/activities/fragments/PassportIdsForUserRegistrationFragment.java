package com.example.ulimorar.activities.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ulimorar.R;
import com.example.ulimorar.adapters.PassportIdAdapter;
import com.example.ulimorar.entities.User;
import com.example.ulimorar.fragments.DeleteBottomSheetFragment;
import com.example.ulimorar.fragments.interfaces.BottomSheetListener;
import com.example.ulimorar.utils.GetDialogsStandardButtons;
import com.example.ulimorar.utils.controllers.SwipeController;
import com.example.ulimorar.utils.controllers.SwipeControllerActions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

public class PassportIdsForUserRegistrationFragment extends Fragment{

    private FloatingActionButton addPassportIdButton;

    private ArrayList<String> passportIds;
    private PassportIdAdapter adapter;

    private AlertDialog alertDialog;

    private DatabaseReference passportIdsDbReference;

    private TextInputLayout passportIdInputLayout;

    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_passport_ids_for_user_registration, container, false);

        passportIdsDbReference = FirebaseDatabase.getInstance().getReference("passportIds");

        passportIds = new ArrayList<>();

        adapter = new PassportIdAdapter(view.getContext(), passportIds);
        ListView listView = view.findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                showDeleteConfirmationDialog(position, view);
            }
        });

        addPassportIdButton = view.findViewById(R.id.addPassportIdFloatingButton);
        addPassportIdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddDialog(R.string.add_passport_id, view);
            }
        });

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getPassportIds();
            }
        });

        return view;
    }

    private void getPassportIds() {
        Query query = FirebaseDatabase.getInstance().getReference("passportIds");
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                passportIds.clear();  // because everytime when data updates in your firebase database it creates the list with updated items
                // so to avoid duplicate fields we clear the list everytime
                if (snapshot.exists()) {
                    for (DataSnapshot passportSnapshot : snapshot.getChildren()) {
                        String passport = passportSnapshot.getValue(String.class);
                        assert passport != null;
                        if (!passport.isEmpty()){
                            passportIds.add(passport);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void showAddDialog(int dialogTitle, View view) {
        View alertDialogCustomView = LayoutInflater.from(view.getContext()).inflate(R.layout.add_passport_id_dialog, null);

        TextView titleTextView = alertDialogCustomView.findViewById(R.id.dialogTitleTextView);
        titleTextView.setText(dialogTitle);

        passportIdInputLayout = alertDialogCustomView.findViewById(R.id.passportIdInputLayout);

        // Personalized dialog interface
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setView(alertDialogCustomView);

        // Show dialog
        alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        GetDialogsStandardButtons.getSaveButton(alertDialogCustomView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String passportId = passportIdInputLayout.getEditText().getText().toString().trim();

                boolean isValid = true;

                if (passportId.length() != 13) {
                    passportIdInputLayout.setError(getString(R.string.idnp_length_error));
                    isValid = false;
                }else {
                    passportIdInputLayout.setError(null);
                }

                if (isValid){
                    addPassport(view, passportId);
                }
            }
        });

        GetDialogsStandardButtons.getCancelButton(alertDialogCustomView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.cancel();
            }
        });
    }

    private void addPassport(View view, String passportId) {
        // Check if the passport ID already exists in the database
        passportIdsDbReference.child(passportId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Passport ID already exists
                    Toast.makeText(view.getContext(), R.string.passport_already_exists, Toast.LENGTH_SHORT).show();
                } else {
                    // Passport ID does not exist, add it to the database
                    passportIdsDbReference.child(passportId).setValue(passportId).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(view.getContext(), R.string.passport_is_added, Toast.LENGTH_SHORT).show();
                            alertDialog.dismiss();
                        } else {
                            Toast.makeText(view.getContext(), R.string.add_passport_fail, Toast.LENGTH_SHORT).show();
                            Log.d("FailureAddPassport", task.getException().getMessage());
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error if necessary
                Log.e("DatabaseError", "Error checking passport ID existence: " + databaseError.getMessage());
            }
        });
    }

    private void showDeleteConfirmationDialog(final int position, View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext(), R.style.AlertDialogCustomStyle);
        builder.setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String passportId = adapter.getItem(position);
                        deletePassportIdFromDb(view, passportId);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    public void deletePassportIdFromDb(View view, String passportId){
        passportIdsDbReference.child(passportId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    adapter.remove(passportId);
                    passportIdsDbReference.child(passportId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
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

    @Override
    public void onStart() {
        super.onStart();
        getPassportIds();
    }
}
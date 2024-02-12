package com.example.ulimorar.activities.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ulimorar.R;
import com.example.ulimorar.adapters.PassportIdAdapter;
import com.example.ulimorar.callbacks.PassportExistCallback;
import com.example.ulimorar.entities.User;
import com.example.ulimorar.utils.GetDialogsStandardButtons;
import com.example.ulimorar.viewmodels.PassportIdViewModel;
import com.example.ulimorar.viewmodels.UserViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PassportIdsForUserRegistrationFragment extends Fragment{

    private FloatingActionButton addPassportIdButton;

    private SearchView searchView;

    private ArrayList<String> passportIds;
    private ArrayList<String> searchResults;
    private PassportIdAdapter adapter;

    private AlertDialog alertDialog;

//    private DatabaseReference passportIdsDbReference;
//    private DatabaseReference userDbReference;

    private TextInputLayout passportIdInputLayout;

    private SwipeRefreshLayout swipeRefreshLayout;

    private UserViewModel userViewModel;
    private PassportIdViewModel passportIdViewModel;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        passportIdViewModel = new ViewModelProvider(this).get(PassportIdViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        passportIdViewModel.getPassportIdsLiveData().observe(this, passportList ->{
                adapter.clear();
                adapter.addAll(passportList);
                adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_passport_ids_for_user_registration, container, false);

//        passportIdsDbReference = FirebaseDatabase.getInstance().getReference("passportIds");
//        userDbReference = FirebaseDatabase.getInstance().getReference("users");

        passportIds = new ArrayList<>();
        searchResults = new ArrayList<>();

        adapter = new PassportIdAdapter(requireContext(), passportIds);
        ListView listView = view.findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                showDeleteConfirmationDialog(position, view);
            }
        });

        addPassportIdButton = view.findViewById(R.id.addPassportIdFloatingButton);
        searchView = view.findViewById(R.id.searchView);
        addPassportIdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewOnclick) {
                showAddDialog(R.string.add_passport_id, view);
            }
        });

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                getPassportIds();
                passportIdViewModel.getPassportIds();
                searchView.clearFocus();
                searchView.setQueryHint(getText(R.string.search_by_passport));
            }
        });

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                searchView.setQueryHint(null);
            }
        });

        int closeBtnId = searchView.getContext().getResources().getIdentifier("android:id/search_close_btn", null, null);
        searchView.findViewById(closeBtnId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new PassportIdsForUserRegistrationFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newQuery) {
                updateSearchResults(newQuery);
                return true;
            }
        });

        return view;
    }

//    private void getPassportIds() {
//        Query query = FirebaseDatabase.getInstance().getReference("passportIds");
//        query.addValueEventListener(new ValueEventListener() {
//            @SuppressLint("NotifyDataSetChanged")
//            @Override
//            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
//                passportIds.clear();  // because everytime when data updates in your firebase database it creates the list with updated items
//                // so to avoid duplicate fields we clear the list everytime
//                if (snapshot.exists()) {
//                    for (DataSnapshot passportSnapshot : snapshot.getChildren()) {
//                        String passport = passportSnapshot.getValue(String.class);
//                        assert passport != null;
//                        if (!passport.isEmpty()){
//                            passportIds.add(passport);
//                        }
//                    }
//                    adapter.notifyDataSetChanged();
//                    swipeRefreshLayout.setRefreshing(false);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull @NotNull DatabaseError error) {
//
//            }
//        });
//    }

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
            public void onClick(View viewOnclick) {

                String passportId = passportIdInputLayout.getEditText().getText().toString().trim();

                boolean isValid = true;

                if (passportId.length() != 13) {
                    passportIdInputLayout.setError(getString(R.string.idnp_length_error));
                    isValid = false;
                }else {
                    passportIdInputLayout.setError(null);
                }

                if (isValid){
                    userViewModel.checkPassportExistence(passportId, new PassportExistCallback() {
                        @Override
                        public void onResult(boolean exists) {
                            if (!exists){
                                passportIdViewModel.addPassport(view, passportId, alertDialog);
                            }
                        }
                    }, passportIdInputLayout, getActivity());
//                    checkPassportExistence(passportId, exists -> {
//                        if (!exists) {
//                            addPassport(view, passportId);
//                        }
//                    });
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

//    private interface PassportExistCallback {
//        void onResult(boolean exists);
//    }

//    private void checkPassportExistence(String passportId, PassportExistCallback callback) {
//        userDbReference.orderByChild("idnp").equalTo(passportId).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                boolean exists = snapshot.exists();
//                if (exists) {
//                    passportIdInputLayout.setError(getString(R.string.passport_already_exists));
//                }
//                callback.onResult(exists);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                callback.onResult(false);
//            }
//        });
//    }

//    private void addPassport(View view, String passportId) {
//        // Check if the passport ID for user already exists in the database
//            passportIdsDbReference.child(passportId).addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    if (dataSnapshot.exists()) {
//                        // Passport ID already exists
//                        Toast.makeText(view.getContext(), R.string.passport_already_exists, Toast.LENGTH_SHORT).show();
//                    } else {
//                        // Passport ID does not exist, add it to the database
//                        passportIdsDbReference.child(passportId).setValue(passportId).addOnCompleteListener(task -> {
//                            if (task.isSuccessful()) {
//                                Toast.makeText(view.getContext(), R.string.passport_is_added, Toast.LENGTH_SHORT).show();
//                                alertDialog.dismiss();
//                            } else {
//                                Toast.makeText(view.getContext(), R.string.add_passport_fail, Toast.LENGTH_SHORT).show();
//                                Log.d("FailureAddPassport", task.getException().getMessage());
//                            }
//                        });
//                    }
//                }
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//                    // Handle the error if necessary
//                    Log.e("DatabaseError", "Error checking passport ID existence: " + databaseError.getMessage());
//                }
//            });
//    }

    private void showDeleteConfirmationDialog(final int position, View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext(), R.style.AlertDialogCustomStyle);
        builder.setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String passportId = adapter.getItem(position);
                        passportIdViewModel.deletePassport(view, passportId);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

//    public void deletePassportIdFromDb(View view, String passportId){
//        passportIdsDbReference.child(passportId).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    adapter.remove(passportId);
//                    passportIdsDbReference.child(passportId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            Toast.makeText(view.getContext(), R.string.passport_is_removed, Toast.LENGTH_SHORT).show();
//                        }
//                    }).addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Toast.makeText(view.getContext(), R.string.passport_remove_fail, Toast.LENGTH_SHORT).show();
//                        }
//                    });
//
//                } else {
//                    Toast.makeText(view.getContext(), R.string.passport_not_exists, Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                // Handle the error if necessary
//                Log.e("DatabaseError", "Error checking passport ID existence: " + databaseError.getMessage());
//            }
//        });
//    }

    @Override
    public void onStart() {
        super.onStart();
//        getPassportIds();
        passportIdViewModel.getPassportIds();
    }

    private void performSearch(String query) {
        // Dummy search operation
        searchResults.clear();
        for (String passport : passportIds) {
            if (passport.toLowerCase().contains(query.toLowerCase())) {
                searchResults.add(passport);
            }
        }
        // Display search results
        if (!searchResults.isEmpty()){
            displayResults(searchResults);
        }else{
            Snackbar snackbar = Snackbar.make(getView(), "No results!", 2000);
            snackbar.show();
        }

    }

    private void updateSearchResults(String newText) {
        // Dummy logic to update search results dynamically
        // based on the user's input

        List<String> updatedResults = new ArrayList<>();
        for (String passport : passportIds) {
            if (passport.toLowerCase().contains(newText.toLowerCase())) {
                updatedResults.add(passport);
            }
        }

        if (!updatedResults.isEmpty()){
            adapter.updateList(updatedResults);
        }

    }

    private void displayResults(List<String> results) {
        // Dummy logic to display the search results
        adapter.updateList(results);
    }
}
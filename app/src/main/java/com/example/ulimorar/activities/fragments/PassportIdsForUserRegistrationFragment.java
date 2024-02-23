package com.example.ulimorar.activities.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.ulimorar.R;
import com.example.ulimorar.adapters.PassportIdAdapter;
import com.example.ulimorar.callbacks.PassportExistCallback;
import com.example.ulimorar.utils.GetDialogsStandardButtons;
import com.example.ulimorar.viewmodels.PassportIdViewModel;
import com.example.ulimorar.viewmodels.UserViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class PassportIdsForUserRegistrationFragment extends Fragment{

    private FloatingActionButton addPassportIdButton;

    private SearchView searchView;

    private ArrayList<String> passportIds;
    private ArrayList<String> searchResults;
    private PassportIdAdapter adapter;

    private AlertDialog alertDialog;

    private TextInputLayout passportIdInputLayout;

    private SwipeRefreshLayout swipeRefreshLayout;

    private ImageView emptyImageView;

    private UserViewModel userViewModel;
    private PassportIdViewModel passportIdViewModel;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        passportIdViewModel = new ViewModelProvider(this).get(PassportIdViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_passport_ids_for_user_registration, container, false);

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

        emptyImageView = view.findViewById(R.id.emptyImageView);

        passportIdViewModel.getPassportIdsLiveData().observe(getViewLifecycleOwner(), passportList ->{
            if (!passportList.isEmpty()){
                emptyImageView.setVisibility(View.GONE);
                adapter.clear();
                adapter.addAll(passportList);
                adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }else{
                emptyImageView.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }

    private void showAddDialog(int dialogTitle, View view) {
        View alertDialogCustomView = LayoutInflater.from(view.getContext()).inflate(R.layout.add_passport_id_dialog, null);

        TextView titleTextView = alertDialogCustomView.findViewById(R.id.dialogTitleTextView);
        titleTextView.setText(dialogTitle);

        passportIdInputLayout = alertDialogCustomView.findViewById(R.id.passportIdInputLayout);

        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setView(alertDialogCustomView);

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

    @Override
    public void onStart() {
        super.onStart();
        passportIdViewModel.getPassportIds();
    }

    private void performSearch(String query) {
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
        // logic to update search results dynamically
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
        // logic to display the search results
        adapter.updateList(results);
    }
}
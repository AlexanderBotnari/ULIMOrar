package com.example.ulimorar.viewmodels;

import android.app.Activity;
import android.app.AlertDialog;
import android.net.Uri;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ulimorar.entities.Faculty;
import com.example.ulimorar.repositories.FacultyRepository;

import java.util.List;

public class FacultyViewModel extends ViewModel {

    private FacultyRepository facultyRepository;
    private MutableLiveData<List<Faculty>> facultyListLiveData;

    public FacultyViewModel(){
        facultyRepository = new FacultyRepository();
        facultyListLiveData = facultyRepository.getFacultyListLiveData();
    }

    public void getFaculties(){
        facultyRepository.getFaculties();
    }

    public void addFaculty(Activity activity, String facultyName, String facultyDescription, Uri imageUri, AlertDialog alertDialog){
        facultyRepository.addFaculty(activity, facultyName, facultyDescription, imageUri, alertDialog);
    }

    public void editFaculty(String facultyId, String facultyName, String facultyDescription,
                            Faculty facultyToUpdate, Uri imageUri, Activity activity, AlertDialog alertDialog){
        facultyRepository.editFaculty(facultyId, facultyName, facultyDescription, facultyToUpdate,
                imageUri, activity, alertDialog);
    }

    public void deleteFaculty(Faculty facultyToDelete, Activity activity){
        facultyRepository.deleteFaculty(facultyToDelete, activity);
    }

    public MutableLiveData<List<Faculty>> getFacultyListLiveData() {
        return facultyListLiveData;
    }
}

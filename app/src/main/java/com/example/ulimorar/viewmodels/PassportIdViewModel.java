package com.example.ulimorar.viewmodels;

import android.app.AlertDialog;
import android.view.View;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ulimorar.callbacks.PassportExistCallback;
import com.example.ulimorar.repositories.PassportIdRepository;

import java.util.ArrayList;

public class PassportIdViewModel extends ViewModel {

    private PassportIdRepository passportIdRepository;
    private MutableLiveData<ArrayList<String>> passportIdsLiveData;

    public PassportIdViewModel() {
        passportIdRepository = new PassportIdRepository();
        passportIdsLiveData = passportIdRepository.getPassportsListLiveData();
    }

    public void getPassportIds(){
        passportIdRepository.getPassportIds();
    }

    public void addPassport(View view, String passportId, AlertDialog alertDialog){
        passportIdRepository.addPassport(view, passportId, alertDialog);
    }

    public void deletePassport(View view, String passportId){
        passportIdRepository.deletePassportId(view, passportId);
    }

    public void checkPassportToRegisterExistence(String passportId, PassportExistCallback passportExistCallback){
        passportIdRepository.checkPassportToRegisterExistence(passportId, passportExistCallback);
    }

    public MutableLiveData<ArrayList<String>> getPassportIdsLiveData(){
        return passportIdsLiveData;
    }

}

package com.example.ulimorar.utils;

import android.view.View;
import android.widget.Button;
import com.example.ulimorar.R;

public class GetDialogsStandartButtons {

    public static Button getSaveButton(View view) {
        return view.findViewById(R.id.saveButton);
    }

    public static Button getCancelButton(View view) {
        return view.findViewById(R.id.cancelButton);
    }
}

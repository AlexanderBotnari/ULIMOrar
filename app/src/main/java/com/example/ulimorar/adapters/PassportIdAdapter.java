package com.example.ulimorar.adapters;

import android.app.LauncherActivity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ulimorar.R;
import com.example.ulimorar.entities.User;

import java.util.ArrayList;
import java.util.List;

public class PassportIdAdapter extends ArrayAdapter<String> {

    private ArrayList<String> passportIds;

    public PassportIdAdapter(Context context, ArrayList<String> passportIds) {
        super(context, 0, passportIds);
        this.passportIds = passportIds;
    }

    public void updateList(List<String> newList) {
        passportIds.clear();
        passportIds.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String passportId = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.passport_id_list_item, parent, false);
        }

        TextView textViewListItem = convertView.findViewById(R.id.textViewListItem);
        textViewListItem.setText(passportId);

        return convertView;
    }
}

package com.example.ulimorar.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.example.ulimorar.R;
import com.example.ulimorar.databinding.PassportIdListItemBinding;

import java.util.List;

public class PassportIdAdapter extends ArrayAdapter<String> {

    public PassportIdAdapter(Context context, List<String> passportIds) {
        super(context, 0, passportIds);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        PassportIdListItemBinding binding;

        if (convertView == null) {
            binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()),
                    R.layout.passport_id_list_item, parent, false);
            convertView = binding.getRoot();
            convertView.setTag(binding);
        } else {
            binding = (PassportIdListItemBinding) convertView.getTag();
        }

        String passportId = getItem(position);

        binding.setItem(passportId);
        binding.executePendingBindings();

        return convertView;
    }

}

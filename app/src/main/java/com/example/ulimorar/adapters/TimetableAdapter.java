package com.example.ulimorar.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ulimorar.R;
import com.example.ulimorar.activities.TimetableFullscreenActivity;
import com.example.ulimorar.entities.Timetable;
import com.squareup.picasso.Picasso;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static androidx.core.content.res.TypedArrayUtils.getText;

public class TimetableAdapter extends RecyclerView.Adapter<TimetableAdapter.TimetableViewHolder> {

    private List<Timetable> timetables;
    private Context context;
    private boolean isAdmin;

    public TimetableAdapter(List<Timetable> timetables, Context context) {
        this.timetables = timetables;
        this.context = context;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    @NonNull
    @NotNull
    @Override
    public TimetableViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.timetable_list_item, parent, false);
        return new TimetableViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull @NotNull TimetableAdapter.TimetableViewHolder holder, int position) {

        Timetable timetable = timetables.get(position);

        Date date = new Date(timetable.getUpdateTime());
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String strDate = formatter.format(date);

        Picasso.get().load(timetable.getImageUrl()).placeholder(R.drawable.ulim_logo).into(holder.timetableImageView);
        holder.sessionNameTextView.setText(timetable.getTimetableName());
        holder.timetableDateTextView.setText(context.getText(R.string.last_update) + " " + strDate);

        holder.viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, TimetableFullscreenActivity.class);
                intent.putExtra("photoUrl", timetable.getImageUrl());
                context.startActivity(intent);
            }
        });

        if (isAdmin){
            holder.editTimetableButton.setVisibility(View.VISIBLE);
            holder.deleteTimetableButton.setVisibility(View.VISIBLE);

            holder.editTimetableButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(context, "Edit timetable!", Toast.LENGTH_SHORT).show();
                }
            });

            holder.deleteTimetableButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(context, "Delete timetable!", Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            holder.editTimetableButton.setVisibility(View.GONE);
            holder.deleteTimetableButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return timetables.size();
    }

    public static class TimetableViewHolder extends RecyclerView.ViewHolder{

        private ImageView timetableImageView;
        private TextView sessionNameTextView;
        private TextView timetableDateTextView;
        private Button viewButton;
        private Button editTimetableButton;
        private Button deleteTimetableButton;

        public TimetableViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            timetableImageView = itemView.findViewById(R.id.timetableImageView);
            sessionNameTextView = itemView.findViewById(R.id.sessionNameTextView);
            timetableDateTextView = itemView.findViewById(R.id.timetableDateTextView);
            viewButton = itemView.findViewById(R.id.viewButton);
            editTimetableButton = itemView.findViewById(R.id.editTimetableButton);
            deleteTimetableButton = itemView.findViewById(R.id.deleteTimetableButton);

        }
    }
}

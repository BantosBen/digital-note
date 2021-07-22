package com.sanj.digote.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.sanj.digote.R;
import com.sanj.digote.activities.localActivities.AddNote;
import com.sanj.digote.activities.cloudActivities.CloudAddNote;
import com.sanj.digote.models.NoteModel;

import java.util.List;

public class NoteRecyclerViewAdapter extends RecyclerView.Adapter<NoteRecyclerViewAdapter.ViewHolder> {

    private final List<NoteModel> noteModelList;
    private final Context context;
    private final boolean isCloud;

    public NoteRecyclerViewAdapter(List<NoteModel> noteModelList, Context context, boolean isCloud) {
        this.noteModelList = noteModelList;
        this.context = context;
        this.isCloud = isCloud;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item, parent, false));
    }

    @SuppressLint({"SetTextI18n", "ResourceAsColor"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NoteModel noteModel = noteModelList.get(position);
        holder.txtDate.setText(noteModel.getDate());
        holder.txtTitle.setText(noteModel.getTitle());
        if (noteModel.getNote().length() > 85) {
            holder.txtNote.setText(noteModel.getNote().substring(0, 85) + "...");
        } else {
            holder.txtNote.setText(noteModel.getNote());
        }

        holder.cardView.setOnClickListener(v -> {
            Intent intent;
            if (isCloud) {
                intent = new Intent(context, CloudAddNote.class);
            } else {
                intent = new Intent(context, AddNote.class);
            }
            intent.putExtra("id", noteModel.getId());
            context.startActivity(intent);
        });

        switch (noteModel.getColor()) {
            case "White":
                holder.cardView.setBackgroundResource(R.drawable.card_white);
                break;
            case "Blue":
                holder.cardView.setBackgroundResource(R.drawable.card_blue);
                break;
            case "Red":
                holder.cardView.setBackgroundResource(R.drawable.card_red);
                break;
            case "Green":
                holder.cardView.setBackgroundResource(R.drawable.card_green);
                break;
            case "Orange":
                holder.cardView.setBackgroundResource(R.drawable.card_orange);
                break;
        }

    }

    @Override
    public int getItemCount() {
        return noteModelList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView txtTitle, txtNote, txtDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            txtTitle = itemView.findViewById(R.id.noteTitle);
            txtDate = itemView.findViewById(R.id.noteDate);
            txtNote = itemView.findViewById(R.id.note);
        }
    }
}

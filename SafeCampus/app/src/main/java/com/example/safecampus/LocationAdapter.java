package com.example.safecampus;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {

    private ArrayList<LocationData> locationList;

    public LocationAdapter(ArrayList<LocationData> locationList) {
        this.locationList = locationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_location, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocationData data = locationList.get(position);


// Emoji based on type
        String emoji = "❓";
        if (data.getType() != null) {
            switch (data.getType()) {
                case "Accident": emoji = "🛣️"; break;
                case "Crime": emoji = "👮"; break;
                case "Damage": emoji = "🏠"; break;
            }
        }
        holder.tvEmoji.setText(emoji);
        holder.tvDescription.setText(data.getDescription());


// Timestamp is already string
        String timestampStr = data.getTimestamp() != null ? data.getTimestamp() : "N/A";


        String username = data.getUsername() != null ? data.getUsername() : "Anonymous";
        holder.tvTimestamp.setText(timestampStr + " by " + username);
    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvDescription, tvTimestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmoji = itemView.findViewById(R.id.tvEmoji);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}
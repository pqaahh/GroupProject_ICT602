package com.example.safecampus;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class RecentIncidentActivity extends AppCompatActivity {

    private RecyclerView rvIncidents;
    private LocationAdapter adapter;
    private ArrayList<LocationData> locationList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_incident);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Recent Incidents");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvIncidents = findViewById(R.id.rvIncidents);
        rvIncidents.setLayoutManager(new LinearLayoutManager(this));

        locationList = new ArrayList<>();
        adapter = new LocationAdapter(locationList);
        rvIncidents.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // Use snapshot listener to update in real time
        db.collection("locations")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot value, FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(RecentIncidentActivity.this,
                                    "Failed to load incidents: " + error.getMessage(),
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (value != null) {
                            locationList.clear();
                            for (DocumentChange dc : value.getDocumentChanges()) {
                                LocationData data = dc.getDocument().toObject(LocationData.class);
                                if (data != null && data.getType() != null) {
                                    locationList.add(data);
                                }
                            }
                            adapter.notifyDataSetChanged();

                            if (locationList.isEmpty()) {
                                Toast.makeText(RecentIncidentActivity.this,
                                        "No incidents reported yet.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
}
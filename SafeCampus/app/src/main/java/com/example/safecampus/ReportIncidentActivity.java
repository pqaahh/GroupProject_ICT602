package com.example.safecampus;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportIncidentActivity extends AppCompatActivity {

    private Spinner spinnerIncidentType;
    private EditText etDescription;
    private Button btnSendReport;
    private TextView tvLocation, tvLatLng, tvTime;

    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;

    private Double selectedLat = null;
    private Double selectedLng = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_incident);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Report Incident");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        spinnerIncidentType = findViewById(R.id.spinnerIncidentType);
        etDescription = findViewById(R.id.edtDescription);
        btnSendReport = findViewById(R.id.btnSendReport);
        tvLocation = findViewById(R.id.tvLocation);
        tvLatLng = findViewById(R.id.tvLatLng);
        tvTime = findViewById(R.id.tvTime);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseFirestore.getInstance();

        // Spinner setup
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Accident", "Crime", "Damage"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIncidentType.setAdapter(adapter);

        // Get selected location from Home
        Intent intent = getIntent();
        if (intent.hasExtra("selected_lat") && intent.hasExtra("selected_lng")) {
            selectedLat = intent.getDoubleExtra("selected_lat", Double.NaN);
            selectedLng = intent.getDoubleExtra("selected_lng", Double.NaN);

            if (!selectedLat.isNaN() && !selectedLng.isNaN()) {
                tvLatLng.setText("Latitude: " + selectedLat + "\nLongitude: " + selectedLng);
                tvLocation.setText("Selected location (from map)");
            } else {
                checkLocationPermission();
            }
        } else {
            checkLocationPermission();
        }

        String timestamp = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
        ).format(new Date());
        tvTime.setText(timestamp);

        btnSendReport.setOnClickListener(v -> submitReport());
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        } else {
            loadLocation();
        }
    }

    private void loadLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location == null) {
                tvLocation.setText("Location not detected");
                return;
            }

            if (selectedLat == null || selectedLng == null) {
                selectedLat = location.getLatitude();
                selectedLng = location.getLongitude();
                tvLatLng.setText("Latitude: " + selectedLat + "\nLongitude: " + selectedLng);

                new Thread(() -> {
                    try {
                        Geocoder geocoder = new Geocoder(this);
                        List<Address> addresses = geocoder.getFromLocation(
                                selectedLat, selectedLng, 1
                        );
                        if (!addresses.isEmpty()) {
                            runOnUiThread(() -> tvLocation.setText(addresses.get(0).getAddressLine(0)));
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> tvLocation.setText("Location detected"));
                    }
                }).start();
            }
        });
    }

    private void submitReport() {
        String desc = etDescription.getText().toString().trim();
        if (TextUtils.isEmpty(desc)) {
            etDescription.setError("Description required");
            return;
        }

        if (selectedLat == null || selectedLng == null) {
            Toast.makeText(this, "Location not detected", Toast.LENGTH_SHORT).show();
            return;
        }

        String username = getSharedPreferences(
                "SafeCampusPrefs", MODE_PRIVATE)
                .getString("username", "User");

        String timestamp = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
        ).format(new Date());

        Map<String, Object> data = new HashMap<>();
        data.put("type", spinnerIncidentType.getSelectedItem().toString());
        data.put("description", desc);
        data.put("latitude", selectedLat);
        data.put("longitude", selectedLng);
        data.put("timestamp", timestamp);
        data.put("username", username);

        db.collection("locations")
                .add(data)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Report submitted successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, RecentIncidentActivity.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to submit report", Toast.LENGTH_LONG).show());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadLocation();
        }
    }
}
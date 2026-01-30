package com.example.safecampus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class SafeCampusHomeActivity extends AppCompatActivity implements OnMapReadyCallback {

    private TextView tvWelcome, tvLocation;
    private MapView mapView;
    private GoogleMap gMap;

    private Button btnReport, btnViewIncidents, btnRefreshLocation;

    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final int LOCATION_PERMISSION_CODE = 101;

    // Store user-selected marker
    private LatLng selectedMarker = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Bind UI
        tvWelcome = findViewById(R.id.tvWelcome);
        tvLocation = findViewById(R.id.tvLocation);
        mapView = findViewById(R.id.mapView);

        btnReport = findViewById(R.id.btnReport);
        btnViewIncidents = findViewById(R.id.btnViewIncidents);
        btnRefreshLocation = findViewById(R.id.btnRefreshLocation);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseFirestore.getInstance();

        String username = getSharedPreferences("SafeCampusPrefs", MODE_PRIVATE)
                .getString("username", "User");
        tvWelcome.setText("Hello, " + username + "!");

        // MapView lifecycle
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        btnViewIncidents.setOnClickListener(v ->
                startActivity(new Intent(this, RecentIncidentActivity.class)));

        btnRefreshLocation.setOnClickListener(v -> {
            if (gMap != null) {
                gMap.clear();
                selectedMarker = null;
                enableLocation();
                loadMarkersFromFirestore();
                Toast.makeText(this, "Location refreshed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        gMap.getUiSettings().setZoomControlsEnabled(true);

        enableLocation();
        loadMarkersFromFirestore();

        // Tap map to select location
        gMap.setOnMapClickListener(latLng -> {
            selectedMarker = latLng;
            gMap.clear(); // optional: remove previous marker
            gMap.addMarker(new MarkerOptions()
                    .position(selectedMarker)
                    .title("Selected Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

            checkNearbyIncidents(selectedMarker);

            // Pass selected location to ReportIncidentActivity
            btnReport.setOnClickListener(v -> {
                Intent intent = new Intent(SafeCampusHomeActivity.this, ReportIncidentActivity.class);
                intent.putExtra("selected_lat", selectedMarker.latitude);
                intent.putExtra("selected_lng", selectedMarker.longitude);
                startActivity(intent);
            });
        });

        // If user clicks Report without selecting, default to current location
        btnReport.setOnClickListener(v -> {
            if (selectedMarker != null) return; // already handled above
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    Intent intent = new Intent(SafeCampusHomeActivity.this, ReportIncidentActivity.class);
                    intent.putExtra("selected_lat", location.getLatitude());
                    intent.putExtra("selected_lng", location.getLongitude());
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void enableLocation() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_CODE
            );
            return;
        }

        gMap.setMyLocationEnabled(true);

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location == null) {
                tvLocation.setText("Unable to get current location");
                return;
            }

            LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());

            // Current location marker (blue)
            gMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title("You are here")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 17));

            try {
                Geocoder geocoder = new Geocoder(this);
                List<Address> addresses = geocoder.getFromLocation(
                        location.getLatitude(),
                        location.getLongitude(),
                        1);

                if (addresses != null && !addresses.isEmpty()) {
                    tvLocation.setText("Your current location: " +
                            addresses.get(0).getAddressLine(0));
                } else {
                    tvLocation.setText("Your current location: Unknown place");
                }

            } catch (Exception e) {
                tvLocation.setText("Your current location: Unable to detect");
            }
        });
    }

    private void loadMarkersFromFirestore() {
        db.collection("locations")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {
                        try {
                            LocationData location = doc.toObject(LocationData.class);
                            if (location == null) continue;

                            Double lat = location.getLatitude();
                            Double lng = location.getLongitude();
                            if (lat == null || lng == null) continue;

                            float color;
                            switch (location.getType()) {
                                case "Accident":
                                    color = BitmapDescriptorFactory.HUE_RED;
                                    break;
                                case "Crime":
                                    color = BitmapDescriptorFactory.HUE_YELLOW;
                                    break;
                                case "Damage":
                                    color = BitmapDescriptorFactory.HUE_ORANGE;
                                    break;
                                default:
                                    color = BitmapDescriptorFactory.HUE_VIOLET;
                            }

                            gMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(lat, lng))
                                    .title(location.getType())
                                    .snippet(location.getDescription())
                                    .icon(BitmapDescriptorFactory.defaultMarker(color)));
                        } catch (Exception ignored) {}
                    }
                });
    }

    private void checkNearbyIncidents(LatLng selectedLatLng) {
        final double RADIUS_METERS = 500;

        db.collection("locations")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = 0;
                    for (DocumentSnapshot doc : querySnapshot) {
                        LocationData location = doc.toObject(LocationData.class);
                        if (location == null) continue;

                        Double lat = location.getLatitude();
                        Double lng = location.getLongitude();
                        if (lat == null || lng == null) continue;

                        float[] results = new float[1];
                        android.location.Location.distanceBetween(
                                selectedLatLng.latitude,
                                selectedLatLng.longitude,
                                lat,
                                lng,
                                results
                        );

                        if (results[0] <= RADIUS_METERS) count++;
                    }

                    if (count > 0) {
                        Toast.makeText(this,
                                count + " incident(s) found within 500m",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this,
                                "No incidents near this location",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableLocation();
        }
    }

    // MapView lifecycle
    @Override protected void onResume() { super.onResume(); mapView.onResume(); }
    @Override protected void onPause() { super.onPause(); mapView.onPause(); }
    @Override protected void onStart() { super.onStart(); mapView.onStart(); }
    @Override protected void onStop() { super.onStop(); mapView.onStop(); }
    @Override protected void onDestroy() { super.onDestroy(); mapView.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapBundle == null) mapBundle = new Bundle();
        mapView.onSaveInstanceState(mapBundle);
        outState.putBundle(MAPVIEW_BUNDLE_KEY, mapBundle);
    }
}
package com.example.trackinjava;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView locationText;
    private boolean isTrackingLocation = false;
    private List<String> locationStrings = new ArrayList<>();

    private final BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null &&
                    intent.getAction().equals("com.example.mygpsapp.LOCATION_UPDATE")) {

                double lat = intent.getDoubleExtra("lat", 0);
                double lon = intent.getDoubleExtra("lon", 0);

                String locText = "Lat: " + lat + ", Lon: " + lon;
                locationText.setText(locText);
            }
        }
    };

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Location permission is granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Location permission is required.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationText = findViewById(R.id.textView3);
        Button button = findViewById(R.id.button);
        Button button2 = findViewById(R.id.button2);

        checkPermission();

        button.setOnClickListener(v -> {

            if (isTrackingLocation) {
                stopTracking();
                isTrackingLocation = false;
            } else {
                startTracking();
                isTrackingLocation = true;
            }
        });

        button2.setOnClickListener(v -> loadSavedLocations());
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onResume() {
        super.onResume();
            registerReceiver(locationReceiver, new IntentFilter("com.example.mygpsapp.LOCATION_UPDATE"), Context.RECEIVER_EXPORTED);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(locationReceiver);
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void startTracking() {
        Intent serviceIntent = new Intent(this, LocationService.class);
        startForegroundService(serviceIntent);
        Log.d("SERVICE LOCATION", "Tracking successfully started");
    }

    private void stopTracking() {
        Intent serviceIntent = new Intent(this, LocationService.class);
        stopService(serviceIntent);
        Log.d("SERVICE LOCATION", "Tracking successfully stopped");
    }

    private void loadSavedLocations() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            List<LocationEntity> savedLocations = db.locationDao().getAll();
            List<TrackSession> savedSessions = db.trackSessionDao().getAll();

            locationStrings.clear();
            for (LocationEntity loc : savedLocations) {
                locationStrings.add(
                        String.format("Lat: %.5f, Lon: %.5f\n%s, %d",
                                loc.latitude,
                                loc.longitude,
                                new SimpleDateFormat("HH:mm:ss dd/MM/yyyy")
                                        .format(new Date(loc.timestamp)),
                                loc.sessionId
                        )
                );
            }

            for (TrackSession loc : savedSessions) {
                String entity = String.format("Session id: " + loc.sessionId +
                        " Session start time: " + loc.startTime +
                        " Session end time: " + loc.endTime
                );

                Log.d("DATABASE", entity);
            }


            Log.d("DATABASE", String.valueOf(savedLocations.size()));
            Log.d("DATABASE", String.valueOf(locationStrings));

            // Update UI on main thread
        }).start();
    }
}
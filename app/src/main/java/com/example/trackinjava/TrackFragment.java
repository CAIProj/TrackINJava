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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class TrackFragment extends Fragment {
    private Button startStopButton;
    private Button viewLogsButton;
    private TextView latlonText;
    private TextView altText;
    private TextView distanceText;
    private String lastlatlon = null;
    private String lastalt = null;
    private boolean isTrackingLocation = false;
    private List<String> locationStrings = new ArrayList<>();
    private BroadcastReceiver locationReceiver;
    private ActivityResultLauncher<String> locationPermissionLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_track, container, false);

        startStopButton = view.findViewById(R.id.startStopButton);
        viewLogsButton = view.findViewById(R.id.viewLogsButton);
        latlonText = view.findViewById(R.id.latLonText);
        altText = view.findViewById(R.id.altitudeText);
        distanceText = view.findViewById(R.id.distanceText);

        if (savedInstanceState != null) {
            lastlatlon = savedInstanceState.getString("lastlatlon");
            lastalt = savedInstanceState.getString("lastalt");
            isTrackingLocation = savedInstanceState.getBoolean("tracking", false);
        }

        if (lastalt != null && lastlatlon != null ) {
            latlonText.setText(lastlatlon);
            altText.setText(lastalt);
        }

        if (LocationService.isRunning) {
            isTrackingLocation = true;
            startStopButton.setText("STOP");
        } else {
            isTrackingLocation = false;
            startStopButton.setText("START");
        }

        setupLocationPermissionLauncher();
        checkPermission();
        setupReceiver();

        startStopButton.setOnClickListener(v -> {
            if (isTrackingLocation) {
                stopTracking();
                isTrackingLocation = false;
                startStopButton.setText("START");
            } else {
                startTracking();
                isTrackingLocation = true;
                startStopButton.setText("STOP");
            }
        });

        viewLogsButton.setOnClickListener(v -> loadSavedLocations());
        return view;
    }

    private void setupReceiver() {
        locationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null && "com.example.trackinjava.LOCATION_UPDATE".equals(intent.getAction())) {
                    double lat = intent.getDoubleExtra("lat", 0);
                    double lon = intent.getDoubleExtra("lon", 0);
                    double alt = intent.getDoubleExtra("alt", 0);

                    String locText = String.format("Lat: %.5f, Lon: %.5f", lat, lon);
                    String altStr = String.format("Altitude: %.2f m", alt);

                    lastlatlon = locText;
                    lastalt = altStr;

                    latlonText.setText(locText);  // ✅ use correct variable
                    altText.setText(altStr);

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            latlonText.setText(locText);
                            altText.setText(altStr);
                            distanceText.setText("Distance: -- m");
                        });
                    }
                }
            }
        };
    }

    private void setupLocationPermissionLauncher() {
        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Toast.makeText(getContext(), "Location permission granted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Location permission is required.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void startTracking() {
        Intent serviceIntent = new Intent(requireContext(), LocationService.class);
        requireContext().startForegroundService(serviceIntent);
        Log.d("SERVICE LOCATION", "Tracking successfully started");
    }

    private void stopTracking() {
        Intent serviceIntent = new Intent(requireContext(), LocationService.class);
        requireContext().stopService(serviceIntent);
        Log.d("SERVICE LOCATION", "Tracking successfully stopped");
    }

    private void loadSavedLocations() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            List<LocationEntity> savedLocations = db.locationDao().getAll();

            locationStrings.clear();
            for (LocationEntity loc : savedLocations) {
                String formatted = String.format("Lat: %.5f, Lon: %.5f, Alt: %.2f m\n%s",
                        loc.latitude, loc.longitude, loc.altitude,
                        new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(new Date(loc.timestamp)));
                locationStrings.add(formatted);
            }

            Log.d("DATABASE", "Loaded " + savedLocations.size() + " locations");

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    // Show in toast or dialog (replace with RecyclerView if needed)
                    Toast.makeText(requireContext(), "Loaded " + locationStrings.size() + " entries", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(locationReceiver, new IntentFilter("com.example.trackinjava.LOCATION_UPDATE"),
                    Context.RECEIVER_EXPORTED);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        requireContext().unregisterReceiver(locationReceiver);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("tracking", isTrackingLocation);
        outState.putString("lastlatlon", lastlatlon);
        outState.putString("lastalt", lastalt);
    }
}
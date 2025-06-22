package com.example.trackinjava;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;


public class TrackFragment extends Fragment {
    private Button startStopButton;
    private TextView latlonText;
    private TextView altText;
    private TextView distanceText;
    private TextView timeText;
    private String lastlatlon = null;
    private String lastalt = null;
    private String lastdist = null;
    private String lasttime = null;
    private boolean isTrackingLocation = false;
    private BroadcastReceiver locationReceiver;
    private ActivityResultLauncher<String> locationPermissionLauncher;
    private List<Location> trackedLocations = new ArrayList<>();
    private float totalDistance = 0f;
    private long trackingStartTime = 0L;
    private Handler timeHandler = new Handler(Looper.getMainLooper());
    private Runnable timeRunnable;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_track, container, false);

        startStopButton = view.findViewById(R.id.startStopButton);
        latlonText = view.findViewById(R.id.latLonText);
        altText = view.findViewById(R.id.altitudeText);
        distanceText = view.findViewById(R.id.distanceText);
        timeText = view.findViewById(R.id.timeText);

        if (savedInstanceState != null) {
            lastlatlon = savedInstanceState.getString("lastlatlon");
            lastalt = savedInstanceState.getString("lastalt");
            lastdist = savedInstanceState.getString("lastdist");
            lasttime = savedInstanceState.getString("lasttime");
            isTrackingLocation = savedInstanceState.getBoolean("tracking", false);

            latlonText.setText(lastlatlon);
            altText.setText(lastalt);
            distanceText.setText(lastdist);
            timeText.setText(lasttime);
        }

        startStopButton.setText(isTrackingLocation ? "STOP" : "START");
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

                    String locText = String.format("Lat: %.5f  Lon: %.5f", lat, lon);
                    String altStr = String.format("%.2f m", alt);

                    Location currentLocation = new Location("gps");
                    currentLocation.setLatitude(lat);
                    currentLocation.setLongitude(lon);

                    if (!trackedLocations.isEmpty()) {
                        Location lastLocation = trackedLocations.get(trackedLocations.size() - 1);
                        float distance = lastLocation.distanceTo(currentLocation);
                        totalDistance += distance;
                    }
                    trackedLocations.add(currentLocation);

                    String distStr = String.format("%.2f m", totalDistance);
                    String timeStr = formatDuration(System.currentTimeMillis() - trackingStartTime);

                    lastlatlon = locText;
                    lastalt = altStr;
                    lastdist = distStr;
                    lasttime = timeStr;

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            latlonText.setText(locText);
                            altText.setText(altStr);
                            distanceText.setText(distStr);
                            timeText.setText(timeStr);
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
        totalDistance = 0f;
        trackedLocations.clear();
        trackingStartTime = System.currentTimeMillis();
        Intent serviceIntent = new Intent(requireContext(), LocationService.class);
        requireContext().startForegroundService(serviceIntent);
        Log.d("SERVICE LOCATION", "Tracking successfully started");
        startElapsedTimeUpdates();
    }

    private void stopTracking() {
        long trackingEndTime = System.currentTimeMillis();
        long durationMillis = trackingEndTime - trackingStartTime;
        Log.d("SERVICE LOCATION", "Tracking duration: " + (durationMillis / 1000) + " seconds");
        Intent serviceIntent = new Intent(requireContext(), LocationService.class);
        requireContext().stopService(serviceIntent);
        Log.d("SERVICE LOCATION", "Tracking successfully stopped");
        stopElapsedTimeUpdates();
    }

    private String formatDuration(long durationMillis) {
        long seconds = (durationMillis / 1000) % 60;
        long minutes = (durationMillis / (1000 * 60)) % 60;
        long hours = (durationMillis / (1000 * 60 * 60));
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void startElapsedTimeUpdates() {
        timeRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsedMillis = System.currentTimeMillis() - trackingStartTime;
                String formatted = formatDuration(elapsedMillis);
                lasttime = formatted;
                if (timeText != null) {
                    timeText.setText(formatted);
                }
                timeHandler.postDelayed(this, 1000); // update every second
            }
        };
        timeHandler.post(timeRunnable);
    }
    private void stopElapsedTimeUpdates() {
        if (timeHandler != null && timeRunnable != null) {
            timeHandler.removeCallbacks(timeRunnable);
        }
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
        outState.putString("lastdist", lastdist);
        outState.putString("lasttime", lasttime);
    }
}
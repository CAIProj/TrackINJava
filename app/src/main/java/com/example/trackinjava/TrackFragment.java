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
import androidx.lifecycle.ViewModelProvider;


public class TrackFragment extends Fragment {
    private Button startStopButton;
    private TextView latlonText;
    private TextView altText;
    private TextView distanceText;
    private TextView timeText;
    private ActivityResultLauncher<String> locationPermissionLauncher;
    private TrackInfoViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_track, container, false);

        startStopButton = view.findViewById(R.id.startStopButton);
        latlonText = view.findViewById(R.id.latLonText);
        altText = view.findViewById(R.id.altitudeText);
        distanceText = view.findViewById(R.id.distanceText);
        timeText = view.findViewById(R.id.timeText);

        viewModel = new ViewModelProvider(requireActivity()).get(TrackInfoViewModel.class);

        viewModel.getSelectedTime().observe(getViewLifecycleOwner(), time -> {
            timeText.setText(time);
        });
        viewModel.getSelectedTrackInfo().observe(getViewLifecycleOwner(), locationInfo -> {

            if (locationInfo.latAndLong != null) {
                latlonText.setText(locationInfo.latAndLong);
            } else {
                latlonText.setText("Lat:  Lon:");
            }

            if (locationInfo.altitude != null) {
                altText.setText(locationInfo.altitude);
            } else {
                altText.setText("0.00 m");
            }

            if (locationInfo.distance != null) {
                distanceText.setText(locationInfo.distance);
            } else {
                distanceText.setText("0.00 m");
            }
        });
        viewModel.getSelectedTrackBoolean().observe(getViewLifecycleOwner(), isTrackingLocation -> {
            if (isTrackingLocation) {
                startStopButton.setText("STOP");
            } else {
                startStopButton.setText("START");
            }
        });

        setupLocationPermissionLauncher();
        checkPermission();
        viewModel.setupReceiver();

        startStopButton.setOnClickListener(v -> {
            viewModel.startStopButtonPressed(requireContext());
        });
        return view;
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


    @Override
    public void onResume() {
        super.onResume();
        viewModel.registerReceiver(requireContext());
    }

    @Override
    public void onPause() {
        super.onPause();
        viewModel.unregisterReceiver(requireContext());
    }
}
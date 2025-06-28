package com.example.trackinjava;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
    private TextView latLonText;
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
        latLonText = view.findViewById(R.id.latLonText);
        altText = view.findViewById(R.id.altitudeText);
        distanceText = view.findViewById(R.id.distanceText);
        timeText = view.findViewById(R.id.timeText);

        viewModel = new ViewModelProvider(requireActivity()).get(TrackInfoViewModel.class);

        viewModel.getSelectedTime().observe(getViewLifecycleOwner(), time -> {
            timeText.setText(time);
        });
        viewModel.getSelectedTrackInfo().observe(getViewLifecycleOwner(), locationInfo -> {

            if (locationInfo != null) {
                latLonText.setText(locationInfo.latAndLong);
                altText.setText(locationInfo.altitude);
                distanceText.setText(locationInfo.distance);
            } else {
                latLonText.setText("Lat:  Lon:");
                altText.setText("0.00 m");
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
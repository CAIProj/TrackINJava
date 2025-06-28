package com.example.trackinjava;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

class LocationInfo {
    public String latAndLong;
    public String altitude;
    public String distance;

    public LocationInfo(String latAndLong, String altitude, String distance) {
        this.latAndLong = latAndLong;
        this.altitude = altitude;
        this.distance = distance;
    }
}

public class TrackInfoViewModel extends ViewModel {
    private final MediatorLiveData<LocationInfo> trackInfo = new MediatorLiveData<LocationInfo>();
    private final MediatorLiveData<String> time = new MediatorLiveData<String>("00:00:00");
    private final MediatorLiveData<Boolean> isTrackingLocation = new MediatorLiveData<Boolean>(false);

    private BroadcastReceiver locationReceiver;
    private final List<Location> trackedLocations = new ArrayList<>();
    private float totalDistance = 0f;
    private final Handler timeHandler = new Handler(Looper.getMainLooper());
    private Runnable timeRunnable;
    private long trackingStartTime = 0L;

    public LiveData<LocationInfo> getSelectedTrackInfo() {
        return trackInfo;
    }
    public LiveData<String> getSelectedTime() {
        return time;
    }
    public LiveData<Boolean> getSelectedTrackBoolean() {
        return isTrackingLocation;
    }

    public void setupReceiver() {
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

                    trackInfo.setValue(new LocationInfo(locText, altStr, distStr));
                }
            }
        };
    }

    public void registerReceiver(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(locationReceiver, new IntentFilter("com.example.trackinjava.LOCATION_UPDATE"),
                    Context.RECEIVER_EXPORTED);
        }
    }

    public void unregisterReceiver(Context context) {
        context.unregisterReceiver(locationReceiver);
    }

    public void startStopButtonPressed(Context context) {

        if (isTrackingLocation.getValue()) {
            stopTracking(context);
            isTrackingLocation.setValue(false);
        } else {
            startTracking(context);
            trackInfo.setValue(null);
            isTrackingLocation.setValue(true);
        }
    }

    private void startTracking(Context context) {
        totalDistance = 0f;
        trackedLocations.clear();
        Intent serviceIntent = new Intent(context, LocationTrackingService.class);
        context.startForegroundService(serviceIntent);
        startElapsedTimeUpdates();
    }

    private void stopTracking(Context context) {
        Intent serviceIntent = new Intent(context, LocationTrackingService.class);
        context.stopService(serviceIntent);
        stopElapsedTimeUpdates();
    }

    public void startElapsedTimeUpdates() {
        trackingStartTime = System.currentTimeMillis();

        timeRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsedMillis = System.currentTimeMillis() - trackingStartTime;
                String formatted = formatDuration(elapsedMillis);
                time.setValue(formatted);
                timeHandler.postDelayed(this, 1000); // update every second
            }
        };
        timeHandler.post(timeRunnable);
    }

    public void stopElapsedTimeUpdates() {
        if (timeHandler != null && timeRunnable != null) {
            timeHandler.removeCallbacks(timeRunnable);
        }
    }

    private String formatDuration(long durationMillis) {
        long seconds = (durationMillis / 1000) % 60;
        long minutes = (durationMillis / (1000 * 60)) % 60;
        long hours = (durationMillis / (1000 * 60 * 60));
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}

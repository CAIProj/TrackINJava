package com.example.trackinjava;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.*;

public class LocationService extends Service {

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private long currentSessionId = -1;
    public static boolean isRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        startForegroundService();
        isRunning = true;

        AppDatabase db = AppDatabase.getInstance(this);
        TrackSession session = new TrackSession();
        session.startTime = System.currentTimeMillis();

        new Thread(() -> currentSessionId = db.trackSessionDao().insert(session)).start();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(2000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    String locText = "Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude();
                    Log.d("LOCATION", locText);

                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    double altitude = location.getAltitude();

                    new Thread(() -> db.locationDao().insert(
                            new LocationEntity(currentSessionId, altitude, latitude, longitude, System.currentTimeMillis())
                    )).start();

                    Intent intent = new Intent("com.example.trackinjava.LOCATION_UPDATE");
                    intent.putExtra("alt", altitude);
                    intent.putExtra("lat", latitude);
                    intent.putExtra("lon", longitude);
                    sendBroadcast(intent);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
    }

    private void startForegroundService() {
        String channelId = "location_channel";
        NotificationChannel channel = new NotificationChannel(
                channelId, "Location Service", NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("GPS Tracking Active")
                .setContentText("Tracking location in background")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .build();

        startForeground(1, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        AppDatabase db = AppDatabase.getInstance(this);
        new Thread(() -> db.trackSessionDao().endSession(currentSessionId, System.currentTimeMillis())).start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

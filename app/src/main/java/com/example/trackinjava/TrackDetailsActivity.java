package com.example.trackinjava;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class TrackDetailsActivity extends AppCompatActivity {
    private TextView startTime;
    private TextView endTime;
    private TextView distance;
    private ListView pointsListView;
    private ArrayAdapter<LocationEntity> pointAdapter;
    private List<LocationEntity> points = new ArrayList<LocationEntity>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button shareButton = findViewById(R.id.shareButton);

        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);
        distance = findViewById(R.id.distance);

        Intent intent = getIntent();
        long sessionId = intent.getLongExtra("trackDetailsScreenSessionId", 0);

        pointsListView = findViewById(R.id.pointsListView);
        pointAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, points);
        pointsListView.setAdapter(pointAdapter);

        loadSavedPoints(sessionId);

        shareButton.setOnClickListener(v -> {
            String gpxString = exportToGpx(points);
            File tempGpxFile = createTempGpxFile(gpxString);

            Uri gpxFileUri = FileProvider.getUriForFile(
                    this,
                    "com.example.trackinjava.fileprovider",
                    tempGpxFile
            );

            Intent shareFileIntent = new Intent(Intent.ACTION_SEND);
            shareFileIntent.setType("application/gpx+xml");
            shareFileIntent.putExtra(Intent.EXTRA_STREAM, gpxFileUri);
            shareFileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareFileIntent, "Share GPX"));
        });
    }

    private void loadSavedPoints(long sessionId) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            List<LocationEntity> savedPoints = db.locationDao().getBySession(sessionId);

            // Update UI on main thread
            new Handler(Looper.getMainLooper()).post(() -> {
                points.clear();

                points.addAll(savedPoints);
                pointAdapter.notifyDataSetChanged();

                startTime.setText(points.get(0).getDate());
                endTime.setText(points.get(points.size() - 1).getDate());

                float totalDistance = 0;
                for (int i = 0; i < points.size()-1; i++) {
                    Location firstLocation = new Location("gps");
                    firstLocation.setLatitude(points.get(i).latitude);
                    firstLocation.setLongitude(points.get(i).longitude);

                    Location secondLocation = new Location("gps");
                    secondLocation.setLatitude(points.get(i+1).latitude);
                    secondLocation.setLongitude(points.get(i+1).longitude);

                    totalDistance += firstLocation.distanceTo(secondLocation);
                }

                distance.setText(String.valueOf((int) totalDistance) + " m");
            });
        }).start();
    }

    // Files in cache should not exceed 1MB size
    // https://stackoverflow.com/questions/3425906/creating-temporary-files-in-android
    public File createTempGpxFile(String gpxString) {
        try {
            File tempFile = File.createTempFile("track_", ".gpx", getCacheDir());
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(gpxString.getBytes(StandardCharsets.UTF_8));
            fos.close();
            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String exportToGpx(List<LocationEntity> points) {
        StringBuilder gpx = new StringBuilder();
        gpx.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        gpx.append("<gpx version=\"1.1\" creator=\"MyGPSApp\">\n");

        gpx.append("  <trk><name>Track</name><trkseg>\n");

        for (LocationEntity point : points) {
            gpx.append(String.format("    <trkpt lat=\"%.6f\" lon=\"%.6f\">\n", point.latitude, point.longitude));
            gpx.append(String.format("      <time>%s</time>\n", iso8601(point.timestamp)));
            gpx.append("    </trkpt>\n");
        }

        gpx.append("  </trkseg></trk>\n");
        gpx.append("</gpx>");
        return gpx.toString();
    }

    private String iso8601(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.GERMANY);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date(millis));
    }
}
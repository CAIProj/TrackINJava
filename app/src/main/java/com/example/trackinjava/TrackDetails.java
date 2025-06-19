package com.example.trackinjava;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class TrackDetails extends AppCompatActivity {
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

        Intent intent = getIntent();
        long sessionId = intent.getLongExtra("trackDetailsScreenSessionId", 0);

        pointsListView = findViewById(R.id.pointsListView);
        pointAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, points);
        pointsListView.setAdapter(pointAdapter);

        loadSavedPoints(sessionId);
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
            });
        }).start();
    }

}
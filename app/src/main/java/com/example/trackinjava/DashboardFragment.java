package com.example.trackinjava;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {
    private ListView sessionsListView;
    private ArrayAdapter<TrackSession> trackAdapter;
    private List<TrackSession> tracks = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        sessionsListView = view.findViewById(R.id.sessionsListView);
        trackAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_list_item_1, tracks);
        sessionsListView.setAdapter(trackAdapter);

        loadSavedLocations();

        sessionsListView.setOnItemClickListener((adapter, view1, position, arg) -> {
            Intent trackDetails = new Intent(getActivity().getApplicationContext(), TrackDetails.class);
            TrackSession trackSession = (TrackSession) adapter.getItemAtPosition(position);
            trackDetails.putExtra("trackDetailsScreenSessionId", trackSession.sessionId);
            startActivity(trackDetails);
        });

        return view;
    }

    private void loadSavedLocations() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getActivity().getApplicationContext());
            List<TrackSession> savedSessions = db.trackSessionDao().getAll();

            // Update UI on main thread
            new Handler(Looper.getMainLooper()).post(() -> {
                tracks.clear();

                        tracks.addAll(savedSessions);
                trackAdapter.notifyDataSetChanged();
            });
        }).start();
    }
}

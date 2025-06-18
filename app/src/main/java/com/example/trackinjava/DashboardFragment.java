package com.example.trackinjava;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DashboardFragment extends Fragment {
    private ListView sessionsListView;
    private ArrayAdapter<String> locationAdapter;
    private List<String> locationStrings = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        sessionsListView = view.findViewById(R.id.sessionsListView);
        locationAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_list_item_1, locationStrings);
        sessionsListView.setAdapter(locationAdapter);

        loadSavedLocations();

        return view;
    }


    private void loadSavedLocations() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getActivity().getApplicationContext());
            List<TrackSession> savedSessions = db.trackSessionDao().getAll();

            locationStrings.clear();

            for (TrackSession loc : savedSessions) {
                String entity = String.format("Session id: " + loc.sessionId +
                        " \nStart : " + convertDate(loc.startTime)
                );

                locationStrings.add(entity);
            }

            Log.d("DATABASE", String.valueOf(locationStrings));

            // Update UI on main thread
            new Handler(Looper.getMainLooper()).post(() -> locationAdapter.notifyDataSetChanged());
        }).start();
    }

    private static String convertDate(long dateInMilliseconds) {
        return DateFormat.format("dd/MM/yyyy hh:mm:ss", dateInMilliseconds).toString();
    }
}

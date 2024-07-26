package com.example.piculinary;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class HistoryAlbumFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history_album, container, false);

        // Find the back button and set the click listener
        ImageView backButton = view.findViewById(R.id.arrowBack);
        backButton.setOnClickListener(v -> {
            // Go back to the previous page/fragment
            ((MainActivity) requireActivity()).navigateToFragment(new HistoryFragment());
        });

        return view;
    }
}
package com.example.piculinary;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.graphics.Rect;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.util.Objects;
import java.util.HashMap;
import java.util.Map;

public class AboutUsFragment extends Fragment {

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about_us, container, false);

        Map<Integer, Fragment> buttonFragmentMap = new HashMap<>();
        buttonFragmentMap.put(R.id.researchPaperId, new AboutResearchFragment());
        buttonFragmentMap.put(R.id.alleyahId, new AboutAlleyahFragment());
        buttonFragmentMap.put(R.id.aaronId, new AboutAaronFragment());

        for (Map.Entry<Integer, Fragment> entry : buttonFragmentMap.entrySet()) {
            ImageButton button = view.findViewById(entry.getKey());
            Fragment fragment = entry.getValue();

            button.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Rect buttonRect = new Rect();
                    button.getGlobalVisibleRect(buttonRect);

                    // Assuming overlayImage is the ID of the overlay image covering part of the button
                    View table = view.findViewById(R.id.tableImageView);  // Adjust ID as needed
                    Rect overlayRect = new Rect();
                    table.getGlobalVisibleRect(overlayRect);

                    // Check if the touch is on the button and not on the overlay
                    if (buttonRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                        if (v.getId() == R.id.researchPaperId || !overlayRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                            // Perform click action
                            v.performClick();
                            ((MainActivity) requireActivity()).navigateToFragment(fragment);
                            return true;
                        }
                    }
                }
                return false;
            });
        }

        // Inflate the layout for this fragment
        return view;
    }
}
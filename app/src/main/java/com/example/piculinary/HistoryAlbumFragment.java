package com.example.piculinary;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.example.piculinary.databinding.FragmentHistoryAlbumBinding;

public class HistoryAlbumFragment extends Fragment {

    private FragmentHistoryAlbumBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment using view binding
        binding = FragmentHistoryAlbumBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Set the click listener for the back button using view binding
        binding.arrowBack.setOnClickListener(v -> {
            // Go back to the previous page/fragment
            ((MainActivity) requireActivity()).navigateToFragment(new HistoryFragment());
        });

        String[] cuisineName = {"Cuisine 1", "Cuisine 2", "Cuisine 3", "Cuisine 4"};
        int[] cuisineImage = {R.drawable.image_template, R.drawable.image_template, R.drawable.image_template, R.drawable.image_template};

        HistoryGridAdapter historyGridAdapter = new HistoryGridAdapter(getActivity(), cuisineName, cuisineImage);
        binding.historyAlbumGrid.setAdapter(historyGridAdapter);

        binding.historyAlbumGrid.setOnItemClickListener((adapterView, view1, i, l) -> ((MainActivity) requireActivity()).navigateToFragment(new HistorySpecificFood()));

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Avoid memory leaks
    }
}
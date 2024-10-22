package com.example.piculinary;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Collections;

public class CuisineListView extends Fragment {

    private AlphabetAdapter adapter;
    private RecyclerView recyclerView;
    private LinearLayout indexBar;
    private TextView currentCategoryView;
    private final String[] categories = {"All", "Main Dish", "Vegetable Dish", "Snack", "Dessert"};
    private int currentCategoryIndex = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cuisine_list_view, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Sample data
        List<RecipeItem> data = Arrays.asList(
                new RecipeItem("Achara", "Vegetable Dish"),
                new RecipeItem("Adobong Nukos", "Main Dish"),
                new RecipeItem("Adobong Pina-uga", "Main Dish"),
                new RecipeItem("Arroz a la Valenciana", "Main Dish"),
                new RecipeItem("Bam-i", "Main Dish"),
                new RecipeItem("Bibingka", "Snack"),
                new RecipeItem("Biko", "Snack"),
                new RecipeItem("Binignit", "Snack"),
                new RecipeItem("Brazo de Mercedes", "Dessert"),
                new RecipeItem("Caldereta", "Main Dish"),
                new RecipeItem("Dinugu-an", "Main Dish"),
                new RecipeItem("Dried Mangoes", "Snack"),
                new RecipeItem("Embotido", "Main Dish"),
                new RecipeItem("Empanada", "Snack"),
                new RecipeItem("Escabeche", "Main Dish"),
                new RecipeItem("Humba", "Main Dish"),
                new RecipeItem("Leche Flan", "Dessert"),
                new RecipeItem("Lechon de Cebu", "Main Dish"),
                new RecipeItem("Linat-ang Baka", "Main Dish"),
                new RecipeItem("Maruya", "Snack"),
                new RecipeItem("Monggos", "Vegetable Dish"),
                new RecipeItem("Nangka nga Tinuno-an", "Vegetable Dish"),
                new RecipeItem("Otap", "Snack"),
                new RecipeItem("Pata", "Main Dish"),
                new RecipeItem("Sinugba nga Isda", "Main Dish"),
                new RecipeItem("Utan Bisaya", "Vegetable Dish"),
                new RecipeItem("Yemas", "Dessert")
        );

        adapter = new AlphabetAdapter(data, this::onItemClicked);
        adapter.setOnDataChangedListener(this::updateIndexBar); // Set listener to update index bar
        recyclerView.setAdapter(adapter);

        // Setup index bar
        indexBar = view.findViewById(R.id.indexBar);
        setupIndexBar();

        // Setup category navigation
        setupCategoryNavigation(view);

        // Find the back button and set the click listener
        ImageView backButton = view.findViewById(R.id.arrowBack);
        backButton.setOnClickListener(v -> {
            // Go back to the previous page/fragment
            ((MainActivity) requireActivity()).navigateToFragment(new CuisineFragment());
        });

        return view;
    }

    private void setupIndexBar() {
        // Collect unique section letters
        Set<String> sections = new HashSet<>();
        for (AlphabetAdapter.Item item : adapter.getOriginalItems()) {
            if (item.type == AlphabetAdapter.TYPE_ITEM) {
                String firstChar = item.text.substring(0, 1).toUpperCase();
                sections.add(firstChar);
            }
        }

        // Sort sections alphabetically
        List<String> sortedSections = new ArrayList<>(sections);
        Collections.sort(sortedSections);

        // Create and add buttons to index bar
        indexBar.removeAllViews(); // Clear previous views
        for (String section : sortedSections) {
            TextView indexLetter = new TextView(getContext());
            indexLetter.setText(section);
            indexLetter.setPadding(12, 12, 12, 12); // Adjust padding as needed
            indexLetter.setTextSize(16); // Adjust text size as needed
            indexLetter.setTextColor(Color.parseColor("#593D3B"));
            indexLetter.setOnClickListener(v -> scrollToSection(section));
            indexBar.addView(indexLetter);
        }
    }

    private void updateIndexBar() {
        // Update index bar based on filtered data
        Set<String> sections = new HashSet<>();
        for (AlphabetAdapter.Item item : adapter.getItemList()) {
            if (item.type == AlphabetAdapter.TYPE_ITEM) {
                String firstChar = item.text.substring(0, 1).toUpperCase();
                sections.add(firstChar);
            }
        }

        // Sort sections alphabetically
        List<String> sortedSections = new ArrayList<>(sections);
        Collections.sort(sortedSections);

        // Create and add buttons to index bar
        indexBar.removeAllViews(); // Clear previous views
        for (String section : sortedSections) {
            TextView indexLetter = new TextView(getContext());
            indexLetter.setText(section);
            indexLetter.setPadding(12, 12, 12, 12); // Adjust padding as needed
            indexLetter.setTextSize(16); // Adjust text size as needed
            indexLetter.setTextColor(Color.parseColor("#593D3B"));
            indexLetter.setOnClickListener(v -> scrollToSection(section));
            indexBar.addView(indexLetter);
        }
    }

    private void scrollToSection(String section) {
        int position = adapter.getSectionPosition(section);
        if (position != -1) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (layoutManager != null) {
                layoutManager.scrollToPositionWithOffset(position, 0); // 0 to align to top
            }
        }
    }

//    private void onItemClicked(RecipeItem item) {
//        // Example of navigating to another fragment
//        ((MainActivity) requireActivity()).navigateToFragment(new CuisineSpecificFood()); // Replace CuisineSpecificFood with the target fragment
//    }


    private void onItemClicked(RecipeItem item) {
        // Create a new instance of the CuisineSpecificFood fragment
        CuisineSpecificFood fragment = new CuisineSpecificFood();

        // Prepare the bundle to pass the cuisine name
        Bundle bundle = new Bundle();
        bundle.putString("CuisineName", item.getName());  // Pass the cuisine name

        // Set the bundle as arguments for the fragment
        fragment.setArguments(bundle);

        // Navigate to the fragment
        ((MainActivity) requireActivity()).navigateToFragment(fragment);
    }

    private void setupCategoryNavigation(View view) {
        currentCategoryView = view.findViewById(R.id.category_name);
        ImageView arrowLeft = view.findViewById(R.id.arrowLeft);
        ImageView arrowRight = view.findViewById(R.id.arrowRight);

        // Initialize current category view
        updateCategoryView();

        arrowLeft.setOnClickListener(v -> {
            currentCategoryIndex = (currentCategoryIndex > 0) ? currentCategoryIndex - 1 : categories.length - 1;
            updateCategoryView();
            adapter.filterByCategory(categories[currentCategoryIndex]);
        });

        arrowRight.setOnClickListener(v -> {
            currentCategoryIndex = (currentCategoryIndex < categories.length - 1) ? currentCategoryIndex + 1 : 0;
            updateCategoryView();
            adapter.filterByCategory(categories[currentCategoryIndex]);
        });
    }

    private void updateCategoryView() {
        currentCategoryView.setText(categories[currentCategoryIndex]);
    }
}

package com.example.piculinary;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CuisineSpecificFood extends Fragment {

    private String cuisineName;
    private int currentRecipeIndex = 0; // Track the current recipe index
    private List<QueryDocumentSnapshot> availableRecipes; // Store the available recipes

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cuisine_specific_food, container, false);

        ProgressBar loadingSpinner = view.findViewById(R.id.loading_spinner);
        ScrollView scrollView = view.findViewById(R.id.scrollView);

        // Retrieve the TextView and ImageView instances
        TextView cuisineNameTextView = view.findViewById(R.id.cuisine_name);
        TextView categoryNameTextView = view.findViewById(R.id.category_name);
        TextView websiteNameTextView = view.findViewById(R.id.cookbook_website_name);
        TextView ingredientsTextView = view.findViewById(R.id.ingredients_list);
        TextView instructionsTextView = view.findViewById(R.id.instructions_list);
        ImageView imageView = view.findViewById(R.id.cuisine_picture);  // Add ImageView reference
        ImageView backButton = view.findViewById(R.id.arrowBack);
        ImageView nextButton = view.findViewById(R.id.next); // Add reference to the next button

        loadingSpinner.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE); // Hide ScrollView initially
        nextButton.setVisibility(View.GONE);

        // Get the cuisine name from the arguments (set in the previous fragment)
        if (getArguments() != null) {
            cuisineName = getArguments().getString("CuisineName"); // Ensure the key matches
        }

        // Initialize Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Fetch the recipes based on the cuisine name
        db.collection("recipeCollection")
                .whereEqualTo("cuisine_name", cuisineName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    loadingSpinner.setVisibility(View.GONE);
                    scrollView.setVisibility(View.VISIBLE); // Hide ScrollView initially

                    if (queryDocumentSnapshots.isEmpty()) {
                        // Handle case where no documents are found
                        cuisineNameTextView.setText("No recipe found");
                        return;
                    }

                    // Filter recipes based on the specified order
                    List<String> preferredSources = Arrays.asList(
                            "Panlasang Pinoy", "Kawaling Pinoy",
                            "Pinoy Recipe At Iba Pa", "tasteatlas", "Fresh Off The Grid"
                    );
                    availableRecipes = new ArrayList<>();

                    for (String source : preferredSources) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            if (source.equals(document.getString("source_name"))) {
                                availableRecipes.add(document);
                                break; // Break to ensure only the first matching recipe is added
                            }
                        }
                    }

                    if (availableRecipes.size() <= 1) {
                        nextButton.setVisibility(View.GONE); // Hide next button if there's only one recipe
                    } else {
                        nextButton.setVisibility(View.VISIBLE); // Show next button if multiple recipes exist
                    }

                    // Display the first recipe
                    displayRecipe(availableRecipes.get(currentRecipeIndex), cuisineNameTextView, categoryNameTextView, websiteNameTextView, ingredientsTextView, instructionsTextView, imageView);
                })
                .addOnFailureListener(e -> {
                    // Handle the error
                    cuisineNameTextView.setText("Error fetching recipe");
                });

        // Set the click listener for the back button
        backButton.setOnClickListener(v -> {
            // Go back to the previous page/fragment
            ((MainActivity) requireActivity()).navigateToFragment(new CuisineListView());
        });

        // Set the click listener for the next button
        nextButton.setOnClickListener(v -> {
            // Move to the next recipe
            currentRecipeIndex++;
            if (currentRecipeIndex >= availableRecipes.size()) {
                currentRecipeIndex = 0; // Loop back to the first recipe
            }
            displayRecipe(availableRecipes.get(currentRecipeIndex), cuisineNameTextView, categoryNameTextView, websiteNameTextView, ingredientsTextView, instructionsTextView, imageView);

            scrollView.scrollTo(0, 0);
        });

        return view;
    }

    private void displayRecipe(QueryDocumentSnapshot document, TextView cuisineNameTextView, TextView categoryNameTextView, TextView websiteNameTextView, TextView ingredientsTextView, TextView instructionsTextView, ImageView imageView) {
        String categoryName = document.getString("category");
        String websiteName = document.getString("source_name");
        List<String> ingredients = (List<String>) document.get("ingredients");
        List<String> instructions = (List<String>) document.get("instructions");

        // Update the UI with the retrieved data
        cuisineNameTextView.setText(cuisineName);
        categoryNameTextView.setText(categoryName);
        websiteNameTextView.setText(websiteName);

        StringBuilder ingredientsFormatted = new StringBuilder();
        boolean isUnderHeader = false; // Track if we are under an all-caps header

        assert ingredients != null;
        if ("Lechon de Cebu".equals(cuisineName)) {
            // Special formatting for "Lechon de Cebu"
            for (String ingredient : ingredients) {
                // Check if the ingredient is in all caps (section header)
                if (ingredient.equals(ingredient.toUpperCase())) {
                    // Add the section header with a main bullet
                    ingredientsFormatted.append("• ").append(ingredient).append("\n");
                    isUnderHeader = true; // Set to true since this is a header

                } else {
                    // Add the regular ingredient
                    if (isUnderHeader) {
                        ingredientsFormatted.append("\t→ ").append(ingredient).append("\n"); // Indented bullet
                    } else {
                        ingredientsFormatted.append("• ").append(ingredient).append("\n"); // Regular bullet if not under a header
                    }
                }
            }
        } else {
            // Default formatting for other cuisines
            for (String ingredient : ingredients) {
                ingredientsFormatted.append("• ").append(ingredient).append("\n");
            }
        }
        ingredientsTextView.setText(ingredientsFormatted.toString().trim());
        ingredientsTextView.setLineSpacing(5, 1f);

// Prepare the instructions list
        StringBuilder instructionsFormatted = new StringBuilder();
        for (int i = 0; i < (instructions != null ? instructions.size() : 0); i++) {
            instructionsFormatted.append(i + 1).append(". ").append(instructions.get(i)).append("\n"); // Add an extra newline
        }
        instructionsTextView.setText(instructionsFormatted.toString().trim());
        instructionsTextView.setLineSpacing(5, 1f);

        // Set the image resource (you can update this later with actual images)
        int imageResource = R.drawable.image_template; // Placeholder image
        imageView.setImageResource(imageResource);
    }
}

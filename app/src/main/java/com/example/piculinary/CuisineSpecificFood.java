package com.example.piculinary;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

public class CuisineSpecificFood extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cuisine_specific_food, container, false);

        // Retrieve the JustifiedTextView instances
        TextView cuisineNameTextView = view.findViewById(R.id.cuisine_name);
        TextView categoryNameTextView = view.findViewById(R.id.category_name);
        TextView websiteNameTextView = view.findViewById(R.id.cookbook_website_name);

        String CuisineName = "Lechon de Cebu";
        String CategoryName = "Main Dish";
        String WebsiteName = "yummy.ph";

        cuisineNameTextView.setText(CuisineName);
        categoryNameTextView.setText(CategoryName);
        websiteNameTextView.setText(WebsiteName);

        // Retrieve the ImageView instance
        ImageView imageView = view.findViewById(R.id.cuisine_picture);

        int imageResource = R.drawable.image_template;
        imageView.setImageResource(imageResource);

        List<String> ingredients = Arrays.asList("Ingredient 1", "Ingredient 2", "Ingredient 3");
        TextView ingredientsTextView = view.findViewById(R.id.ingredients_list);

        // Prepare the formatted ingredients list with bullet points
        StringBuilder ingredientsFormatted = new StringBuilder();
        for (String ingredient : ingredients) {
            ingredientsFormatted.append("• ").append(ingredient).append("\n");
        }

        // Set the formatted text to the TextView
        ingredientsTextView.setText(ingredientsFormatted.toString().trim());


        List<String> instructions = Arrays.asList("Step 1", "Step 2", "Step 3");
        TextView instructionsTextView = view.findViewById(R.id.instructions_list);

        // Prepare the formatted ingredients list with numbers
        StringBuilder instructionsFormatted = new StringBuilder();
        for (int i = 0; i < ingredients.size(); i++) {
            instructionsFormatted.append(i + 1).append(". ").append(instructions.get(i)).append("\n");
        }

        // Set the formatted text to the TextView
        instructionsTextView.setText(instructionsFormatted.toString().trim());

        // Find the back button and set the click listener
        ImageView backButton = view.findViewById(R.id.arrowBack);
        backButton.setOnClickListener(v -> {
            // Go back to the previous page/fragment
            ((MainActivity) requireActivity()).navigateToFragment(new CuisineListView());
        });

        return view;
    }
}
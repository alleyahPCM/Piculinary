package com.example.piculinary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.piculinary.Utils.NetworkChangeListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Results extends Fragment {

    private final NetworkChangeListener nc = new NetworkChangeListener();
    private final OkHttpClient client = new OkHttpClient();
    private int currentRecipeIndex = 0; // Track the current recipe index
    private List<QueryDocumentSnapshot> availableRecipes; // Store the available recipes

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_results, container, false);

        // Find the back button and set the click listener
        ImageView backButton = view.findViewById(R.id.arrowBack);
        backButton.setOnClickListener(v -> {
            // Go back to the previous page/fragment
            ((MainActivity) requireActivity()).navigateToFragment(new CameraFragment());
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Retrieve the image data
        Uri imageUri = Uri.parse(requireArguments().getString("image_data"));

        ImageView imageView = view.findViewById(R.id.cuisine_picture);
        imageView.setImageURI(imageUri);

        ProgressBar loadingSpinner = view.findViewById(R.id.loading_spinner);
        ScrollView scrollView = view.findViewById(R.id.scrollView);
        ImageView nextButton = view.findViewById(R.id.next);

        loadingSpinner.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE); // Hide ScrollView initially
        nextButton.setVisibility(View.GONE);

        uploadImageToFirebase(imageUri);
    }

    private void uploadImageToFirebase(Uri imageUri) {
        Uri compressed;
        if (imageUri != null) {
            try {
                compressed = decodeUri(requireContext(), imageUri, 750);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // Create a storage reference
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference();

            // Create a reference to the image
            StorageReference imageRef = storageReference.child("images/" + System.currentTimeMillis() + ".jpg");

            // Upload the image
            imageRef.putFile(compressed)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Get the download URL
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String img = uri.toString();
                            sendImageUri(img);

                        });
                    })
                    .addOnFailureListener(e -> Log.e("Upload Error", Objects.requireNonNull(e.getMessage())));
        }
    }

    private void sendImageUri(String img) {

        // Prepare the JSON request body
        String requestBody = "{\n  \"data\": [\n    {\"path\":\"" + img + "\"}\n]}";

        // Create the RequestBody
        RequestBody body = RequestBody.create(requestBody, MediaType.parse("application/json"));

        // Build the request
        Request request = new Request.Builder()
                .url("https://rieze-cebuanocuisine.hf.space/call/predict")
                .post(body)
                .build();

        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                // Get the response body
                assert response.body() != null;
                String responseBody = response.body().string();
                try {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    String eventId = jsonResponse.getString("event_id");
                    System.out.println(eventId);
                    getImageData(eventId);

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            } catch (IOException e) {
//                e.printStackTrace();
//                Log.e("Network Error", Objects.requireNonNull(e.getMessage()));
                Log.e("Network Error", "An IO exception occurred", e);
            }
        }).start();
    }

    @SuppressLint("SetTextI18n")
    private void getImageData(String event_id) {

        // Build the URL with the query parameter
        String url = "https://rieze-cebuanocuisine.hf.space/call/predict/" + event_id;

        // Build the request
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        // Execute the request in a background thread
        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                // Get the response body
                assert response.body() != null;
                String responseBody = response.body().string();
                String trimmed = responseBody.substring(23, responseBody.length() - 3);
                String[] data = trimmed.split(",", 2);
                String cuis = data[0].trim().replace("\"", "");
                String per = data[1].trim().replace("\"", "") + "%";

                // Update UI on the main thread
                requireActivity().runOnUiThread(() -> {
                    ProgressBar loadingSpinner = requireView().findViewById(R.id.loading_spinner);
                    ScrollView scrollView = requireView().findViewById(R.id.scrollView);
                    ImageView nextButton = requireView().findViewById(R.id.next);

                    TextView cuisine = requireView().findViewById(R.id.cuisine_name);
                    TextView percentage = requireView().findViewById(R.id.percentage);
                    // Use a local variable inside the lambda
                    String cuisineName;
                    if ("Brazo de Mercedez".equalsIgnoreCase(cuis)) {
                        cuisineName = "Brazo de Mercedes";
                    } else {
                        cuisineName = cuis;
                    }

                    // Set the text with the modified value
                    cuisine.setText(cuisineName);
                    percentage.setText(per);

                    TextView categoryNameTextView = requireView().findViewById(R.id.category_name);
                    TextView websiteNameTextView = requireView().findViewById(R.id.cookbook_website_name);
                    TextView ingredientsTextView = requireView().findViewById(R.id.ingredients_list);
                    TextView instructionsTextView = requireView().findViewById(R.id.instructions_list);

                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    // Fetch the recipes based on the cuisine name
                    String finalCuisineName = cuisineName;
                    db.collection("recipeCollection")
                            .whereEqualTo("cuisine_name", finalCuisineName)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                loadingSpinner.setVisibility(View.GONE);
                                scrollView.setVisibility(View.VISIBLE);

                                if (queryDocumentSnapshots.isEmpty()) {
                                    // Handle case where no documents are found
                                    cuisine.setText("No recipe found");
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
                                displayRecipe(availableRecipes.get(currentRecipeIndex), finalCuisineName, categoryNameTextView, websiteNameTextView, ingredientsTextView, instructionsTextView);
                            })
                            .addOnFailureListener(e -> {
                                // Handle the error
                                cuisine.setText("Error fetching recipe");
                            });

                    nextButton.setOnClickListener(v -> {
                        try {
                            // Ensure that availableRecipes is not null or empty before proceeding
                            if (availableRecipes != null && !availableRecipes.isEmpty()) {
                                // Move to the next recipe
                                currentRecipeIndex++;

                                // Ensure index is within bounds
                                if (currentRecipeIndex >= availableRecipes.size()) {
                                    currentRecipeIndex = 0; // Loop back to the first recipe
                                }

                                // Display the next recipe
                                displayRecipe(availableRecipes.get(currentRecipeIndex), finalCuisineName, categoryNameTextView, websiteNameTextView, ingredientsTextView, instructionsTextView);

                                // Scroll to the top of the ScrollView
                                scrollView.scrollTo(0, 0);
                            } else {
                                Log.e("Recipe Error", "No recipes available to display");
                            }
                        } catch (Exception e) {
                            Log.e("ButtonClickError", "Error occurred when clicking Next button", e);
                            e.printStackTrace(); // Logs the full error stack trace
                        }
                    });

//                    dialog.dismiss();
                });

            } catch (IOException e) {
//                e.printStackTrace();
//                Log.e("Network Error", Objects.requireNonNull(e.getMessage()));
                Log.e("Network Error", "An IO exception occurred", e);
            }
        }).start();
    }


    @Override
    public void onStart() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        requireContext().registerReceiver(nc, filter);
        super.onStart();
    }

    @Override
    public void onStop() {
        requireContext().unregisterReceiver(nc);
        super.onStop();
    }

    public static Uri decodeUri(Context c, Uri uri, final int requiredSize)
            throws IOException {
        // First, decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o);

        int width_tmp = o.outWidth;
        int height_tmp = o.outHeight;
        int scale = 1;

        // Calculate the appropriate scale
        while (width_tmp / 2 >= requiredSize && height_tmp / 2 >= requiredSize) {
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode the bitmap with the calculated scale
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        Bitmap bitmap = BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o2);

        // Rotate the bitmap -90 degrees
        Bitmap rotatedBitmap;
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        assert bitmap != null;
        rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        // Save the rotated bitmap to a temporary file and return its URI
        File tempFile = new File(c.getCacheDir(), "temp_image.png"); // or any other file type
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            rotatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // Save the bitmap
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            // Recycle the original and rotated bitmaps to free memory
            bitmap.recycle();
            rotatedBitmap.recycle();
        }

        return Uri.fromFile(tempFile); // Return the URI of the saved file
    }

    private void displayRecipe(QueryDocumentSnapshot document, String cuisine, TextView categoryNameTextView, TextView websiteNameTextView, TextView ingredientsTextView, TextView instructionsTextView) {
        String categoryName = document.getString("category");
        String websiteName = document.getString("source_name");
        List<String> ingredients = (List<String>) document.get("ingredients");
        List<String> instructions = (List<String>) document.get("instructions");

        categoryNameTextView.setText(categoryName);
        websiteNameTextView.setText(websiteName);

        StringBuilder ingredientsFormatted = new StringBuilder();
        boolean isUnderHeader = false; // Track if we are under an all-caps header

        assert ingredients != null;
        if ("Lechon de Cebu".equalsIgnoreCase(cuisine)) {
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
    }
}

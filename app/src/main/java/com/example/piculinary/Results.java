//package com.example.piculinary;
//
//import android.os.Bundle;
//
//import androidx.fragment.app.Fragment;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import java.util.Arrays;
//import java.util.List;
//
//public class Results extends Fragment {
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        View view = inflater.inflate(R.layout.fragment_results, container, false);
//
//        // Retrieve the JustifiedTextView instances
//        TextView cuisineNameTextView = view.findViewById(R.id.cuisine_name);
//        TextView percentageTextView = view.findViewById(R.id.percentage);
//        TextView categoryNameTextView = view.findViewById(R.id.category_name);
//        TextView websiteNameTextView = view.findViewById(R.id.cookbook_website_name);
//
//        String CuisineName = "Lechon de Cebu";
//        String Percentage = "90%";
//        String CategoryName = "Main Dish";
//        String WebsiteName = "yummy.ph";
//
//        cuisineNameTextView.setText(CuisineName);
//        percentageTextView.setText(Percentage);
//        categoryNameTextView.setText(CategoryName);
//        websiteNameTextView.setText(WebsiteName);
//
//        // Retrieve the ImageView instance
//        ImageView imageView = view.findViewById(R.id.cuisine_picture);
//
//        int imageResource = R.drawable.image_template;
//        imageView.setImageResource(imageResource);
//
//        List<String> ingredients = Arrays.asList("Ingredient 1", "Ingredient 2", "Ingredient 3");
//        TextView ingredientsTextView = view.findViewById(R.id.ingredients_list);
//
//        // Prepare the formatted ingredients list with bullet points
//        StringBuilder ingredientsFormatted = new StringBuilder();
//        for (String ingredient : ingredients) {
//            ingredientsFormatted.append("• ").append(ingredient).append("\n");
//        }
//
//        // Set the formatted text to the TextView
//        ingredientsTextView.setText(ingredientsFormatted.toString().trim());
//
//
//        List<String> instructions = Arrays.asList("Step 1", "Step 2", "Step 3");
//        TextView instructionsTextView = view.findViewById(R.id.instructions_list);
//
//        // Prepare the formatted ingredients list with numbers
//        StringBuilder instructionsFormatted = new StringBuilder();
//        for (int i = 0; i < ingredients.size(); i++) {
//            instructionsFormatted.append(i + 1).append(". ").append(instructions.get(i)).append("\n");
//        }
//
//        // Set the formatted text to the TextView
//        instructionsTextView.setText(instructionsFormatted.toString().trim());
//
//        return view;
//    }
//}

package com.example.piculinary;

import android.app.ProgressDialog;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.piculinary.Utils.NetworkChangeListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Results extends Fragment {

    private final NetworkChangeListener nc = new NetworkChangeListener();
    private final OkHttpClient client = new OkHttpClient();
    private ProgressDialog dialog;

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

    @SuppressWarnings("deprecation")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Retrieve the image data
        Uri imageUri = Uri.parse(requireArguments().getString("image_data"));

        ImageView imageView = view.findViewById(R.id.cuisine_picture);
        imageView.setImageURI(imageUri);

        dialog = ProgressDialog.show(getActivity(), "",
                "Loading. Please wait...", true);
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

//    private void getImageData(String event_id) {
//
//        // Build the URL with the query parameter
//        String url = "https://rieze-cebuanocuisine.hf.space/call/predict/" + event_id;
//
//        // Build the request
//        Request request = new Request.Builder()
//                .url(url)
//                .get()
//                .build();
//
//        // Execute the request in a background thread
//        new Thread(() -> {
//            try (Response response = client.newCall(request).execute()) {
//                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
//
//                // Get the response body
//                String responseBody = response.body().string();
//                String trimmed = responseBody.substring(23, responseBody.length() - 3);
//                String[] data = trimmed.split(",", 2);
//                String cuis = data[0].trim().replace("\"", "");
//                String per = data[1].trim().replace("\"", "") + "%";
//                TextView cuisine = requireView().findViewById(R.id.cuisine_name);
//                TextView percentage = requireView().findViewById(R.id.percentage);
//                cuisine.setText(cuis);
//                percentage.setText(per);
//                dialog.dismiss();
//
//            } catch (IOException e) {
//                e.printStackTrace();
//                Log.e("Network Error", e.getMessage());
//            }
//        }).start();
//    }

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
                    TextView cuisine = requireView().findViewById(R.id.cuisine_name);
                    TextView percentage = requireView().findViewById(R.id.percentage);
                    cuisine.setText(cuis);
                    percentage.setText(per);
                    dialog.dismiss();
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
}

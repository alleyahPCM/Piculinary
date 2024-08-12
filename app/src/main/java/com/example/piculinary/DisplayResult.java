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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.piculinary.Utils.NetworkChangeListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class DisplayResult extends AppCompatActivity {

    private NetworkChangeListener nc = new NetworkChangeListener();
    private OkHttpClient client = new OkHttpClient();
    private ProgressDialog dialog;

    public static Uri decodeUri(Context c, Uri uri, final int requiredSize)
            throws FileNotFoundException, IOException {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_result);
        // Retrieve the image data
        Uri uri = Uri.parse(getIntent().getStringExtra("image_data"));

        ImageView imageView = findViewById(R.id.cuisine_picture);
        imageView.setImageURI(uri);

        dialog = ProgressDialog.show(this, "",
                "Loading. Please wait...", true);
        uploadImageToFirebase(uri);
//        sendImageUri("https://firebasestorage.googleapis.com/v0/b/thesis-2261c.appspot.com/o/images%2F1723442948056.jpg?alt=media&token=d65c3452-284a-4a3b-b4cf-f5941b9799ad");

    }

    private void uploadImageToFirebase(Uri imageUri) {
        Uri compressed;
        if (imageUri != null) {
            try {
                compressed = decodeUri(this, imageUri, 750);
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
                    .addOnFailureListener(e -> {
                        Log.e("Upload Error", Objects.requireNonNull(e.getMessage()));
                    });
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
                e.printStackTrace();
                Log.e("Network Error", e.getMessage());
            }
        }).start();
    }

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
                String responseBody = response.body().string();
                String trimmed = responseBody.substring(23, responseBody.length() - 3);
                String[] data = trimmed.split(",", 2);
                String cuis = data[0].trim().replace("\"", "");
                String per = data[1].trim().replace("\"", "") + "%";
                TextView cuisine = findViewById(R.id.cuisine_name);
                TextView percentage = findViewById(R.id.percentage);
                cuisine.setText(cuis);
                percentage.setText(per);
                dialog.dismiss();

            } catch (IOException e) {
                e.printStackTrace();
                Log.e("Network Error", e.getMessage());
            }
        }).start();
    }

    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(nc, filter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(nc);
        super.onStop();
    }
}
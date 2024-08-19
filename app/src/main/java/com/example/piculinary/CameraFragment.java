package com.example.piculinary;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;

public class CameraFragment extends Fragment {

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private String currentPermission;
    private Uri imageUri;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if (result) {
                        if (currentPermission.equals(Manifest.permission.CAMERA)) {
                            Intent openCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            imageUri = createImageUri(requireActivity());
                            openCamera.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                            cameraLauncher.launch(openCamera);
                        } else if (currentPermission.equals(Manifest.permission.READ_EXTERNAL_STORAGE) || currentPermission.equals(Manifest.permission.READ_MEDIA_IMAGES)) {
                            Intent openGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            galleryLauncher.launch(openGallery);
                        }
                    } else {
                        String permissionMessage = currentPermission.equals(Manifest.permission.CAMERA) ?
                                "Camera permission is required to take photos." :
                                "Gallery permission is required to access photos.";
                        Toast.makeText(getActivity(), permissionMessage, Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private Uri createImageUri(Context context) {
        File imageFolder = new File(context.getExternalFilesDir(null), "images");
        if (!imageFolder.exists()) {
            imageFolder.mkdirs();
        }
        File file = new File(imageFolder, "captured_image.jpg");
        return FileProvider.getUriForFile(context, "com.example.piculinary.provider", file);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize the camera launcher
//        cameraLauncher = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    if (result.getResultCode() == Activity.RESULT_OK) {
//                        Intent intent = new Intent(getActivity(), DisplayResult.class);
//                        intent.putExtra("image_data", imageUri.toString());
//                        startActivity(intent);
//                    }
//                });
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Results displayResultFragment = new Results();
                        Bundle bundle = new Bundle();
                        bundle.putString("image_data", imageUri.toString());
                        displayResultFragment.setArguments(bundle);

//                        requireActivity().getSupportFragmentManager().beginTransaction()
//                                .replace(R.id.frameLayout, displayResultFragment)
//                                .addToBackStack(null)
//                                .commit();
                        ((MainActivity) requireActivity()).navigateToFragment(displayResultFragment);

                    }
                }
        );

        // Initialize the gallery launcher
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri selectedImage = data.getData();
//                            Intent intent = new Intent(getActivity(), DisplayResult.class);
//                            intent.putExtra("image_data", selectedImage.toString());
//                            startActivity(intent);
                            Results displayResultFragment = new Results();
                            Bundle bundle = new Bundle();
                            assert selectedImage != null;
                            bundle.putString("image_data", selectedImage.toString());
                            displayResultFragment.setArguments(bundle);

//                            requireActivity().getSupportFragmentManager().beginTransaction()
//                                    .replace(R.id.frameLayout, displayResultFragment)
//                                    .addToBackStack(null)
//                                    .commit();
                            ((MainActivity) requireActivity()).navigateToFragment(displayResultFragment);
                        }
                    }
                });

        // Set up a click listener on the entire view
        view.setOnClickListener(v -> showImageSourceDialog());
    }

    // Method to show the dialog for selecting image source
    private void showImageSourceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Image Source")
                .setItems(new CharSequence[]{"Camera", "Gallery"}, (dialog, which) -> {
                    switch (which) {
                        case 0: // Camera
                            if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                currentPermission = Manifest.permission.CAMERA;
                                requestPermissionLauncher.launch(currentPermission);
                            } else {
                                imageUri = createImageUri(requireActivity());
                                Intent openCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                openCamera.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                                cameraLauncher.launch(openCamera);
                            }
                            break;
                        case 1: // Gallery
//                            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                                currentPermission = Manifest.permission.READ_EXTERNAL_STORAGE;
//                                requestPermissionLauncher.launch(currentPermission);
//                            } else {
//                                Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                                galleryLauncher.launch(pickPhoto);
//                            }
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            galleryLauncher.launch(pickPhoto);
                            break;
                    }
                });
        builder.create().show();
    }
}

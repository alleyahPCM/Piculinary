package com.example.piculinary;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.piculinary.Utils.NetworkChangeListener;

public class StartingScreen extends AppCompatActivity {

    private NetworkChangeListener nc = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starting_screen);

        // Find the root view of the layout
        View rootView = findViewById(android.R.id.content);

        // Find ImageView and apply fade-in animation
        ImageView imageView = findViewById(R.id.title);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        imageView.startAnimation(fadeIn);

        TextView textView = findViewById(R.id.start);
        Animation fadeInOut = AnimationUtils.loadAnimation(this, R.anim.fade_in_out);
        textView.startAnimation(fadeInOut);

        // Set up the click listener on the entire root view
        rootView.setOnClickListener(v -> {
            Intent intent = new Intent(StartingScreen.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close StartingScreen so user can't return to it
        });
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
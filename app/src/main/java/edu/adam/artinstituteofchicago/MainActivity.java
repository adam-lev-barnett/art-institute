package edu.adam.artinstituteofchicago;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.JsonSyntaxException;

import edu.adam.artinstituteofchicago.artworkcomponents.UserFavorites;
import edu.adam.artinstituteofchicago.databinding.ActivityMainBinding;
import edu.adam.artinstituteofchicago.uicomponents.BottomNavigation;
import edu.adam.artinstituteofchicago.uicomponents.ErrorDialog;


public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Utility class to set up the bottom nav bar based on the calling activity
        BottomNavigation.setUp(this, binding.bottomNav);
        setSupportActionBar(binding.topBanner.getRoot());

        // Only attempt to load favorites once per app startup
        if (!UserFavorites.loadAttempted()) {
            try {
                UserFavorites.loadFaves(this);
            } catch(JsonSyntaxException e) {
                ErrorDialog.show(this, getString(R.string.favorites_error), getString(R.string.favorites_load_failed));
            }
        }
    }

    public void openGalleries(View v) {
        startActivity(new Intent(this, GalleryActivity.class));
    }

    public void openSearch(View v) {
        startActivity(new Intent(this, SearchActivity.class));
    }

    public void openRandom(View v) {
        startActivity(new Intent(this, ArtworkDetailActivity.class));
    }
}
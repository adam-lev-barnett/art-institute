package edu.adam.artinstituteofchicago;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import edu.adam.artinstituteofchicago.databinding.ActivityArtworkDetailBinding;

public class ArtworkDetailActivity extends AppCompatActivity {

    public ActivityArtworkDetailBinding artworkDetailBinding;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        artworkDetailBinding = ActivityArtworkDetailBinding.inflate(getLayoutInflater());
        setContentView(artworkDetailBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(artworkDetailBinding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
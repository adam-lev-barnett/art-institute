package edu.adam.artinstituteofchicago;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import edu.adam.artinstituteofchicago.databinding.ActivityGalleryBinding;
import edu.adam.artinstituteofchicago.gallerycomponents.GalleryAdapter;

/*
    RecyclerView from API call has GALLERY objects, not artwork
    - Name, floor, image
    -- Floor is blank if no floor returned
    -- Image is FIRST returned artwork

    Tapping gallery opens Artworks

    Nav on bottom:
    - home, browse galleries, search artwork,
    - view faves, view random artwork
    -- Galleries icon should be red because currently on it

*/
public class GalleryActivity extends AppCompatActivity implements View.OnClickListener {
    public ActivityGalleryBinding binding;
    private LinearLayoutManager linearLayoutManager;
    private GalleryAdapter adapter;
    private final List<Gallery> galleries = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityGalleryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavBar.setUp(this, binding.bottomNav);
        setSupportActionBar(binding.topBanner.getRoot());
    }

    private void setUpGalleryRecycler() {
        this.adapter = new GalleryAdapter(galleries, this);
        this.linearLayoutManager = new LinearLayoutManager(this);
        binding.galleryRecycler.setAdapter(adapter);
        binding.galleryRecycler.setLayoutManager(linearLayoutManager);
    }

    @Override
    public void onClick(View v) {

    }


}
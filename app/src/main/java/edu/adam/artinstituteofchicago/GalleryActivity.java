package edu.adam.artinstituteofchicago;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import edu.adam.artinstituteofchicago.databinding.ActivityGalleryBinding;
import edu.adam.artinstituteofchicago.databinding.BottomNavigationBinding;
import edu.adam.artinstituteofchicago.gallerycomponents.Gallery;
import edu.adam.artinstituteofchicago.gallerycomponents.GalleryAdapter;
import edu.adam.artinstituteofchicago.gallerycomponents.GalleryDownloader;
import edu.adam.artinstituteofchicago.uicomponents.BottomNavigation;

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
    public ActivityGalleryBinding galleryBinding;
    private LinearLayoutManager linearLayoutManager;
    private GalleryAdapter adapter;
    private final List<Gallery> galleries = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        galleryBinding = ActivityGalleryBinding.inflate(getLayoutInflater());
        setContentView(galleryBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(galleryBinding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigation.setUp(this, galleryBinding.bottomNav);
        setSupportActionBar(galleryBinding.topBanner.getRoot());
        setUpGalleryRecycler();
        GalleryDownloader.getSourceData(this);
    }

    private void setUpGalleryRecycler() {
        this.adapter = new GalleryAdapter(galleries, this);
        this.linearLayoutManager = new LinearLayoutManager(this);
        galleryBinding.galleryRecycler.setAdapter(adapter);
        galleryBinding.galleryRecycler.setLayoutManager(linearLayoutManager);
    }

    @Override
    public void onClick(View v) {

    }


    public void downloadFailed(String errMessage) {
        String message = (
                errMessage == null ?
                        getString(R.string.download_failed) :
                        errMessage);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void updateData(List<Gallery> galleryList) {
        galleries.clear();
        galleries.addAll(galleryList);
        adapter.notifyDataSetChanged();
    }
}
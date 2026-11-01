package edu.adam.artinstituteofchicago;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
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
import edu.adam.artinstituteofchicago.uicomponents.ErrorDialog;

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
        Intent intent = new Intent(this, ArtworkListActivity.class);
        if (galleries != null && !galleries.isEmpty()) {
            Gallery gallery = galleries.get(galleryBinding.galleryRecycler.getChildLayoutPosition(v));
            intent.putExtra(getString(R.string.gallery_extra), gallery);
        }
        startActivity(intent);
    }

    /** Null title or message means the downloader had no details, so use the defaults */
    public void downloadFailed(String title, String errMessage) {
        ErrorDialog.show(this,
                title == null ? getString(R.string.download_error) : title,
                errMessage == null ? getString(R.string.download_failed) : errMessage);
    }

    public void updateData(List<Gallery> galleryList) {
        int sizeBeforeAdd = galleries.size();
        galleries.addAll(galleryList);
        adapter.notifyItemRangeInserted(sizeBeforeAdd, galleryList.size());
    }

    public void updateGalleryThumbnail(int position) {
        adapter.notifyItemChanged(position);
    }
}
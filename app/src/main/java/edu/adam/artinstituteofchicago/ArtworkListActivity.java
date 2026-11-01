package edu.adam.artinstituteofchicago;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import edu.adam.artinstituteofchicago.artworkcomponents.Artwork;
import edu.adam.artinstituteofchicago.artworkcomponents.ArtworkAdapter;
import edu.adam.artinstituteofchicago.artworkcomponents.ArtworkDownloader;
import edu.adam.artinstituteofchicago.databinding.ActivityArtworkListBinding;
import edu.adam.artinstituteofchicago.gallerycomponents.Gallery;
import edu.adam.artinstituteofchicago.gallerycomponents.GalleryDownloader;
import edu.adam.artinstituteofchicago.uicomponents.BottomNavigation;
import edu.adam.artinstituteofchicago.uicomponents.ErrorDialog;

public class ArtworkListActivity extends AppCompatActivity implements View.OnClickListener {
    // Matches the spec's mockup: "Jan 20, 2022 12:15 AM"
    private static final String LAST_UPDATE_PATTERN = "MMM d, yyyy h:mm a";
    public ActivityArtworkListBinding binding;
    private LinearLayoutManager linearLayoutManager;
    private ArtworkAdapter adapter;
    private final List<Artwork> artworks = new ArrayList<>();
    private Gallery gallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityArtworkListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigation.setUp(this, binding.bottomNav);
        setSupportActionBar(binding.topBanner.getRoot());

        if (getIntent().hasExtra(getString(R.string.gallery_extra))) {
            gallery = getIntent().getSerializableExtra(getString(R.string.gallery_extra), Gallery.class);
        }

        if (gallery != null) ArtworkDownloader.getSourceData(this, gallery.getId());

        setUpArtworkRecycler();
        showGalleryInfo();
    }

    private void setUpArtworkRecycler() {
        this.adapter = new ArtworkAdapter(artworks, this, null);
        this.linearLayoutManager = new LinearLayoutManager(this);
        binding.artworkRecycler.setAdapter(adapter);
        binding.artworkRecycler.setLayoutManager(linearLayoutManager);
    }

    // Only the fields already on the Gallery; the count is set in updateData once artworks arrive
    private void showGalleryInfo() {
        if (gallery == null) {
            return;
        }

        binding.artworksGalleryTitle.setText(gallery.getTitle());

        // Floor dot disappears with floor if unavailable
        if (gallery.getFloor().isEmpty()) {
            binding.floorDot.setVisibility(View.GONE);
            binding.artworksFloor.setVisibility(View.GONE);
        } else {
            binding.artworksFloor.setText(getString(R.string.floor_label, gallery.getFloor()));
        }

        // No count until artworks arrive
        binding.artworkCountDot.setVisibility(View.INVISIBLE);
        binding.artworksCount.setVisibility(View.INVISIBLE);

        if (gallery.getSource_updated_at().isEmpty()) {
            binding.lastUpdate.setVisibility(View.GONE);
        } else {
            binding.lastUpdate.setText(
                    getString(R.string.last_update, formatLastUpdate(gallery.getSource_updated_at())));
        }
    }

    /** source_updated_at is ISO 8601 ("2022-04-15T16:40:14-05:00"); OffsetDateTime parses that format directly.
     *  Keeps the API's own offset, so the time shown is Chicago time like the mockup */
    private String formatLastUpdate(String sourceUpdatedAt) {
        try {
            OffsetDateTime updated = OffsetDateTime.parse(sourceUpdatedAt);
            return updated.format(DateTimeFormatter.ofPattern(LAST_UPDATE_PATTERN, Locale.getDefault()));
        } catch (DateTimeParseException e) {
            // Unexpected format: the raw value is still better than nothing
            return sourceUpdatedAt;
        }
    }

    @Override
    public void onClick(View v) {
        int pos = binding.artworkRecycler.getChildLayoutPosition(v);
        Artwork artwork = artworks.get(pos);
        if (artwork != null) {
            Intent intent = new Intent(this, ArtworkDetailActivity.class);
            intent.putExtra(getString(R.string.artwork), artwork);
            startActivity(intent);
        }
        else {
            ErrorDialog.show(this, getString(R.string.artwork_error), getString(R.string.artwork_does_not_exist));
        }

    }

    public void updateData(List<Artwork> artList) {
        int sizeBeforeAdd = artworks.size();
        artworks.addAll(artList);
        adapter.notifyItemRangeInserted(sizeBeforeAdd, artList.size());

        // Count grows as pages load for full gallery size
        // Plurals resource picks "artwork" or "artworks" based on the count
        binding.artworksCount.setText(getResources().getQuantityString(
                R.plurals.artwork_count, artworks.size(), artworks.size()));
        binding.artworkCountDot.setVisibility(View.VISIBLE);
        binding.artworksCount.setVisibility(View.VISIBLE);
    }

    public void updateGalleryThumbnail(int position) {
        adapter.notifyItemChanged(position);
    }

    /** Null title or message means the downloader had no details, so use the defaults */
    public void downloadFailed(String title, String errorMsg) {
        ErrorDialog.show(this,
                title == null ? getString(R.string.download_error) : title,
                errorMsg == null ? getString(R.string.artworks_download_failed) : errorMsg);
    }
}

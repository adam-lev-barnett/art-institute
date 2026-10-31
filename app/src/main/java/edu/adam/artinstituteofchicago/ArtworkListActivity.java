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

import edu.adam.artinstituteofchicago.artworkcomponents.Artwork;
import edu.adam.artinstituteofchicago.artworkcomponents.ArtworkAdapter;
import edu.adam.artinstituteofchicago.artworkcomponents.ArtworkDownloader;
import edu.adam.artinstituteofchicago.databinding.ActivityArtworkListBinding;
import edu.adam.artinstituteofchicago.gallerycomponents.Gallery;
import edu.adam.artinstituteofchicago.gallerycomponents.GalleryDownloader;
import edu.adam.artinstituteofchicago.uicomponents.BottomNavigation;

/*
    Opened by tapping a gallery in GalleryActivity (Gallery passed in with putExtra)
    - Heading shows the gallery, the floor, and the number of artworks
    - RecyclerView of ARTWORK objects: title, artist, date (date is just a string)
    - "Last update" below the list comes from the gallery's source_updated_at

    Tapping an artwork opens ArtworkDetailActivity

    Nav on bottom:
    - all icons grey on this screen
*/
public class ArtworkListActivity extends AppCompatActivity implements View.OnClickListener {
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
            gallery = (Gallery) getIntent()
                    .getSerializableExtra(getString(R.string.gallery_extra));
        }

        if (gallery != null) ArtworkDownloader.getSourceData(this, gallery.getId());

        setUpArtworkRecycler();
        showGalleryInfo();
    }

    private void setUpArtworkRecycler() {
        this.adapter = new ArtworkAdapter(artworks, this);
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

        // Floor and its dot hide together; the count's dot then sits right after the title
        if (gallery.getFloor().isEmpty()) {
            binding.floorDot.setVisibility(View.GONE);
            binding.artworksFloor.setVisibility(View.GONE);
        } else {
            binding.artworksFloor.setText(getString(R.string.floor_label, gallery.getFloor()));
        }

        // No count until artworks arrive. INVISIBLE, not GONE: the RecyclerView is
        // constrained below the count, so the count has to keep its space
        binding.artworkCountDot.setVisibility(View.INVISIBLE);
        binding.artworksCount.setVisibility(View.INVISIBLE);

        if (gallery.getSource_updated_at().isEmpty()) {
            binding.lastUpdate.setVisibility(View.GONE);
        } else {
            binding.lastUpdate.setText(getString(R.string.last_update, gallery.getSource_updated_at()));
        }
    }

    @Override
    public void onClick(View v) {
        int pos = binding.artworkRecycler.getChildLayoutPosition(v);
        Artwork artwork = artworks.get(pos);
        //TODO Open ArtworkDetailActivity with this artwork (next step)
    }

    public void updateData(List<Artwork> artList) {
        int sizeBeforeAdd = artworks.size();
        artworks.addAll(artList);
        adapter.notifyItemRangeInserted(sizeBeforeAdd, artList.size());

        // Runs once per page, so the count grows as pages load
        binding.artworksCount.setText(getString(R.string.artwork_count, artworks.size()));
        binding.artworkCountDot.setVisibility(View.VISIBLE);
        binding.artworksCount.setVisibility(View.VISIBLE);
    }

    public void updateGalleryThumbnail(int position) {
        adapter.notifyItemChanged(position);
    }

    public void downloadFailed(String errorMsg) {
    }
}

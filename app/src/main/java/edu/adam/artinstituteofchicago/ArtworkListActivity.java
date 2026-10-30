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
import edu.adam.artinstituteofchicago.databinding.ActivityArtworkListBinding;
import edu.adam.artinstituteofchicago.gallerycomponents.Gallery;
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
    public static final String EXTRA_GALLERY = "GALLERY";

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

        if (getIntent().hasExtra(EXTRA_GALLERY)) {
            gallery = (Gallery) getIntent().getSerializableExtra(EXTRA_GALLERY);
        }

        setUpArtworkRecycler();
        showGalleryInfo();
    }

    private void setUpArtworkRecycler() {
        this.adapter = new ArtworkAdapter(artworks, this);
        this.linearLayoutManager = new LinearLayoutManager(this);
        binding.artworkRecycler.setAdapter(adapter);
        binding.artworkRecycler.setLayoutManager(linearLayoutManager);
    }

    // Call again after the artworks are loaded so the count is up to date
    private void showGalleryInfo() {
        if (gallery == null) {
            return;
        }

        if (gallery.getFloor().isEmpty()) {
            binding.artworksSummary.setText(getString(R.string.artwork_list_summary_no_floor,
                    gallery.getTitle(), artworks.size()));
        } else {
            binding.artworksSummary.setText(getString(R.string.artwork_list_summary,
                    gallery.getTitle(), gallery.getFloor(), artworks.size()));
        }

        binding.lastUpdate.setText(getString(R.string.last_update, gallery.getSource_updated_at()));
    }

    @Override
    public void onClick(View v) {
        int pos = binding.artworkRecycler.getChildLayoutPosition(v);
        Artwork artwork = artworks.get(pos);
        //TODO Open ArtworkDetailActivity with this artwork (next step)
    }
}

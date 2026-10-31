package edu.adam.artinstituteofchicago;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.List;

import edu.adam.artinstituteofchicago.artworkcomponents.Artwork;
import edu.adam.artinstituteofchicago.artworkcomponents.ArtworkAdapter;
import edu.adam.artinstituteofchicago.artworkcomponents.UserFavorites;
import edu.adam.artinstituteofchicago.databinding.ActivityFavoritesBinding;
import edu.adam.artinstituteofchicago.uicomponents.BottomNavigation;
import edu.adam.artinstituteofchicago.uicomponents.ErrorDialog;

/*
    Opened from the favorites icon in the bottom nav
    - RecyclerView of the user's favorite artworks, sorted by title
    - Tapping an artwork opens ArtworkDetailActivity
    - Long-pressing an artwork asks to confirm, then removes it from favorites

    Nav on bottom:
    - favorites icon is maroon on this screen
*/
public class FavoritesActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener {

    private static final String TAG = "FavoritesActivity";
    public ActivityFavoritesBinding binding;
    private ArtworkAdapter adapter;
    private final List<Artwork> faves = new ArrayList<>();

    private LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityFavoritesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigation.setUp(this, binding.bottomNav);
        setSupportActionBar(binding.topBanner.getRoot());

        setUpFavesRecycler();
    }

    /** Reloads in onResume instead of onCreate, so un-favoriting on the detail screen
     *  and pressing back removes the artwork here too */
    @Override
    protected void onResume() {
        super.onResume();

        // If Android killed the app and reopened it on this screen, MainActivity never loaded the faves
        if (!UserFavorites.isLoadAttempted()) {
            try {
                UserFavorites.loadFaves(this);
            } catch (JsonSyntaxException e) {
                // Faves couldn't be read, so the list stays empty and shows the no-favorites message
                Log.d(TAG, "onResume: " + e.getMessage());
                ErrorDialog.show(this, getString(R.string.favorites_error), getString(R.string.favorites_load_failed));
            }
        }

        faves.clear();
        faves.addAll(UserFavorites.getFaves());
        // Whole list was replaced, so a specific notify doesn't apply
        adapter.notifyDataSetChanged();
        showEmptyMessageIfNoFaves();
    }

    private void setUpFavesRecycler() {
        this.adapter = new ArtworkAdapter(faves, this, this);
        this.linearLayoutManager = new LinearLayoutManager(this);
        binding.favesRecycler.setAdapter(adapter);
        binding.favesRecycler.setLayoutManager(linearLayoutManager);
    }

    private void showEmptyMessageIfNoFaves() {
        binding.favesEmpty.setVisibility(faves.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View v) {
        int pos = binding.favesRecycler.getChildLayoutPosition(v);
        // Row can be mid-animation after a removal
        if (pos == RecyclerView.NO_POSITION) return;

        Intent intent = new Intent(this, ArtworkDetailActivity.class);
        intent.putExtra(getString(R.string.artwork), faves.get(pos));
        startActivity(intent);
    }

    @Override
    public boolean onLongClick(View v) {
        int pos = binding.favesRecycler.getChildLayoutPosition(v);
        if (pos == RecyclerView.NO_POSITION) return false;

        Artwork artwork = faves.get(pos);

        new AlertDialog.Builder(this)
                .setIcon(R.drawable.fav_on)
                .setTitle(R.string.remove_favorite_title)
                .setMessage(getString(R.string.remove_favorite_message, artwork.getTitle()))
                .setPositiveButton(R.string.yes, (dialog, which) -> removeFave(artwork))
                .setNegativeButton(R.string.no, null)
                .show();

        // true = long press handled, so a regular click doesn't also fire
        return true;
    }

    private void removeFave(Artwork artwork) {
        // Look the position up again; pos from the long press could be stale by the time Yes is tapped
        int pos = faves.indexOf(artwork);
        if (pos == -1) return;

        UserFavorites.removeFave(artwork, this);
        faves.remove(pos);
        adapter.notifyItemRemoved(pos);
        showEmptyMessageIfNoFaves();
    }
}

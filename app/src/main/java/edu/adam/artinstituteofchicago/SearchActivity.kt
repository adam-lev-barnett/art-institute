package edu.adam.artinstituteofchicago;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.adam.artinstituteofchicago.artworkcomponents.Artwork;
import edu.adam.artinstituteofchicago.artworkcomponents.ArtworkAdapter;
import edu.adam.artinstituteofchicago.artworkcomponents.ArtworkDownloader;
import edu.adam.artinstituteofchicago.databinding.ActivitySearchBinding;
import edu.adam.artinstituteofchicago.uicomponents.BottomNavigation;
import edu.adam.artinstituteofchicago.uicomponents.ErrorDialog;

public class SearchActivity extends AppCompatActivity implements View.OnClickListener {

    public ActivitySearchBinding binding;
    private ArtworkAdapter adapter;
    private LinearLayoutManager linearLayoutManager;
    private final List<Artwork> results = new ArrayList<>();

    // Most recent keyword searched; results for any older keyword are ignored
    private String lastSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigation.setUp(this, binding.bottomNav);
        setSupportActionBar(binding.topBanner.getRoot());
        setUpSearchRecycler();

        // Rotation recreates the activity and empties the list, so run the same search again
        if (savedInstanceState != null) {
            lastSearch = savedInstanceState.getString(getString(R.string.search_extra));
            if (lastSearch != null) ArtworkDownloader.search(this, lastSearch);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(getString(R.string.search_extra), lastSearch);
        super.onSaveInstanceState(outState);
    }

    private void setUpSearchRecycler() {
        this.adapter = new ArtworkAdapter(results, this, null);
        this.linearLayoutManager = new LinearLayoutManager(this);
        binding.searchRecycler.setAdapter(adapter);
        binding.searchRecycler.setLayoutManager(linearLayoutManager);
    }

    /** Set with onClick for the search icon. Trimming input is handled via ArtworkDownloader  */
    public void searchArtworks(View v) {
        String keyword = binding.searchInput.getText().toString();

        if (keyword.isEmpty()) {
            binding.searchInput.setError(getString(R.string.search_required));
            binding.searchInput.requestFocus();
            return;
        }

        lastSearch = keyword;
        ArtworkDownloader.search(this, keyword);
    }

    /** Called by ArtworkDownloader with the results for one keyword */
    public void updateData(String keyword, List<Artwork> searchResults) {

        results.clear();
        results.addAll(searchResults);
        // New search replaces the whole list
        adapter.notifyDataSetChanged();
        binding.searchRecycler.scrollToPosition(0);

        if (results.isEmpty()) {
            ErrorDialog.show(this, R.drawable.ic_search,
                    getString(R.string.no_results_title),
                    getString(R.string.no_results_message, keyword));
        }
    }

    /** Null title or message means the downloader had no details, so use the defaults */
    public void downloadFailed(String title, String errorMsg) {
        ErrorDialog.show(this,
                title == null ? getString(R.string.download_error) : title,
                errorMsg == null ? getString(R.string.search_download_failed) : errorMsg);
    }

    @Override
    public void onClick(View v) {
        int pos = binding.searchRecycler.getChildLayoutPosition(v);
        if (pos == RecyclerView.NO_POSITION) return;

        Intent intent = new Intent(this, ArtworkDetailActivity.class);
        intent.putExtra(getString(R.string.artwork), results.get(pos));
        startActivity(intent);
    }
}

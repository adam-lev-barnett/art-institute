package edu.adam.artinstituteofchicago;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

import edu.adam.artinstituteofchicago.artworkcomponents.Artwork;
import edu.adam.artinstituteofchicago.artworkcomponents.ArtworkDownloader;
import edu.adam.artinstituteofchicago.artworkcomponents.UserFavorites;
import edu.adam.artinstituteofchicago.databinding.ActivityArtworkDetailBinding;
import edu.adam.artinstituteofchicago.uicomponents.BottomNavigation;
import edu.adam.artinstituteofchicago.uicomponents.ErrorDialog;

public class ArtworkDetailActivity extends AppCompatActivity {

    public ActivityArtworkDetailBinding binding;
    private Intent intent;
    private Artwork artwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityArtworkDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigation.setUp(this, binding.bottomNav);
        setSupportActionBar(binding.topBanner.getRoot());
        intent = getIntent();

        if (savedInstanceState != null && savedInstanceState.containsKey(getString(R.string.artwork))) {
            // Rotation: keep the same artwork, otherwise random mode would fetch a new one
            artwork = savedInstanceState.getSerializable(getString(R.string.artwork), Artwork.class);
            loadInfo();
        } else if (intent != null && intent.hasExtra(getString(R.string.artwork))) {
            artwork = intent.getSerializableExtra(getString(R.string.artwork), Artwork.class);
            loadInfo();
        } else {
            // No artwork passed in means a Random icon/card opened this screen.
            // Hide the empty labels until the artwork arrives
            binding.detailContent.setVisibility(View.INVISIBLE);
            ArtworkDownloader.getRandomArtwork(this);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (artwork != null) outState.putSerializable(getString(R.string.artwork), artwork);
        super.onSaveInstanceState(outState);
    }

    /** Called by ArtworkDownloader when the random artwork arrives */
    public void showRandomArtwork(Artwork randomArtwork) {
        artwork = randomArtwork;
        loadInfo();
    }

    /** Null title or message means the downloader had no details, so use the defaults */
    public void downloadFailed(String title, String errorMsg) {
        ErrorDialog.show(this,
                title == null ? getString(R.string.download_error) : title,
                errorMsg == null ? getString(R.string.random_download_failed) : errorMsg);
    }

    private void loadInfo() {
        if (artwork == null) {
            ErrorDialog.show(this, getString(R.string.artwork_error), getString(R.string.artwork_does_not_exist));
            return;
        }

        binding.detailContent.setVisibility(View.VISIBLE);

        loadImage();

        binding.detailTitle.setText(artwork.getTitle());
        showArtistAndDate();

        // Layout starts on fav_off, so only change it if it's already a favorite
        if (UserFavorites.isFavorite(artwork)) {
            binding.detailFavorite.setImageResource(R.drawable.fav_on);
        }

        showOrHide(binding.detailGalleryLabel, binding.detailGallery, artwork.getGalleryTitle());
        showOrHide(binding.detailMediumLabel, binding.detailMedium, artwork.getMediumDisplay());
        showOrHide(binding.detailDimensionsLabel, binding.detailDimensions, artwork.getDimensions());
        showOrHide(binding.detailLocationLabel, binding.detailLocation, artwork.getPlaceOfOrigin());

        // The API sends the description as HTML (<p>, <em>), so convert it instead of showing the tags
        if (artwork.getDescription().isEmpty()) {
            binding.detailDescription.setVisibility(View.GONE);
        } else {
            binding.detailDescription.setText(
                    Html.fromHtml(artwork.getDescription(), Html.FROM_HTML_MODE_COMPACT));
        }
    }
    private void loadImage() {
        if (artwork.getImageId().isEmpty()) {
            binding.detailImage.setImageResource(R.drawable.artwork_default);
            return;
        }

        // Defaults to full size, falls back to smaller size, falls back to default artwork
        Glide.with(this)
                .load(ArtworkDownloader.getImageUrl(artwork.getImageId(), ArtworkDownloader.FULL_SIZE))
                .error(Glide.with(this)
                        .load(ArtworkDownloader.getImageUrl(artwork.getImageId(), ArtworkDownloader.FALLBACK_SIZE))
                        .error(R.drawable.artwork_default))
                .into(binding.detailImage);
    }
    private void showArtistAndDate() {
        // artist_display can have extra lines (nationality, lifespan); the first line is the name
        String artist = artwork.getArtistDisplay().split("\n")[0];
        String date = artwork.getDateDisplay();

        if (artist.isEmpty() && date.isEmpty()) {
            binding.detailArtistDate.setVisibility(View.GONE);
        } else if (date.isEmpty()) {
            binding.detailArtistDate.setText(artist);
        } else if (artist.isEmpty()) {
            binding.detailArtistDate.setText(date);
        } else {
            binding.detailArtistDate.setText(getString(R.string.artist_date, artist, date));
        }
    }


    /** Missing data hides the value and its label together */
    private void showOrHide(TextView label, TextView value, String text) {
        if (text.isEmpty()) {
            label.setVisibility(View.GONE);
            value.setVisibility(View.GONE);
        } else {
            value.setText(text);
        }
    }

    private void setFavorite() {
        // Don't do anything if it's already favorited
        if (!UserFavorites.isFavorite(artwork)) {
            binding.detailFavorite.setImageResource(R.drawable.fav_on);
            UserFavorites.addFavorite(artwork, this);
        }
    }

    private void removeFavorite() {
        if (UserFavorites.isFavorite(artwork)) {
            UserFavorites.removeFave(artwork, this);
            binding.detailFavorite.setImageResource(R.drawable.fav_off);
        }
    }

    /** Set with android:onClick on detail_favorite in both layouts */
    public void handleHeartClick(View v) {
        // No artwork loaded, so there's nothing to favorite
        if (artwork == null) return;

        if (UserFavorites.isFavorite(artwork)) removeFavorite();
        else setFavorite();
    }
}
package edu.adam.artinstituteofchicago

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import edu.adam.artinstituteofchicago.artworkcomponents.Artwork
import edu.adam.artinstituteofchicago.artworkcomponents.ArtworkDownloader
import edu.adam.artinstituteofchicago.artworkcomponents.UserFavorites
import edu.adam.artinstituteofchicago.databinding.ActivityArtworkDetailBinding
import edu.adam.artinstituteofchicago.uicomponents.BottomNavigation
import edu.adam.artinstituteofchicago.uicomponents.ErrorDialog

class ArtworkDetailActivity : AppCompatActivity() {
    var binding: ActivityArtworkDetailBinding? = null
    private var intent: Intent? = null
    private var artwork: Artwork? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        binding = ActivityArtworkDetailBinding.inflate(getLayoutInflater())
        setContentView(binding!!.getRoot())
        ViewCompat.setOnApplyWindowInsetsListener(
            binding!!.getRoot(),
            OnApplyWindowInsetsListener { v: View?, insets: WindowInsetsCompat? ->
                val systemBars = insets!!.getInsets(WindowInsetsCompat.Type.systemBars())
                v!!.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            })

        BottomNavigation.setUp(this, binding!!.bottomNav)
        setSupportActionBar(binding!!.topBanner.getRoot())
        intent = getIntent()

        if (savedInstanceState != null && savedInstanceState.containsKey(getString(R.string.artwork))) {
            // keep the same artwork, otherwise random mode would fetch a new one
            artwork = savedInstanceState.getSerializable<Artwork?>(
                getString(R.string.artwork),
                Artwork::class.java
            )
            loadInfo()
        } else if (intent != null && intent!!.hasExtra(getString(R.string.artwork))) {
            artwork = intent!!.getSerializableExtra<Artwork?>(
                getString(R.string.artwork),
                Artwork::class.java
            )
            loadInfo()
        } else {
            // No artwork passed in means a Random icon/card opened this screen.
            // Hide the empty labels until the artwork arrives
            binding!!.detailContent.setVisibility(View.INVISIBLE)
            ArtworkDownloader.getRandomArtwork(this)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (artwork != null) outState.putSerializable(getString(R.string.artwork), artwork)
        super.onSaveInstanceState(outState)
    }

    /** Called by ArtworkDownloader when the random artwork arrives  */
    fun showRandomArtwork(randomArtwork: Artwork?) {
        artwork = randomArtwork
        loadInfo()
    }

    /** Null title or message means the downloader had no details, so use the defaults  */
    fun downloadFailed(title: String?, errorMsg: String?) {
        ErrorDialog.show(
            this,
            if (title == null) getString(R.string.download_error) else title,
            if (errorMsg == null) getString(R.string.random_download_failed) else errorMsg
        )
    }

    private fun loadInfo() {
        if (artwork == null) {
            ErrorDialog.show(
                this,
                getString(R.string.artwork_error),
                getString(R.string.artwork_does_not_exist)
            )
            return
        }

        binding!!.detailContent.setVisibility(View.VISIBLE)

        loadImage()

        binding!!.detailTitle.setText(artwork!!.getTitle())
        showArtistAndDate()

        if (UserFavorites.isFavorite(artwork)) {
            binding!!.detailFavorite.setImageResource(R.drawable.fav_on)
        }

        showOrHide(
            binding!!.detailGalleryLabel,
            binding!!.detailGallery,
            artwork!!.getGalleryTitle()
        )
        showOrHide(
            binding!!.detailMediumLabel,
            binding!!.detailMedium,
            artwork!!.getMediumDisplay()
        )
        showOrHide(
            binding!!.detailDimensionsLabel,
            binding!!.detailDimensions,
            artwork!!.getDimensions()
        )
        showOrHide(
            binding!!.detailLocationLabel,
            binding!!.detailLocation,
            artwork!!.getPlaceOfOrigin()
        )

        // The API sends the description as HTML (<p>, <em>), so convert it instead of showing the tags
        if (artwork!!.getDescription().isEmpty()) {
            binding!!.detailDescription.setVisibility(View.GONE)
        } else {
            binding!!.detailDescription.text = Html.fromHtml(artwork!!.getDescription(), Html.FROM_HTML_MODE_COMPACT)
        }
    }

    private fun loadImage() {
        if (artwork!!.getImageId().isEmpty()) {
            binding!!.detailImage.setImageResource(R.drawable.artwork_default)
            return
        }

        // Defaults to full size, falls back to smaller size, falls back to default artwork
        Glide.with(this)
            .load(
                ArtworkDownloader.getImageUrl(
                    artwork!!.getImageId(),
                    ArtworkDownloader.FULL_SIZE
                )
            )
            .error(
                Glide.with(this)
                    .load(
                        ArtworkDownloader.getImageUrl(
                            artwork!!.getImageId(),
                            ArtworkDownloader.FALLBACK_SIZE
                        )
                    )
                    .error(R.drawable.artwork_default)
            )
            .into(binding!!.detailImage)
    }

    private fun showArtistAndDate() {
        val artist =
            artwork!!.getArtistDisplay().split("\n".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[0]
        val date = artwork!!.getDateDisplay()

        if (artist.isEmpty() && date.isEmpty()) {
            binding!!.detailArtistDate.setVisibility(View.GONE)
        } else if (date.isEmpty()) {
            binding!!.detailArtistDate.setText(artist)
        } else if (artist.isEmpty()) {
            binding!!.detailArtistDate.setText(date)
        } else {
            binding!!.detailArtistDate.setText(getString(R.string.artist_date, artist, date))
        }
    }

    private fun showOrHide(label: TextView, value: TextView, text: String) {
        if (text.isEmpty()) {
            label.setVisibility(View.GONE)
            value.setVisibility(View.GONE)
        } else {
            value.setText(text)
        }
    }

    private fun setFavorite() {
        // Don't do anything if it's already favorited
        if (!UserFavorites.isFavorite(artwork)) {
            binding!!.detailFavorite.setImageResource(R.drawable.fav_on)
            UserFavorites.addFavorite(artwork, this)
        }
    }

    private fun removeFavorite() {
        if (UserFavorites.isFavorite(artwork)) {
            UserFavorites.removeFave(artwork, this)
            binding!!.detailFavorite.setImageResource(R.drawable.fav_off)
        }
    }

    fun handleHeartClick(v: View?) {
        if (artwork == null) return

        if (UserFavorites.isFavorite(artwork)) removeFavorite()
        else setFavorite()
    }
}
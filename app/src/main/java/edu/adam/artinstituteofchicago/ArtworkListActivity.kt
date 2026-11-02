package edu.adam.artinstituteofchicago

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import edu.adam.artinstituteofchicago.artworkcomponents.Artwork
import edu.adam.artinstituteofchicago.artworkcomponents.ArtworkAdapter
import edu.adam.artinstituteofchicago.artworkcomponents.ArtworkDownloader
import edu.adam.artinstituteofchicago.databinding.ActivityArtworkListBinding
import edu.adam.artinstituteofchicago.gallerycomponents.Gallery
import edu.adam.artinstituteofchicago.uicomponents.BottomNavigation
import edu.adam.artinstituteofchicago.uicomponents.ErrorDialog
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

class ArtworkListActivity : AppCompatActivity(), View.OnClickListener {
    var binding: ActivityArtworkListBinding? = null
    private var linearLayoutManager: LinearLayoutManager? = null
    private var adapter: ArtworkAdapter? = null
    private val artworks: MutableList<Artwork?> = ArrayList<Artwork?>()
    private var gallery: Gallery? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        binding = ActivityArtworkListBinding.inflate(layoutInflater)
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

        if (intent.hasExtra(getString(R.string.gallery_extra))) {
            gallery = intent.getSerializableExtra<Gallery?>(
                getString(R.string.gallery_extra),
                Gallery::class.java
            )
        }

        if (gallery != null) ArtworkDownloader.getSourceData(this, gallery!!.getId())

        setUpArtworkRecycler()
        showGalleryInfo()
    }

    private fun setUpArtworkRecycler() {
        this.adapter = ArtworkAdapter(artworks, this, null)
        this.linearLayoutManager = LinearLayoutManager(this)
        binding!!.artworkRecycler.setAdapter(adapter)
        binding!!.artworkRecycler.setLayoutManager(linearLayoutManager)
    }

    // Only the fields already on the Gallery; the count is set in updateData once artworks arrive
    private fun showGalleryInfo() {
        if (gallery == null) {
            return
        }

        binding!!.artworksGalleryTitle.text = gallery!!.title

        // Floor dot disappears with floor if unavailable
        if (gallery!!.floor.isEmpty()) {
            binding!!.floorDot.visibility = View.GONE
            binding!!.artworksFloor.visibility = View.GONE
        } else {
            binding!!.artworksFloor.text = getString(R.string.floor_label, gallery!!.getFloor())
        }

        // No count until artworks arrive
        binding!!.artworkCountDot.visibility = View.INVISIBLE
        binding!!.artworksCount.visibility = View.INVISIBLE

        if (gallery!!.source_updated_at.isEmpty()) {
            binding!!.lastUpdate.visibility = View.GONE
        } else {
            binding!!.lastUpdate.text = getString(R.string.last_update, formatLastUpdate(gallery!!.getSource_updated_at()))
        }
    }

    /** source_updated_at is ISO 8601 ("2022-04-15T16:40:14-05:00"); OffsetDateTime parses that format directly.
     * Keeps the API's own offset, so the time shown is Chicago time like the mockup  */
    private fun formatLastUpdate(sourceUpdatedAt: String?): String? {
        try {
            val updated = OffsetDateTime.parse(sourceUpdatedAt)
            return updated.format(
                DateTimeFormatter.ofPattern(
                    LAST_UPDATE_PATTERN,
                    Locale.getDefault()
                )
            )
        } catch (e: DateTimeParseException) {
            // Unexpected format: the raw value is still better than nothing
            return sourceUpdatedAt
        }
    }

    override fun onClick(v: View) {
        val pos = binding!!.artworkRecycler.getChildLayoutPosition(v)
        val artwork = artworks.get(pos)
        if (artwork != null) {
            val intent = Intent(this, ArtworkDetailActivity::class.java)
            intent.putExtra(getString(R.string.artwork), artwork)
            startActivity(intent)
        } else {
            ErrorDialog.show(
                this,
                getString(R.string.artwork_error),
                getString(R.string.artwork_does_not_exist)
            )
        }
    }

    fun updateData(artList: MutableList<Artwork?>) {
        val sizeBeforeAdd = artworks.size
        artworks.addAll(artList)
        adapter!!.notifyItemRangeInserted(sizeBeforeAdd, artList.size)

        // Count grows as pages load for full gallery size
        // Plurals resource picks "artwork" or "artworks" based on the count
        binding!!.artworksCount.text = getResources().getQuantityString(
            R.plurals.artwork_count, artworks.size, artworks.size
        )
        binding!!.artworkCountDot.visibility = View.VISIBLE
        binding!!.artworksCount.visibility = View.VISIBLE
    }

    fun updateGalleryThumbnail(position: Int) {
        adapter!!.notifyItemChanged(position)
    }

    /** Null title or message means the downloader had no details, so use the defaults  */
    fun downloadFailed(title: String?, errorMsg: String?) {
        ErrorDialog.show(
            this,
            title ?: getString(R.string.download_error),
            errorMsg ?: getString(R.string.artworks_download_failed)
        )
    }

    companion object {
        // Matches the spec's mockup: "Jan 20, 2022 12:15 AM"
        private const val LAST_UPDATE_PATTERN = "MMM d, yyyy h:mm a"
    }
}

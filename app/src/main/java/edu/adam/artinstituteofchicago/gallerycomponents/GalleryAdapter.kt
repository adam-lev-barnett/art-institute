package edu.adam.artinstituteofchicago.gallerycomponents

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import edu.adam.artinstituteofchicago.GalleryActivity
import edu.adam.artinstituteofchicago.R
import edu.adam.artinstituteofchicago.artworkcomponents.ArtworkDownloader
import edu.adam.artinstituteofchicago.databinding.GalleryListRowBinding
import edu.adam.artinstituteofchicago.gallerycomponents.GalleryDownloader.getGalleryThumbnail

class GalleryAdapter(galleries: MutableList<Gallery?>?, private val galleryAct: GalleryActivity) :
    RecyclerView.Adapter<GalleryViewHolder?>() {
    private var galleries: MutableList<Gallery> = ArrayList<Gallery>();

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val rowBinding =
            GalleryListRowBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
            )
        rowBinding.getRoot().setOnClickListener(galleryAct)

        return GalleryViewHolder(rowBinding)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        val gallery = galleries[position]
        holder.binding.galleryId.text = gallery.title

        if (gallery.floor.isEmpty()) {
            holder.binding.galleryFloor.text = ""
        } else {
            holder.binding.galleryFloor.text = galleryAct.getString(
                R.string.floor_label,
                gallery.floor
            )
        }

        if (gallery.imageId.isEmpty()) {
            // Cancel any load still running from this row's previous gallery
            Glide.with(galleryAct).clear(holder.binding.galleryThumbnail)
            holder.binding.galleryThumbnail.setImageResource(R.drawable.artwork_default)
        } else {
            Glide.with(galleryAct)
                .load(
                    ArtworkDownloader.getImageUrl(
                        gallery.imageId,
                        ArtworkDownloader.THUMBNAIL_SIZE
                    )
                )
                .into(holder.binding.galleryThumbnail)
        }

        // Only request a thumbnail once its row is on screen, so the requests stay under the API rate limit
        if (!gallery.isThumbnailFetched) {
            gallery.isThumbnailFetched = true
            getGalleryThumbnail(gallery, position)
        }
    }

    override fun getItemCount(): Int {
        return galleries.size
    }
}

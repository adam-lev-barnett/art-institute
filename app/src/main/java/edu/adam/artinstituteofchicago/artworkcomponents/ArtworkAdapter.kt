package edu.adam.artinstituteofchicago.artworkcomponents

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import edu.adam.artinstituteofchicago.R
import edu.adam.artinstituteofchicago.artworkcomponents.ArtworkDownloader.getImageUrl
import edu.adam.artinstituteofchicago.databinding.ArtworkListRowBinding
import edu.adam.artinstituteofchicago.gallerycomponents.Gallery

/** Used by several different activities whose adapter instance only differs in their on-clicks/long-clicks */
class ArtworkAdapter(
    artworks: MutableList<Artwork?>?,
    private val clickListener: View.OnClickListener?,
    private val longClickListener: OnLongClickListener?,
) : RecyclerView.Adapter<ArtworkViewHolder>() {

    // The Elvis operator directly assigns 'artworks' if not null, or falls back to a new ArrayList
    private val artworks: MutableList<Artwork?> = artworks ?: ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtworkViewHolder {
        val rowBinding =
            ArtworkListRowBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        rowBinding.getRoot().setOnClickListener(clickListener)
        if (longClickListener != null) rowBinding.getRoot()
            .setOnLongClickListener(longClickListener)

        return ArtworkViewHolder(rowBinding)
    }

    override fun onBindViewHolder(holder: ArtworkViewHolder, position: Int) {
        val artwork = artworks[position]
        holder.binding.artworkTitle.text = artwork?.title

        // Visibility always set because row layouts are recycled
        if (artwork?.artistDisplay?.isEmpty() == true) {
            holder.binding.artworkArtist.visibility = View.GONE
        } else {
            holder.binding.artworkArtist.visibility = View.VISIBLE
            holder.binding.artworkArtist.text = artwork?.artistDisplay
        }

        if (artwork?.dateDisplay?.isEmpty() == true) {
            holder.binding.artworkDate.visibility = View.GONE
        } else {
            holder.binding.artworkDate.visibility = View.VISIBLE
            holder.binding.artworkDate.text = artwork?.dateDisplay
        }

        if (artwork?.imageId?.isEmpty() == true) {
            // Cancel any load still running from previous recycled row occupant due to async loading
            Glide.with(holder.itemView).clear(holder.binding.artworkThumbnail)
            holder.binding.artworkThumbnail.setImageResource(R.drawable.artwork_default)
        } else {
            Glide.with(holder.itemView)
                .load(getImageUrl(artwork?.imageId, ArtworkDownloader.THUMBNAIL_SIZE))
                .into(holder.binding.artworkThumbnail)
        }
    }

    override fun getItemCount(): Int {
        return artworks.size
    }
}

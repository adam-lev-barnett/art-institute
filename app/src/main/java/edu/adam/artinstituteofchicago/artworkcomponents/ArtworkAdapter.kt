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

/** Used by several different activities whose adapter instance only differs in their on-clicks/long-clicks */
class ArtworkAdapter(
    artworks: MutableList<Artwork?>?,
    private val clickListener: View.OnClickListener?,
    private val longClickListener: OnLongClickListener?
) : RecyclerView.Adapter<ArtworkViewHolder?>() {
    private val artworks = new MutableList<Artwork>

    /** Pass null for longClickListener if the activity has no longClick functionality  */
    init {
        this.artworks = (artworks ?: ArrayList<Artwork>()) as MutableList<Artwork>
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtworkViewHolder {
        val rowBinding =
            ArtworkListRowBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
            )
        rowBinding.getRoot().setOnClickListener(clickListener)
        if (longClickListener != null) rowBinding.getRoot()
            .setOnLongClickListener(longClickListener)

        return ArtworkViewHolder(rowBinding)
    }

    override fun onBindViewHolder(holder: ArtworkViewHolder, position: Int) {
        val artwork = artworks.get(position)
        holder.binding.artworkTitle.setText(artwork.getTitle())

        // Visibility always set because row layouts are recycled
        if (artwork.getArtistDisplay().isEmpty()) {
            holder.binding.artworkArtist.setVisibility(View.GONE)
        } else {
            holder.binding.artworkArtist.setVisibility(View.VISIBLE)
            holder.binding.artworkArtist.setText(artwork.getArtistDisplay())
        }

        if (artwork.getDateDisplay().isEmpty()) {
            holder.binding.artworkDate.setVisibility(View.GONE)
        } else {
            holder.binding.artworkDate.setVisibility(View.VISIBLE)
            holder.binding.artworkDate.setText(artwork.getDateDisplay())
        }

        if (artwork.getImageId().isEmpty()) {
            // Cancel any load still running from previous recycled row occupant due to async loading
            Glide.with(holder.itemView).clear(holder.binding.artworkThumbnail)
            holder.binding.artworkThumbnail.setImageResource(R.drawable.artwork_default)
        } else {
            Glide.with(holder.itemView)
                .load(getImageUrl(artwork.getImageId(), ArtworkDownloader.THUMBNAIL_SIZE))
                .into(holder.binding.artworkThumbnail)
        }
    }

    override fun getItemCount(): Int {
        return artworks.size
    }
}

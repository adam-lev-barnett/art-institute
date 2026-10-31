package edu.adam.artinstituteofchicago.artworkcomponents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import edu.adam.artinstituteofchicago.R;
import edu.adam.artinstituteofchicago.databinding.ArtworkListRowBinding;

public class ArtworkAdapter extends RecyclerView.Adapter<ArtworkViewHolder> {
    private final List<Artwork> artworks;
    // Listeners instead of a specific activity, so any artwork list screen can reuse this adapter
    private final View.OnClickListener clickListener;
    private final View.OnLongClickListener longClickListener;

    /** Pass null for longClickListener if the screen has no long-press action */
    public ArtworkAdapter(List<Artwork> artworks,
                          View.OnClickListener clickListener,
                          View.OnLongClickListener longClickListener) {
        this.artworks = artworks == null ? new ArrayList<>() : artworks;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ArtworkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ArtworkListRowBinding rowBinding =
                ArtworkListRowBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false
                );
        rowBinding.getRoot().setOnClickListener(clickListener);
        if (longClickListener != null) rowBinding.getRoot().setOnLongClickListener(longClickListener);

        return new ArtworkViewHolder(rowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtworkViewHolder holder, int position) {
        Artwork artwork = artworks.get(position);
        holder.binding.artworkTitle.setText(artwork.getTitle());

        // Rows are recycled, so the else branches have to undo a previous GONE
        if (artwork.getArtistDisplay().isEmpty()) {
            holder.binding.artworkArtist.setVisibility(View.GONE);
        } else {
            holder.binding.artworkArtist.setVisibility(View.VISIBLE);
            holder.binding.artworkArtist.setText(artwork.getArtistDisplay());
        }

        if (artwork.getDateDisplay().isEmpty()) {
            holder.binding.artworkDate.setVisibility(View.GONE);
        } else {
            holder.binding.artworkDate.setVisibility(View.VISIBLE);
            holder.binding.artworkDate.setText(artwork.getDateDisplay());
        }

        if (artwork.getImageId().isEmpty()) {
            // Cancel any load still running from this row's previous artwork
            Glide.with(holder.itemView).clear(holder.binding.artworkThumbnail);
            holder.binding.artworkThumbnail.setImageResource(R.drawable.artwork_default);
        } else {
            // The row's view ties Glide to whichever activity is showing it
            Glide.with(holder.itemView)
                    .load(ArtworkDownloader.getImageUrl(artwork.getImageId(), ArtworkDownloader.THUMBNAIL_SIZE))
                    .into(holder.binding.artworkThumbnail);
        }
    }

    @Override
    public int getItemCount() {
        return artworks.size();
    }
}

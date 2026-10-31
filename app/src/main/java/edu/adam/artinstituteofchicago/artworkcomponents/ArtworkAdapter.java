package edu.adam.artinstituteofchicago.artworkcomponents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import edu.adam.artinstituteofchicago.ArtworkListActivity;
import edu.adam.artinstituteofchicago.R;
import edu.adam.artinstituteofchicago.databinding.ArtworkListRowBinding;

public class ArtworkAdapter extends RecyclerView.Adapter<ArtworkViewHolder> {
    private final List<Artwork> artworks;
    private final ArtworkListActivity artworkListAct;

    public ArtworkAdapter(List<Artwork> artworks, ArtworkListActivity artworkListActivity) {
        this.artworks = artworks == null ? new ArrayList<>() : artworks;
        this.artworkListAct = artworkListActivity;
    }

    @NonNull
    @Override
    public ArtworkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ArtworkListRowBinding rowBinding =
                ArtworkListRowBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false
                );
        rowBinding.getRoot().setOnClickListener(artworkListAct);

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
            Glide.with(artworkListAct).clear(holder.binding.artworkThumbnail);
            holder.binding.artworkThumbnail.setImageResource(R.drawable.artwork_default);
        } else {
            Glide.with(artworkListAct)
                    .load(ArtworkDownloader.getImageUrl(artwork.getImageId(), ArtworkDownloader.THUMBNAIL_SIZE))
                    .into(holder.binding.artworkThumbnail);
        }
    }

    @Override
    public int getItemCount() {
        return artworks.size();
    }
}

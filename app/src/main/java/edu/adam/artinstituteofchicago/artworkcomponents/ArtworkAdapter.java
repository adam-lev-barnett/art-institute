package edu.adam.artinstituteofchicago.artworkcomponents;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.adam.artinstituteofchicago.Artwork;
import edu.adam.artinstituteofchicago.ArtworkListActivity;
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
        holder.binding.artworkArtist.setText(artwork.getArtistDisplay());
        holder.binding.artworkDate.setText(artwork.getDateDisplay());
    }

    @Override
    public int getItemCount() {
        return artworks.size();
    }
}

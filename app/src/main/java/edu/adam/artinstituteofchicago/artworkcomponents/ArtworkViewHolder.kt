package edu.adam.artinstituteofchicago.artworkcomponents;


import androidx.recyclerview.widget.RecyclerView;

import edu.adam.artinstituteofchicago.databinding.ArtworkListRowBinding;

public class ArtworkViewHolder extends RecyclerView.ViewHolder {
    public final ArtworkListRowBinding binding;
    public ArtworkViewHolder(ArtworkListRowBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }
}

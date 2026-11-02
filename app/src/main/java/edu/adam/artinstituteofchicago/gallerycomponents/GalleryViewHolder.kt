package edu.adam.artinstituteofchicago.gallerycomponents;


import androidx.recyclerview.widget.RecyclerView;

import edu.adam.artinstituteofchicago.databinding.GalleryListRowBinding;

public class GalleryViewHolder extends RecyclerView.ViewHolder {
    public final GalleryListRowBinding binding;
    public GalleryViewHolder(GalleryListRowBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }
}

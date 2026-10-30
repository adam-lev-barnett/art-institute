package edu.adam.artinstituteofchicago.gallerycomponents;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.adam.artinstituteofchicago.GalleryActivity;
import edu.adam.artinstituteofchicago.R;
import edu.adam.artinstituteofchicago.databinding.GalleryListRowBinding;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryViewHolder> {
    private final List<Gallery> galleries;
    private final GalleryActivity galleryAct;

    public GalleryAdapter(List<Gallery> galleries, GalleryActivity galleryActivity) {
        this.galleries = galleries == null ? new ArrayList<>() : galleries;
        this.galleryAct = galleryActivity;
    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        GalleryListRowBinding rowBinding =
                GalleryListRowBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false
                );
        rowBinding.getRoot().setOnClickListener(galleryAct);

        return new GalleryViewHolder(rowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {
        Gallery gallery = galleries.get(position);
        holder.binding.galleryId.setText(gallery.getName());

        if (gallery.getFloor().isEmpty()) {
            holder.binding.galleryFloor.setText("");
        } else {
            holder.binding.galleryFloor.setText(galleryAct.getString(R.string.floor_label, gallery.getFloor()));
        }
    }

    @Override
    public int getItemCount() {
        return galleries.size();
    }
}

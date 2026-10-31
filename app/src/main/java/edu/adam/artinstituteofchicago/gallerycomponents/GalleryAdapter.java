package edu.adam.artinstituteofchicago.gallerycomponents;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import edu.adam.artinstituteofchicago.GalleryActivity;
import edu.adam.artinstituteofchicago.R;
import edu.adam.artinstituteofchicago.artworkcomponents.ArtworkDownloader;
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
        holder.binding.galleryId.setText(gallery.getTitle());

        if (gallery.getFloor().isEmpty()) {
            holder.binding.galleryFloor.setText("");
        } else {
            holder.binding.galleryFloor.setText(galleryAct.getString(R.string.floor_label, gallery.getFloor()));
        }

        if (gallery.getImageId().isEmpty()) {
            // Cancel any load still running from this row's previous gallery
            Glide.with(galleryAct).clear(holder.binding.galleryThumbnail);
            holder.binding.galleryThumbnail.setImageResource(R.drawable.artwork_default);
        }
        else {
            Glide.with(galleryAct)
                    .load(ArtworkDownloader.getImageUrl(gallery.getImageId(), ArtworkDownloader.THUMBNAIL_SIZE))
                    .into(holder.binding.galleryThumbnail);
        }

        // Only request a thumbnail once its row is on screen, so the requests stay under the API rate limit
        if (!gallery.isThumbnailFetched()) {
            gallery.setThumbnailFetched(true);
            GalleryDownloader.getGalleryThumbnail(gallery, position);
        }
    }

    @Override
    public int getItemCount() {
        return galleries.size();
    }
}

package edu.adam.artinstituteofchicago.uicomponents;

import android.app.Activity;
import android.content.Intent;

import edu.adam.artinstituteofchicago.ArtworkDetailActivity;
import edu.adam.artinstituteofchicago.FavoritesActivity;
import edu.adam.artinstituteofchicago.GalleryActivity;
import edu.adam.artinstituteofchicago.MainActivity;
import edu.adam.artinstituteofchicago.R;
import edu.adam.artinstituteofchicago.SearchActivity;
import edu.adam.artinstituteofchicago.databinding.BottomNavigationBinding;

public class BottomNavBar {

    /** Global setup for each activity's bottom nav bar to centralize logic.
     * The calling activity passes itself and the binding to directly access the bottom navigation's custom layout.
     * - The calling activity's icon turns maroon, while the rest are grey.
     * - The calling activity's icon's on-click isn't set*/
    public static void setUp(Activity activity, BottomNavigationBinding nav) {
        if (activity instanceof MainActivity) {
            nav.homeIcon.setColorFilter(activity.getColor(R.color.maroon));
        } else {
            nav.homeIcon.setOnClickListener(v ->
                    activity.startActivity(new Intent(activity, MainActivity.class)));
        }

        if (activity instanceof GalleryActivity) {
            nav.galleriesIcon.setColorFilter(activity.getColor(R.color.maroon));
        } else {
            nav.galleriesIcon.setOnClickListener(v ->
                    activity.startActivity(new Intent(activity, GalleryActivity.class)));
        }

        if (activity instanceof SearchActivity) {
            nav.searchIcon.setColorFilter(activity.getColor(R.color.maroon));
        } else {
            nav.searchIcon.setOnClickListener(v ->
                    activity.startActivity(new Intent(activity, SearchActivity.class)));
        }

        if (activity instanceof FavoritesActivity) {
            nav.faveIcon.setColorFilter(activity.getColor(R.color.maroon));
        } else {
            nav.faveIcon.setOnClickListener(v ->
                    activity.startActivity(new Intent(activity, FavoritesActivity.class)));
        }

        if (activity instanceof ArtworkDetailActivity) {
            nav.randomIcon.setColorFilter(activity.getColor(R.color.maroon));
        } else {
            nav.randomIcon.setOnClickListener(v ->
                    activity.startActivity(new Intent(activity, ArtworkDetailActivity.class)));
        }
    }
}

package edu.adam.artinstituteofchicago.uicomponents

import android.app.Activity
import android.content.Intent
import android.view.View
import edu.adam.artinstituteofchicago.ArtworkDetailActivity
import edu.adam.artinstituteofchicago.FavoritesActivity
import edu.adam.artinstituteofchicago.GalleryActivity
import edu.adam.artinstituteofchicago.MainActivity
import edu.adam.artinstituteofchicago.R
import edu.adam.artinstituteofchicago.SearchActivity
import edu.adam.artinstituteofchicago.databinding.BottomNavigationBinding

object BottomNavigation {
    /** Global setup for each activity's bottom nav bar to centralize logic.
     * The calling activity passes itself and the binding to directly access the bottom navigation's custom layout.
     * - The calling activity's icon turns maroon, while the rest are grey.
     * - The calling activity's icon's on-click isn't set */
    fun setUp(activity: Activity, nav: BottomNavigationBinding) {
        if (activity is MainActivity) {
            nav.homeIcon.setColorFilter(activity.getColor(R.color.maroon))
        } else {
            nav.homeIcon.setOnClickListener(View.OnClickListener { v: View? ->
                activity.startActivity(
                    Intent(activity, MainActivity::class.java)
                )
            })
        }

        if (activity is GalleryActivity) {
            nav.galleriesIcon.setColorFilter(activity.getColor(R.color.maroon))
        } else {
            nav.galleriesIcon.setOnClickListener(View.OnClickListener { v: View? ->
                activity.startActivity(
                    Intent(activity, GalleryActivity::class.java)
                )
            })
        }

        if (activity is SearchActivity) {
            nav.searchIcon.setColorFilter(activity.getColor(R.color.maroon))
        } else {
            nav.searchIcon.setOnClickListener(View.OnClickListener { v: View? ->
                activity.startActivity(
                    Intent(activity, SearchActivity::class.java)
                )
            })
        }

        if (activity is FavoritesActivity) {
            nav.faveIcon.setColorFilter(activity.getColor(R.color.maroon))
        } else {
            nav.faveIcon.setOnClickListener(View.OnClickListener { v: View? ->
                activity.startActivity(
                    Intent(activity, FavoritesActivity::class.java)
                )
            })
        }

        nav.randomIcon.setOnClickListener(View.OnClickListener { v: View? ->
            activity.startActivity(
                Intent(activity, ArtworkDetailActivity::class.java)
            )
        })
    }
}

package edu.adam.artinstituteofchicago

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.JsonSyntaxException
import edu.adam.artinstituteofchicago.artworkcomponents.UserFavorites
import edu.adam.artinstituteofchicago.databinding.ActivityMainBinding
import edu.adam.artinstituteofchicago.uicomponents.BottomNavigation
import edu.adam.artinstituteofchicago.uicomponents.ErrorDialog

class MainActivity : AppCompatActivity() {
    var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.getRoot())

        ViewCompat.setOnApplyWindowInsetsListener(
            binding!!.getRoot(),
            OnApplyWindowInsetsListener { v: View?, insets: WindowInsetsCompat? ->
                val systemBars = insets!!.getInsets(WindowInsetsCompat.Type.systemBars())
                v!!.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            })

        // Utility class to set up the bottom nav bar based on the calling activity
        BottomNavigation.setUp(this, binding!!.bottomNav)
        setSupportActionBar(binding!!.topBanner.getRoot())

        // Only attempt to load favorites once per app startup
        if (!UserFavorites.loadAttempted()) {
            try {
                UserFavorites.loadFaves(this)
            } catch (e: JsonSyntaxException) {
                ErrorDialog.show(
                    this,
                    getString(R.string.favorites_error),
                    getString(R.string.favorites_load_failed)
                )
            }
        }
    }

    fun openGalleries(v: View?) {
        startActivity(Intent(this, GalleryActivity::class.java))
    }

    fun openSearch(v: View?) {
        startActivity(Intent(this, SearchActivity::class.java))
    }

    fun openRandom(v: View?) {
        startActivity(Intent(this, ArtworkDetailActivity::class.java))
    }
}
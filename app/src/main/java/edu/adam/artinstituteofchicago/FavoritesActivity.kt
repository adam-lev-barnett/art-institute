package edu.adam.artinstituteofchicago

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnLongClickListener
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonSyntaxException
import edu.adam.artinstituteofchicago.artworkcomponents.Artwork
import edu.adam.artinstituteofchicago.artworkcomponents.ArtworkAdapter
import edu.adam.artinstituteofchicago.artworkcomponents.UserFavorites
import edu.adam.artinstituteofchicago.databinding.ActivityFavoritesBinding
import edu.adam.artinstituteofchicago.uicomponents.BottomNavigation
import edu.adam.artinstituteofchicago.uicomponents.ErrorDialog

class FavoritesActivity : AppCompatActivity(), View.OnClickListener, OnLongClickListener {
    var binding: ActivityFavoritesBinding? = null
    private var adapter: ArtworkAdapter? = null
    private val faves: MutableList<Artwork> = ArrayList<Artwork>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding!!.getRoot())
        ViewCompat.setOnApplyWindowInsetsListener(
            binding!!.getRoot(),
            OnApplyWindowInsetsListener { v: View?, insets: WindowInsetsCompat? ->
                val systemBars = insets!!.getInsets(WindowInsetsCompat.Type.systemBars())
                v!!.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            })

        BottomNavigation.setUp(this, binding!!.bottomNav)
        setSupportActionBar(binding!!.topBanner.getRoot())

        setUpFavesRecycler()
    }

    override fun onResume() {
        super.onResume()

        if (!UserFavorites.loadAttempted()) {
            try {
                UserFavorites.loadFaves(this)
            } catch (e: JsonSyntaxException) {
                // Faves couldn't be read, so the list stays empty and shows the no-favorites message
                Log.d(TAG, "onResume: " + e.message)
                ErrorDialog.show(
                    this,
                    getString(R.string.favorites_error),
                    getString(R.string.favorites_load_failed)
                )
            }
        }

        faves.clear()
        faves.addAll(UserFavorites.getFaves())
        adapter!!.notifyDataSetChanged()
        showEmptyMessageIfNoFaves()
    }

    private fun setUpFavesRecycler() {
        this.adapter = ArtworkAdapter(faves, this, this)
        val linearLayoutManager = LinearLayoutManager(this)
        binding!!.favesRecycler.setAdapter(adapter)
        binding!!.favesRecycler.setLayoutManager(linearLayoutManager)
    }

    private fun showEmptyMessageIfNoFaves() {
        binding!!.favesEmpty.visibility = if (faves.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onClick(v: View) {
        val pos = binding!!.favesRecycler.getChildLayoutPosition(v)
        if (pos == RecyclerView.NO_POSITION) return

        val intent = Intent(this, ArtworkDetailActivity::class.java)
        intent.putExtra(getString(R.string.artwork), faves.get(pos))
        startActivity(intent)
    }

    override fun onLongClick(v: View): Boolean {
        val pos = binding!!.favesRecycler.getChildLayoutPosition(v)
        if (pos == RecyclerView.NO_POSITION) return false

        val artwork = faves.get(pos)

        AlertDialog.Builder(this)
            .setIcon(R.drawable.fav_on)
            .setTitle(R.string.remove_favorite_title)
            .setMessage(getString(R.string.remove_favorite_message, artwork.title))
            .setPositiveButton(
                R.string.yes,
                DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                    removeFave(artwork)
                })
            .setNegativeButton(R.string.no, null)
            .show()

        return true
    }

    private fun removeFave(artwork: Artwork) {
        val pos = faves.indexOf(artwork)
        if (pos == -1) return

        UserFavorites.removeFave(artwork, this)
        faves.removeAt(pos)
        adapter!!.notifyItemRemoved(pos)
        showEmptyMessageIfNoFaves()
    }

    companion object {
        private const val TAG = "FavoritesActivity"
    }
}

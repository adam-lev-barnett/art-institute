package edu.adam.artinstituteofchicago

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.adam.artinstituteofchicago.artworkcomponents.Artwork
import edu.adam.artinstituteofchicago.artworkcomponents.ArtworkAdapter
import edu.adam.artinstituteofchicago.artworkcomponents.ArtworkDownloader
import edu.adam.artinstituteofchicago.databinding.ActivitySearchBinding
import edu.adam.artinstituteofchicago.uicomponents.BottomNavigation
import edu.adam.artinstituteofchicago.uicomponents.ErrorDialog

class SearchActivity : AppCompatActivity(), View.OnClickListener {
    var binding: ActivitySearchBinding? = null
    private var adapter: ArtworkAdapter? = null
    private var linearLayoutManager: LinearLayoutManager? = null
    private val results: MutableList<Artwork?> = ArrayList<Artwork?>()

    // Most recent keyword searched; results for any older keyword are ignored
    private var lastSearch: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        binding = ActivitySearchBinding.inflate(getLayoutInflater())
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
        setUpSearchRecycler()

        // Rotation recreates the activity and empties the list, so run the same search again
        if (savedInstanceState != null) {
            lastSearch = savedInstanceState.getString(getString(R.string.search_extra))
            if (lastSearch != null) ArtworkDownloader.search(this, lastSearch)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(getString(R.string.search_extra), lastSearch)
        super.onSaveInstanceState(outState)
    }

    private fun setUpSearchRecycler() {
        this.adapter = ArtworkAdapter(results, this, null)
        this.linearLayoutManager = LinearLayoutManager(this)
        binding!!.searchRecycler.setAdapter(adapter)
        binding!!.searchRecycler.setLayoutManager(linearLayoutManager)
    }

    /** Set with onClick for the search icon. Trimming input is handled via ArtworkDownloader   */
    fun searchArtworks(v: View?) {
        val keyword = binding!!.searchInput.getText().toString()

        if (keyword.isEmpty()) {
            binding!!.searchInput.setError(getString(R.string.search_required))
            binding!!.searchInput.requestFocus()
            return
        }

        lastSearch = keyword
        ArtworkDownloader.search(this, keyword)
    }

    /** Called by ArtworkDownloader with the results for one keyword  */
    fun updateData(keyword: String?, searchResults: MutableList<Artwork?>) {
        results.clear()
        results.addAll(searchResults)
        // New search replaces the whole list
        adapter!!.notifyDataSetChanged()
        binding!!.searchRecycler.scrollToPosition(0)

        if (results.isEmpty()) {
            ErrorDialog.show(
                this, R.drawable.ic_search,
                getString(R.string.no_results_title),
                getString(R.string.no_results_message, keyword)
            )
        }
    }

    /** Null title or message means the downloader had no details, so use the defaults  */
    fun downloadFailed(title: String?, errorMsg: String?) {
        ErrorDialog.show(
            this,
            title ?: getString(R.string.download_error),
            errorMsg ?: getString(R.string.search_download_failed)
        )
    }

    override fun onClick(v: View) {
        val pos = binding!!.searchRecycler.getChildLayoutPosition(v)
        if (pos == RecyclerView.NO_POSITION) return

        val intent = Intent(this, ArtworkDetailActivity::class.java)
        intent.putExtra(getString(R.string.artwork), results.get(pos))
        startActivity(intent)
    }
}

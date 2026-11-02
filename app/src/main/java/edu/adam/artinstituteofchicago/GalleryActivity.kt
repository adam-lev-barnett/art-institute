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
import edu.adam.artinstituteofchicago.databinding.ActivityGalleryBinding
import edu.adam.artinstituteofchicago.gallerycomponents.Gallery
import edu.adam.artinstituteofchicago.gallerycomponents.GalleryAdapter
import edu.adam.artinstituteofchicago.gallerycomponents.GalleryDownloader
import edu.adam.artinstituteofchicago.uicomponents.BottomNavigation
import edu.adam.artinstituteofchicago.uicomponents.ErrorDialog

class GalleryActivity : AppCompatActivity(), View.OnClickListener {
    var galleryBinding: ActivityGalleryBinding? = null
    private var linearLayoutManager: LinearLayoutManager? = null
    private var adapter: GalleryAdapter? = null
    private val galleries: MutableList<Gallery?>? = ArrayList<Gallery?>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        galleryBinding = ActivityGalleryBinding.inflate(getLayoutInflater())
        setContentView(galleryBinding!!.getRoot())
        ViewCompat.setOnApplyWindowInsetsListener(
            galleryBinding!!.getRoot(),
            OnApplyWindowInsetsListener { v: View?, insets: WindowInsetsCompat? ->
                val systemBars = insets!!.getInsets(WindowInsetsCompat.Type.systemBars())
                v!!.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            })

        BottomNavigation.setUp(this, galleryBinding!!.bottomNav)
        setSupportActionBar(galleryBinding!!.topBanner.getRoot())
        setUpGalleryRecycler()
        GalleryDownloader.getSourceData(this)
    }

    private fun setUpGalleryRecycler() {
        this.adapter = GalleryAdapter(galleries, this)
        this.linearLayoutManager = LinearLayoutManager(this)
        galleryBinding!!.galleryRecycler.setAdapter(adapter)
        galleryBinding!!.galleryRecycler.setLayoutManager(linearLayoutManager)
    }

    override fun onClick(v: View) {
        val intent = Intent(this, ArtworkListActivity::class.java)
        if (!galleries.isNullOrEmpty()) {
            val gallery = galleries.get(galleryBinding!!.galleryRecycler.getChildLayoutPosition(v))
            intent.putExtra(getString(R.string.gallery_extra), gallery)
        }
        startActivity(intent)
    }

    /** Null title or message means the downloader had no details, so use the defaults  */
    fun downloadFailed(title: String?, errMessage: String?) {
        ErrorDialog.show(
            this,
            title ?: getString(R.string.download_error),
            errMessage ?: getString(R.string.download_failed)
        )
    }

    fun updateData(galleryList: MutableList<Gallery?>) {
        val sizeBeforeAdd = galleries!!.size
        galleries.addAll(galleryList)
        adapter!!.notifyItemRangeInserted(sizeBeforeAdd, galleryList.size)
    }

    fun updateGalleryThumbnail(position: Int) {
        adapter!!.notifyItemChanged(position)
    }
}
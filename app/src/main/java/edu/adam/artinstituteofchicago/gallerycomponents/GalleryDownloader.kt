package edu.adam.artinstituteofchicago.gallerycomponents

import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.NoConnectionError
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import edu.adam.artinstituteofchicago.GalleryActivity
import edu.adam.artinstituteofchicago.R
import org.json.JSONException
import org.json.JSONObject
import androidx.core.net.toUri

object GalleryDownloader {
    private var galleryActivity: GalleryActivity? = null
    private const val TAG = "GalleryDownloader"
    private const val BASE_URL = "https://api.artic.edu/api/v1/galleries"
    private const val FIELDS = "id,title,number,floor,is_closed,source_updated_at"
    private const val startingPage = "1"
    private var rq: RequestQueue? = null
    private var listener: Response.Listener<JSONObject?>? = null
    private var errorListener: Response.ErrorListener? = null

    private const val ARTWORK_SEARCH_URL = "https://api.artic.edu/api/v1/artworks/search"
    private const val THUMBNAIL_FIELDS = "id,image_id"
    private const val THUMBNAIL_LIMIT = "1"

    private fun buildUrl(): String {
        return BASE_URL.toUri().buildUpon()
            .appendQueryParameter("fields", FIELDS)
            .appendQueryParameter("limit", galleryActivity!!.getString(R.string.page_limit))
            .appendQueryParameter("page", startingPage)
            .build()
            .toString()
    }

    private fun buildThumbnailUrl(galleryId: Long): String {
        return ARTWORK_SEARCH_URL.toUri().buildUpon()
            .appendQueryParameter("query[term][gallery_id]", galleryId.toString())
            .appendQueryParameter("fields", THUMBNAIL_FIELDS) // Only the first artwork is needed
            .appendQueryParameter("limit", THUMBNAIL_LIMIT)
            .appendQueryParameter("page", startingPage)
            .build()
            .toString()
    }

    fun getSourceData(activity: GalleryActivity) {
        galleryActivity = activity
        if (rq == null) rq = Volley.newRequestQueue(galleryActivity)

        listener = Response.Listener { response: JSONObject? ->
            try {
                GalleryDownloader.handleResults(galleryActivity!!, response.toString())
            } catch (e: JSONException) {
                GalleryDownloader.handleError(galleryActivity!!, null, null)
            }
        }

        errorListener = Response.ErrorListener { err: VolleyError? ->
            // Volley's error class names the AlertDialog
            val title = err!!.javaClass.getSimpleName()
            var message = err.message
            if (err is NoConnectionError) {
                message = galleryActivity!!.getString(R.string.no_connection)
            } else if (err.networkResponse != null && err.networkResponse.data != null) {
                try {
                    // Build error json object
                    val errorJson = JSONObject(String(err.networkResponse.data))
                    message = errorJson.getString("message")
                } catch (ignored: JSONException) {
                    // Keeps Volley's message; the dialog still shows
                }
            }
            GalleryDownloader.handleError(galleryActivity!!, title, message)
        }

        val queryUrl = buildUrl()

        requestGET(queryUrl, listener, errorListener)
    }

    /** Looks up the first artwork in a gallery and saves its image id on the Gallery for the thumbnail  */
    @JvmStatic
    fun getGalleryThumbnail(gallery: Gallery, position: Int) {
        if (rq == null) rq = Volley.newRequestQueue(galleryActivity)

        // Async, so each one has to remember which gallery and row it belongs to
        val thumbnailListener = Response.Listener { response: JSONObject? ->
            try {
                val data = response!!.getJSONArray("data")
                // Empty gallery: leave the image id blank so the default image shows
                if (data.length() == 0) return@Listener

                val firstArtwork = data.getJSONObject(0)
                if (!firstArtwork.isNull("image_id")) {
                    gallery.imageId = firstArtwork.getString("image_id")
                    galleryActivity!!.updateGalleryThumbnail(position)
                }
            } catch (e: JSONException) {
                Log.d(TAG, "getGalleryThumbnail: " + e.message)
            }
        }

        // Failed thumbnail keeps the default image and retries the next time its row is bound
        val thumbnailErrorListener = Response.ErrorListener { err: VolleyError? ->
            gallery.isThumbnailFetched = false
            Log.d(TAG, "getGalleryThumbnail: " + gallery.title + " " + err!!.message)
        }

        requestGET(buildThumbnailUrl(gallery.id), thumbnailListener, thumbnailErrorListener)
    }

    private fun requestGET(
        query: String?,
        onResponse: Response.Listener<JSONObject?>?,
        onError: Response.ErrorListener?
    ) {
        val request: JsonObjectRequest =
            object : JsonObjectRequest(Method.GET, query, null, onResponse, onError) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): MutableMap<String?, String?> {
                    val headers: MutableMap<String?, String?> = HashMap<String?, String?>()

                    headers["User-Agent"] = "AICGalleryApp/1.0 (Android; Educational Project)"
                    headers["AIC-User-Agent"] = "AICGalleryApp/1.0 (Android; Educational Project)"
                    headers["Accept"] = "application/json"

                    return headers
                }
            }

        rq!!.add<JSONObject?>(request)
    }

    @Throws(JSONException::class)
    private fun parseJsonArray(jsonString: String): MutableList<Gallery?>? {
        val galleries: MutableList<Gallery?> = ArrayList<Gallery?>()

        try {
            val response = JSONObject(jsonString)
            val jsonGalleries = response.getJSONArray("data")

            for (i in 0..<jsonGalleries.length()) {
                try {
                    val gallery = galleryFactory(jsonGalleries.getJSONObject(i))
                    galleries.add(gallery)
                } catch (e: Exception) {
                    Log.d(TAG, "Error parsing JSON object $i")
                }
            }
            return galleries
        } catch (e: JSONException) {
            GalleryDownloader.handleError(galleryActivity!!, null, null)
        }

        return null
    }

    private fun loadNextPageIfPresent(jsonString: String) {
        try {
            val response = JSONObject(jsonString)
            val pagination = response.getJSONObject("pagination")
            if (pagination.has("next_url")) requestGET(
                pagination.getString("next_url"),
                listener,
                errorListener
            )
        } catch (e: JSONException) {
            GalleryDownloader.handleError(galleryActivity!!, null, null)
        }
    }

    /** Builds Gallery from JSONObject with available fields; returns empty strings if field not available */
    @Throws(JSONException::class)
    private fun galleryFactory(json: JSONObject): Gallery {
        val id = json.getLong("id")
        var title = ""
        var number = ""
        var floor = ""
        val is_closed = json.optBoolean("is_closed", false)
        var source_updated_at = ""

        if (json.has("title")) title = json.getString("title")
        if (json.has("number")) number = json.getString("number")
        if (json.has("floor")) floor = json.getString("floor")
        if (json.has("source_updated_at")) source_updated_at = json.getString("source_updated_at")

        return Gallery(id, title, number, floor, source_updated_at)
    }

    /** Only parses the data if it exists  */
    @Throws(JSONException::class)
    private fun handleResults(activity: GalleryActivity, json: String?) {
        if (json == null) {
            handleError(activity, null, null)
            return
        }

        val galleryList = parseJsonArray(json)

        if (galleryList != null) activity.runOnUiThread(Runnable {
            activity.updateData(galleryList)
        })

        loadNextPageIfPresent(json)
    }

    /** Passes error dialog handling to activity; null title/message use the activity's defaults  */
    private fun handleError(activity: GalleryActivity, title: String?, errorMsg: String?) {
        activity.runOnUiThread(Runnable { activity.downloadFailed(title, errorMsg) })
    }
}

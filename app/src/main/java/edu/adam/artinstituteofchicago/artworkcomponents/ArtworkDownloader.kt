package edu.adam.artinstituteofchicago.artworkcomponents

import android.content.Context
import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.NoConnectionError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import edu.adam.artinstituteofchicago.ArtworkDetailActivity
import edu.adam.artinstituteofchicago.ArtworkListActivity
import edu.adam.artinstituteofchicago.R
import edu.adam.artinstituteofchicago.SearchActivity
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.StringJoiner
import androidx.core.net.toUri

object ArtworkDownloader {
    private var artworkListActivity: ArtworkListActivity? = null
    private const val TAG = "ArtworkDownloader"
    private const val BASE_URL = "https://api.artic.edu/api/v1/artworks/search"
    private val FIELDS = "id,title,artist_display," +
            "date_display,medium_display," +
            "place_of_origin,image_id,dimensions," +
            "short_description,description,credit_line,galleryId," +  // API doesn't return anything for galleryId, so need to include gallery_title as identifier for artwork
            "gallery_title"
    private const val FIRST_PAGE = 1
    private var rq: RequestQueue? = null
    private var listener: Response.Listener<JSONObject?>? = null
    private var errorListener: Response.ErrorListener? = null

    private var currentGalleryId: Long = 0

    private const val IMAGE_BASE_URL = "https://www.artic.edu/iiif/2/"
    const val THUMBNAIL_SIZE: String = "400"
    const val FULL_SIZE: String = "1200"
    const val FALLBACK_SIZE: String = "800"

    private fun buildUrl(galleryId: Long, limit: String?, page: Int): String {
        return BASE_URL.toUri().buildUpon()
            .appendQueryParameter("query[term][gallery_id]", galleryId.toString())
            .appendQueryParameter("fields", FIELDS)
            .appendQueryParameter("limit", limit)
            .appendQueryParameter("page", page.toString())
            .build()
            .toString()
    }

    /** Continuously downloads every artwork in the gallery one page at a time until exhausted/next page doesn't exist */
    fun getSourceData(activity: ArtworkListActivity, galleryId: Long) {
        artworkListActivity = activity
        currentGalleryId = galleryId
        if (rq == null) rq = Volley.newRequestQueue(artworkListActivity)

        listener = Response.Listener { response: JSONObject? ->
            ArtworkDownloader.handleResults(
                artworkListActivity!!, response!!
            )
        }

        errorListener = Response.ErrorListener { err: VolleyError? ->
            ArtworkDownloader.handleError(
                artworkListActivity!!,
                ArtworkDownloader.errorTitle(err!!),
                ArtworkDownloader.errorMessage(artworkListActivity!!, err)
            )
        }

        val queryUrl = buildUrl(
            currentGalleryId,
            artworkListActivity!!.getString(R.string.page_limit), FIRST_PAGE
        )

        requestGET(queryUrl, listener, errorListener)
    }

    fun search(activity: SearchActivity, keywords: String?) {
        if (rq == null) rq = Volley.newRequestQueue(activity)
        if (keywords.isNullOrEmpty()) return

        val querySplit: Array<String?> =
            keywords.trim { it <= ' ' }.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
        val joiner = StringJoiner(" ")

        // Wrapping terms in quotes bypasses API search bug returning all results
        for (term in querySplit) {
            joiner.add("\"" + term + "\"")
        }

        val query = joiner.toString()

        val queryUrl = "https://api.artic.edu/api/v1/artworks/search".toUri()
            .buildUpon()
            .appendQueryParameter("q", query)
            .appendQueryParameter("fields", FIELDS)
            .appendQueryParameter("limit", activity.getString(R.string._100))
            .appendQueryParameter("page", FIRST_PAGE.toString())
            .build()
            .toString()

        // Local listeners remember which keyword they belong to, so a slow older search can be ignored
        val searchListener = Response.Listener { response: JSONObject? ->
            try {
                activity.updateData(keywords, ArtworkDownloader.parseJsonArray(response!!))
            } catch (e: JSONException) {
                /* Multiple activities use this method, so passing null for title and errorMsg
                *  Passing null for either field reverts to the default message for the calling activity */
                activity.downloadFailed(null, null)
            }
        }

        val searchErrorListener = Response.ErrorListener { err: VolleyError? ->
            activity.downloadFailed(
                ArtworkDownloader.errorTitle(err!!), ArtworkDownloader.errorMessage(activity, err)
            )
        }

        requestGET(queryUrl, searchListener, searchErrorListener)
    }

    fun getRandomArtwork(activity: ArtworkDetailActivity) {
        if (rq == null) rq = Volley.newRequestQueue(activity)

        val body: JSONObject?
        try {
            body = buildRandomQuery()
        } catch (e: JSONException) {
            activity.downloadFailed(null, null)
            return
        }

        val randomListener = Response.Listener { response: JSONObject? ->
            try {
                val results = ArtworkDownloader.parseJsonArray(response!!)
                if (results.isEmpty()) {
                    activity.downloadFailed(null, null)
                } else {
                    activity.showRandomArtwork(results.get(0))
                }
            } catch (e: JSONException) {
                activity.downloadFailed(null, null)
            }
        }

        val randomErrorListener = Response.ErrorListener { err: VolleyError? ->
            activity.downloadFailed(
                ArtworkDownloader.errorTitle(err!!), ArtworkDownloader.errorMessage(activity, err)
            )
        }

        sendRequest(Request.Method.POST, BASE_URL, body, randomListener, randomErrorListener)
    }

    /** Builds query specifically for random artwork fetch using API's recommended format.
     * Documentation recommends using current time in millis for random seed generation  */
    @Throws(JSONException::class)
    private fun buildRandomQuery(): JSONObject {
        val filters = JSONArray()
            .put(JSONObject().put("term", JSONObject().put("is_public_domain", true)))
            .put(fieldExists("image_id"))
            .put(fieldExists("thumbnail.width"))
            .put(fieldExists("thumbnail.height"))

        val functionScore = JSONObject()
            .put("query", JSONObject().put("bool", JSONObject().put("filter", filters)))
            .put("boost_mode", "replace")
            .put(
                "random_score", JSONObject()
                    .put("field", "id")
                    .put("seed", System.currentTimeMillis())
            )

        return JSONObject()
            .put("fields", FIELDS)
            .put("boost", false)
            .put("limit", 1)
            .put("query", JSONObject().put("function_score", functionScore))
    }

    @Throws(JSONException::class)
    private fun fieldExists(field: String?): JSONObject {
        return JSONObject().put("exists", JSONObject().put("field", field))
    }

    /** Volley's error class provides error alert dialog's title  */
    private fun errorTitle(err: VolleyError): String {
        return err.javaClass.getSimpleName()
    }

    private fun errorMessage(context: Context, err: VolleyError): String? {
        if (err is NoConnectionError) return context.getString(R.string.no_connection)

        var message = err.message
        if (err.networkResponse != null && err.networkResponse.data != null) {
            try {
                val errorJson = JSONObject(String(err.networkResponse.data))
                message = errorJson.getString("message")
            } catch (ignored: JSONException) {
                // Volley message is passed to alert dialog in calling activity
            }
        }
        return message
    }

    /** Builds the image URL with required headers.
     * Use THUMBNAIL_SIZE in Recyclers, FULL_SIZE (or FALLBACK_SIZE) in ArtworkDetailAcrivity  */
    @JvmStatic
    fun getImageUrl(imageId: String?, size: String?): GlideUrl {
        val imageUrl = IMAGE_BASE_URL + imageId + "/full/" + size + ",/0/default.jpg"

        return GlideUrl(
            imageUrl,
            LazyHeaders.Builder()
                .addHeader("User-Agent", "ArticApp/1.0")
                .addHeader("AIC-User-Agent", "ArticApp/1.0")
                .build()
        )
    }

    private fun requestGET(
        query: String?,
        onResponse: Response.Listener<JSONObject?>?,
        onError: Response.ErrorListener?
    ) {
        sendRequest(Request.Method.GET, query, null, onResponse, onError)
    }

    /** @param method Body is null for GET (used on everything except random fetch).
     * Random uses POST (API says POST is needed for request) and needs the body.
     */
    private fun sendRequest(
        method: Int, url: String?, body: JSONObject?,
        onResponse: Response.Listener<JSONObject?>?,
        onError: Response.ErrorListener?
    ) {
        val request: JsonObjectRequest =
            object : JsonObjectRequest(method, url, body, onResponse, onError) {
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
    private fun parseJsonArray(response: JSONObject): MutableList<Artwork?> {
        val artworks: MutableList<Artwork?> = ArrayList<Artwork?>()
        val jsonArtworks = response.getJSONArray("data")

        for (i in 0..<jsonArtworks.length()) {
            try {
                artworks.add(artworkFactory(jsonArtworks.getJSONObject(i)))
            } catch (e: JSONException) {
                Log.d(TAG, "Error parsing JSON object $i")
            }
        }
        return artworks
    }

    /** Retrieves next page by querying the total number of pages and current page number  */
    @Throws(JSONException::class)
    private fun loadNextPageIfPresent(response: JSONObject) {
        val pagination = response.getJSONObject("pagination")
        val currentPage = pagination.getInt("current_page")
        val totalPages = pagination.getInt("total_pages")

        if (currentPage < totalPages) {
            val queryUrl = buildUrl(
                currentGalleryId,
                artworkListActivity!!.getString(R.string.page_limit),
                currentPage + 1
            )
            requestGET(queryUrl, listener, errorListener)
        }
    }

    @Throws(JSONException::class)
    private fun artworkFactory(json: JSONObject): Artwork {
        return Artwork(
            json.getLong("id"),
            getStringOrEmpty(json, "title"),
            getStringOrEmpty(json, "artist_display"),
            getStringOrEmpty(json, "date_display"),
            getStringOrEmpty(json, "medium_display"),
            getStringOrEmpty(json, "place_of_origin"),
            getStringOrEmpty(json, "image_id"),
            getStringOrEmpty(json, "dimensions"),
            getStringOrEmpty(json, "short_description"),
            getStringOrEmpty(json, "description"),
            getStringOrEmpty(json, "credit_line"),
            getStringOrEmpty(json, "gallery_title")
        )
    }

    @Throws(JSONException::class)
    private fun getStringOrEmpty(json: JSONObject, key: String): String {
        return if (json.isNull(key)) "" else json.getString(key)
    }

    private fun handleResults(activity: ArtworkListActivity, response: JSONObject) {
        try {
            val works = parseJsonArray(response)
            activity.updateData(works)
            loadNextPageIfPresent(response)
        } catch (e: JSONException) {
            handleError(activity, null, null)
        }
    }

    /** Passes error dialog handling to activity. Null title/message use the activity defaults  */
    private fun handleError(activity: ArtworkListActivity, title: String?, errorMsg: String?) {
        activity.downloadFailed(title, errorMsg)
    }
}

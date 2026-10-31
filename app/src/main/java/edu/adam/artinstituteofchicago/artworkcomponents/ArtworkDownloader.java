package edu.adam.artinstituteofchicago.artworkcomponents;

import android.net.Uri;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.adam.artinstituteofchicago.ArtworkListActivity;
import edu.adam.artinstituteofchicago.R;

public class ArtworkDownloader {
    private static ArtworkListActivity artworkListActivity;
    private static final String TAG = "ArtworkDownloader";
    private static final String BASE_URL = "https://api.artic.edu/api/v1/artworks/search";
    private static final String FIELDS =
            "id,title,artist_display," +
            "date_display,medium_display," +
            "place_of_origin,image_id,dimensions," +
            "short_description,description,credit_line,galleryId";
    private static final int FIRST_PAGE = 1;
    private static RequestQueue rq;
    private static Response.Listener<JSONObject> listener;
    private static Response.ErrorListener errorListener;

    private static long currentGalleryId;

    private static final String IMAGE_BASE_URL = "https://www.artic.edu/iiif/2/";
    public static final String THUMBNAIL_SIZE = "400";
    public static final String FULL_SIZE = "1200";
    public static final String FALLBACK_SIZE = "800";

    private static String buildUrl(long galleryId, String limit, int page) {
        return Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter("query[term][gallery_id]", String.valueOf(galleryId))
                .appendQueryParameter("fields", FIELDS)
                // How many items per page
                .appendQueryParameter("limit", limit)
                .appendQueryParameter("page", String.valueOf(page))
                .build()
                .toString();
    }

    /** Downloads every artwork in the gallery one page at a time */
    public static void getSourceData(ArtworkListActivity activity, long galleryId) {
        artworkListActivity = activity;
        currentGalleryId = galleryId;
        if (rq == null) rq = Volley.newRequestQueue(artworkListActivity);

        listener = response -> handleResults(artworkListActivity, response);

        errorListener = err -> {
            String message = err.getMessage();
            if (err.networkResponse != null && err.networkResponse.data != null) {
                try {
                    // Build error json object
                    JSONObject errorJson = new JSONObject(new String(err.networkResponse.data));
                    message = errorJson.getString("message");
                } catch (JSONException ignored) {
                    // Volley/activity toast handles this, so ignored
                }
            }
            handleError(artworkListActivity, message);
        };

        String queryUrl = buildUrl(currentGalleryId,
                artworkListActivity.getString(R.string.page_limit), FIRST_PAGE);

        requestGET(queryUrl, listener, errorListener);
    }

    /** Builds the IIIF image URL with required headers.
     *  Use THUMBNAIL_SIZE in Recyclers, FULL_SIZE (then FALLBACK_SIZE) in the artwork detail activity */
    public static GlideUrl getImageUrl(String imageId, String size) {
        String imageUrl = IMAGE_BASE_URL + imageId + "/full/" + size + ",/0/default.jpg";

        return new GlideUrl(imageUrl,
                new LazyHeaders.Builder()
                        .addHeader("User-Agent", "ArticApp/1.0")
                        .addHeader("AIC-User-Agent", "ArticApp/1.0")
                        .build());
    }

    private static void requestGET(String query,
                                   Response.Listener<JSONObject> onResponse,
                                   Response.ErrorListener onError) {
        JsonObjectRequest request =
                new JsonObjectRequest(Request.Method.GET, query, null, onResponse, onError) {

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();

                        headers.put("User-Agent",
                                "AICGalleryApp/1.0 (Android; Educational Project)");
                        headers.put("AIC-User-Agent",
                                "AICGalleryApp/1.0 (Android; Educational Project)");
                        headers.put("Accept", "application/json");

                        return headers;
                    }
                };

        rq.add(request);
    }

    private static List<Artwork> parseJsonArray(JSONObject response) throws JSONException {
        List<Artwork> artworks = new ArrayList<>();
        JSONArray jsonArtworks = response.getJSONArray("data");

        for (int i = 0; i < jsonArtworks.length(); i++) {
            try {
                artworks.add(artworkFactory(jsonArtworks.getJSONObject(i)));
            } catch (JSONException e) {
                Log.d(TAG, "Error parsing JSON object " + i);
            }
        }
        return artworks;
    }

    /** Dynamically retrieves next page by querying the total number of pages and current page number */
    private static void loadNextPageIfPresent(JSONObject response) throws JSONException {
        JSONObject pagination = response.getJSONObject("pagination");
        int currentPage = pagination.getInt("current_page");
        int totalPages = pagination.getInt("total_pages");

        if (currentPage < totalPages) {
            String queryUrl = buildUrl(
                    currentGalleryId,
                    artworkListActivity.getString(R.string.page_limit),
                    currentPage + 1);
            requestGET(queryUrl, listener, errorListener);
        }
    }

    /** Builds Artwork from JSONObject; missing or null fields become empty strings */
    private static Artwork artworkFactory(JSONObject json) throws JSONException {
        return new Artwork(
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
                getStringOrEmpty(json, "credit_line"));
    }

    /** isNull is true for a missing key AND for "key": null, so "null" never ends up on screen */
    private static String getStringOrEmpty(JSONObject json, String key) throws JSONException {
        return json.isNull(key) ? "" : json.getString(key);
    }

    private static void handleResults(ArtworkListActivity activity, JSONObject response) {
        try {
            List<Artwork> works = parseJsonArray(response);
            activity.updateData(works);
            loadNextPageIfPresent(response);
        } catch (JSONException e) {
            handleError(activity, null);
        }
    }

    /** Passes error toast handling to activity */
    private static void handleError(ArtworkListActivity activity, String errorMsg) {
        activity.downloadFailed(errorMsg);
    }
}

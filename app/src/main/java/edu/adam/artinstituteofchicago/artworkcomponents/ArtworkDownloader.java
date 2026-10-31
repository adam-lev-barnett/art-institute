package edu.adam.artinstituteofchicago.artworkcomponents;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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

import edu.adam.artinstituteofchicago.ArtworkDetailActivity;
import edu.adam.artinstituteofchicago.ArtworkListActivity;
import edu.adam.artinstituteofchicago.R;
import edu.adam.artinstituteofchicago.SearchActivity;

public class ArtworkDownloader {
    private static ArtworkListActivity artworkListActivity;
    private static final String TAG = "ArtworkDownloader";
    private static final String BASE_URL = "https://api.artic.edu/api/v1/artworks/search";
    private static final String FIELDS =
            "id,title,artist_display," +
            "date_display,medium_display," +
            "place_of_origin,image_id,dimensions," +
            "short_description,description,credit_line,galleryId," +
            // Not in the spec's field list, but galleryId returns nothing; this is the gallery's name
            "gallery_title";
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

        errorListener = err -> handleError(artworkListActivity,
                errorTitle(err), errorMessage(artworkListActivity, err));

        String queryUrl = buildUrl(currentGalleryId,
                artworkListActivity.getString(R.string.page_limit), FIRST_PAGE);

        requestGET(queryUrl, listener, errorListener);
    }

    /** Keyword search on titles and artists; only the first page (100 results), since a common
     *  keyword can match thousands of artworks and every page is another request against the rate limit.
     *  The API's "q" parameter matches every artwork for any keyword, so a multi_match query is used
     *  instead; it returns 0 results for nonsense terms, which the "No Results" dialog needs */
    public static void searchArtworks(SearchActivity activity, String keyword) {
        if (rq == null) rq = Volley.newRequestQueue(activity);

        String queryUrl = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter("query[multi_match][query]", keyword)
                .appendQueryParameter("query[multi_match][fields][0]", "title")
                .appendQueryParameter("query[multi_match][fields][1]", "artist_display")
                .appendQueryParameter("fields", FIELDS)
                .appendQueryParameter("limit", activity.getString(R.string.page_limit))
                .appendQueryParameter("page", String.valueOf(FIRST_PAGE))
                .build()
                .toString();

        // Local listeners remember which keyword they belong to, so a slow older search can be ignored
        Response.Listener<JSONObject> searchListener = response -> {
            try {
                activity.updateData(keyword, parseJsonArray(response));
            } catch (JSONException e) {
                activity.downloadFailed(null, null);
            }
        };

        Response.ErrorListener searchErrorListener = err ->
                activity.downloadFailed(errorTitle(err), errorMessage(activity, err));

        requestGET(queryUrl, searchListener, searchErrorListener);
    }

    /** One random public-domain artwork with an image, using the API's random_score query (POST).
     *  The seed is the current time, so every request gets a different artwork */
    public static void getRandomArtwork(ArtworkDetailActivity activity) {
        if (rq == null) rq = Volley.newRequestQueue(activity);

        JSONObject body;
        try {
            body = buildRandomQuery();
        } catch (JSONException e) {
            activity.downloadFailed(null, null);
            return;
        }

        Response.Listener<JSONObject> randomListener = response -> {
            try {
                List<Artwork> results = parseJsonArray(response);
                if (results.isEmpty()) {
                    activity.downloadFailed(null, null);
                } else {
                    activity.showRandomArtwork(results.get(0));
                }
            } catch (JSONException e) {
                activity.downloadFailed(null, null);
            }
        };

        Response.ErrorListener randomErrorListener = err ->
                activity.downloadFailed(errorTitle(err), errorMessage(activity, err));

        sendRequest(Request.Method.POST, BASE_URL, body, randomListener, randomErrorListener);
    }

    /** The API's random artwork query, built with org.json. Same fields as the other requests,
     *  so the detail screen gets everything it shows */
    private static JSONObject buildRandomQuery() throws JSONException {
        JSONArray filters = new JSONArray()
                .put(new JSONObject().put("term", new JSONObject().put("is_public_domain", true)))
                .put(fieldExists("image_id"))
                .put(fieldExists("thumbnail.width"))
                .put(fieldExists("thumbnail.height"));

        JSONObject functionScore = new JSONObject()
                .put("query", new JSONObject().put("bool", new JSONObject().put("filter", filters)))
                .put("boost_mode", "replace")
                .put("random_score", new JSONObject()
                        .put("field", "id")
                        .put("seed", System.currentTimeMillis()));

        return new JSONObject()
                .put("fields", FIELDS)
                .put("boost", false)
                .put("limit", 1)
                .put("query", new JSONObject().put("function_score", functionScore));
    }

    private static JSONObject fieldExists(String field) throws JSONException {
        return new JSONObject().put("exists", new JSONObject().put("field", field));
    }

    /** Volley's error class names the problem (NoConnectionError, TimeoutError...), like the spec's example */
    private static String errorTitle(VolleyError err) {
        return err.getClass().getSimpleName();
    }

    private static String errorMessage(Context context, VolleyError err) {
        if (err instanceof NoConnectionError) return context.getString(R.string.no_connection);

        String message = err.getMessage();
        if (err.networkResponse != null && err.networkResponse.data != null) {
            try {
                // Build error json object
                JSONObject errorJson = new JSONObject(new String(err.networkResponse.data));
                message = errorJson.getString("message");
            } catch (JSONException ignored) {
                // Keeps Volley's message; the dialog still shows
            }
        }
        return message;
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
        sendRequest(Request.Method.GET, query, null, onResponse, onError);
    }

    /** body is null for GET; for POST, JsonObjectRequest sends it as JSON */
    private static void sendRequest(int method, String url, JSONObject body,
                                    Response.Listener<JSONObject> onResponse,
                                    Response.ErrorListener onError) {
        JsonObjectRequest request =
                new JsonObjectRequest(method, url, body, onResponse, onError) {

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
                getStringOrEmpty(json, "credit_line"),
                getStringOrEmpty(json, "gallery_title"));
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
            handleError(activity, null, null);
        }
    }

    /** Passes error dialog handling to activity; null title/message use the activity's defaults */
    private static void handleError(ArtworkListActivity activity, String title, String errorMsg) {
        activity.downloadFailed(title, errorMsg);
    }
}

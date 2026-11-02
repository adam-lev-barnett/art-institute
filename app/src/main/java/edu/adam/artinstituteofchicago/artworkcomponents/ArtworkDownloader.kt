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
import java.util.StringJoiner;

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
            // API doesn't return anything for galleryId, so need to include gallery_title as identifier for artwork
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
                .appendQueryParameter("limit", limit)
                .appendQueryParameter("page", String.valueOf(page))
                .build()
                .toString();
    }

    /** Continuously downloads every artwork in the gallery one page at a time until exhausted/next page doesn't exist*/
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

    public static void search(SearchActivity activity, String keywords) {
        if (rq == null) rq = Volley.newRequestQueue(activity);
        if (keywords == null || keywords.isEmpty()) return;

        String[] querySplit = keywords.trim().split(" ");
        StringJoiner joiner = new StringJoiner(" ");

        // Wrapping terms in quotes bypasses API search bug returning all results
        for (String term : querySplit) {
            joiner.add("\"" + term + "\"");
        }

        String query = joiner.toString();

        String queryUrl = Uri.parse("https://api.artic.edu/api/v1/artworks/search")
                .buildUpon()
                .appendQueryParameter("q", query)
                .appendQueryParameter("fields", FIELDS)
                .appendQueryParameter("limit", activity.getString(R.string._100))
                .appendQueryParameter("page", String.valueOf(FIRST_PAGE))
                .build()
                .toString();

        // Local listeners remember which keyword they belong to, so a slow older search can be ignored
        Response.Listener<JSONObject> searchListener = response -> {
            try {
                activity.updateData(keywords, parseJsonArray(response));
            } catch (JSONException e) {
                /* Multiple activities use this method, so passing null for title and errorMsg
                *  Passing null for either field reverts to the default message for the calling activity */
                activity.downloadFailed(null, null);
            }
        };

        Response.ErrorListener searchErrorListener = err ->
                activity.downloadFailed(errorTitle(err), errorMessage(activity, err));

        requestGET(queryUrl, searchListener, searchErrorListener);
    }

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

    /** Builds query specifically for random artwork fetch using API's recommended format.
     * Documentation recommends using current time in millis for random seed generation */
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

    /** Volley's error class provides error alert dialog's title */
    private static String errorTitle(VolleyError err) {
        return err.getClass().getSimpleName();
    }

    private static String errorMessage(Context context, VolleyError err) {
        if (err instanceof NoConnectionError) return context.getString(R.string.no_connection);

        String message = err.getMessage();
        if (err.networkResponse != null && err.networkResponse.data != null) {
            try {
                JSONObject errorJson = new JSONObject(new String(err.networkResponse.data));
                message = errorJson.getString("message");
            } catch (JSONException ignored) {
                // Volley message is passed to alert dialog in calling activity
            }
        }
        return message;
    }

    /** Builds the image URL with required headers.
     *  Use THUMBNAIL_SIZE in Recyclers, FULL_SIZE (or FALLBACK_SIZE) in ArtworkDetailAcrivity */
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

    /** @param method Body is null for GET (used on everything except random fetch).
     *                 Random uses POST (API says POST is needed for request) and needs the body. */
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

    /** Retrieves next page by querying the total number of pages and current page number */
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

    /** Passes error dialog handling to activity. Null title/message use the activity defaults */
    private static void handleError(ArtworkListActivity activity, String title, String errorMsg) {
        activity.downloadFailed(title, errorMsg);
    }
}

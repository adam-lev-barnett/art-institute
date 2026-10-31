package edu.adam.artinstituteofchicago.gallerycomponents;

import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.adam.artinstituteofchicago.GalleryActivity;
import edu.adam.artinstituteofchicago.R;

public class GalleryDownloader {
    private static GalleryActivity galleryActivity;
    private static final String TAG = "GalleryDownloader";
    private static final String BASE_URL = "https://api.artic.edu/api/v1/galleries";
    private static final String FIELDS = "id,title,number,floor,is_closed,source_updated_at";
    private static String startingPage = "1";
    private static RequestQueue rq;
    private static Response.Listener<JSONObject> listener;
    private static Response.ErrorListener errorListener;

    /** Gallery thumbnails come from the first artwork in each gallery, so these hit the artworks endpoint */
    private static final String ARTWORK_SEARCH_URL = "https://api.artic.edu/api/v1/artworks/search";
    private static final String THUMBNAIL_FIELDS = "id,image_id";
    private static final String THUMBNAIL_LIMIT = "1";

    private static String buildUrl() {
        return Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter("fields", FIELDS)
                // How many items per page
                .appendQueryParameter("limit", galleryActivity.getString(R.string.page_limit))
                // start at page one
                .appendQueryParameter("page", startingPage)
                .build()
                .toString();
    }

    private static String buildThumbnailUrl(long galleryId) {
        return Uri.parse(ARTWORK_SEARCH_URL).buildUpon()
                .appendQueryParameter("query[term][gallery_id]", String.valueOf(galleryId))
                .appendQueryParameter("fields", THUMBNAIL_FIELDS)
                // Only the first artwork is needed
                .appendQueryParameter("limit", THUMBNAIL_LIMIT)
                .appendQueryParameter("page", startingPage)
                .build()
                .toString();
    }

    public static void getSourceData(GalleryActivity activity) {
        galleryActivity = activity;
        if (rq == null) rq = Volley.newRequestQueue(galleryActivity);

        listener = response -> {
            try {
                handleResults(galleryActivity, response.toString());
            } catch (JSONException e) {
                Toast.makeText(galleryActivity, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        };

        errorListener = err -> {
            String message = err.getMessage();
            if (err.networkResponse != null && err.networkResponse.data != null) {
                try {
                    // Build error json object
                    JSONObject errorJson = new JSONObject(new String(err.networkResponse.data));
                    message = errorJson.getString("message");
                } catch (JSONException ignored) {
                    // The volley error handler already sends out the toast
                }
            }
            handleError(galleryActivity, message);
        };

        String queryUrl = buildUrl();

        requestGET(queryUrl, listener, errorListener);
    }

    /** Looks up the first artwork in a gallery and saves its image id on the Gallery for the thumbnail */
    public static void getGalleryThumbnail(Gallery gallery, int position) {
        if (rq == null) rq = Volley.newRequestQueue(galleryActivity);

        // Async, so each one has to remember which gallery and row it belongs to
        Response.Listener<JSONObject> thumbnailListener = response -> {
            try {
                JSONArray data = response.getJSONArray("data");
                // Empty gallery: leave the image id blank so the default image shows
                if (data.length() == 0) return;

                JSONObject firstArtwork = data.getJSONObject(0);
                if (!firstArtwork.isNull("image_id")) {
                    gallery.setImageId(firstArtwork.getString("image_id"));
                    galleryActivity.updateGalleryThumbnail(position);
                }
            } catch (JSONException e) {
                Log.d(TAG, "getGalleryThumbnail: " + e.getMessage());
            }
        };

        // Failed thumbnail keeps the default image and retries the next time its row is bound
        Response.ErrorListener thumbnailErrorListener = err -> {
            gallery.setThumbnailFetched(false);
            Log.d(TAG, "getGalleryThumbnail: " + gallery.getTitle() + " " + err.getMessage());
        };

        requestGET(buildThumbnailUrl(gallery.getId()), thumbnailListener, thumbnailErrorListener);
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

    private static List<Gallery> parseJsonArray(String jsonString) throws JSONException {

        List<Gallery> galleries = new ArrayList<>();

        try {
            JSONObject response = new JSONObject(jsonString);
            JSONArray jsonGalleries = response.getJSONArray("data");

            for (int i = 0; i < jsonGalleries.length(); i++) {
                try {
                    Gallery gallery = galleryFactory(jsonGalleries.getJSONObject(i));
                    galleries.add(gallery);
                } catch (Exception e) {
                    Log.d(TAG, "Error parsing JSON object " + i);
                }
            }
            return galleries;

        } catch (JSONException e) {
            Toast.makeText(galleryActivity, "Error: " + "could not retrieve galleries", Toast.LENGTH_LONG).show();
        }

        return null;
    }

    private static void loadNextPageIfPresent(String jsonString) {
        try {
            JSONObject response = new JSONObject(jsonString);
            JSONObject pagination = response.getJSONObject("pagination");
            if (pagination.has("next_url")) requestGET(pagination.getString("next_url"), listener, errorListener);

        } catch (JSONException e) {
            handleError(galleryActivity, "Error interpreting pagination");
        }
    }

    /** Builds Gallery from JSONObject with available fields; returns empty strings if field not available*/
    private static Gallery galleryFactory(JSONObject json) throws JSONException {
        long id = json.getLong("id");
        String title = "";
        String number = "";
        String floor = "";
        boolean is_closed = json.optBoolean("is_closed", false);
        String source_updated_at = "";

        if (json.has("title")) title = json.getString("title");
        if (json.has("number")) number = json.getString("number");
        if (json.has("floor")) floor = json.getString("floor");
        if (json.has("source_updated_at")) source_updated_at = json.getString("source_updated_at");

        return new Gallery(id, title, number, floor, is_closed, source_updated_at);
    }

    /** Only parses the data if it exists */
    private static void handleResults(GalleryActivity activity, String json) throws JSONException {
        if (json == null) {
            handleError(activity, null);
            return;
        }

        List<Gallery> galleryList = parseJsonArray(json);

        if (galleryList != null) activity.runOnUiThread(() -> {
            activity.updateData(galleryList);
        });

        loadNextPageIfPresent(json);
    }

    /** Passes error toast handling to activity */
    private static void handleError(GalleryActivity activity, String errorMsg) {
        activity.runOnUiThread(() -> activity.downloadFailed(errorMsg));
    }

}

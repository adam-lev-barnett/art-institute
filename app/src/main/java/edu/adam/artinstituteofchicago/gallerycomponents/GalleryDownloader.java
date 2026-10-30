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
    private static RequestQueue requestQueue;
    private static final String TAG = "GalleryDownloader";
    private static final String BASE_URL = "https://api.artic.edu/api/v1/galleries";
    private static final String FIELDS = "id,title,number,floor,is_closed,source_updated_at";

    private static String buildUrl(int page) {
        return Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter("fields", FIELDS)
                .appendQueryParameter("limit", galleryActivity.getString(R.string.page_limit))
                .appendQueryParameter("page", String.valueOf(page))
                .build()
                .toString();
    }

    public static void getSourceData(GalleryActivity activity) {
        galleryActivity = activity;
        requestQueue = Volley.newRequestQueue(galleryActivity);

        int firstPage = Integer.parseInt(activity.getString(R.string.starting_page_number));
        requestPage(firstPage);
    }

    // Each response requests the next page until the end of the API's list
    private static void requestPage(int page) {
        Response.Listener<JSONObject> listener =
                response -> {
                    try {
                        handleResults(galleryActivity, response.toString());
                    } catch (JSONException e) {
                        Toast.makeText(galleryActivity, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                };

        Response.ErrorListener error = err -> {
            String message = err.getMessage();
            if (err.networkResponse != null && err.networkResponse.data != null) {
                try {
                    JSONObject errorJson = new JSONObject(new String(err.networkResponse.data));
                    message = errorJson.getString("message");
                } catch (JSONException ignored) {
                    // The volley error handler already sends out the toast
                }
            }
            handleError(galleryActivity, message);
        };

        String queryUrl = buildUrl(page);

        JsonObjectRequest request =
                new JsonObjectRequest(Request.Method.GET, queryUrl, null, listener, error) {

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

        requestQueue.add(request);
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

    private static Gallery galleryFactory(JSONObject json) throws JSONException {
        long id = json.getLong("id");
        String name = getStringOrEmpty(json, "title");
        String number = getStringOrEmpty(json, "number");
        String floor = getStringOrEmpty(json, "floor");
        boolean closed = json.optBoolean("is_closed", false);
        String lastUpdate = getStringOrEmpty(json, "source_updated_at");

        return new Gallery(id, name, number, floor, closed, lastUpdate);
    }

    // isNull covers both a missing key and a JSON null (optString would return "null" for the latter)
    private static String getStringOrEmpty(JSONObject json, String key) throws JSONException {
        return json.isNull(key) ? "" : json.getString(key);
    }

    private static void handleResults(GalleryActivity activity, String json) throws JSONException {
        if (json == null) {
            handleError(activity, null);
            return;
        }

        List<Gallery> galleryList = parseJsonArray(json);

        if (galleryList != null) activity.runOnUiThread(() -> {
            activity.updateData(galleryList);
        });

        JSONObject pagination = new JSONObject(json).getJSONObject("pagination");
        int currentPage = pagination.getInt("current_page");
        int totalPages = pagination.getInt("total_pages");

        if (currentPage < totalPages) {
            requestPage(currentPage + 1);
        }
    }

    private static void handleError(GalleryActivity activity, String errorMsg) {
        activity.runOnUiThread(() -> activity.downloadFailed(errorMsg));
    }


}

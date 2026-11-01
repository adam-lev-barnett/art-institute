package edu.adam.artinstituteofchicago.artworkcomponents;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.adam.artinstituteofchicago.R;
import edu.adam.artinstituteofchicago.uicomponents.ErrorDialog;

/** Utility class/structure for easy persistence of favorites across activities without repeated loading */
public abstract class UserFavorites {

    private static final String TAG = "UserFavorites";
    private static final Map<Long, Artwork> faves = new HashMap<>();

    /** Only attempts to load faves once.
     * If file-loading resulted in an initial error, still becomes true*/
    private static boolean loadAttempted = false;

    public static void addFavorite(Artwork art, Activity activity) {
        if (art != null) {
            faves.put(art.getId(), art);
            saveFaves(activity);
        }
    }

    /** @throws JsonSyntaxException if the file exists but isn't valid JSON;
     * the calling activity catches it so it can show the error */
    public static void loadFaves(Activity activity) {
        loadAttempted = true;
        try {
            Context context = activity.getApplicationContext();
            String filename = context.getString(R.string.fave_file);
            InputStream is = context.openFileInput(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            if (sb.length() == 0) {
                return;
            }

            Gson gson = new Gson();
            Artwork[] artworks = gson.fromJson(sb.toString(), Artwork[].class);

            for (Artwork art : artworks) {
                if (art != null) faves.put(art.getId(), art);
            }

        } catch (IOException ignore) {
            // Not an error if no actual favorites;
            // will be handled with default message on favorites view
        }
    }

    private static void saveFaves(Activity activity) {
        Context context = activity.getApplicationContext();

        try {
            FileOutputStream fos = context.
                    openFileOutput(context.getString(R.string.fave_file), Context.MODE_PRIVATE);
            List<Artwork> faveListOut = new ArrayList<>(faves.values());
            PrintWriter printWriter = new PrintWriter(fos);
            printWriter.print(new Gson().toJson(faveListOut));
            printWriter.close();
            fos.close();
        } catch (Exception e) {
            Log.d(TAG, "saveFaves: " + e.getMessage());
            ErrorDialog.show(activity, activity.getString(R.string.favorites_error),
                    activity.getString(R.string.favorites_save_failed));
        }
    }

    public static boolean isFavorite(Artwork art) {
        return faves.containsKey(art.getId());
    }

    public static void removeFave(Artwork art, Activity activity) {
        faves.remove(art.getId());
        saveFaves(activity);
    }


    public static List<Artwork> getFaves() {
        List<Artwork> sorted = new ArrayList<>(faves.values());
        // sorts by name, per-spec
        sorted.sort((a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()));
        return sorted;
    }

    public static boolean loadAttempted() {
        return loadAttempted;
    }
}

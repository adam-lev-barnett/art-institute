package edu.adam.artinstituteofchicago.artworkcomponents

import android.app.Activity
import android.util.Log
import com.google.gson.Gson
import edu.adam.artinstituteofchicago.R
import edu.adam.artinstituteofchicago.uicomponents.ErrorDialog.show
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.PrintWriter
import java.nio.charset.StandardCharsets

/** Utility class/structure for easy persistence of favorites across activities without repeated loading  */
object UserFavorites {
    private const val TAG = "UserFavorites"
    private val faves: MutableMap<Long?, Artwork?> = HashMap<Long?, Artwork?>()

    /** Only attempts to load faves once.
     * If file-loading resulted in an initial error, still becomes true */
    private var loadAttempted = false

    fun addFavorite(art: Artwork?, activity: Activity) {
        if (art != null) {
            faves[art.id] = art
            saveFaves(activity)
        }
    }

    /** @throws com.google.gson.JsonSyntaxException if the file exists but isn't valid JSON;
     * the calling activity catches it so it can show the error
     */
    fun loadFaves(activity: Activity) {
        loadAttempted = true
        try {
            val context = activity.applicationContext
            val filename = context.getString(R.string.fave_file)
            val `is`: InputStream? = context.openFileInput(filename)
            val reader = BufferedReader(InputStreamReader(`is`, StandardCharsets.UTF_8))

            val sb = StringBuilder()
            var line: String?
            while ((reader.readLine().also { line = it }) != null) {
                sb.append(line)
            }
            reader.close()

            if (sb.isEmpty()) {
                return
            }

            val gson = Gson()
            val artworks =
                gson.fromJson<Array<Artwork>>(sb.toString(), Array<Artwork>::class.java)

            for (art in artworks) {
                faves[art.id] = art
            }
        } catch (ignore: IOException) {
            // Not an error if no actual favorites;
            // will be handled with default message on favorites view
        }
    }

    private fun saveFaves(activity: Activity) {
        val context = activity.applicationContext

        try {
            val fos = context.openFileOutput( (context.getString(R.string.fave_file)), android.content.Context.MODE_PRIVATE)
            val faveListOut: MutableList<Artwork?> = ArrayList<Artwork?>(faves.values)
            val printWriter = PrintWriter(fos)
            printWriter.print(Gson().toJson(faveListOut))
            printWriter.close()
            fos.close()
        } catch (e: Exception) {
            Log.d(TAG, "saveFaves: " + e.message)
            show(
                activity, activity.getString(R.string.favorites_error),
                activity.getString(R.string.favorites_save_failed)
            )
        }
    }

    fun isFavorite(art: Artwork): Boolean {
        return faves.containsKey(art.id)
    }

    fun removeFave(art: Artwork, activity: Activity) {
        faves.remove(art.id)
        saveFaves(activity)
    }


    fun getFaves(): MutableList<Artwork?> {
        val sorted: MutableList<Artwork?> = ArrayList<Artwork?>(faves.values)
        // sorts by name, per-spec
        sorted.sortWith(Comparator { a: Artwork?, b: Artwork? ->
                    a!!.title.compareTo(b!!.title, ignoreCase = true)
                })
        return sorted
    }

    fun loadAttempted(): Boolean {
        return loadAttempted
    }
}

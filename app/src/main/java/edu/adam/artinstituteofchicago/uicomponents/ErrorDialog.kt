package edu.adam.artinstituteofchicago.uicomponents

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import edu.adam.artinstituteofchicago.R

object ErrorDialog {
    /** Global error AlertDialog to match spec  */
    @JvmStatic
    fun show(activity: Activity, title: String?, message: String?) {
        show(activity, R.drawable.alert_logo, title, message)
    }

    /** Same dialog with different icons per caller  */
    fun show(activity: Activity, icon: Int, title: String?, message: String?) {
        // Downloads can fail after the user already left the screen;
        // showing a dialog on a closed activity crashes the app
        if (activity.isFinishing || activity.isDestroyed) return

        AlertDialog.Builder(activity)
            .setIcon(icon)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.ok, null)
            .show()
    }
}

package edu.adam.artinstituteofchicago.uicomponents;

import android.app.Activity;

import androidx.appcompat.app.AlertDialog;

import edu.adam.artinstituteofchicago.R;

public class ErrorDialog {

    /** Global error AlertDialog to match spec */
    public static void show(Activity activity, String title, String message) {
        show(activity, R.drawable.alert_logo, title, message);
    }

    /** Same dialog with different icons per caller */
    public static void show(Activity activity, int icon, String title, String message) {
        // Downloads can fail after the user already left the screen;
        // showing a dialog on a closed activity crashes the app
        if (activity.isFinishing() || activity.isDestroyed()) return;

        new AlertDialog.Builder(activity)
                .setIcon(icon)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .show();
    }
}

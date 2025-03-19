package com.example.expensetracker;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import androidx.core.content.ContextCompat;
import com.google.android.material.snackbar.Snackbar;
import android.graphics.Color;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import android.view.ViewGroup;
import android.os.Handler;
import android.os.Looper;

/**
 * TutorialHelper: Manages the app's interactive tutorial experience
 * Features:
 * - Shows first-time user tutorial
 * - Highlights key UI elements with explanations
 * - Provides step-by-step guidance through app features
 * - Persists tutorial completion status
 * - Customizes tutorial UI elements for better visibility
 */
public class TutorialHelper {
    // Configuration constants
    private static final String PREFS_NAME = "tutorial_prefs";
    private static final String KEY_TUTORIAL_SHOWN = "tutorial_shown";

    // Instance variables
    private final Activity activity;
    private final Handler handler;
    private int currentStep = 0;

    /**
     * Constructor initializes the helper with the host activity
     * @param activity The activity where tutorial will be shown
     */
    public TutorialHelper(Activity activity) {
        this.activity = activity;
        this.handler = new Handler(Looper.getMainLooper());
    }

    /**
     * Checks if tutorial should be shown and starts it if needed
     * Uses SharedPreferences to track if tutorial was previously shown
     */
    public void showTutorialIfNeeded() {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (!prefs.getBoolean(KEY_TUTORIAL_SHOWN, false)) {
            startTutorial();
            prefs.edit().putBoolean(KEY_TUTORIAL_SHOWN, true).apply();
        }
    }

    /**
     * Initiates the tutorial sequence by showing welcome message
     */
    private void startTutorial() {
        showWelcomeMessage();
    }

    /**
     * Displays the initial welcome message using a Snackbar
     * Includes a "Next" button to proceed with the tutorial
     */
    private void showWelcomeMessage() {
        Snackbar snackbar = Snackbar.make(
                activity.findViewById(android.R.id.content),
                R.string.tutorial_welcome,
                Snackbar.LENGTH_INDEFINITE
        );

        customizeSnackbar(snackbar);
        snackbar.setAction(R.string.tutorial_next, v -> showNextTip());
        snackbar.show();
    }

    /**
     * Controls the tutorial flow by showing the next tip in sequence
     * Handles different tutorial steps:
     * 1. Income feature
     * 2. Expense feature
     * 3. Visualization feature
     * 4. Theme customization
     * 5. Notification settings
     * 6. Data export feature
     */
    private void showNextTip() {
        currentStep++;
        switch (currentStep) {
            case 1:
                highlightView(R.id.incomeCard, R.string.tutorial_income);
                break;
            case 2:
                highlightView(R.id.expenseCard, R.string.tutorial_expense);
                break;
            case 3:
                highlightView(R.id.visualizeCard, R.string.tutorial_visualization);
                break;
            case 4:
                showMenuTip(R.string.tutorial_theme);
                break;
            case 5:
                showMenuTip(R.string.tutorial_notifications);
                break;
            case 6:
                showFinalTip();
                break;
        }
    }

    /**
     * Highlights a specific view with an explanation message
     * Temporarily increases view elevation for visual emphasis
     * @param viewId ID of the view to highlight
     * @param messageId Resource ID of the explanation message
     */
    private void highlightView(int viewId, int messageId) {
        View view = activity.findViewById(viewId);
        if (view != null) {
            view.setElevation(16f); // Temporarily increase elevation

            Snackbar snackbar = Snackbar.make(
                    activity.findViewById(android.R.id.content),
                    messageId,
                    Snackbar.LENGTH_INDEFINITE
            );

            customizeSnackbar(snackbar);
            snackbar.setAction(currentStep < 6 ? R.string.tutorial_next : R.string.tutorial_finish,
                    v -> {
                        view.setElevation(1f); // Reset elevation
                        showNextTip();
                    });
            snackbar.show();
        }
    }

    /**
     * Shows a tip about menu-based features
     * Used for explaining theme and notification settings
     * @param messageId Resource ID of the tip message
     */
    private void showMenuTip(int messageId) {
        Snackbar snackbar = Snackbar.make(
                activity.findViewById(android.R.id.content),
                messageId,
                Snackbar.LENGTH_INDEFINITE
        );

        customizeSnackbar(snackbar);
        snackbar.setAction(currentStep < 6 ? R.string.tutorial_next : R.string.tutorial_finish,
                v -> showNextTip());
        snackbar.show();
    }

    /**
     * Shows the final tutorial tip about data export
     * Completes the tutorial sequence
     */
    private void showFinalTip() {
        Snackbar snackbar = Snackbar.make(
                activity.findViewById(android.R.id.content),
                R.string.tutorial_export,
                Snackbar.LENGTH_INDEFINITE
        );

        customizeSnackbar(snackbar);
        snackbar.setAction(R.string.tutorial_finish, v -> {});
        snackbar.show();
    }

    /**
     * Customizes the appearance of tutorial Snackbars
     * Enhances visibility and readability of tutorial messages
     * @param snackbar The Snackbar to customize
     */
    private void customizeSnackbar(Snackbar snackbar) {
        View snackbarView = snackbar.getView();
        snackbarView.setElevation(8f);
        TextView messageView = snackbarView.findViewById(
                com.google.android.material.R.id.snackbar_text
        );
        if (messageView != null) {
            messageView.setMaxLines(3);
            messageView.setTextColor(Color.WHITE);
        }
    }
}
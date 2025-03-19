package com.example.expensetracker;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.StringRes;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import com.google.android.material.snackbar.Snackbar;

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
    private static final String PREFS_NAME = "tutorial_prefs";
    private static final String KEY_TUTORIAL_SHOWN = "tutorial_shown";
    private final Activity activity;
    private final Handler handler;
    private int currentStep = 0;

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
        boolean tutorialShown = prefs.getBoolean(KEY_TUTORIAL_SHOWN, false);

        if (!tutorialShown) {
            startTutorial();
            prefs.edit().putBoolean(KEY_TUTORIAL_SHOWN, true).apply();
        }
    }

    /**
     * Initiates the tutorial sequence by showing welcome message
     */
    public void startTutorial() {
        handler.postDelayed(this::showWelcomeMessage, 1000);
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

        snackbar.setAction(R.string.tutorial_next, view -> {
            currentStep = 1;
            showNextTip();
        });

        customizeSnackbar(snackbar);
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
        switch (currentStep) {
            case 1:
                highlightView(R.id.fabAddIncome, R.string.tutorial_income);
                break;
            case 2:
                highlightView(R.id.fabAddExpense, R.string.tutorial_expense);
                break;
            case 3:
                // Use menu item for visualization instead of a card
                showMenuTip(R.string.tutorial_visualization);
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
    private void highlightView(int viewId, @StringRes int messageId) {
        View targetView = activity.findViewById(viewId);
        if (targetView == null) {
            // Skip this step if view not found
            currentStep++;
            showNextTip();
            return;
        }

        // Highlight effect
        float originalElevation = targetView.getElevation();
        targetView.setElevation(originalElevation + 10f);

        Snackbar snackbar = Snackbar.make(
                activity.findViewById(android.R.id.content),
                messageId,
                Snackbar.LENGTH_INDEFINITE
        );

        snackbar.setAction(R.string.tutorial_next, view -> {
            targetView.setElevation(originalElevation);
            currentStep++;
            showNextTip();
        });

        customizeSnackbar(snackbar);
        snackbar.show();
    }

    /**
     * Shows a tip about menu-based features
     * Used for explaining theme and notification settings
     * @param messageId Resource ID of the tip message
     */
    private void showMenuTip(@StringRes int messageId) {
        Snackbar snackbar = Snackbar.make(
                activity.findViewById(android.R.id.content),
                messageId,
                Snackbar.LENGTH_INDEFINITE
        );

        snackbar.setAction(R.string.tutorial_next, view -> {
            currentStep++;
            showNextTip();
        });

        customizeSnackbar(snackbar);
        snackbar.show();
    }

    /**
     * Shows the final tutorial tip about data export
     * Completes the tutorial sequence
     */
    private void showFinalTip() {
        Snackbar snackbar = Snackbar.make(
                activity.findViewById(android.R.id.content),
                R.string.tutorial_complete,
                Snackbar.LENGTH_INDEFINITE
        );

        snackbar.setAction(R.string.tutorial_finish, view -> {
            // Tutorial completed
        });

        customizeSnackbar(snackbar);
        snackbar.show();
    }

    /**
     * Customizes the appearance of tutorial Snackbars
     * Enhances visibility and readability of tutorial messages
     * @param snackbar The Snackbar to customize
     */
    private void customizeSnackbar(Snackbar snackbar) {
        ViewGroup snackbarView = (ViewGroup) snackbar.getView();
        snackbarView.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorPrimary));
        
        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setMaxLines(5);
        
        // Position at the top of the screen
        ViewGroup.LayoutParams params = snackbarView.getLayoutParams();
        if (params instanceof CoordinatorLayout.LayoutParams) {
            ((CoordinatorLayout.LayoutParams) params).gravity = android.view.Gravity.TOP;
            snackbarView.setLayoutParams(params);
        }
    }
}
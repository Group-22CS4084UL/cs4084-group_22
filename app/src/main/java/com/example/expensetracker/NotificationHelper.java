package com.example.expensetracker;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import java.util.Calendar;
import android.util.Log;
import com.example.expensetracker.DatabaseHelper;

/**
 * NotificationHelper: Manages daily expense tracking reminders
 * Features:
 * - Creates and manages notification channels
 * - Schedules daily reminders at 8:00 PM
 * - Handles notification display and click actions
 * - Supports Android's notification permission system
 * - Provides methods to cancel scheduled notifications
 */
public class NotificationHelper extends BroadcastReceiver {
    // Notification configuration constants
    private static final String CHANNEL_ID = "expense_tracker_channel";
    private static final String CHANNEL_NAME = "Expense Tracker";
    private static final String CHANNEL_DESC = "Daily expense tracking reminders";
    private static final int NOTIFICATION_ID = 1;
    private static final String ACTION_SHOW_NOTIFICATION = "com.example.expensetracker.SHOW_NOTIFICATION";
    private static final String TAG = "NotificationHelper";

    /**
     * Handles broadcast events for showing notifications
     * Called by the AlarmManager when it's time to show the daily reminder
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_SHOW_NOTIFICATION.equals(intent.getAction())) {
            createNotificationChannel(context);
            showNotification(context);
        }
    }

    /**
     * Creates a notification channel for Android O and above
     * This is required for notifications to work on newer Android versions
     */
    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESC);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 200, 500});

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created");
            }
        }
    }

    /**
     * Displays the actual notification to the user
     * Creates a pending intent to open ExpenseActivity when clicked
     */
    private void showNotification(Context context) {
        Log.d(TAG, "Showing notification now");
        Intent intent = new Intent(context, ExpenseActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Track Your Expenses")
            .setContentText("Don't forget to log your daily expenses!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(new long[]{0, 500, 200, 500})
            .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            Log.d(TAG, "Attempting to show notification");
            notificationManager.notify(NOTIFICATION_ID, builder.build());
            Log.d(TAG, "Notification sent successfully");
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to show notification: " + e.getMessage());
        }
    }

    /**
     * Shows an enhanced notification with financial summary
     */
    public static void showWelcomeNotification(Context context) {
        Log.d(TAG, "Showing welcome notification");
        createNotificationChannel(context);

        // Get financial data
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        double totalIncome = dbHelper.getTotalIncome();
        double totalExpense = dbHelper.getTotalExpense();
        double balance = totalIncome - totalExpense;

        // Create notification style with expanded layout
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle()
            .setBigContentTitle("Your Financial Summary")
            .bigText(String.format("Current Balance: $%.2f\nTotal Income: $%.2f\nTotal Expenses: $%.2f\n\nTap to track your expenses!", 
                balance, totalIncome, totalExpense));

        // Create main intent
        Intent mainIntent = new Intent(context, ExpenseActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            mainIntent, 
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Create action intents
        Intent incomeIntent = new Intent(context, IncomeActivity.class);
        incomeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent incomePendingIntent = PendingIntent.getActivity(
            context,
            1,
            incomeIntent,
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Build enhanced notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Welcome to Expense Tracker")
            .setContentText(String.format("Current Balance: $%.2f", balance))
            .setStyle(bigTextStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(new long[]{0, 500, 200, 500})
            .setContentIntent(mainPendingIntent)
            // Add action buttons
            .addAction(R.drawable.ic_launcher_foreground, "Add Income", incomePendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            Log.d(TAG, "Attempting to show enhanced notification");
            notificationManager.notify(NOTIFICATION_ID, builder.build());
            Log.d(TAG, "Enhanced notification sent successfully");
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to show notification: " + e.getMessage());
        }
    }

    /**
     * Schedules a daily notification reminder at 8:00 PM
     * Uses different scheduling methods based on Android version for optimal battery performance
     */
    public static void scheduleNotification(Context context) {
        Log.d(TAG, "Starting notification scheduling");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationHelper.class);
        intent.setAction(ACTION_SHOW_NOTIFICATION);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Set alarm for 10 seconds from now for immediate testing
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 10); // Schedule for 10 seconds from now
        
        Log.d(TAG, "Scheduling notification for: " + calendar.getTime());

        if (alarmManager != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Log.d(TAG, "Using setExactAndAllowWhileIdle for Android M and above");
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                    );
                } else {
                    Log.d(TAG, "Using setExact for older Android versions");
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                    );
                }
                Log.d(TAG, "Notification scheduled successfully");
            } catch (Exception e) {
                Log.e(TAG, "Failed to schedule notification: " + e.getMessage());
            }
        }
    }

    /**
     * Cancels all scheduled notifications
     * Used when user disables notifications or during app cleanup
     */
    public static void cancelNotifications(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationHelper.class);
        intent.setAction(ACTION_SHOW_NOTIFICATION);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}

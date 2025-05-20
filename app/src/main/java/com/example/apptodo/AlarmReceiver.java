package com.example.apptodo;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "task_alarms";
    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String taskTitle = intent.getStringExtra("task_title");
        String taskDescription = intent.getStringExtra("task_description");
        int notificationId = intent.getIntExtra("notification_id", 0);

        // Check for POST_NOTIFICATIONS permission if running on Android 13 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Notification permission not granted. Cannot show notification for task: " + taskTitle);
                // Optionally, you might want to log this event or handle it in another way
                return; // Stop here, as we can't show the notification
            }
        }

        // Build a notification to display when the alarm goes off
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_ckeck)
                .setContentTitle("Task Reminder: " + taskTitle)
                .setContentText(taskDescription)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true); // Dismiss the notification when tapped

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.notify(notificationId, builder.build()); // Use the retrieved notification ID
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException while trying to show notification: " + e.getMessage());
            // Handle the exception, perhaps by logging it or informing the user later
        }
    }
}
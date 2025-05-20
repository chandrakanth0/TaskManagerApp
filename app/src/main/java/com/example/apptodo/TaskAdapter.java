package com.example.apptodo;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private static final String CHANNEL_ID = "task_deleted_channel";
    private static final String PREF_DONE_TODAY = "done_today_task_"; // Prefix for shared preference key
    private ArrayList<Task> taskList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private Context context;

    public TaskAdapter(Context context, ArrayList<Task> tasks) {
        this.context = context;
        this.taskList = tasks;
        createNotificationChannel(context);
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Task Deleted Notifications";
            String description = "Notification when a task is deleted";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setSound(null, null); //remove sound
            channel.enableVibration(true); // explicitly enable vibration
            channel.setVibrationPattern(new long[]{0, 250, 250, 250});
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d("TaskAdapter", "Notification channel created");
            } else {
                Log.e("TaskAdapter", "NotificationManager is null");
            }
        } else {
            Log.d("TaskAdapter", "Running on pre-Oreo, no channel needed");
        }
    }

    private void showTaskDeletedNotification(Context context, String taskTitle) {
        Log.d("TaskAdapter", "showTaskDeletedNotification called with title: " + taskTitle);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // **Request Permission Here** - Consider handling the permission request more gracefully in your Activity
                ActivityCompat.requestPermissions(
                        (android.app.Activity) context,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        101 // Different request code
                );
                Log.w("TaskAdapter", "Notification permission not granted, requesting permission");
                return; // IMPORTANT: Return after requesting permission. The notification will be sent in onRequestPermissionsResult
            }
        }
        // Permission is granted (or on pre-Tiramisu), show the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_tasks)
                .setContentTitle("Task Deleted!")
                .setContentText("You have deleted the task: " + taskTitle)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVibrate(new long[]{0, 250, 250, 250})
                .setFullScreenIntent(null, true);

        builder.setAutoCancel(true);
        builder.setLights(Color.BLUE, 1000, 1000);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (notificationManager != null) {
            notificationManager.notify(generateNotificationId(taskTitle), builder.build());
            Log.d("TaskAdapter", "Notification sent with ID: " + generateNotificationId(taskTitle));
        } else {
            Log.e("TaskAdapter", "NotificationManagerCompat is null");
            Toast.makeText(context, "Failed to show notification", Toast.LENGTH_SHORT).show();
        }
    }

    private int generateNotificationId(String title) {
        int id = title.hashCode();
        Log.d("TaskAdapter", "Generated notification ID: " + id + " for title: " + title);
        return id;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, descriptionText, startDateText, dueDateText, timeText, remainingDaysText;
        Button deleteButton, completeButton, doneTodayButton;
        String currentTaskId;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            descriptionText = itemView.findViewById(R.id.descriptionText);
            startDateText = itemView.findViewById(R.id.startDateText);
            dueDateText = itemView.findViewById(R.id.dueDateText);
            timeText = itemView.findViewById(R.id.timeText);
            remainingDaysText = itemView.findViewById(R.id.remainingDaysText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            completeButton = itemView.findViewById(R.id.completeButton);
            doneTodayButton = itemView.findViewById(R.id.buttonDoneToday); // Initialize the new button

            doneTodayButton.setOnClickListener(v -> markAsDoneToday(currentTaskId));
        }

        public void bind(Task task) {
            currentTaskId = task.getDocumentId();
            titleText.setText(task.getTitle());
            descriptionText.setText(task.getDescription());
            startDateText.setText(task.getStartDate());
            dueDateText.setText(task.getDueDate());
            timeText.setText(task.getTime());

            // Calculate and display remaining days
            try {
                Date dueDate = dateFormat.parse(task.getDueDate());
                Date currentDate = new Date();
                long timeDifference = dueDate.getTime() - currentDate.getTime();
                Long remainingDaysCalculated = TimeUnit.DAYS.convert(timeDifference, TimeUnit.MILLISECONDS);
                task.setRemainingDays(remainingDaysCalculated); // Ensure remainingDays in Task is updated

                if (!task.isCompleted()) {
                    if (task.getRemainingDays() != null) {
                        remainingDaysText.setText(String.valueOf(task.getRemainingDays()) + " days left");
                        remainingDaysText.setTextColor(Color.parseColor("#1976D2")); // Blue
                    } else {
                        remainingDaysText.setText("Invalid Days");
                        remainingDaysText.setTextColor(Color.RED);
                    }
                } else {
                    remainingDaysText.setText("Completed");
                    remainingDaysText.setTextColor(Color.GRAY);
                }
            } catch (ParseException e) {
                remainingDaysText.setText("Invalid Date");
                remainingDaysText.setTextColor(Color.RED);
                e.printStackTrace();
            }

            if (task.isCompleted()) {
                titleText.setPaintFlags(titleText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                titleText.setTextColor(Color.GRAY);
                completeButton.setVisibility(View.GONE);
                doneTodayButton.setVisibility(View.GONE); // Hide "Done for Today" for completed tasks
            } else {
                titleText.setPaintFlags(titleText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                titleText.setTextColor(Color.BLACK);
                completeButton.setVisibility(View.VISIBLE);
                doneTodayButton.setVisibility(View.VISIBLE); // Show for incomplete tasks
            }

            // Initial state of the "Done for Today" button
            checkAndEnableDoneButton(currentTaskId);

            // Mark as completed
            completeButton.setOnClickListener(v -> {
                task.setCompleted(true);
                notifyItemChanged(getAdapterPosition());

                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                FirebaseFirestore.getInstance()
                        .collection("users").document(userId)
                        .collection("tasks").document(task.getDocumentId())
                        .update("completed", true);
            });

            // Delete task
            deleteButton.setOnClickListener(v -> {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                FirebaseFirestore.getInstance()
                        .collection("users").document(userId)
                        .collection("tasks").document(task.getDocumentId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            showTaskDeletedNotification(context, task.getTitle());
                            int position = getAdapterPosition();
                            taskList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, taskList.size());
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Failed to delete task", Toast.LENGTH_SHORT).show();
                            Log.e("TaskAdapter", "Failed to delete task: " + e.getMessage());
                        });
            });
        }

        private void checkAndEnableDoneButton(String taskId) {
            if (taskId != null) {
                SharedPreferences prefs = context.getSharedPreferences("task_progress", Context.MODE_PRIVATE);
                long lastDoneTime = prefs.getLong(PREF_DONE_TODAY + taskId, 0);
                Calendar lastDoneCalendar = Calendar.getInstance();
                lastDoneCalendar.setTimeInMillis(lastDoneTime);

                Calendar nowCalendar = Calendar.getInstance();

                if (nowCalendar.get(Calendar.YEAR) > lastDoneCalendar.get(Calendar.YEAR) ||
                        nowCalendar.get(Calendar.DAY_OF_YEAR) > lastDoneCalendar.get(Calendar.DAY_OF_YEAR)) {
                    doneTodayButton.setEnabled(true);
                } else {
                    doneTodayButton.setEnabled(false);
                }

                // Disable the button again at the beginning of the next day
                Handler handler = new Handler();
                long delay = getMillisUntilNextDay();
                handler.postDelayed(() -> {
                    if (doneTodayButton != null && getAdapterPosition() != RecyclerView.NO_POSITION && taskList.get(getAdapterPosition()).getDocumentId().equals(taskId)) {
                        doneTodayButton.setEnabled(true);
                    }
                }, delay);
            } else {
                doneTodayButton.setEnabled(false);
            }
        }

        private long getMillisUntilNextDay() {
            Calendar now = Calendar.getInstance();
            Calendar nextDay = Calendar.getInstance();
            nextDay.add(Calendar.DAY_OF_YEAR, 1);
            nextDay.set(Calendar.HOUR_OF_DAY, 0);
            nextDay.set(Calendar.MINUTE, 0);
            nextDay.set(Calendar.SECOND, 0);
            nextDay.set(Calendar.MILLISECOND, 0);
            return nextDay.getTimeInMillis() - now.getTimeInMillis();
        }

        private void markAsDoneToday(String taskId) {
            if (taskId != null) {
                // Disable the button immediately
                doneTodayButton.setEnabled(false);

                // Update the "remainingDays" in Firestore
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                FirebaseFirestore.getInstance()
                        .collection("users").document(userId)
                        .collection("tasks").document(taskId)
                        .update("remainingDays", FieldValue.increment(-1))
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(context, "Marked as done for today!", Toast.LENGTH_SHORT).show();
                            // Save the current time to shared preferences
                            SharedPreferences.Editor editor = context.getSharedPreferences("task_progress", Context.MODE_PRIVATE).edit();
                            editor.putLong(PREF_DONE_TODAY + taskId, System.currentTimeMillis());
                            editor.apply();

                            // Directly update the task in the list
                            for (int i = 0; i < taskList.size(); i++) {
                                if (taskList.get(i).getDocumentId().equals(taskId)) {
                                    Long currentRemainingDays = taskList.get(i).getRemainingDays();
                                    if (currentRemainingDays != null && currentRemainingDays > 0) {
                                        taskList.get(i).setRemainingDays(currentRemainingDays - 1);
                                        notifyItemChanged(i); // Refresh the view holder at the correct index
                                    }
                                    break;
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Failed to update task progress: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            // Re-enable the button if the update failed
                            doneTodayButton.setEnabled(true);
                        });
            }
        }
    }
}
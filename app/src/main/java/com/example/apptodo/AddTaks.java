package com.example.apptodo;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddTaks extends AppCompatActivity {

    private static final String CHANNEL_ID = "task_alarms";
    private DrawerLayout drawerLayoutAddTask;
    private NavigationView navigationViewAddTask;
    private Button menuButtonAddTask;
    private EditText editTextTaskTitle, editTextTaskDescription, editTextTime;
    private TextView selectedStartDate, selectedEndDate;
    private Button buttonAddTask, selectStartDateButton, selectEndDateButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Calendar startDateCalendar = Calendar.getInstance();
    private Calendar endDateCalendar = Calendar.getInstance();
    private Calendar timeCalendar = Calendar.getInstance(); // Calendar for time
    private TextView navName;
    private TextView navMail;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private AlarmManager alarmManager;
    private ActivityResultLauncher<String> requestAlarmPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Initialize UI elements
        drawerLayoutAddTask = findViewById(R.id.drawer_layout_add_task);
        navigationViewAddTask = findViewById(R.id.navigationViewAddTask);

        View headerView = navigationViewAddTask.getHeaderView(0);
        navName = headerView.findViewById(R.id.userName);
        navMail = headerView.findViewById(R.id.userEmail);

        menuButtonAddTask = findViewById(R.id.menuButtonAddTask);
        editTextTaskTitle = findViewById(R.id.editTextTaskTitle);
        editTextTaskDescription = findViewById(R.id.editTextTaskDescription);
        editTextTime = findViewById(R.id.editTextTime);
        buttonAddTask = findViewById(R.id.buttonAddTask);
        selectStartDateButton = findViewById(R.id.selectStartDateButton);
        selectedStartDate = findViewById(R.id.selectedStartDate);
        selectEndDateButton = findViewById(R.id.selectEndDateButton);
        selectedEndDate = findViewById(R.id.selectedEndDate);

        // Initialize permission request launcher for SCHEDULE_EXACT_ALARM
        requestAlarmPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Toast.makeText(this, "Alarm permission granted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Alarm permission denied. Reminders may not work precisely.", Toast.LENGTH_LONG).show();
                    }
                }
        );

        // Create notification channel
        createNotificationChannel();

        final FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String fullName = document.getString("fullName");
                            navName.setText(fullName);
                            navMail.setText(document.getString("email"));
                        }
                    });
        }

        // Open Drawer on Menu Button Click
        menuButtonAddTask.setOnClickListener(v -> drawerLayoutAddTask.openDrawer(GravityCompat.START));

        // Handle Navigation Menu Clicks
        navigationViewAddTask.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_profile) {
                startActivity(new Intent(AddTaks.this, ProfileActivity.class));
            } else if (id == R.id.nav_add_task) {
                drawerLayoutAddTask.closeDrawer(GravityCompat.START);
            } else if (id == R.id.nav_tasks) {
                startActivity(new Intent(AddTaks.this, WelcomeActivity.class));
                finish();
            } else if (id == R.id.nav_logout) {
                logoutUser();
            }

            drawerLayoutAddTask.closeDrawer(GravityCompat.START);
            return true;
        });

        // Date Picker Dialogs
        DatePickerDialog.OnDateSetListener startDateSetListener = (view, year, monthOfYear, dayOfMonth) -> {
            startDateCalendar.set(Calendar.YEAR, year);
            startDateCalendar.set(Calendar.MONTH, monthOfYear);
            startDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateStartDateDisplay();
        };

        DatePickerDialog.OnDateSetListener endDateSetListener = (view, year, monthOfYear, dayOfMonth) -> {
            endDateCalendar.set(Calendar.YEAR, year);
            endDateCalendar.set(Calendar.MONTH, monthOfYear);
            endDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateEndDateDisplay();
        };

        selectStartDateButton.setOnClickListener(v -> new DatePickerDialog(AddTaks.this,
                startDateSetListener,
                startDateCalendar.get(Calendar.YEAR),
                startDateCalendar.get(Calendar.MONTH),
                startDateCalendar.get(Calendar.DAY_OF_MONTH)).show());

        selectEndDateButton.setOnClickListener(v -> new DatePickerDialog(AddTaks.this,
                endDateSetListener,
                endDateCalendar.get(Calendar.YEAR),
                endDateCalendar.get(Calendar.MONTH),
                endDateCalendar.get(Calendar.DAY_OF_MONTH)).show());

        // Time Picker Dialog
        editTextTime.setOnClickListener(v -> {
            int hour = timeCalendar.get(Calendar.HOUR_OF_DAY);
            int minute = timeCalendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(AddTaks.this,
                    (view, hourOfDay, minuteOfHour) -> {
                        timeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        timeCalendar.set(Calendar.MINUTE, minuteOfHour);
                        updateTimeDisplay();
                    }, hour, minute, true); // true for 24-hour format
            timePickerDialog.show();
        });

        // Button click to add a new task to Firestore and set alarm
        buttonAddTask.setOnClickListener(v -> {
            String title = editTextTaskTitle.getText().toString().trim();
            String description = editTextTaskDescription.getText().toString().trim();
            String startDate = selectedStartDate.getText().toString().trim();
            String dueDate = selectedEndDate.getText().toString().trim();
            String time = editTextTime.getText().toString().trim();
            boolean completed = false;

            if (!title.isEmpty() && !description.isEmpty() && !startDate.isEmpty() && !dueDate.isEmpty() && !time.isEmpty()) {
                try {
                    Date startDateObj = dateFormat.parse(startDate);
                    Date dueDateObj = dateFormat.parse(dueDate);
                    Date currentDateObj = new Date();

                    if (dueDateObj.before(currentDateObj)) {
                        Toast.makeText(AddTaks.this, "Due date cannot be in the past", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (dueDateObj.before(startDateObj)) {
                        Toast.makeText(AddTaks.this, "Due date cannot be before the start date", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (user != null) {
                        Map<String, Object> taskData = new HashMap<>();
                        taskData.put("title", title);
                        taskData.put("description", description);
                        taskData.put("startDate", startDate);
                        taskData.put("dueDate", dueDate);
                        taskData.put("time", time);
                        taskData.put("completed", completed);
                        taskData.put("remainingDays", null);

                        db.collection("users").document(user.getUid()).collection("tasks")
                                .add(taskData)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(AddTaks.this, "Task added successfully", Toast.LENGTH_SHORT).show();
                                    // Set the alarm after successfully adding the task
                                    setAlarmForTask(title, description, startDate, time, documentReference.getId());
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(AddTaks.this, "Error adding task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                } catch (ParseException e) {
                    Toast.makeText(AddTaks.this, "Invalid date format", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(AddTaks.this, "Please enter all task details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Task Alarms";
            String description = "Notifications for upcoming tasks";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void setAlarmForTask(String title, String description, String startDateStr, String timeStr, String taskId) {
        Calendar alarmCalendar = Calendar.getInstance();
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        try {
            Date alarmDateTime = dateTimeFormat.parse(startDateStr + " " + timeStr);
            alarmCalendar.setTime(alarmDateTime);

            // Check if the alarm time is in the future
            if (alarmCalendar.getTimeInMillis() <= System.currentTimeMillis()) {
                Toast.makeText(this, "Alarm time must be in the future", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra("task_title", title);
            intent.putExtra("task_description", description);
            intent.putExtra("notification_id", generateNotificationId(taskId)); // Use task ID for unique ID

            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, generateAlarmRequestId(taskId), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager != null && alarmManager.canScheduleExactAlarms()) {
                    // Permission to schedule exact alarms is granted
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmCalendar.getTimeInMillis(), pendingIntent);
                    Toast.makeText(this, "Alarm set for " + dateTimeFormat.format(alarmCalendar.getTime()), Toast.LENGTH_LONG).show();
                } else {
                    // Permission to schedule exact alarms is NOT granted
                    Toast.makeText(this, "Precise reminders may not work. Please grant the necessary permission.", Toast.LENGTH_LONG).show();
                    // Consider showing a dialog or UI element to guide the user to settings
                    Intent requestIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    startActivity(requestIntent);
                    // As a fallback, you can set a less precise alarm
                    // alarmManager.set(AlarmManager.RTC_WAKEUP, alarmCalendar.getTimeInMillis(), pendingIntent);
                }
            } else {
                // For older Android versions, just set the alarm
                if (alarmManager != null) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmCalendar.getTimeInMillis(), pendingIntent);
                    Toast.makeText(this, "Alarm set for " + dateTimeFormat.format(alarmCalendar.getTime()), Toast.LENGTH_LONG).show();
                }
            }

        } catch (ParseException e) {
            Toast.makeText(this, "Error parsing date and time for alarm", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private int generateAlarmRequestId(String taskId) {
        // Generate a unique request code based on the task ID
        return taskId.hashCode();
    }

    private int generateNotificationId(String taskId) {
        // Generate a unique notification ID based on the task ID
        return taskId.hashCode();
    }

    private void updateStartDateDisplay() {
        String formattedDate = dateFormat.format(startDateCalendar.getTime());
        selectedStartDate.setText(formattedDate);
    }

    private void updateEndDateDisplay() {
        String formattedDate = dateFormat.format(endDateCalendar.getTime());
        selectedEndDate.setText(formattedDate);
    }

    private void updateTimeDisplay() {
        String formattedTime = timeFormat.format(timeCalendar.getTime());
        editTextTime.setText(formattedTime);
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(this, "Logging Out...", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(AddTaks.this, LoginActivity.class));
        finish();
    }
}
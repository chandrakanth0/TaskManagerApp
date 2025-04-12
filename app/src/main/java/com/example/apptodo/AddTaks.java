package com.example.apptodo;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddTaks extends AppCompatActivity {

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
    private TextView navName;
    private TextView navMail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task); // Ensure this layout has DrawerLayout, NavigationView, and menuButtonAddTask

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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
                // Already in AddTaks, so just close the drawer
                drawerLayoutAddTask.closeDrawer(GravityCompat.START);
            } else if (id == R.id.nav_tasks) {
                startActivity(new Intent(AddTaks.this, WelcomeActivity.class));
                finish(); // Go back to task list
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

        // Button click to add a new task to Firestore
        buttonAddTask.setOnClickListener(v -> {
            String title = editTextTaskTitle.getText().toString().trim();
            String description = editTextTaskDescription.getText().toString().trim();
            String startDate = selectedStartDate.getText().toString().trim();
            String dueDate = selectedEndDate.getText().toString().trim();
            String time = editTextTime.getText().toString().trim();
            boolean completed = false; // Default value

            if (!title.isEmpty() && !description.isEmpty() && !startDate.isEmpty() && !dueDate.isEmpty() && !time.isEmpty()) {
//                user = mAuth.getCurrentUser();
                if (user != null) {
                    Map<String, Object> taskData = new HashMap<>();
                    taskData.put("title", title);
                    taskData.put("description", description);
                    taskData.put("startDate", startDate);
                    taskData.put("dueDate", dueDate);
                    taskData.put("time", time);
                    taskData.put("completed", completed);

                    db.collection("users").document(user.getUid()).collection("tasks")
                            .add(taskData)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(AddTaks.this, "Task added successfully", Toast.LENGTH_SHORT).show();
                                finish(); // Go back to the task list (WelcomeActivity)
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(AddTaks.this, "Error adding task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            } else {
                Toast.makeText(AddTaks.this, "Please enter all task details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStartDateDisplay() {
        String formattedDate = String.format(Locale.getDefault(), "%d-%02d-%02d",
                startDateCalendar.get(Calendar.YEAR),
                startDateCalendar.get(Calendar.MONTH) + 1, // Month is 0-indexed
                startDateCalendar.get(Calendar.DAY_OF_MONTH));
        selectedStartDate.setText(formattedDate);
    }

    private void updateEndDateDisplay() {
        String formattedDate = String.format(Locale.getDefault(), "%d-%02d-%02d",
                endDateCalendar.get(Calendar.YEAR),
                endDateCalendar.get(Calendar.MONTH) + 1, // Month is 0-indexed
                endDateCalendar.get(Calendar.DAY_OF_MONTH));
        selectedEndDate.setText(formattedDate);
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(this, "Logging Out...", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(AddTaks.this, LoginActivity.class));
        finish();
    }
}
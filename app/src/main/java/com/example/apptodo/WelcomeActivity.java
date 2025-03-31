package com.example.apptodo;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apptodo.LoginActivity;
import com.example.apptodo.R;
import com.example.apptodo.TaskAdapter;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.example.apptodo.R;


public class WelcomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CollectionReference tasksRef;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Button menuButton;

    private TextView welcomeText;
    private EditText taskInput;
    private Button addTaskButton;
    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private ArrayList<String> taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        tasksRef = db.collection("tasks");

        // UI Elements
        welcomeText = findViewById(R.id.welcomeText);

        taskInput = findViewById(R.id.taskInput);
        addTaskButton = findViewById(R.id.addTaskButton);
        recyclerView = findViewById(R.id.recyclerView);
        menuButton = findViewById(R.id.menuButton);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigationView);

        // RecyclerView Setup
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(taskAdapter);





        // Add Task Button Click
        addTaskButton.setOnClickListener(v -> addTask());
        // Load tasks from Firestore
        loadTasks();

        // Open Drawer
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
            } else if (id == R.id.nav_tasks) {
                Toast.makeText(this, "Viewing tasks", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_logout) {
                logoutUser();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Fetch and Display User's Name
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String fullName = document.getString("fullName");
                            welcomeText.setText("Welcome " + fullName + "!");
                        }
                    });
        }
    }

    private void loadTasks() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).collection("tasks")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        taskList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            taskList.add(document.getString("task"));
                        }
                        taskAdapter.notifyDataSetChanged();
                    }
                });
    }


    private void addTask() {
        String taskText = taskInput.getText().toString().trim();
        if (taskText.isEmpty()) {
            Toast.makeText(this, "Enter a task", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();  // Get current user ID
        DocumentReference userRef = db.collection("users").document(userId);
        CollectionReference tasksRef = userRef.collection("tasks"); // Reference to user's tasks

        // Task data
        Map<String, Object> taskData = new HashMap<>();
        taskData.put("task", taskText);


        tasksRef.add(taskData).addOnSuccessListener(documentReference -> {
            taskList.add(taskText);
            taskAdapter.notifyDataSetChanged();
            taskInput.setText("");
        }).addOnFailureListener(e -> {
            Toast.makeText(WelcomeActivity.this, "Failed to add task", Toast.LENGTH_SHORT).show();
        });

    }

    private void logoutUser() {
        mAuth.signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();


    }
}
package com.example.apptodo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WelcomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CollectionReference tasksRef;

    private TextView welcomeText;
    private EditText taskInput;
    private Button addTaskButton, logoutButton;
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
        logoutButton = findViewById(R.id.logoutButton);
        recyclerView = findViewById(R.id.recyclerView);

        // RecyclerView Setup
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(taskAdapter);

        // Load tasks from Firestore
        loadTasks();

        // Add Task Button Click
        addTaskButton.setOnClickListener(v -> addTask());

        // Logout Button Click
        logoutButton.setOnClickListener(v -> logoutUser());

        // Fetch and Display User's Name
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String fullName = document.getString("fullName");
                            welcomeText.setText("Welcome " + fullName + "!");
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(WelcomeActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private void loadTasks() {
        String userId = mAuth.getCurrentUser().getUid();  // Get current user ID
        CollectionReference tasksRef = db.collection("users").document(userId).collection("tasks");

        tasksRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                taskList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    taskList.add(document.getString("task"));  // Extract the task
                }
                taskAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(WelcomeActivity.this, "Error loading tasks", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logoutUser() {
        mAuth.signOut();  // Logs out the user
        Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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

}

package com.example.apptodo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
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
        taskInput = findViewById(R.id.taskInput);
        addTaskButton = findViewById(R.id.addTaskButton);
        recyclerView = findViewById(R.id.recyclerView);

        // RecyclerView Setup
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(taskAdapter);

        // Load tasks
        loadTasks();

        // Add Task Button Click
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTask();
            }
        });
    }

    private void loadTasks() {
        tasksRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    taskList.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        taskList.add(document.getString("task"));
                    }
                    taskAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(WelcomeActivity.this, "Error loading tasks", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addTask() {
        String taskText = taskInput.getText().toString().trim();
        if (taskText.isEmpty()) {
            Toast.makeText(this, "Enter a task", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a task object
        Map<String, Object> taskData = new HashMap<>();
        taskData.put("task", taskText);

        tasksRef.add(taskData).addOnCompleteListener(new OnCompleteListener<com.google.firebase.firestore.DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<com.google.firebase.firestore.DocumentReference> task) {
                if (task.isSuccessful()) {
                    taskList.add(taskText);
                    taskAdapter.notifyDataSetChanged();
                    taskInput.setText("");
                } else {
                    Toast.makeText(WelcomeActivity.this, "Failed to add task", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

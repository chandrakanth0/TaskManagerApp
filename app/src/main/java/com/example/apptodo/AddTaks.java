package com.example.apptodo;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AddTaks extends AppCompatActivity {

    private EditText editTextTaskTitle, editTextTaskDescription;
    private Button buttonAddTask;
    private RecyclerView recyclerViewTasks;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        // Initialize UI elements
        editTextTaskTitle = findViewById(R.id.editTextTaskTitle);
        editTextTaskDescription = findViewById(R.id.editTextTaskDescription);
        buttonAddTask = findViewById(R.id.buttonAddTask);
        recyclerViewTasks = findViewById(R.id.recyclerViewTasks);

        // Initialize task list and adapter
        taskList = new ArrayList<>();
//        taskAdapter = new TaskAdapter((ArrayList<>) taskList);
//        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTasks.setAdapter(taskAdapter);

        // Button click to add a new task
        buttonAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = editTextTaskTitle.getText().toString().trim();
                String description = editTextTaskDescription.getText().toString().trim();

                if (!title.isEmpty() && !description.isEmpty()) {
                    Task newTask = new Task(title, description);
                    taskList.add(newTask);
                    taskAdapter.notifyDataSetChanged();
                    editTextTaskTitle.setText("");
                    editTextTaskDescription.setText("");
                } else {
                    Toast.makeText(AddTaks.this, "Please enter title and description", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

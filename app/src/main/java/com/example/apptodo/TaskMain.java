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

public class TaskMain extends AppCompatActivity {

    private EditText editTextTaskTitle, editTextTaskDescription, editTextStartDate, editTextDueDate, editTextTime;
    private Button buttonAddTask;
    private RecyclerView recyclerViewTasks;
    private TaskAdapter taskAdapter;
    private ArrayList<Task> taskList; // Changed ArrayList to ArrayList<Task>

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks); // Assuming this layout has all the EditText fields

        // Initialize UI elements
        editTextTaskTitle = findViewById(R.id.editTextTaskTitle);
        editTextTaskDescription = findViewById(R.id.editTextTaskDescription);
        editTextStartDate = findViewById(R.id.editTextStartDate);
        editTextDueDate = findViewById(R.id.editTextDueDate);
        editTextTime = findViewById(R.id.editTextTime);
        buttonAddTask = findViewById(R.id.buttonAddTask);
        recyclerViewTasks = findViewById(R.id.recyclerViewTasks);

        // Initialize task list and adapter
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(this ,taskList);
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTasks.setAdapter(taskAdapter);

        // Button click to add a new task
        buttonAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = editTextTaskTitle.getText().toString().trim();
                String description = editTextTaskDescription.getText().toString().trim();
                String startDate = editTextStartDate.getText().toString().trim();
                String dueDate = editTextDueDate.getText().toString().trim();
                String time = editTextTime.getText().toString().trim();
                boolean completed = false; // Default value

                if (!title.isEmpty() && !description.isEmpty() && !startDate.isEmpty() && !dueDate.isEmpty() && !time.isEmpty()) {
                    // Create a Task object using the constructor with all parameters
                    Task newTask = new Task(title, description, startDate, dueDate, time, completed);
                    taskList.add(newTask);
                    taskAdapter.notifyDataSetChanged();
                    editTextTaskTitle.setText("");
                    editTextTaskDescription.setText("");
                    editTextStartDate.setText("");
                    editTextDueDate.setText("");
                    editTextTime.setText("");
                } else {
                    Toast.makeText(TaskMain.this, "Please enter all task details", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
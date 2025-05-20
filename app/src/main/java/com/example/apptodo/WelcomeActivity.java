package com.example.apptodo;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;

public class WelcomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Button menuButton;
    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private ArrayList<Task> taskList;
    private TextView navName;
    private TextView navMail;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI Elements
        recyclerView = findViewById(R.id.recyclerView);
        menuButton = findViewById(R.id.menuButton);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigationView);
        View headerView = navigationView.getHeaderView(0);
        navName = headerView.findViewById(R.id.userName);
        navMail = headerView.findViewById(R.id.userEmail);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        // RecyclerView Setup
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(this ,taskList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(taskAdapter);

        // Load tasks initially
        loadTasks();

        // Set up SwipeRefreshLayout listener
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // This method will be called when the user swipes down to refresh
            loadTasks();
        });

        // Open Drawer
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
            } else if (id == R.id.nav_add_task) {
                startActivity(new Intent(this, AddTaks.class)); // Start AddTaks Activity
            } else if (id == R.id.nav_tasks) {
                Toast.makeText(this, "Viewing tasks", Toast.LENGTH_SHORT).show();
                // You are already viewing tasks in WelcomeActivity,
                // so you might just want to close the drawer.
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
                            navName.setText(fullName);
                            navMail.setText(document.getString("email"));
                        }
                    });
        }
    }

    private void loadTasks() {
        swipeRefreshLayout.setRefreshing(true); // Show refreshing indicator
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).collection("tasks")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        taskList.clear();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Task task = doc.toObject(Task.class);
                            task.setDocumentId(doc.getId());
                            taskList.add(task);
                        }
                        taskAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false); // Hide refreshing indicator
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(WelcomeActivity.this, "Failed to load tasks.", Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false); // Hide refreshing indicator on failure
                    });
        } else {
            swipeRefreshLayout.setRefreshing(false); // Hide indicator if no user
        }
    }

    private void logoutUser() {
        mAuth.signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
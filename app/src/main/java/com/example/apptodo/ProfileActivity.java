package com.example.apptodo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView;
    private NavigationView navigationView;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private TextView navName, navMail;
    private TextView profileName, profileEmail, profilePhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Firebase Auth
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        // UI Elements
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigationView);
        View headerView = navigationView.getHeaderView(0);
        navName = headerView.findViewById(R.id.userName);
        navMail = headerView.findViewById(R.id.userEmail);
        profileName = findViewById(R.id.userName);
        profileEmail = findViewById(R.id.userEmail);
        profilePhone = findViewById(R.id.userPhone);

        if (user != null) {
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String fullName = document.getString("fullName");
                            String email = document.getString("email");
                            String phone = document.getString("phone");

                            navName.setText(fullName);
                            navMail.setText(email);
                            profileName.setText(fullName);
                            profileEmail.setText(email);
                            profilePhone.setText(phone != null ? phone : "N/A");
                        }
                    });
        }

        // Open Drawer on Menu Button Click
        findViewById(R.id.menuButton).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Handle Navigation Menu Clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_tasks) {
                startActivity(new Intent(ProfileActivity.this, WelcomeActivity.class));
            } else if (id == R.id.nav_add_task) {
                startActivity(new Intent(this, AddTaks.class));
            } else if (id == R.id.nav_logout) {
                logoutUser();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void logoutUser() {
        auth.signOut();
        Toast.makeText(this, "Logging Out...", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
        finish();
    }
}
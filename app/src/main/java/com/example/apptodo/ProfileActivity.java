package com.example.apptodo;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
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

public class ProfileActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth auth;
    private TextView userName, userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Firebase Auth
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        // UI Elements
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigationView);
        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);

        // Set User Info
        if (user != null) {
            FirebaseFirestore.getInstance().collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            userName.setText(document.getString("fullName"));
                            userEmail.setText(document.getString("email"));
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

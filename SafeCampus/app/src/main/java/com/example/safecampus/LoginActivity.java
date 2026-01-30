package com.example.safecampus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText etUsername, etPassword;
    Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.edtUsername);
        etPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty()) {
                etUsername.setError("Enter username");
                return;
            }

            if (password.isEmpty()) {
                etPassword.setError("Enter password");
                return;
            }

            // Simpan username dalam SharedPreferences
            SharedPreferences prefs = getSharedPreferences("SafeCampusPrefs", MODE_PRIVATE);
            prefs.edit().putString("username", username).apply();

            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();

            // Buka Home Activity
            Intent intent = new Intent(LoginActivity.this, SafeCampusHomeActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
package com.example.expensemanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class signup extends AppCompatActivity {
    EditText etFirstName, etLastName, etEmailSignup, etPasswordSignup;
    Button btnSignup;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmailSignup = findViewById(R.id.etEmailSignup);
        etPasswordSignup = findViewById(R.id.etPasswordSignup);
        btnSignup = findViewById(R.id.btnSignup);

        db = new DatabaseHelper(this);

        btnSignup.setOnClickListener(v -> {
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String email = etEmailSignup.getText().toString().trim();
            String password = etPasswordSignup.getText().toString().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(signup.this, "All fields are required", Toast.LENGTH_SHORT).show();
            } else {
                boolean insertSuccess = db.insertUser(firstName, lastName, email, password);
                if (insertSuccess) {
                    Toast.makeText(signup.this, "Signup Successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(signup.this, login.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(signup.this, "Email already exists", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

package edu.northeastern.my_ds;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button signInButton;
    private Button signUpButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signInButton = findViewById(R.id.signInButton);
        signUpButton = findViewById(R.id.signUpButton);


        mAuth = FirebaseAuth.getInstance();

        // sign in functionality
        signInButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            // Check if the email and password are empty
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(
                        MainActivity.this,
                        "Please enter email and password", Toast.LENGTH_SHORT).show();
                // Check if the email is valid
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(
                        MainActivity.this,
                        "Please enter a valid email", Toast.LENGTH_SHORT).show();
            } else {
                // Sign in the user
                signInUser(email, password);
            }
        });

        // sign up functionality
        signUpButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SignUpActivity.class));
        });
    }

    private void signInUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        // After sign-in, check if pet name exists for the user
                    } else {
                        if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                            // User does not exist
                            Toast.makeText(
                                    MainActivity.this,
                                    "User does not exist", Toast.LENGTH_SHORT).show();
                            // lead user to sign up page
                            startActivity(new Intent(MainActivity.this, SignUpActivity.class));
                            finish();
                        } else {
                            // Password is incorrect
                            Toast.makeText(
                                    MainActivity.this,
                                    "Authentication failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
}

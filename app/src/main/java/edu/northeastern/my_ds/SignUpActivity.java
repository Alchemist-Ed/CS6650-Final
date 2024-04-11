package edu.northeastern.my_ds;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignUpActivity extends AppCompatActivity{

    private Button signUpButton;
    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private FirebaseAuth auth;
    private boolean isUsernameValid, isEmailValid, isPasswordValid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        auth = FirebaseAuth.getInstance();
        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signUpButton = findViewById(R.id.signUpButton);

        signUpButton.setOnClickListener(v -> signUpUser());

        addValidationListeners();
    }
        private void addValidationListeners () {
            usernameEditText.addTextChangedListener(new ValidationTextWatcher(usernameEditText));
            emailEditText.addTextChangedListener(new ValidationTextWatcher(emailEditText));
            passwordEditText.addTextChangedListener(new ValidationTextWatcher(passwordEditText));
        }

        //check if username is valid
        private boolean isValidUsername (String username){
            // Username must be between 4 and 20 characters long
            // and contain only letters and numbers
            String usernamePattern = "^[a-zA-Z0-9]{4,20}$";
            return username.matches(usernamePattern);
        }

        //check if email is valid
        private boolean isValidEmail (String email){
            return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
        }

        //check if password is valid
        private boolean isValidPassword (String password){
            // Password must be between 6 and 12 characters long
            // case sensitive
            // and contain at least one letter and one number
            String passwordPattern = "^(?=.*[0-9])(?=.*[a-zA-Z]).{6,12}$";
            return password.matches(passwordPattern);
        }

        private class ValidationTextWatcher implements TextWatcher {
            private EditText view;

            private ValidationTextWatcher(EditText view) {
                this.view = view;
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // if any of the fields are empty
                // set the error message
                if (s.toString().isEmpty()) {
                    view.setError("Required");
                    if (view.getId() == R.id.usernameEditText) {
                        isUsernameValid = false;
                    } else if (view.getId() == R.id.emailEditText) {
                        isEmailValid = false;
                    } else if (view.getId() == R.id.passwordEditText) {
                        isPasswordValid = false;
                    }
                } else {
                    if (view.getId() == R.id.usernameEditText) {
                        isUsernameValid = isValidUsername(s.toString());
                        if (!isUsernameValid) {
                            view.setError("Username must be between 4 and 20 characters long and contain only letters and numbers");
                        } else {
                            // Check if username is already taken
                            DatabaseReference usernameRef
                                    = FirebaseDatabase.getInstance().getReference("usernames").child(s.toString());
                            usernameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        view.setError("Username already taken");
                                        isUsernameValid = false;
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e(TAG, "loadUsername:onCancelled", error.toException());
                                    view.setError("Error checking for existing username");
                                    isUsernameValid = false;
                                }
                            });
                        }
                    } else if (view.getId() == R.id.emailEditText) {
                        isEmailValid = isValidEmail(s.toString());
                        if (!isEmailValid) {
                            view.setError("Invalid email");
                        }
                    } else if (view.getId() == R.id.passwordEditText) {
                        isPasswordValid = isValidPassword(s.toString());
                        if (!isPasswordValid) {
                            view.setError("Password must be between 6 and 12 characters long, case sensitive, and contain at least one letter and one number");
                        }
                    }
                }
            }

        }

        private void signUpUser () {
            String username = usernameEditText.getText().toString();
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (isUsernameValid && isEmailValid && isPasswordValid) {
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                // Sign up success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = auth.getCurrentUser();
                                // Add user to database
                                if (user != null) {
                                    String id = user.getUid();
                                    DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference();
                                    User newUser = new User(id, username, email);
                                    dbReference.child("users").child(id).setValue(newUser);
                                    dbReference.child("usernames").child(username).setValue(user.getUid());
                                }
                                updateUI(user);
                            } else {
                                // If sign up fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(SignUpActivity.this, "Sign up failed.", Toast.LENGTH_SHORT).show();
                                updateUI(null);
                            }
                        });
            } else {
                Toast.makeText(this, "Invalid entries. Please check the fields.", Toast.LENGTH_SHORT).show();
            }
        }

        private void updateUI (FirebaseUser user){
            if (user != null) {
                Toast.makeText(this, "Sign up successful.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Sign up failed.", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onBackPressed () {
            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
            startActivity(intent);
            super.onBackPressed();
        }
}
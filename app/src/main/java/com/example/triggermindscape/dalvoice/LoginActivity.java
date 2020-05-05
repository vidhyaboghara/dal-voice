package com.example.triggermindscape.dalvoice;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/*
 *   Created by Gaurav Anand (gr874432@dal.ca)
 *   Class to handle user login functionality
 *
 *   Design and validations updated by Anshdeep Singh
 */

public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private TextView signUpText, resetPasswordText, goToHomeText;
    private TextInputLayout emailInput;
    private TextInputLayout passwordInput;
    private FirebaseAuth auth;

    private static final String emailPattern = "[a-zA-Z0-9._-]+@dal.ca";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
        setContentView(R.layout.activity_login);
        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        signUpText = findViewById(R.id.sign_up_direct);
        goToHomeText = findViewById(R.id.go_to_home_button);
        loginButton = findViewById(R.id.login_button);
        resetPasswordText = findViewById(R.id.reset_password_button);

        emailInput.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (TextUtils.isEmpty(editable.toString())) {
                    emailInput.setError("Email address cannot be empty!");
                } else if (!editable.toString().matches(emailPattern)) {
                    emailInput.setError("Enter a valid dal email address");

                } else {
                    emailInput.setError(null);
                }

            }
        });

        passwordInput.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (TextUtils.isEmpty(editable.toString())) {
                    passwordInput.setError("Password cannot be empty!");
                } else if (editable.toString().length() < 6) {
                    passwordInput.setError("Password should be minimum 6 characters");
                } else {
                    passwordInput.setError(null);
                }

            }
        });

        signUpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        resetPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailInput.getEditText().getText().toString();
                final String password = passwordInput.getEditText().getText().toString();

                if (TextUtils.isEmpty(email)) {
                    emailInput.setError("Email address cannot be empty!");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    passwordInput.setError("Password cannot be empty!");
                    return;
                }

                //authenticate user
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(
                                LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        // If sign in fails, display a message to the user.
                                        // If sign in is complete, check if email is verified.
                                        // If yes, go to the MainActivity. If not, show email verify message.
                                        if (!task.isSuccessful()) {
                                            if (password.length() < 6) {
                                                passwordInput.setError(
                                                        getString(R.string.password_min_error));
                                            } else {
                                                Toast.makeText(LoginActivity.this, getString(
                                                        R.string.invalid_email_error),
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                            if (user != null && user.isEmailVerified()) {
                                                finish();
                                            } else {
                                                Toast.makeText(LoginActivity.this,
                                                        getString(R.string.email_auth_error), Toast.LENGTH_SHORT).show();
                                                FirebaseAuth.getInstance().signOut();
                                            }
                                        }
                                    }
                                });
            }
        });

        goToHomeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               finish();
            }
        });
    }
}
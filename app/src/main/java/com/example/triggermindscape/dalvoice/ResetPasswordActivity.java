package com.example.triggermindscape.dalvoice;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/*
 *   Created by Gaurav Anand (gr874432@dal.ca)
 *   Class to handle reset password functionality
 */

public class ResetPasswordActivity extends AppCompatActivity {

    private Button resetButton;
    private TextInputLayout emailInput;
    private FirebaseAuth auth;
    private ProgressBar progressBar;

    private final String emailPattern = "[a-zA-Z0-9._-]+@dal.ca";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        emailInput = findViewById(R.id.email);
        resetButton = findViewById(R.id.btn_reset_password);
        progressBar = findViewById(R.id.progressBar);

        auth = FirebaseAuth.getInstance();

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
                    emailInput.setError(getString(R.string.email_not_empty_error));
                } else if (!editable.toString().matches(emailPattern)) {
                    emailInput.setError(getString(R.string.enter_valid_email_error));
                } else {
                    emailInput.setError(null);
                }
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailInput.getEditText().getText().toString().trim();
                if (TextUtils.isEmpty(email)) {
                    emailInput.setError(getString(R.string.email_not_empty_error));
                } else if (emailInput.getError() != null) {
                    Toast.makeText(ResetPasswordActivity.this, getString(R.string.fix_error_text), Toast.LENGTH_SHORT).show();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    auth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ResetPasswordActivity.this, getString(R.string.sent_instructions_text), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ResetPasswordActivity.this, getString(R.string.failed_reset_email), Toast.LENGTH_SHORT).show();
                                    }
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                }
            }
        });
    }
}
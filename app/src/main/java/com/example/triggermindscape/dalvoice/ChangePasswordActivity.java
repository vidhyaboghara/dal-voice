package com.example.triggermindscape.dalvoice;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/*
 *   Class to change password from user profile screen
 *   Design and validations updated by Anshdeep Singh
 */

public class ChangePasswordActivity extends AppCompatActivity {

    private Button btnSubmit;
    private TextInputLayout oldPass, newPass, confirmNewPass;
    private FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        user = FirebaseAuth.getInstance().getCurrentUser();
        final String email = user.getEmail();
        oldPass = findViewById(R.id.etOldPass);
        newPass = findViewById(R.id.etNewPass);
        confirmNewPass = findViewById(R.id.etConfirmNewPass);
        btnSubmit = findViewById(R.id.btnSubmit);

        oldPass.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (TextUtils.isEmpty(editable.toString())) {
                    oldPass.setError("Old Password cannot be empty!");
                    oldPass.setErrorIconDrawable(null);
                } else if (editable.toString().length() < 6) {
                    oldPass.setError("Password should be minimum 6 characters");
                    oldPass.setErrorIconDrawable(null);
                } else {
                    oldPass.setError(null);
                }
            }
        });

        newPass.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (TextUtils.isEmpty(editable.toString())) {
                    newPass.setError("Password cannot be empty!");
                    newPass.setErrorIconDrawable(null);
                } else if (editable.toString().length() < 6) {
                    newPass.setError("Password should be minimum 6 characters");
                    newPass.setErrorIconDrawable(null);
                } else if (editable.toString().equals(oldPass.getEditText().getText().toString())) {
                    newPass.setError("Password cannot be same as old password!");
                    newPass.setErrorIconDrawable(null);
                } else {
                    newPass.setError(null);
                }
            }
        });

        confirmNewPass.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (TextUtils.isEmpty(editable.toString())) {
                    confirmNewPass.setError("Confirm password cannot be empty!");
                    confirmNewPass.setErrorIconDrawable(null);
                } else if (!editable.toString().equals(newPass.getEditText().getText().toString())) {
                    confirmNewPass.setError("Passwords don't match!");
                    confirmNewPass.setErrorIconDrawable(null);
                } else {
                    confirmNewPass.setError(null);
                }
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String oldpassword = oldPass.getEditText().getText().toString().trim();
                final String newPassword = newPass.getEditText().getText().toString().trim();
                final String confirmNewPassword = confirmNewPass.getEditText().getText().toString().trim();
                if (TextUtils.isEmpty(oldpassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmNewPassword)) {
                    Toast.makeText(getApplicationContext(), "Please fill all the fields!", Toast.LENGTH_SHORT).show();
                } else if (oldPass.getError() != null || newPass.getError() != null || confirmNewPass.getError() != null) {
                    Toast.makeText(getApplicationContext(), "Please fix the errors!", Toast.LENGTH_SHORT).show();
                } else {
                    final AuthCredential credential = EmailAuthProvider.getCredential(email, oldpassword);
                    user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (!task.isSuccessful()) {
                                            Toast.makeText(ChangePasswordActivity.this, "Error in updating!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(ChangePasswordActivity.this, "Password successfully updated!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(ChangePasswordActivity.this, "Authentication failed!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}
package com.example.triggermindscape.dalvoice;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

/*
 *   Created by Gaurav Anand (gaurav.anand@dal.ca)
 *   Class to handle user registration functionality
 */

public class RegisterActivity extends AppCompatActivity {

    private final static String emailPattern = "[a-zA-Z0-9._-]+@dal.ca";
    private final static int RESULT_LOAD_IMAGE = 1;

    private Button signUpButton;
    private TextInputLayout emailInput, passwordInput, confirmPasswordInput, nameInput;
    private TextView signInText;
    private FirebaseAuth auth;
    private CircleImageView profileImage;
    private android.widget.Button selectPhotoButton;
    private Uri selectedImage = null;

    private StorageReference storageReference;
    private String imageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("Images");
        signInText = findViewById(R.id.login_direct);
        signUpButton = findViewById(R.id.button_sign_up);
        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        confirmPasswordInput = findViewById(R.id.confirm_password);
        nameInput = findViewById(R.id.name);
        profileImage = findViewById(R.id.select_photo_image_view);
        selectPhotoButton = findViewById(R.id.select_photo_button);

        selectPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("image/*");
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        signInText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        nameInput.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (TextUtils.isEmpty(editable.toString())) {
                    nameInput.setError("Name cannot be empty!");
                } else {
                    nameInput.setError(null);
                }

            }
        });
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
                    passwordInput.setError(getString(R.string.password_empty_error));
                    passwordInput.setErrorIconDrawable(null);
                } else if (editable.toString().length() < 6) {
                    passwordInput.setError(getString(R.string.password_min_error));
                    passwordInput.setErrorIconDrawable(null);
                } else {
                    passwordInput.setError(null);

                }
            }
        });

        confirmPasswordInput.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (TextUtils.isEmpty(editable.toString())) {
                    confirmPasswordInput.setError(getString(R.string.confirm_pwd_empty_error));
                    confirmPasswordInput.setErrorIconDrawable(null);
                } else if (!editable.toString().equals(passwordInput.getEditText().getText().toString())) {
                    confirmPasswordInput.setError(getString(R.string.pwd_match_error));
                    confirmPasswordInput.setErrorIconDrawable(null);
                } else {
                    confirmPasswordInput.setError(null);
                }
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = emailInput.getEditText().getText().toString().trim();
                final String password = passwordInput.getEditText().getText().toString().trim();
                final String confirmPassword = confirmPasswordInput.getEditText().getText().toString().trim();
                final String name = nameInput.getEditText().getText().toString().trim();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)
                        || TextUtils.isEmpty(confirmPassword)
                        || TextUtils.isEmpty(name)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.fill_fields_error), Toast.LENGTH_SHORT).show();

                } else if (emailInput.getError() != null || passwordInput.getError() != null
                        || confirmPasswordInput.getError() != null ||
                        nameInput.getError() != null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.fix_errors_text), Toast.LENGTH_SHORT).show();
                } else if (selectedImage == null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.select_pic_error), Toast.LENGTH_SHORT).show();
                } else {
                    //create a new user
                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    // If sign in fails, display a message to the user.
                                    // If sign in is complete, send email verification link and finish the activity.
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this, "Error: " + task.getException(),
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        addUserToDatabase(email, password, name);
                                        uploadToFirebase();
                                        sendVerificationEmail();
                                    }
                                }
                            });
                }
            }
        });
    }

    private void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, getString(R.string.email_sent_text),
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            // email not sent, so display message and restart the activity
                            overridePendingTransition(0, 0);
                            finish();
                            overridePendingTransition(0, 0);
                            startActivity(getIntent());
                        }
                    }
                });
    }

    private void addUserToDatabase(String email, String password, String name) {
        User user = new User(email, password, name);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("profile").setValue(user);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImage = data.getData();
            profileImage.setImageURI(selectedImage);
            selectPhotoButton.setAlpha(0f);
        }
    }

    private String getExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }

    private void uploadToFirebase() {
        final StorageReference reference = storageReference.child(System.currentTimeMillis() + "." + getExtension(selectedImage));
        reference.putFile(selectedImage)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                imageReference = uri.toString();
                                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("profile").child("imageUrl").setValue(imageReference);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
        System.out.println(imageReference);
    }
}
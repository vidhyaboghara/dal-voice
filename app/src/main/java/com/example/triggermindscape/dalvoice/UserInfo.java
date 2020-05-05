package com.example.triggermindscape.dalvoice;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserInfo extends AppCompatActivity {

    private TextInputLayout etName, email;
    private CircleImageView ivProfilePic;
    private Button btnLogOut, btnChangePassword, btnSave;
    private Uri imageURI = null;
    private ProgressDialog progressDialog;
    private int flagET = 0, flagIMG = 0, flagOnStart = 0, flagSave = 0;
    private int GALLERY = 1;
    private String uid;
    private FirebaseUser user;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private String originalName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        requestMultiplePermissions();
        etName = findViewById(R.id.etName);
        email = findViewById(R.id.email);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        btnLogOut = findViewById(R.id.btnLogOut);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnSave = findViewById(R.id.btnSave);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();
        storageReference = FirebaseStorage.getInstance().getReference("Images");

        ValueEventListener postListener = new ValueEventListener() {
            //loading user details
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                String path = (dataSnapshot.child("users").child(uid).child("profile").child("imageUrl").getValue(String.class));
                if (path != null) {
                    Glide.with(getApplicationContext()).load(path)
                            .into(ivProfilePic);
                }
                originalName = dataSnapshot.child("users").child(uid).child("profile").child("name").getValue(String.class);
                etName.getEditText().setText(dataSnapshot.child("users").child(uid).child("profile").child("name").getValue(String.class));
                etName.getEditText().setSelection(etName.getEditText().getText().length());
                email.getEditText().setText(dataSnapshot.child("users").child(uid).child("profile").child("email").getValue(String.class));
                Runnable progressRunnable = new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.cancel();
                    }
                };
                Handler processDialogDelay = new Handler();
                processDialogDelay.postDelayed(progressRunnable, 1000);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(UserInfo.this, getString(R.string.could_not_load_error), Toast.LENGTH_SHORT).show();
            }
        };
        databaseReference.addValueEventListener(postListener);

        ivProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder picEditDialog = new AlertDialog.Builder(UserInfo.this);
                String DialogChoice[] = {"Edit photo"};
                picEditDialog.setItems(DialogChoice,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int ch) {
                                switch (ch) {
                                    case 0:
                                        changePhoto();
                                        break;
                                }
                            }
                        });
                picEditDialog.show();
            }
        });

        etName.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString())) {
                    etName.setError(getString(R.string.username_empty_error));
                } else {
                    etName.setError(null);
                }
            }
        });

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if ((flagSave != 1) && (flagET != 0 || flagIMG != 0)) {
                    new AlertDialog.Builder(UserInfo.this)
                            .setTitle(getString(R.string.save_changes))
                            .setMessage(getString(R.string.save_changes_before_proceed))
                            .setPositiveButton(android.R.string.yes, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                } else {
                    Intent intent = new Intent(getApplicationContext(), ChangePasswordActivity.class);
                    startActivity(intent);
                }
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (etName.getError() != null) {
                    Toast.makeText(getApplicationContext(), "Please fix the error", Toast.LENGTH_SHORT).show();
                } else {
                    if (originalName != null) {
                        if (!etName.getEditText().getText().toString().equals(originalName)) {
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            database.getReference().child("users").child(uid).child("profile").child("name").setValue(etName.getEditText().getText().toString());
                            Toast.makeText(UserInfo.this, "Name updated!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(UserInfo.this, "No changes to save!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        btnLogOut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if ((flagSave != 1) && (flagET != 0 || flagIMG != 0)) {
                    new AlertDialog.Builder(UserInfo.this)
                            .setTitle(getString(R.string.save_changes))
                            .setMessage(getString(R.string.save_changes_text))
                            .setPositiveButton(android.R.string.yes, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                } else {
                    progressDialog = new ProgressDialog(UserInfo.this);
                    progressDialog.setMessage("Logging out...");
                    progressDialog.show();
                    FirebaseAuth.getInstance().signOut();
                    Toast.makeText(UserInfo.this, "Logged Out", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    //requesting permission to access gallery
    private void requestMultiplePermissions() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            Toast.makeText(getApplicationContext(), getString(R.string.all_permissions_granted), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), getString(R.string.error_setting_permissions), Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }

    //gallery intent
    public void changePhoto() {
        Intent gIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(gIntent, GALLERY);
    }

    //upon selecting gallery intent
    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) {
        super.onActivityResult(reqCode, resCode, data);
        if (resCode == this.RESULT_CANCELED) {
            return;
        }
        if (reqCode == GALLERY) {
            if (data != null) {
                imageURI = data.getData();
                ivProfilePic.setImageURI(Uri.parse(imageURI.toString()));
                flagIMG = 1;
                uploadImageToDatabase();
            }
        }
    }

    //storing image in database
    private void uploadImageToDatabase() {
        final StorageReference reference = storageReference.child(System.currentTimeMillis() + "." + getExtension(imageURI));
        reference.putFile(imageURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String imageReference = uri.toString();
                        updateDataToDatabase(imageReference);
                    }
                });
            }
        });
    }

    private String getExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }

    //updating image path
    private void updateDataToDatabase(String userImageReference) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference().child("users").child(uid).child("profile").child("imageUrl").setValue(userImageReference);
        Toast.makeText(UserInfo.this, getString(R.string.profile_picture_updated), Toast.LENGTH_SHORT).show();
    }

    protected void onStart() {
        super.onStart();
        if (flagOnStart == 0) {
            progressDialog = new ProgressDialog(UserInfo.this);
            progressDialog.setMessage(getString(R.string.getting_user_details));
            progressDialog.show();
            flagOnStart = 1;
        }
    }

    protected void onStop() {
        super.onStop();
        flagET = 0;
        flagIMG = 0;
        flagSave = 0;
    }
}
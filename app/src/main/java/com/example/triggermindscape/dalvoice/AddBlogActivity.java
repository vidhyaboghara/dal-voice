package com.example.triggermindscape.dalvoice;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
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
import com.vikramezhil.droidspeech.DroidSpeech;
import com.vikramezhil.droidspeech.OnDSListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/*
 *   Class to handle add blog functionality
 *   Design and validations updated by Anshdeep Singh
 */


public class AddBlogActivity extends AppCompatActivity {
    private Button publish;

    private FloatingActionButton attachment;
    private FloatingActionButton voiceToText;
    private TextView coverPicture;
    private Uri imageURI = null;
    private StorageReference storageReference;

    private TextInputLayout title, description, category;
    private AutoCompleteTextView categoryTextView;
    private ProgressBar progressBar;
    private boolean imageSelected = false;

    private DroidSpeech droidSpeech;

    private static final String IMAGE_DIRECTORY = "/coverPics";
    private int GALLERY = 1, CAMERA = 2;
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_blog);

        requestMultiplePermissions();
        progressBar = findViewById(R.id.progressBar);
        coverPicture = findViewById(R.id.tvCoverPicture);
        voiceToText = findViewById(R.id.btnVoiceToText);
        attachment = findViewById(R.id.btnAddImage);
        publish = findViewById(R.id.btnPublishBlog);
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        category = findViewById(R.id.spinnerCategory);


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_menu_popup_item, getResources().getStringArray(R.array.category));
        categoryTextView = findViewById(R.id.filled_exposed_dropdown);
        categoryTextView.setAdapter(adapter);

        title.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (TextUtils.isEmpty(editable.toString())) {
                    title.setError("Title cannot be empty!");
                } else {
                    title.setError(null);
                }

            }
        });

        description.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (TextUtils.isEmpty(editable.toString())) {
                    description.setError("Description cannot be empty!");
                } else {
                    description.setError(null);
                }

            }
        });

        category.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (TextUtils.isEmpty(editable.toString())) {
                    category.setError("Please select a category");
                } else {
                    category.setError(null);
                }

            }
        });

        storageReference = FirebaseStorage.getInstance().getReference("UserImage");

        attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choiceDialog();
            }
        });

        publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!imageSelected) {
                    Toast.makeText(AddBlogActivity.this, "Choose a cover picture!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (title.getEditText().getText().toString().length() == 0) //checking if value is empty
                {
                    title.setError("Title cannot be empty!");
                    return;
                }

                if (description.getEditText().getText().toString().length() == 0) {
                    description.setError("Description cannot be empty!");
                    return;
                }

                if (categoryTextView.getText().toString().length() == 0) {
                    category.setError("Please select a category");
                    return;
                }

                //push the user image to db
                uploadImageToDatabase();
                progressBar.setVisibility(View.VISIBLE);
            }
        });

        voiceToText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(AddBlogActivity.this,
                        Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {

                    // No explanation needed; request the permission
                    ActivityCompat.requestPermissions(AddBlogActivity.this,
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            MY_PERMISSIONS_REQUEST_RECORD_AUDIO);


                } else {
                    // Permission has already been granted
                    startVoiceToText();
                }
            }
        });
    }

    private void addBlogToDatabase(final String userImageReference) {
        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final String key = database.getReference("blogs").push().getKey();
        database.getReference().child("users").child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String userName = dataSnapshot.child("profile").child("name").getValue(String.class);
                        Map likeMap = new HashMap();
                        Blog blog = new Blog(key, title.getEditText().getText().toString(), description.getEditText().getText().toString(),
                                categoryTextView.getText().toString(), userImageReference, userId, userName, System.currentTimeMillis(), likeMap);
                        database.getReference().child("blogs").child(key).setValue(blog, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                    Toast.makeText(AddBlogActivity.this, "Error uploading blog!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(AddBlogActivity.this, "Blog uploaded successfully!", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.INVISIBLE);
                                    finish();
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }


    private void startVoiceToText() {
        droidSpeech = new DroidSpeech(this, null);
        droidSpeech.setShowRecognitionProgressView(true);
        droidSpeech.setListeningMsg("Click anywhere to stop recording!");
        droidSpeech.setOnDroidSpeechListener(new OnDSListener() {
            @Override
            public void onDroidSpeechSupportedLanguages(String currentSpeechLanguage, List<String> supportedSpeechLanguages) {

            }

            @Override
            public void onDroidSpeechRmsChanged(float rmsChangedValue) {

            }

            @Override
            public void onDroidSpeechLiveResult(String liveSpeechResult) {

            }

            @Override
            public void onDroidSpeechFinalResult(String finalSpeechResult) {
                description.getEditText().setText(String.format("%s%s", description.getEditText().getText().toString(), finalSpeechResult));
            }

            @Override
            public void onDroidSpeechClosedByUser() {

            }

            @Override
            public void onDroidSpeechError(String errorMsg) {
            }
        });
        droidSpeech.startDroidSpeechRecognition();
    }

    //choosing between gallery and camera
    private void choiceDialog() {
        AlertDialog.Builder picDialog = new AlertDialog.Builder(this);
        picDialog.setTitle("Complete action using");
        String picDialogChoices[] = {"Photos", "Camera"};
        picDialog.setItems(picDialogChoices,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int choice) {
                        switch (choice) {
                            case 0:
                                gallerySelected();
                                break;
                            case 1:
                                cameraSelected();
                                break;
                        }
                    }
                });
        picDialog.show();
    }

    //GalleryIntent
    public void gallerySelected() {
        Intent gIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(gIntent, GALLERY);
    }

    //CameraIntent
    private void cameraSelected() {
        Intent cIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cIntent, CAMERA);
    }

    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) {

        super.onActivityResult(reqCode, resCode, data);
        if (resCode == this.RESULT_CANCELED) {
            return;
        }
        if (reqCode == GALLERY) {
            if (data != null) {
                imageURI = data.getData();
                coverPicture.setText("Image selected from gallery");
                imageSelected = true;
            }
        } else if (reqCode == CAMERA) {
            Bitmap img = (Bitmap) data.getExtras().get("data");
            saveImage(img);
            coverPicture.setText("Image selected from camera");
            imageSelected = true;
        }
    }

    private void uploadImageToDatabase() {
        final StorageReference reference = storageReference.child(System.currentTimeMillis() + "." + getExtension(imageURI));
        reference.putFile(imageURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String imageReference = uri.toString();
                        addBlogToDatabase(imageReference);
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

    private String saveImage(Bitmap myIcon) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        File directory = new File(getCacheDir(), IMAGE_DIRECTORY);

        // have the object build the directory structure, if needed.
        if (!directory.exists()) {
            directory.mkdirs();
        }

        try {
            File f = new File(directory, Calendar.getInstance()
                    .getTimeInMillis() + ".jpg");
            f.createNewFile();
            imageURI = Uri.fromFile(f);
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(this,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startVoiceToText();
                } else {
                    Toast.makeText(AddBlogActivity.this, "Permission denied by user!", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

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
                            Toast.makeText(getApplicationContext(), "All permissions are granted by user!", Toast.LENGTH_SHORT).show();
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            //openSettingsDialog();
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
                        Toast.makeText(getApplicationContext(), "Some Error! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }
}
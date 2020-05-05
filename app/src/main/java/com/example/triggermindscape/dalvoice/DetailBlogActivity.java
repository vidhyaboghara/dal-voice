package com.example.triggermindscape.dalvoice;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Created by Anshdeep Singh (anshdeep.singh@dal.ca)
 * Class to show detailed blog content
 */
public class DetailBlogActivity extends AppCompatActivity {

    private ImageView coverImage;
    private TextView title, author, content, category;
    private FloatingActionButton btnLike, btnShare;
    private boolean mLike;
    DatabaseReference numberLikesReference, likesReference;
    FirebaseDatabase database;
    String blogID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_page);

        title = findViewById(R.id.title);
        author = findViewById(R.id.author);
        coverImage = findViewById(R.id.coverPage);
        content = findViewById(R.id.content);
        category = findViewById(R.id.category);
        btnLike = findViewById(R.id.like_button);
        btnShare = findViewById(R.id.share_button);
        database = FirebaseDatabase.getInstance();

        Intent intent = getIntent();
        blogID = intent.getExtras().getString("blogID");
        String imagePath = intent.getExtras().getString("image");
        String titleText = intent.getExtras().getString("title");
        String categoryText = intent.getExtras().getString("category");
        String contentText = intent.getExtras().getString("content");
        String userName = intent.getExtras().getString("username");

        title.setText(titleText);
        author.setText("Created by: " + userName);
        content.setText(contentText);
        category.setText("Category: " + categoryText);

        Glide.with(getApplicationContext())
                .load(imagePath)
                .apply(new RequestOptions().error(R.drawable.noimg))
                .transition(withCrossFade())
                .into(coverImage);


        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            btnShare.setVisibility(View.INVISIBLE);
            btnLike.setVisibility(View.INVISIBLE);
        } else {
            btnShare.setVisibility(View.VISIBLE);
            btnLike.setVisibility(View.VISIBLE);

            likesReference = database.getReference().child("blogs").child(blogID).child("likedBy");

            likesReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // already liked
                    if (dataSnapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        btnLike.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.colorPrimary));
                        btnLike.setEnabled(false);
                    }
                    // not liked
                    else {
                        btnLike.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), android.R.color.darker_gray));
                        btnLike.setEnabled(true);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, title.getText());
                intent.putExtra(android.content.Intent.EXTRA_TEXT, content.getText());
                startActivity(Intent.createChooser(intent, getString(R.string.shareViaText)));
            }
        });
        btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLike = true;
                numberLikesReference = database.getReference().child("blogs").child(blogID).child("numberOfLikes");
                likesReference = database.getReference().child("blogs").child(blogID).child("likedBy");
                numberLikesReference.keepSynced(true);
                likesReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (mLike) {
                            if (dataSnapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                likesReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
                                updateCounter(false);
                                mLike = false;
                            } else {
                                likesReference.child(FirebaseAuth.getInstance().getCurrentUser()
                                        .getUid()).setValue(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                                updateCounter(true);
                                mLike = false;
                                btnLike.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.colorPrimary));
                                btnLike.setEnabled(false);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            private void updateCounter(final boolean increment) {
                numberLikesReference.runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        if (mutableData.getValue() != null) {
                            int value = mutableData.getValue(Integer.class);
                            if (increment) {
                                value++;
                            } else {
                                value--;
                            }
                            mutableData.setValue(value);
                        }
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b,
                                           DataSnapshot dataSnapshot) {
                        // Transaction completed
                    }
                });
            }
        });
    }
}

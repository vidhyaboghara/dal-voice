package com.example.triggermindscape.dalvoice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/*
 * Class to implement Home Page of application
 */

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseRecyclerAdapter<Blog, BlogViewHolder> adapter;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private FloatingActionButton addBlogButton,userProfileButton,categoryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DatabaseReference databaseReference;
        databaseReference = FirebaseDatabase.getInstance().getReference().child("blogs");
        mFirebaseAuth = FirebaseAuth.getInstance();
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mProgressBar = findViewById(R.id.progressBar);
        addBlogButton = findViewById(R.id.addBlogButton);
        userProfileButton=findViewById(R.id.userProfileButton);
        categoryButton=findViewById(R.id.categoryButton);

        Query blogsQuery = databaseReference.orderByKey();
        FirebaseRecyclerOptions<Blog> options = new FirebaseRecyclerOptions.Builder<Blog>()
                .setQuery(blogsQuery, Blog.class).build();
        adapter = new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull BlogViewHolder blogViewHolder, int i, @NonNull final Blog blog) {
                blogViewHolder.setBlogImage(blog.getImagePath());
                blogViewHolder.setBlogTitle(blog.getTitle());
                blogViewHolder.setBlogCreatedBy(blog.getUserName());
                blogViewHolder.setBlogLikes(blog.getNumberOfLikes());
                blogViewHolder.setBlogDateAndTime(blog.getTimestamp());
                blogViewHolder.setBlogItemClickListener(new BlogItemClickListener() {
                    @Override
                    public void onItemClick(View view, int pos) {
                        Intent intent = new Intent(MainActivity.this, DetailBlogActivity.class);
                        intent.putExtra("blogID", blog.getBlogID());
                        intent.putExtra("image", blog.getImagePath());
                        intent.putExtra("title", blog.getTitle());
                        intent.putExtra("content", blog.getContent());
                        intent.putExtra("category", blog.getCategory());
                        intent.putExtra("username", blog.getUserName());
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.blog_card_view, parent, false);
                return new BlogViewHolder(getApplicationContext(), view);
            }

            @Override
            public void onDataChanged() {
                if (mProgressBar != null) {
                    mProgressBar.setVisibility(View.GONE);
                }
            }
            @NonNull
            @Override
            public Blog getItem(int position) {
                return super.getItem(getItemCount()-1-position);
            }
        };
        mRecyclerView.setAdapter(adapter);

        addBlogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFirebaseAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(loginIntent);
                } else {
                    startActivity(new Intent(MainActivity.this, AddBlogActivity.class));
                }
            }
        });

        userProfileButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                if (mFirebaseAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(loginIntent);
                } else {
                    Intent intent = new Intent(view.getContext(), UserInfo.class);
                    startActivity(intent);
                }
            }
        });
        categoryButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(view.getContext(), CategoryDisplayActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        View view;
        private Context context;
        private ImageView blogImage;
        private TextView blogTitle, blogCreatedBy, blogLikeCount, blogTime;
        private BlogItemClickListener blogItemClickListener;

        BlogViewHolder(Context context, View itemView) {
            super(itemView);
            view = itemView;
            this.context = context;
            blogImage = view.findViewById(R.id.blog_image);
            blogTitle = view.findViewById(R.id.blog_title);
            blogCreatedBy = view.findViewById(R.id.blog_created_by);
            blogLikeCount = view.findViewById(R.id.like_count);
            blogTime = view.findViewById(R.id.blog_time);
            itemView.setOnClickListener(this);
        }

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            this.blogItemClickListener.onItemClick(v, getLayoutPosition());
        }

        public void setBlogItemClickListener(BlogItemClickListener clickListener) {
            this.blogItemClickListener = clickListener;
        }

        public void setBlogImage(String blogImagePath) {
            Glide.with(context)
                    .load(blogImagePath)
                    .apply(new RequestOptions().error(R.drawable.noimg))
                    .transition(withCrossFade())
                    .into(blogImage);
        }

        public void setBlogTitle(String text) {
            blogTitle.setText(text);
        }

        public void setBlogCreatedBy(String text) {
            blogCreatedBy.setText("Created by: " + text);
        }

        public void setBlogLikes(int likes) {
            blogLikeCount.setText(": " + String.valueOf(likes));
        }

        public void setBlogDateAndTime(long timestamp) {
            blogTime.setText(getDateFromMillis(timestamp));
        }

        private String getDateFromMillis(long millis) {
            DateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy");
            Date result = new Date(millis);
            return dateFormat.format(result);
        }
    }
}
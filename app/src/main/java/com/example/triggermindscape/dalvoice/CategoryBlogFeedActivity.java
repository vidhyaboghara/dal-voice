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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;


/*
 *   Class to show the  blog feed according to selected category
 */

public class CategoryBlogFeedActivity extends AppCompatActivity {

    private DatabaseReference mDatabaseReference;
    private String categoryChosen;
    private FirebaseAuth mFirebaseAuth;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private FirebaseRecyclerAdapter<Blog, CategoryBlogFeedActivity.BlogViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_blog_feed);
        categoryChosen = getIntent().getStringExtra("CategoryChosen");

        mFirebaseAuth = FirebaseAuth.getInstance();
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mProgressBar = findViewById(R.id.progressBar);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("blogs");

        Query blogsQuery = mDatabaseReference.orderByChild("category").equalTo(categoryChosen);

        FirebaseRecyclerOptions<Blog> options = new FirebaseRecyclerOptions.Builder<Blog>()
                .setQuery(blogsQuery, Blog.class).build();

        adapter = new FirebaseRecyclerAdapter<Blog, CategoryBlogFeedActivity.BlogViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CategoryBlogFeedActivity.BlogViewHolder blogViewHolder, int i, @NonNull final Blog blog) {
                blogViewHolder.setBlogImage(blog.getImagePath());
                blogViewHolder.setBlogTitle(blog.getTitle());
                blogViewHolder.setBlogCreatedBy(blog.getUserName());
                blogViewHolder.setBlogLikes(blog.getNumberOfLikes());
                blogViewHolder.setBlogDateAndTime("26th October 2019 5:55 PM");

                blogViewHolder.setBlogItemClickListener(new BlogItemClickListener() {
                    @Override
                    public void onItemClick(View view, int pos) {
                        Intent intent = new Intent(CategoryBlogFeedActivity.this, DetailBlogActivity.class);
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
            public CategoryBlogFeedActivity.BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.blog_card_view, parent, false);
                return new CategoryBlogFeedActivity.BlogViewHolder(getApplicationContext(), view);
            }

            @Override
            public void onDataChanged() {
                if (mProgressBar != null) {
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        };
        mRecyclerView.setAdapter(adapter);
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

    public static class BlogViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View view;
        private Context context;
        private ImageView blogImage;
        private TextView blogTitle, blogCreatedBy, blogLikeCount, blogTime;
        private BlogItemClickListener blogItemClickListener;

        public BlogViewHolder(Context context, View itemView) {
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

        public void setBlogDateAndTime(String timestamp) {
            blogTime.setText(timestamp);
        }
    }
}
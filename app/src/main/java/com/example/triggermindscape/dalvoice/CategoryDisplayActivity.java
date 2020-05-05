package com.example.triggermindscape.dalvoice;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

/*
 *   Class to handle add blog functionality
 *   Design and validations updated by Anshdeep Singh
 */


public class CategoryDisplayActivity extends AppCompatActivity {

    private CardView categoryGeneral,categoryTechnology,categoryUniversity,categoryTravel,categoryEntertainment,categorySports,categoryBusiness,categoryFood;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_display);
        categoryGeneral=findViewById(R.id.cat_general);
        categoryTechnology=findViewById(R.id.cat_technology);
        categoryUniversity=findViewById(R.id.cat_university);
        categoryTravel=findViewById(R.id.cat_travel);
        categoryEntertainment=findViewById(R.id.cat_entertainment);
        categorySports=findViewById(R.id.cat_sports);
        categoryBusiness=findViewById(R.id.cat_business);
        categoryFood=findViewById(R.id.cat_food);

        categoryGeneral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), CategoryBlogFeedActivity.class);
                String category="General";
                intent.putExtra("CategoryChosen",category);
                startActivity(intent);
            }
        });

        categoryTechnology.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), CategoryBlogFeedActivity.class);
                String category="Technology";
                intent.putExtra("CategoryChosen",category);
                startActivity(intent);
            }
        });

        categoryUniversity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), CategoryBlogFeedActivity.class);
                String category="University";
                intent.putExtra("CategoryChosen",category);
                startActivity(intent);
            }
        });

        categoryTravel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), CategoryBlogFeedActivity.class);
                String category="Travel";
                intent.putExtra("CategoryChosen",category);
                startActivity(intent);
            }
        });

        categoryEntertainment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), CategoryBlogFeedActivity.class);
                String category="Entertainment";
                intent.putExtra("CategoryChosen",category);
                startActivity(intent);
            }
        });

        categorySports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), CategoryBlogFeedActivity.class);
                String category="Sports";
                intent.putExtra("CategoryChosen",category);
                startActivity(intent);
            }
        });

        categoryBusiness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), CategoryBlogFeedActivity.class);
                String category="Business";
                intent.putExtra("CategoryChosen",category);
                startActivity(intent);
            }
        });

        categoryFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), CategoryBlogFeedActivity.class);
                String category="Food";
                intent.putExtra("CategoryChosen",category);
                startActivity(intent);
            }
        });
    }
}
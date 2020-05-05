package com.example.triggermindscape.dalvoice;

import java.util.Map;

/*
    Created by Gaurav Anand (gr874432@dal.ca)
    Class to define Blog entity
*/

public class Blog {
    private String blogID, title, content, category, imagePath, userId, userName;
    private long timestamp;
    private int numberOfLikes;
    private Map<String, String> likedBy;

    public Blog() {}
    public Blog(String blogID, String title, String content, String category, String imagePath, String userId,
                String userName, long timestamp, Map<String, String> likedBy) {
        this.blogID = blogID;
        this.title = title;
        this.content = content;
        this.category = category;
        this.imagePath = imagePath;
        this.userId = userId;
        this.userName = userName;
        this.numberOfLikes = 0;
        this.timestamp = timestamp;
        this.likedBy = likedBy;
    }


    public void setContent(String content) {
        this.content = content;
    }

    public String getBlogID() {
        return blogID;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getCategory() {
        return category;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getUserName() {
        return userName;
    }

    public int getNumberOfLikes() {
        return numberOfLikes;
    }

    public long getTimestamp() {
        return timestamp;
    }
}

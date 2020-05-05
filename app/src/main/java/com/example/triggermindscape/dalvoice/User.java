package com.example.triggermindscape.dalvoice;

/*
    Created by Gaurav Anand (gr874432@dal.ca)
    Class to define User entity
*/

public class User {
    private String email, password, name, imageUrl;

    User (String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}

// Post.java
package com.example.proyectofinal;

import com.google.firebase.Timestamp;
import java.util.Date;

public class Post {
    private String title;
    private String content;
    private double price;
    private double rating;
    private String imageUrl;
    private Date timestamp;
    private String userId; // Para identificar al usuario creador

    public Post() {}

    public Post(String title, String content, double price, double rating, String imageUrl, String userId) {
        this.title = title;
        this.content = content;
        this.price = price;
        this.rating = rating;
        this.imageUrl = imageUrl;
        this.timestamp = new Date();
        this.userId = userId;
    }

    // Getters
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public double getPrice() { return price; }
    public double getRating() { return rating; }
    public String getImageUrl() { return imageUrl; }
    public Date getTimestamp() { return timestamp; }
    public String getUserId() { return userId; }
}
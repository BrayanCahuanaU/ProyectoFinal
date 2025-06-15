package com.example.proyectofinal;

import com.google.firebase.Timestamp;
import java.util.Date;

public class Post {
    private String content;
    private Date timestamp;

    public Post() {}

    public Post(String content) {
        this.content = content;
        this.timestamp = new Date();
    }

    // Getters necesarios para Firestore
    public String getContent() { return content; }
    public Date getTimestamp() { return timestamp; }
}
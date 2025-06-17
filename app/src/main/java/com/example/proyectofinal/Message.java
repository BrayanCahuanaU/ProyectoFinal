// Message.java
package com.example.proyectofinal;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Message")
public class Message extends ParseObject {
    public static final String KEY_FROM_USER = "fromUser";
    public static final String KEY_TO_USER = "toUser";
    public static final String KEY_CONTENT = "content";
    public static final String KEY_POST = "post";

    public ParseUser getFromUser() {
        return getParseUser(KEY_FROM_USER);
    }

    public void setFromUser(ParseUser user) {
        put(KEY_FROM_USER, user);
    }

    public ParseUser getToUser() {
        return getParseUser(KEY_TO_USER);
    }

    public void setToUser(ParseUser user) {
        put(KEY_TO_USER, user);
    }

    public String getContent() {
        return getString(KEY_CONTENT);
    }

    public void setContent(String content) {
        put(KEY_CONTENT, content);
    }

    public ParseObject getPost() {
        return getParseObject(KEY_POST);
    }

    public void setPost(ParseObject post) {
        put(KEY_POST, post);
    }
}
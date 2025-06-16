package com.example.proyectofinal;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.List;

@ParseClassName("Post")
public class Post extends ParseObject {
    public static final String KEY_USER       = "user";
    public static final String KEY_IMAGES     = "images";
    public static final String KEY_TITLE      = "title";
    public static final String KEY_DESC       = "description";
    public static final String KEY_PRICE      = "price";
    public static final String KEY_SCHEDULES  = "schedules";
    public static final String KEY_CATEGORY   = "category";
    public static final String KEY_LOCATIONS  = "locations";
    public static final String KEY_RATING     = "rating";

    // GETTERS
    public ParseUser getUser()                { return getParseUser(KEY_USER); }
    public List<ParseFile> getImages()        { return getList(KEY_IMAGES); }
    public String getTitle()                  { return getString(KEY_TITLE); }
    public String getDescription()            { return getString(KEY_DESC); }
    public double getPrice()                  { return getDouble(KEY_PRICE); }
    public List<String> getSchedules()        { return getList(KEY_SCHEDULES); }
    public String getCategory()               { return getString(KEY_CATEGORY); }
    public List<String> getLocations()        { return getList(KEY_LOCATIONS); }
    public Number getRating()                 { return getNumber(KEY_RATING); }

    // SETTERS
    public void setUser(ParseUser user)               { put(KEY_USER, user); }
    public void setImages(List<ParseFile> images)     { put(KEY_IMAGES, images); }
    public void setTitle(String title)                { put(KEY_TITLE, title); }
    public void setDescription(String desc)            { put(KEY_DESC, desc); }
    public void setPrice(double price)                { put(KEY_PRICE, price); }
    public void setSchedules(List<String> schedules)   { put(KEY_SCHEDULES, schedules); }
    public void setCategory(String category)           { put(KEY_CATEGORY, category); }
    public void setLocations(List<String> locations)   { put(KEY_LOCATIONS, locations); }
    public void setRating(Number rating)               { put(KEY_RATING, rating); }
}

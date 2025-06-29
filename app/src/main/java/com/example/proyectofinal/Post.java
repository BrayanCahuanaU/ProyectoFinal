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

    public static final String KEY_SERVICE_COMPLETED = "serviceCompleted";
    public static final String KEY_RATING_SUM        = "ratingSum";
    public static final String KEY_RATING_COUNT      = "ratingCount";

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

    public boolean isServiceCompleted() {
        return getBoolean(KEY_SERVICE_COMPLETED);
    }
    public double getRatingAverage() {
        Number sum   = getNumber(KEY_RATING_SUM);
        Number count = getNumber(KEY_RATING_COUNT);
        return (sum != null && count != null && count.doubleValue() > 0)
                ? sum.doubleValue() / count.doubleValue()
                : 0;
    }

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
    public void setServiceCompleted(boolean done) {
        put(KEY_SERVICE_COMPLETED, done);
    }
    // Internos: no expongas setters de sum/count directamente
    protected void incrementRating(int score) {
        double currentSum = getNumber(KEY_RATING_SUM) != null ?
                getNumber(KEY_RATING_SUM).doubleValue() : 0;
        double currentCount = getNumber(KEY_RATING_COUNT) != null ?
                getNumber(KEY_RATING_COUNT).doubleValue() : 0;

        put(KEY_RATING_SUM, currentSum + score);
        put(KEY_RATING_COUNT, currentCount + 1);

        // Actualizar rating promedio
        double newAverage = (currentSum + score) / (currentCount + 1);
        put(KEY_RATING, newAverage);
    }
}

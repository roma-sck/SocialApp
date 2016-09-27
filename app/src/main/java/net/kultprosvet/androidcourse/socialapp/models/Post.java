package net.kultprosvet.androidcourse.socialapp.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Post {

    private static final String UID = "uid";
    private static final String AUTHOR = "author";
    private static final String TITLE = "title";
    private static final String BODY = "body";
    private static final String LIKES_COUNT = "likesCount";
    private static final String LIKES = "likes";

    public String uid;
    public String author;
    public String title;
    public String body;
    public int likesCount = 0;
    public Map<String, Boolean> likes = new HashMap<>();

    public Post() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Post(String uid, String author, String title, String body) {
        this.uid = uid;
        this.author = author;
        this.title = title;
        this.body = body;
    }

    //  post_to_map
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(UID, uid);
        if (author != null) {
            result.put(AUTHOR, author);
        }
        if (title != null) {
            result.put(TITLE, title);
        }
        if(body != null) {
            result.put(BODY, body);
        }
        result.put(LIKES_COUNT, likesCount);
        result.put(LIKES, likes);

        return result;
    }


    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
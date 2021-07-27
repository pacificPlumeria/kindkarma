package edu.neu.madcourse.kindkarma.models;

public class Post {
    public String description;
    public String image_url;
    public Long creation_time_ms;
    public String posterId;
    public String postId;

    public Post(String description, String image_url, Long creation_time_ms, String posterId, String postId){
        this.description = description;
        this.image_url = image_url;
        this.creation_time_ms = creation_time_ms;
        this.posterId = posterId;
        this.postId = postId;
    }

    public Post(String description, String image_url, Long creation_time_ms, String posterId){
        this.description = description;
        this.image_url = image_url;
        this.creation_time_ms = creation_time_ms;
        this.posterId = posterId;
    }

    public Post(){
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public Long getCreation_time_ms() {
        return creation_time_ms;
    }

    public void setCreation_time_ms(Long creation_time_ms) {
        this.creation_time_ms = creation_time_ms;
    }

    public String getPosterId() {
        return posterId;
    }

    public void setPosterId(String posterId) {
        this.posterId = posterId;
    }

    public void onListItemClick(int position) {
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }
}
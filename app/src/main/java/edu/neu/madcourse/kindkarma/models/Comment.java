package edu.neu.madcourse.kindkarma.models;

import java.util.Date;

public class Comment {
    private String comment, uid;
    private Long creation_time_ms;

    public Comment(String comment, String uid, Long creation_time_ms) {
        this.comment = comment;
        this.uid = uid;
        this.creation_time_ms = creation_time_ms;
    }

    public Comment() {
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Long getCreation_time_ms() {
        return creation_time_ms;
    }

    public void setCreation_time_ms(Long creation_time_ms) {
        this.creation_time_ms = creation_time_ms;
    }
}

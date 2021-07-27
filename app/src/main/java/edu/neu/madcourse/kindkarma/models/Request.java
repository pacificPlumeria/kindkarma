package edu.neu.madcourse.kindkarma.models;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class Request {
    public String title;
    public String body;
    public String image_url;
    public Long creation_time_ms;
    public String requesterId;
    public HashMap<String,String> requestCategories;
    public String status;
    public Boolean showRequestee = false;
    public String requestId;
    public int lastSpinnerPosition = 0;
    public ArrayList<String> favorite;
    public String startDate;
    public String endDate;
    public Boolean online;

    public Request(String title, String body, String image_url, Long creation_time_ms,
                   String requesterId, HashMap<String,String> requestCategories, String status,
                   Boolean showRequestee, String requestId, ArrayList<String> favorite,
                   String startDate, String endDate, Boolean online) {
        this.title = title;
        this.body = body;
        this.image_url = image_url;
        this.creation_time_ms = creation_time_ms;
        this.requesterId = requesterId;
        this.requestCategories = requestCategories;
        this.status = status;
        this.showRequestee = false;
        this.requestId = requestId;
        this.lastSpinnerPosition = 0;
        this.favorite = favorite;
        this.startDate = startDate;
        this.endDate = endDate;
        this.online = online;
    }

    public Request() {
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
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

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public HashMap<String,String> getRequestCategories() {
        return requestCategories;
    }

    public void setRequestCategories(HashMap<String,String> requestCategories) {
        this.requestCategories = requestCategories;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getShowRequestee() {
        return showRequestee;
    }

    public void setShowRequestee(Boolean showRequestee) {
        this.showRequestee = showRequestee;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public int getLastSpinnerPosition() {
        return lastSpinnerPosition;
    }

    public void setLastSpinnerPosition(int lastSpinnerPosition) {
        this.lastSpinnerPosition = lastSpinnerPosition;
    }

    public ArrayList<String> getFavorite() {
        return favorite;
    }

    public void setFavorite(ArrayList<String> favorite) {
        this.favorite = favorite;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }
}

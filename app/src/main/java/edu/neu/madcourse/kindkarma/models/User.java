package edu.neu.madcourse.kindkarma.models;

import java.util.HashMap;

public class User {
    public String username;
    public String about;
    public String firstName;
    public String lastName;
    public String profileImage;
    public int zipcode;
    public String state;
    public String city;
    public String community;
    public HashMap<String,String> categories;
    public long totalHoursWorked;
    public String userToken;

    public User(String username, String about, String firstName, String lastName,
                String profileImage, int zipcode, HashMap<String,String> categories, String state,
                String city, String community, long totalHoursWorked, String userToken) {

        this.username = username;
        this.about = about;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profileImage = profileImage;
        this.zipcode = zipcode;
        this.categories = categories;
        this.state = state;
        this.city = city;
        this.community = community;
        this.totalHoursWorked = totalHoursWorked;
        this.userToken = userToken;

    }

    public User(){

    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public int getZipcode() {
        return zipcode;
    }

    public void setZipcode(int zipcode) {
        this.zipcode = zipcode;
    }

    public void setState(String state){
        this.state = state;
    }
    public String getState(){
        return state;
    }

    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    public String getCommunity() {
        return community;
    }

    public HashMap<String, String> getCategories() {
        return categories;
    }

    public void setCategories(HashMap<String, String> categories) {
        this.categories = categories;
    }

    public long getTotalHoursWorked() {
        return totalHoursWorked;
    }

    public void setTotalHoursWorked(long totalHoursWorked) {
        this.totalHoursWorked = totalHoursWorked;
    }
}

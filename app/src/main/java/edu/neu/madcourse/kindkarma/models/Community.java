package edu.neu.madcourse.kindkarma.models;


import java.util.ArrayList;

public class Community {


    public long totalUsers;
    public String state;
    public String county;
    public long totalCommunityHours;
    public ArrayList<String> membersList;
    public String abbreviation;
    public String communityColor;

    public Community(  String state, String county,long totalCommunityHours , long totalUsers,
                       ArrayList<String> membersList,String communityColor,
                       String abbreviation) {

        this.state = state;
        this.county = county;
        this.totalCommunityHours = totalCommunityHours;
        this.totalUsers = totalUsers;
        this.membersList = membersList;
        this.abbreviation = abbreviation;
        this.communityColor = communityColor;
    }

    public Community() {

    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setCommunityColor(String communityColor) {
        this.communityColor = communityColor;
    }

    public String getCommunityColor() {
        return communityColor;
    }

    public ArrayList<String> getMembersList() {
        return membersList;
    }

    public void setMembersList(ArrayList<String> membersList) {
        this.membersList = membersList;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getCounty() {
        return county;
    }

    public void setTotalCommunityHours(long totalCommunityHours) {
        this.totalCommunityHours = totalCommunityHours;
    }

    public long getTotalCommunityHours() {
        return totalCommunityHours;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalUsers() {
        return totalUsers;

    }
}

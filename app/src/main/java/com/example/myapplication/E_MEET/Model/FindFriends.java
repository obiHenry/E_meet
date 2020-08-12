package com.example.myapplication.E_MEET.Model;

public class FindFriends {

    public  String profileImages, fullName, status;

    public FindFriends(){

    }

    public FindFriends(String profileImages, String fullName, String status) {
        this.profileImages = profileImages;
        this.fullName = fullName;
        this.status = status;
    }

    public String getProfileImages() {
        return profileImages;
    }

    public void setProfileImages(String profileImages) {
        this.profileImages = profileImages;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

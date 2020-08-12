package com.example.myapplication.E_MEET.Model;

public class Posts {

    public String date, description, fullName, postImage, profileImages, time, uid;


    public Posts(){

    }


    public Posts(String date, String description, String fullName, String postImage, String profileImage, String time, String uid) {
        this.date = date;
        this.description = description;
        this.fullName = fullName;
        this.postImage = postImage;
        this.profileImages = profileImage;
        this.time = time;
        this.uid = uid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getProfileImage() {
        return profileImages;
    }

    public void setProfileImage(String profileImage) {
        this.profileImages = profileImage;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}

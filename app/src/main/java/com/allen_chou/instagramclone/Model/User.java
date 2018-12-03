package com.allen_chou.instagramclone.Model;

public class User {
    private String userId;
    private String nickName;
    private String imageUrl;
    private String bio;

    public User(String userId, String nickName, String imageUrl, String bio) {
        this.userId = userId;
        this.nickName = nickName;
        this.imageUrl = imageUrl;
        this.bio = bio;
    }

    public User() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}

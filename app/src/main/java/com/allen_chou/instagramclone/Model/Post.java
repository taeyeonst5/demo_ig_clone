package com.allen_chou.instagramclone.Model;

public class Post {
    private String postId;
    private String postImage;
    private String publisher;
    private String postDescription;

    public Post(String postId, String postImage, String description, String publisher) {
        this.postId = postId;
        this.postImage = postImage;
        this.postDescription = description;
        this.publisher = publisher;
    }

    public Post() {
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getPostDescription() {
        return postDescription;
    }

    public void setPostDescription(String postDescription) {
        this.postDescription = postDescription;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    @Override
    public String toString() {
        return "Post{" +
                "postId='" + postId + '\'' +
                ", postImage='" + postImage + '\'' +
                ", publisher='" + publisher + '\'' +
                ", postDescription='" + postDescription + '\'' +
                '}';
    }
}

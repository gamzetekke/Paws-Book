package com.gamze.pawsbook.Models;

public class ModelPost {
    //post yüklerken kullandığım aynı isimleri kullanmalıyım (AddPostActivity'deki)
    String post_uid, post_Id, post_title, post_comments, post_name, post_email, post_dp, post_desc, post_image, post_time;

    public ModelPost() {}

    public ModelPost(String post_uid, String post_Id, String post_title, String post_comments, String post_name, String post_email, String post_dp, String post_desc, String post_image, String post_time) {
        this.post_uid = post_uid;
        this.post_Id = post_Id;
        this.post_title = post_title;
        this.post_comments = post_comments;
        this.post_name = post_name;
        this.post_email = post_email;
        this.post_dp = post_dp;
        this.post_desc = post_desc;
        this.post_image = post_image;
        this.post_time = post_time;
    }

    public String getPost_uid() {
        return post_uid;
    }

    public void setPost_uid(String post_uid) {
        this.post_uid = post_uid;
    }

    public String getPost_Id() {
        return post_Id;
    }

    public void setPost_Id(String post_Id) {
        this.post_Id = post_Id;
    }

    public String getPost_title() {
        return post_title;
    }

    public void setPost_title(String post_title) {
        this.post_title = post_title;
    }

    public String getPost_comments() {
        return post_comments;
    }

    public void setPost_comments(String post_comments) {
        this.post_comments = post_comments;
    }

    public String getPost_name() {
        return post_name;
    }

    public void setPost_name(String post_name) {
        this.post_name = post_name;
    }

    public String getPost_email() {
        return post_email;
    }

    public void setPost_email(String post_email) {
        this.post_email = post_email;
    }

    public String getPost_dp() {
        return post_dp;
    }

    public void setPost_dp(String post_dp) {
        this.post_dp = post_dp;
    }

    public String getPost_desc() {
        return post_desc;
    }

    public void setPost_desc(String post_desc) {
        this.post_desc = post_desc;
    }

    public String getPost_image() {
        return post_image;
    }

    public void setPost_image(String post_image) {
        this.post_image = post_image;
    }

    public String getPost_time() {
        return post_time;
    }

    public void setPost_time(String post_time) {
        this.post_time = post_time;
    }
}



package com.example.studyflow.data.model;

public class UserProfile {

    private String uid;
    private String name;
    private String email;
    private String avatarUrl;
    private String university;
    private String major;
    private String year;
    private String bio;

    public UserProfile() {
    }

    public UserProfile(
            String uid,
            String name,
            String email,
            String avatarUrl,
            String university,
            String major,
            String year,
            String bio
    ) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.university = university;
        this.major = major;
        this.year = year;
        this.bio = bio;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
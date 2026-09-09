package com.example.studyflow.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "courses")
public class Course {

    @PrimaryKey(autoGenerate = true)
    private int courseId;

    private String courseName;
    private String lecturer;
    private String defaultRoom;
    private String color;

    public Course(String courseName, String lecturer,
                  String defaultRoom, String color) {
        this.courseName = courseName;
        this.lecturer = lecturer;
        this.defaultRoom = defaultRoom;
        this.color = color;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getLecturer() {
        return lecturer;
    }

    public void setLecturer(String lecturer) {
        this.lecturer = lecturer;
    }

    public String getDefaultRoom() {
        return defaultRoom;
    }

    public void setDefaultRoom(String defaultRoom) {
        this.defaultRoom = defaultRoom;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
package com.example.studyflow.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.studyflow.data.entity.Course;

import java.util.List;

@Dao
public interface CourseDao {

    @Insert
    long insert(Course course);

    @Update
    void update(Course course);

    @Delete
    void delete(Course course);

    @Query("SELECT * FROM courses ORDER BY courseName ASC")
    LiveData<List<Course>> getAllCourses();

    @Query("SELECT * FROM courses WHERE courseId = :courseId LIMIT 1")
    Course getCourseById(int courseId);
    @Query("SELECT * FROM courses WHERE courseId = :courseId LIMIT 1")
    Course getById(int courseId);
    @Query("SELECT * FROM courses ORDER BY courseName ASC")
    List<Course> getSnapshot();
}

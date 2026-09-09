package com.example.studyflow.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.studyflow.data.entity.StudyPlan;

import java.util.List;

@Dao
public interface StudyPlanDao {

    @Insert
    long insert(StudyPlan studyPlan);

    @Update
    void update(StudyPlan studyPlan);

    @Delete
    void delete(StudyPlan studyPlan);
    @Query("DELETE FROM study_plans")
    void deleteAllPlans();
    @Query("SELECT * FROM study_plans ORDER BY studyDate ASC")
    LiveData<List<StudyPlan>> getAllStudyPlans();

    @Query("SELECT * FROM study_plans WHERE taskId = :taskId ORDER BY studyDate ASC")
    LiveData<List<StudyPlan>> getPlansForTask(int taskId);

    @Query("DELETE FROM study_plans WHERE taskId = :taskId")
    void deletePlansForTask(int taskId);
    @Query("SELECT * FROM study_plans " +
            "WHERE taskId = :taskId " +
            "AND studyDate >= :startOfDay " +
            "AND studyDate < :endOfDay " +
            "LIMIT 1")
    StudyPlan getPlanForTaskOnDate(
            int taskId,
            long startOfDay,
            long endOfDay
    );
    @Query("SELECT * FROM study_plans WHERE taskId = :taskId AND studyDate >= :start AND studyDate < :end ORDER BY studyDate")
    List<StudyPlan> getDailyPlans(int taskId, long start, long end);
    @Query("DELETE FROM study_plans WHERE studyDate >= :today AND completedMinutes = 0")
    void deleteUnstartedFuture(long today);
    @Query("SELECT * FROM study_plans ORDER BY studyDate")
    List<StudyPlan> getSnapshot();
    @Query("SELECT * FROM study_plans WHERE planId = :id LIMIT 1")
    StudyPlan getById(int id);
}
